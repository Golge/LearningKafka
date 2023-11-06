# Installation Guide
See here: https://conduktor.io/kafka/how-to-install-apache-kafka-on-windows

**WSL2** is Windows Subsystem for Linux 2 and provides a Linux environment for your Windows computer that does not require a virtual machine

To install WSL2, make sure you're on Windows 10 version 2004 and higher **(Build 19041 and higher)** or **Windows 11** To check your Windows version do ```Windows logo key + R```, type ```winver```, select OK

Steps are here: https://docs.microsoft.com/en-us/windows/wsl/install 

In an administrator PowerShell:
```wsl --install```

### Troubleshooting article here: 
https://docs.microsoft.com/en-us/windows/wsl/troubleshooting

### Launch WSL2 with Ubuntu (default)

Install Java JDK 11 (Amazon Corretto 11)
https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/linux-info.html

For Ubuntu for example: 
```bash
wget -O- https://apt.corretto.aws/corretto.key | sudo apt-key add - 
sudo add-apt-repository 'deb https://apt.corretto.aws stable main'
sudo apt-get update; sudo apt-get install -y java-11-amazon-corretto-jdk
```
### Check the Java version
```bash 
java -version 
```
Should say something like openjdk version "11.0.10" 2021-01-19 LTS

 1. Download Kafka at https://kafka.apache.org/downloads
```bash
wget https://archive.apache.org/dist/kafka/3.1.0/kafka_2.13-3.1.0.tgz
```
2. Extract Kafka
```bash
tar -xvf kafka_2.13-3.1.0.tgz
```

3. Move the folder & Open
```bash 
mv kafka_2.13-3.1.0 ~
cd kafka_2.13-3.1.0
```

Try out a Kafka command
```bash
bin/kafka-topics.sh
```

Edit .bashrc
```bash
vim ~/.bashrc
```
```bash
PATH="$PATH:/your/path/to/your/kafka/bin"
```
Example: PATH="$PATH:~/kafka_2.13-3.1.0/bin"

Open a new terminal. Try running the command from any directory:
```bash
kafka-topics.sh
```
