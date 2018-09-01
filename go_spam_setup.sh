sudo apt-get update
sudo apt-get -y install maven
sudo rm -rf /home/iota/spam
sudo mkdir /home/iota
sudo mkdir /home/iota/spam
cd /home/iota/spam && sudo git clone https://github.com/tanglewise/JavaGoSpam.git && cd /home/iota/spam/JavaGoSpam
sudo wget -c https://storage.googleapis.com/golang/go1.9.6.linux-amd64.tar.gz && sudo tar -C /usr/local -xzf go1.9.6.linux-amd64.tar.gz && export PATH=$PATH:/usr/local/go/bin && sudo apt install -y golang-go
cd /home/iota && sudo git clone https://github.com/iotaledger/iota.lib.java && cd iota.lib.java && sudo mvn install
cd /home/iota/spam/JavaGoSpam
sudo go get -u github.com/iotaledger/giota
sudo go build pow.go
sudo mvn install && sudo mvn package
