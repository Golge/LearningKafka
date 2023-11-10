# Schema Registry Hands On
Verilerin kalitesini ve tutarlılığını sağlamak için Kafka konuları üzerinden akan mesajların şemalarını tanımlamanız ve yönetmeniz gerekir. **Schema Registry**, Kafka **producer** ve **consumer** şemalarını depolayan ve doğrulayan merkezi bir hizmettir. 

## Avantajları
Kafka ile **Schema Registry** kullanmanın temel faydalarından biri, şema evrimine olanak sağlamasıdır; bu, **producer** ve **consumer** arasındaki **uyumluluğu** bozmadan verilerinizin yapısını ve biçimini değiştirebileceğiniz anlamına gelir. Şema kaydı, ihtiyaçlarınıza ve tercihlerinize bağlı olarak geriye dönük, ileriye dönük veya tam uyumluluk gibi farklı uyumluluk kurallarını uygulayabilir. **Schema Registry** kullanarak ayrıca **şema tekrarını** önleyebilir, mesajların boyutunu azaltabilir ve farklı uygulamalar ve ortamlar arasında şema yönetimini basitleştirebilirsiniz.

## Kafka ile Schema Registry nasıl kullanılır
Kafka ile Schema Registry kullanmak için, bunu producer ve consumer gibi Kafka istemcilerinizle entegre etmeniz gerekir. Kullandığınız dile ve çerçeveye bağlı olarak bunu yapmanın farklı yolları vardır. Örneğin, Java kullanıyorsanız, Schema Registry"den şemaları kaydetmek, almak ve önbelleğe almak için yöntemler sağlayan bir kütüphane olan Confluent Schema Registry Client'ı kullanabilirsiniz. Alternatif olarak, şema işlemlerini gerçekleştirmek için uç noktaları ortaya çıkaran bir web hizmeti olan Schema Registry REST API'sini kullanabilirsiniz. Schema Registry ile etkileşim kurmak için Kafka Connect, Kafka Streams veya KSQL gibi araçları da kullanabilirsiniz.

## Hands-on

 1. Aiven de hesap olustur. https://aiven.io/
 2. Kafka Cluster yarat. Schema registry ozelligini  aktif et.
 3. Conduktor console da Aiven Kafka cluster baglantisi yap.
	 - CA certificate kaydet
	 - Access key, Access certificate alanlarini Conduktor da tanit
	 - test connection
 4. Topic olustur `invoice-orders`
 5. Schema registry kaydi olustur. Asagidaki AVRO tipi schema'yi topic'e ekle.

```json
{
"fields": [
{
"name": "id",
"type": "string"
},
{
"name": "description",
"type": "string"
},
{
"name": "customer",
"type": "string"
},
{
"name": "company",
"type": "string"
},
{
"name": "total",
"type": "double"
}
],
"name": "MyRecord",
"namespace": "com.mycompany",
"type": "record"
}
```
## Kaynak
https://www.conduktor.io/blog/what-is-the-schema-registry-and-why-do-you-need-to-use-it/