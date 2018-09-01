package twise;

import jota.IotaAPI;
import jota.dto.response.GetNodeInfoResponse;
import jota.dto.response.GetAttachToTangleResponse;
import jota.dto.response.GetTransactionsToApproveResponse;
import jota.dto.response.SendTransferResponse;
import jota.error.ArgumentException;
import jota.model.Input;
import jota.model.Transaction;
import jota.model.Transfer;
import jota.model.Bundle;
import jota.model.Transaction;
import jota.utils.Converter;
import jota.pow.SpongeFactory;
import jota.IotaLocalPoW;
import jota.pow.ICurl;
import jota.pow.ICurl.*;
import cfb.pearldiver.PearlDiverLocalPoW;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import org.apache.commons.lang3.StringUtils;


public class Main {

	public static final ThreadGroup SUPER_THREAD = new ThreadGroup( "Super-Thread" );

	public static final String ADDRESS = "PHFJUJFSDHHUZUAZ9TIXQFCEW9FFQOXOLPPDLFYIZCOUGAEMBCNOWLXVDVUCTMWXCLHQVFEMSQXBNJUSN";
	public static final String TAG = "ZZZZZZZZZZZZZZZZZZZZZZZZZZZ";
	public static int MWM = 14;

//	private static final String seed = "SSFM9SBA9GIREICDJ9FVUAKZUU9KLUXUIFIYMAJQHUKCKRXWFJKDJG9DCHSOIEGH9";
//	public static final int security_level = 1;

	public static void main(String[] args) {
		try {
			spamTransactions();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void spamTransactions() throws ArgumentException {
		GoLocalPow local_pow_manager = new GoLocalPow();
		local_pow_manager.init();
		IotaAPI api = new IotaAPI.Builder()
				.protocol("http")
				.host("localhost")
				.port("14265")
				.localPoW(local_pow_manager)
				.build();
		Integer depth = 3;

		GetNodeInfoResponse response = api.getNodeInfo();
		System.out.println(response.toString());

//		List<Transfer> transfers = new ArrayList<>();
//		String message = "";
//		transfers.add(new Transfer(address, 0, StringUtils.rightPad(message, 2187, '9'), tag));

		String[] tips = new String[2];
		System.out.println("Starting spam...");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		long start = timestamp.getTime();
		int num_transactions = 0;
		String tx_hash;
		ArrayList<String> past_txs = new ArrayList<String>();
		while (true) {
			try {
				String reference = api.getNodeInfo().getLatestSolidSubtangleMilestone();
				tips = getTips(api, depth, reference);
				tx_hash = newMilestone(api, tips[0], tips[1]);
				System.out.println(tx_hash + " attached at " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
				num_transactions++;
				Timestamp current_timestamp = new Timestamp(System.currentTimeMillis());
				long current_time = current_timestamp.getTime();
				double elapsed_time = (double) (current_time - start) / 1000;
				double tps = (double) num_transactions / elapsed_time;
				System.out.println("Time elapsed: " + String.valueOf(elapsed_time) + ", number of transactions: " + String.valueOf(num_transactions) + ", TPS: " + String.valueOf(tps));
				past_txs.add(num_transactions, tx_hash);

			} catch (Exception e) {
				System.out.println("Error sending tx: " + e.getMessage());
			}
		}
	}

	static String[] getTips(IotaAPI api, Integer depth, String reference) {
		String[] tips = new String[2];
		try {
			GetTransactionsToApproveResponse trunk_and_branch = api.getTransactionsToApprove(depth, reference);
			String trunk = trunk_and_branch.getTrunkTransaction();
			String branch = trunk_and_branch.getBranchTransaction();
			tips[0] = trunk;
			tips[1] = branch;
		} catch (Exception e) {
			System.out.println("error getting tips: " + e.getMessage());
			tips[0] = api.getNodeInfo().getLatestSolidSubtangleMilestone();
			tips[1] = api.getNodeInfo().getLatestSolidSubtangleMilestone();
		}
		return tips;

//		boolean is_milestone = true;
////		int num_tries = 0;
//		while (is_milestone) {
//			try {
////				num_tries++;
////				if (num_tries >= 0) {
////					reference = api.getNodeInfo().getLatestSolidSubtangleMilestone();
////				}
//				GetTransactionsToApproveResponse trunk_and_branch = api.getTransactionsToApprove(depth, reference);
//				String trunk = trunk_and_branch.getTrunkTransaction();
//				String branch = trunk_and_branch.getBranchTransaction();
//				tips[0] = trunk;
//				tips[1] = branch;
//				is_milestone = false;
//			} catch(Exception | IllegalAccessError e) {
//				System.out.println("error getting tips: " + e.getMessage());
//			}
//		}
//		return tips;
	}


	static String newMilestone (IotaAPI api, final String tip1, final String tip2) throws Exception {
		final Bundle bundle1 = new Bundle();
		long timestamp = System.currentTimeMillis() / 1000;
		bundle1.addEntry(1, ADDRESS, 0, TAG, timestamp);
		bundle1.finalize(null);
		bundle1.addTrytes(Collections.<String>emptyList());
		List<String> trytes = new ArrayList<>();
		for (Transaction trx : bundle1.getTransactions()) {
			trytes.add(trx.toTrytes());
		}
		Collections.reverse(trytes);
		GetAttachToTangleResponse rrr = api.attachToTangle(tip1, tip2, MWM, (String[]) trytes.toArray(new String[trytes.size()]));
//		GetAttachToTangleResponse rrr = api.attachToTangle(tip1, tip2, MWM, trytes);

		String[] tx_trytes_list = rrr.getTrytes();
		String tx_trytes = tx_trytes_list[0];
		String tx_hash = new Transaction(tx_trytes).getHash();
		System.out.println("tip1: " + tip1);
		System.out.println("tip2: " + tip2);
		System.out.println("arrays of trytes below:");
		System.out.println("trytes: " + trytes);
		System.out.println("trytes_array: " + tx_trytes);
		System.out.println("trytes_array 0: " + Arrays.toString(tx_trytes_list));
		System.out.println("tx_hash: " + tx_hash);
		api.storeTransactions(rrr.getTrytes());
		api.broadcastTransactions(rrr.getTrytes());
		return tx_hash;
	}
}
