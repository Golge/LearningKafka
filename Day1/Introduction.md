# Welcome to Kafka Course | DAY-1 Introduction

## Bu derste neler ogreneceksiniz?

- Kafka nedir? IT alaninda ne gibi cozumler saglar? Ornek Kullanim alanlari nelerdir? (Teori)

- Kafka Basics | Topics, Partitions, Offsets | Producers | Consumers - Consumer Groups | Brokers | Topic Replication | Producer ACK | Zookeeper | Kafka KRaft

- Kafka kurulumu icin gereksinimler | WSL | Docker Desktop | Java 11 JDK | VsCode | IntelliJ IDEA

- Conduktor tanitimi | Web UI | UPStash

- Kurs dosyalarinin Github'tan cekilmesi

- Kafka CLI komutlarina giris

### Kafka nedir? IT alaninda ne gibi cozumler saglar? Ornek Kullanim alanlari nelerdir? (Teori)

**Apache Kafka:** Verilerin bir sistemden hızlı bir şekilde toplanıp diğer sistemlere hatasız bir şekilde transferini sağlamak için geliştirilen dağıtık **(distributed)** bir veri akış mekanizmasıdır **(data stream)**. Apache Kafka, LinkedIn tarafından geliştirilmiş, Apache yönetiminde açık kaynak olarak çoğunlukla Confluent şirketi tarafından bakımı ve geliştirimi yapılan bir projedir. Dağıtık (distrubuted) bir veri akış (streaming) platformudur. Java ve Scala kullanilarak yazılmıştir. **(Platform Independent)** 

Kısaca Apache Kafka, birlikte çalışan sistemlerin birbirlerine olan bağımlılıklarını ortadan kaldırmak ve üzerlerinde oluşan ek yükleri düşürecek bir yapı gereksinimiyle ortaya çıkmıştır.

**Nerelerde Kullanilir?**

-   Mesajlaşma Sistemleri **(queue)**

-   Etkinlik Takibi 

-   Log Toplama Sistemleri **(logging)**

-   Stream Processing İşlemleri

-   Web Sayfası Etkinlik İzleme **(click Event - Flink)**

**Apache Kafka’nın Avantajları**

-   Yüksek trafikte düşük gecikme sağlayan Kafka  **hızlıdır**. (Fast - High Throughput)

-   Node ve Partition ile yatay  **ölçeklenebilir**. (Scalable)

-   Ölçeklendirilmiş ve hata toleransı olduğu için  **güvenilirdir**. (Reliable)

-   Veri kaybı olmaz, mesajlar disk üzerinde immutable log ile yazıldığı için  **devamlılık sağlar, sağlamdır**. (Permanent Storage, Durable) 

-   Bir Apache projesi olduğu için  **açık kaynak kodludur.** 

Apache Kafka  **LinkedIn, Spotify, YouTube, Slack, Udemy, Netflix, Twitter**  ve bu firmalar gibi çok yüksek boyutlarda verilere sahip firmalar tarafından kullanılmaktadır.

**Ornek Kullanim (Queue)**
SimpleQueueService klasoru > Start Docker Desktop / Kubernetes
ActiveMQ - Truck Tracking System
```bash
kubectl apply -f .
kubectl get pods
```
check `localhost:30080` uygulama - frontend
check `localhost:30010` ActiveMQ Paneli. Calisan Queue gorebilmek icin sisteme giris: `username: admin` , `sifre: admin`

Sistemi kapatmak icin:
```bash
kubectl delete -f .
```

### Gerekli Kurulum Islemleri
Dersi takip etmek icin hem lokal makinada hem de WSL uzerinde bazi kurulumlar yapilmalidir. Bu kurulumlarin bazilari **derse gelinmeden** once yapilmasi ders akisi icin onemlidir. 

Local makinada yapilacak kurulumlar kursiyerler tarafindan sorunsuz bir sekilde yapilabilir. WSL kisminda bulunan kurulumlar ise ders esnasinda yapilacaktir.

Kurulum islemleri icin gerekli linkler asagida mevcuttur.

| LOKAL Makina Kurulumlari | WSL Kurulumlari|
|--|--|
| VsCode | Java Corretto |
|IntelliJ IDEA ( Community Edition ) | Kafka CLI |
| WSL | Conduktor Console with Docker Compose|
| Docker Desktop | |
| JDK 11 Corretto | |
|Upstash Account (Derste olusturulacak)| |

### Gerekli Linkler

https://code.visualstudio.com/

https://www.jetbrains.com/idea/download/?section=windows

https://www.docker.com/products/docker-desktop/

https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html

https://upstash.com/