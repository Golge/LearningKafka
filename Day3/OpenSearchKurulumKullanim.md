# OpenSearch Kurulumu ve Kullanimi

OpenSearch, birçok web sitesi ve hizmetin içeriklerini arama motorları ve uygulamalar tarafından **indekslemek** ve erişilebilir hale getirmek için kullanılanilir. OpenSearch, özellikle **veri paylaşımı** ve **erişim kolaylığı** sağlama amacıyla geliştirilmiştir. **ElasticSearch** uzerine kurulu bir yapisi bulunur.

## Opensearch u Docker ile localhostta kurmak icin yapilmasi gerekenler:
Oncelikle farkli calisan containerlari ve uygulamalari `Ctrl + C` ile kapatalim.

1. kafka-consumer-opensearch klasorune WSL VSCode terminalinde gecelim.
2. `docker-compose.yml` dosyasinin bulundugu klasorde docker compose komutunu girelim. `docker-compose -f docker-compose.yml up`
3. uygulaminin geldigini kontrol etmek icin `localhost:9200` ve `localhost:5601` sayfalarini kontrol edelim.

### Troubleshoot
Onceki calismalardan cakisma olan network kurulumu veya imajlar varsa terminal uzerinden silelim.
- `docker network ls`
- `docker network rm <networkID>`

## OpenSearch Kulanimi
Dashboarda gecelim ve asagidaki komutlari girelim

Sistemimizde kurulu Opensearch ile ilgili bilgiler:
> GET /

index olusturma:
> PUT /my-first-index

olusturulan index e JSON dokumani gonderme:
> PUT /my-first-index/_doc/1
> {"Description":  "Olmak yada olmamak."}

Gonderdigimiz JSON dokumanini cekme:
> GET /my-first-index/_doc/1

Bir JSON dokumanini silme:
> DELETE /my-first/index/_doc/1
