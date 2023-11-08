# Conduktor Web UI Kurulumu | Upstash Kafka Cluster | Docker Compose
Kafka kurulumu yapildiktan sonra cluster ile etkilesim tamamen **CLI** uzerinden yapilmaktadir. Ancak **Development** ve **egitim** icin CLI uzerinden calismak oldukca zordur. Bu zorlugun ustesinden gelmek ve Kafka cluster a bir arayuz kazandirmak icin **Conduktor Console** u **Docker** komutlari ile bilgisayarimizda calistiracagiz. Conduktor console un bilgisayaramizda sorunsuz calistigindan emin olduktan sonra ise https://upstash.com/ (Uzak sunucu) uzerinde olusturacagimiz bir cluster a guvenli baglanti yapacagiz.
## Conduktor Console nedir? https://www.conduktor.io/
Conduktor, Apache Kafka'yı daha kolay yönetmek ve izlemek için geliştirilen bir dizi araç içerir. Conduktor Console, bu setin bir bileşenidir ve Kafka brokerlarını, konuları, tüketici gruplarını ve daha fazlasını kolayca izlemenize ve yönetmenize yardımcı olan kullanıcı dostu bir arayüze sahiptir.

https://github.com/conduktor/kafka-stack-docker-compose

GitHub reposunu WSL uzerinden kendi bilgisayarimiza indirelim. `full-stack.yml` ve `conduktor.yml` dosyalarini aciklayalim.
```bash
git clone https://github.com/conduktor/kafka-stack-docker-compose.git
cd kafka-stack-docker-compose
```
Klasor adini `docker-compose-kafka-stack` olarak degistirelim. (Opsiyonel). 
```bash 
mv ./kafka-stack-docker-compose docker-compose-kafka-stack
```
Docker compose u calistiralim... ve bir sure bekleyelim **(Ilk kurulum icin image larin cekilmesi uzun suruyor. 15 dk kadar)**
```bash
docker compose -f full-stack.yml up
docker compose -f full-stack.yml down
```
Conduktor Console a giris:
 - localhost:8080
 -  login: `admin@admin.io`  
 -  password: `admin`

## Upstash Kafka Cluster
Upstash, bulut tabanlı bir veritabanı hizmetidir ve özellikle **Apache Kafka** için sunulan bir hizmettir. Upstash, Kafka'in güçlü özelliklerini sunan ve bulut tabanlı altyapısı ile kullanıcıların hızlı ve ölçeklenebilir uygulamalar geliştirmelerine yardımcı olan bir hizmettir. Upstash, bulut platformlarının **(AWS, Azure, Google Cloud, Heroku, vb.)** birçok bölgesinde hızlı ve güvenilir bir şekilde çalışır.
 1. Upstash e uyelik. 
 2. Kafka cluster olusturma. `my-sandbox`
 3. Properties kodlarini kopyalama. **Sifre kismi acik olarak!!!**
 4. `playground.config` dosyasi olusturma. **Kafka CLI'in calistigi dizinde!!!**
 5. `playground.config`dosyasini Conduktor a tanitma.

Ornek: 
```bash
kafka-topics.sh  --command-config  playground.config  --bootstrap-server  cluster.playground.cdkt.io:9092
```
