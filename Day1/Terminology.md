# Welcome to Kafka Course | DAY-1 Terminology

  Apache Kafka'yi ogrenmek icin arka planda calisan sistemleri bilmek gereklidir. Bunun icin asagida belirtilen konular hakkinda teorik bir altyapi olusturulacaktir.![Distributed Data Stream](https://www.conduktor.io/kafka/_next/image/?url=https://images.ctfassets.net/o12xgu4mepom/36HRcNifBz55AUvkBs1p9x/31af23c93be2c7b0d1233a3f9f8797e5/What_is_Apache_Kafka_Part_1_-_Decoupling_Different_Data_Systems.png&w=1920&q=75)

## Bu derste neler ogreneceksiniz?
 1. Topics
 2. Partitions
 3. Brokers
 4. Topic Replication 
 5. Producers - Producer ACK 
 6. Consumers - Consumer Groups 
 7. Zookeeper ve KRaft

**1. Topics (Konu)**
Kafka’da  **Topic (Konu)**  kelimesi belirli bir veri akışını depolamak ve bu verileri yayınlamak için kullanılan kategoriyi niteler. İşlevi gereği topicleri bir veri tabanı tablosuymuş gibi düşünebiliriz ancak tam anlamıyla aynı değillerdir. Topicler veri tabanının sağladığı tüm constraintleri içermez. İstediğimiz kadar topic oluşturabiliriz ve isteğe bağlı olarak bu topiclere isimler verebiliriz. Bir üretici **(producer)** topiclere veri yazar, bir tüketici **(consumer)** de topicle ilgili verilere abone **(pub-sub)** olarak bu verileri okur.

**2. Partitions (Bölümler)**
Bir topic, kendisinin bölümleri olacak şekilde birkaç bölüme bir sıra ile ayrılır. Bu neden bir topic oluştururken bölüm sayısını belirtmemiz beklenir. Her bir mesaj  **offset**  olarak adlandırılan, artımlı bir kimliğe sahip olur. Offset değerlerinin sırası topic genelinde değil de bölüm içinde geçerli olmakla birlikte bir bölüm sonsuz sayıda offset’e sahip olabilmektedir. Bir bölüme yazılan veriler değiştirilemezdir **(immutable)**. Bunu ancak Kafka yapabilir.
![1Topic, 4Partitions, 2Producers](https://miro.medium.com/v2/resize:fit:720/format:webp/1*f-CJRgeykF2HYEWElTBDBg.png)

**3. Brokers (Aracılar)**
Bir Kafka Cluster, brokers veya Kafka Brokers olarak bilinen bir veya birden fazla sunucudan oluşmaktadır. İstemcilerden gelen tüm istekleri işler ve cluster içinde çoğaltılan verileri tutar. Her bir broker bir tür veri içerir. Aşağıdaki görselde bir Kafka Cluster yapısı örneği gösterilmektedir.
![Kafka Cluster - Broker yapisi](https://miro.medium.com/v2/resize:fit:720/format:webp/1*iw9jxZji1rDmKAEhIjJOuA.png)

**4. Topic Replication**
Bir Broker’ın bozulması sonucunda verilerin kaybolması gibi bir problem ortaya çıkacaktır. Kafka, veri kaybı durumunun yaşanmaması için bizlere bir Replication (Çoğaltma) özelliği sunar. Bunu da bir broker’da bulunan topicler için bir  **Çoğaltma Faktörü (Replication Factor)**  oluşturarak yapar.
![Replication Factor of 3](https://miro.medium.com/v2/resize:fit:720/format:webp/1*vXyhU_0S22UKNnTQufBBsg.png)

**5. Producer (Üretici)**
Bir producer topiclere veri yazar. Bu yazma işlemini yaparken, kullanıcının broker’ı ve bölümü belirtmesine ihtiyac duymadan, verinin hangi bölüme ve broker’a yazılması gerektiğini bilirler.

**5.1. Message Keys (Mesaj Anahtarları)**
Mesaj anahtarları, Kafka’nın mesajları belirli bir sıra ile göndermesi için kullanılır. Bu parametre opsiyoneldir. (belirtilebilir veya belirtilmez)
**5.2. Acknowledgement**
Acknowledgement, veri yazma işleminde producer’a bir başka onay verme seçeneğidir. Bunun için üç opsiyonumuz vardır:

-   **acks=0:** hic bekleme
-   **acks=1:** sadece lideri bekle
-   **acks=all (-1):** tum in-sync (ISR) brokerlari bekle
![Kafka Ecosystem](https://www.conduktor.io/kafka/_next/image/?url=https://images.ctfassets.net/o12xgu4mepom/6TdnM9oVflXLhWBSicN58s/1b13dd58a82f853dae39d07f72e0b9c2/Kafka_Cluster__-_Fundamentals.png&w=1920&q=75)

|        ACKS     |     Gecikme (Latency)   |   Verimlilik (Throughput)   |     Devamlılık - Sağlamlık (Durability)        |
| --------------- | ------------------------| --------------------------- | -----------------------------------------------|
|         0       |       Düşük (Low)       |       Yüksek (High)         |      Garanti Edilmez (No Guarantee)            |
|         1       |     Orta (Medium)       |        Orta (Medium)        |       Sadece Liderler (Leaders Only)           |   
|      All (-1)   |       Yüksek (High)     |          Düşük (Low)        |      Tüm Liderler ve Kopyalar (All Leaders and All Replicas)      |

**6. Consumer (Tüketici) ve Consumer Groups (Tüketici Grupları)**
Bir consumer, topic üzerinden verileri istedikleri herhangi bir offset noktasından okur veya tüketir diyebiliriz :). Ayrıca consumer, verileri hangi broker(lar)dan okuması gerektiğini bilir ve aynı anda birden vazla brokerdan verileri okuyabilir.

Consumerlar verileri gruplar halinde okurlar. Bir consumer tek başına bile olsa bir grubu vardır. Grubu kullanıcı oluşturmadığı takdirde Kafka kullanıcı adına bu grubu oluşturur. Her Consumer Grup bir **grup id**’sine sahiptir. Grup id **load balance** için çok önemli bir yere sahiptir. Örnek verecek olursak 10 bölümlü bir topic varsa, aynı grup id’sine sahip iki consumerın her biri normal şartlarda 5 veri okuma işlemi yapacaktır.

**7. Zookeeper ve KRaft**
**KRaft**, Kafka'nın veri yönetimi için **ZooKeeper**'a bağımlılığını ortadan kaldırmamızı sağlayan konsensus protokoldür. Kararli halinin yayinlanmasi Kafka 4.0 icin planlanmaktadir. KRaft, veri sorumluluğunu iki farklı sisteme bölmek yerine Kafka'nın içerisinde birleştirerek Kafka mimarisini basitleştirir. Zookeeper a gore daha guvenlidir ve performansi iyilestirir.
Buna ragmen kararli hali hala yayinlanmadigi icin **zookeeper** ile kurulumlar yapmak akillica olacaktir. 
![Zookeeper Vs KRaft](https://images.ctfassets.net/gt6dp23g0g38/7gQZn9CnRAT60NeyYBYflL/b144fee6dad28ce97c3e91e6d09d1167/20230616-Diagram-KRaft.jpg)