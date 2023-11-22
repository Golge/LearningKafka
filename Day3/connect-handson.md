# Kafka Connect ornegi icin gerekli dosyalarin

## 1- github projesindeki jar | properties dosyalari

https://github.com/conduktor/kafka-connect-wikimedia

bu sitede asagida jar dosyalarini indirecegimiz bir link var. oradan gerekli jar dosyalarini indiriyoruz.
bir de ayni repo icinde connector klasorundeki wikimeadi.properties adli dosyayi indiriyoruz.


## 2- confluent hub dan elasticsearch sink connector indirilmesi

https://www.confluent.io/hub/confluentinc/kafka-connect-elasticsearch

## indirilen dosyalarin gerekli yerlere tasinmasi

- kafka kurulumu icin gerekli klasorde connectors adli klasor olusturun.

```bash
cd kafka_2.13-3.1.0/
mkdir connectors
cd connectors
mkdir kafka-connect-wikimedia
```

- indirdigimiz jar dosyasini olusturdugmuz dizine gonderelim.
- confluent den indirilen elasticsearch klasorunu zipten cikar lib klasorunde bulunan jar dosyalarini `kafka-connect-elasticsearh` adli klasore gonder

```bash
mkdir kafka-connect-elasticsearch
```

- `kafka_2.13-3.1.0/bin` kasorunde degisikler yapilmasi gerekiyor.

`/home/golge/kafka-for-beginners-code-20230404/code/2-kafka-extended/config/connect-standalone.properties` bu dosyada desiklik yapilmasi gerekli.

```bash
key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=true
value.converter.schemas.enable=true
offset.flush.interval.ms=10000

# LOCALHOST CONFIGURATION
bootstrap.servers=localhost:19092

offset.storage.file.filename=/tmp/connect.offsets
plugin.path=/home/golge/kafka_2.13-3.1.0/connectors
```

- son olarak calistirilacak komut

`connect-standalone.sh config/connect-standalone.properties config/wikimedia.properties`