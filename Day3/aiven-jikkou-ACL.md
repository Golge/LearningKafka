
# Kafka Governance | ACL HandsOn
Bu bolum icin WSL'e bagli VSCode ortamini kullanacagiz.
```bash
Download the latest stable release of Jikkou

wget https://github.com/streamthoughts/jikkou/releases/download/v0.30.0/jikkou-0.30.0-linux-x86_64.zip

# Uncompress
unzip jikkou-0.30.0-linux-x86_64.zip

# Copy to the desired location
cp jikkou-0.30.0-linux-x86_64/bin/jikkou $HOME/.local/bin
```
## Konfigurasyon ayarlari
**NOT:** Aiven"dan token alinmasi gerekli.
asagidaki kodu gerekli degiskenleri girerek calistirin.
```bash
jikkou config set-context aiven-demo \  
--config-props=aiven.project=<AIVEN_PROJECT_NAME>\  
--config-props=aiven.service=<AIVEN_SERVICE_NAME> \  
--config-props=aiven.tokenAuth=<AIVEN_AUTHENTICATION_TOKEN>  
  
jikkou config use-context aiven-demo
```
Sistemde kayitli ACL listesini gormek icin asagidaki komutu girin.

`jikkou get avn-kafka-topic-acl`

donmesi beklenen sonuc:
```bash
apiVersion: "kafka.aiven.io/v1beta1"
kind: "KafkaTopicAclEntry"
metadata:
  labels: {}
  annotations:
    kafka.aiven.io/acl-entry-id: "default"
    jikkou.io/generated: "2023-11-11T13:30:54.967131Z"
spec:
  permission: "ADMIN"
  username: "avnadmin"
  topic: "*"
```
### Yeni bir ACL olusturma

```bash
mkdir ACL & cd ACL
touch aiven-topic-acls.yaml 
```
```bash
# ./aiven-topic-acls.yaml  
---  
apiVersion:  "kafka.aiven.io/v1beta1"  
kind:  "KafkaTopicAclEntry"  
spec:  
permission:  "READWRITE"  
username:  "alice"  
topic:  "private-alice*"
```
yeni olusan ACL'i Aiven console da gorebilirsiniz.

### ACL silme islemi 
`jikkou.io/delete`  annotation kullanarak onceden olusturulan bir ACL silinebilir.

Once gerekli yaml dosyasini olusturun.
```bash
# ./aiven-topic-acls.yaml  
---  
apiVersion: "kafka.aiven.io/v1beta1"  
kind: "KafkaTopicAclEntry"  
metadata:  
  annotations:  
    jikkou.io/delete: true  
spec:  
 permission: "READWRITE"  
 username: "alice"  
 topic: "private-alice*"
```
`jikkou delete --files aiven-topic-acls.yaml`

komutun beklenen ciktisi
```bash
TASK [DELETE] Delete Kafka ACL Entry for user 'alice' (topic=private-alice*, permission=readwrite) - CHANGED   
{  
  "status" : "CHANGED",  
  "changed" : true,  
  "failed" : false,  
  "end" : 1690960139775,  
  "data" : {  
    "before" : {  
      "permission" : "readwrite",  
      "topic" : "private-alice*",  
      "username" : "alice",  
      "id" : "acl44e40d03141"  
    },  
    "operation" : "DELETE"  
  }  
}  
EXECUTION in 917ms   
ok : 0, created : 0, altered : 0, deleted : 1 failed : 0
```
### coklu ACL olusturma islemi
Birden fazla ACL olusturmak icin **template** ozelliginden faydalanacagiz.

gerekli 2 dosyayi olusturalim.
```bash 
# ./aiven-users.yaml  
---  
users:  
  - username: alice  
    acls:  
      - spec:  
        permission: "WRITE"  
        topic: "private-alice*"  
  - username: bob  
    acls: []
```
```bash
#./aiven-topic-acls.tpl  
---  
apiVersion: "kafka.aiven.io/v1beta1"  
kind: "KafkaTopicAclEntry"  
spec:  
 permission: "ADMIN"  
 username: "avnadmin"  
 topic: "*"  
{% for user in values.users %}  
---  
apiVersion: "kafka.aiven.io/v1beta1"  
kind: "KafkaTopicAclEntry"  
spec:  
  permission: "READ"  
  username: "{{ user.username }}"  
  topic: "public-*"  
{% for acl in user.acls %}  
---  
apiVersion: "kafka.aiven.io/v1beta1"  
kind: "KafkaTopicAclEntry"  
spec:  
  permission: "{{ acl.permission }}"  
  username: "{{ user.username }}"  
  topic: "{{ acl.topic }}"  
{% endfor %}  
{% endfor %}
```
yukaridaki iki dosyayi Jikkou CLI da arguman olarak girmek icin:
```bash
jikkou apply \  
  --files aiven-topic-acls.tpl \  
  --values-files aiven-users.yaml \  
  --options delete-orphans=true
```
Olusan yeni durumu gozlemlemek icin Aiven console a bakabiliriz.

![](https://miro.medium.com/v2/resize:fit:875/1*8RyzaTPpODnacUMk7D_9Wg.png)

## Kaynaklar

- https://github.com/streamthoughts/jikkou

- https://medium.com/@fhussonnois/jikkou-declarative-acls-configuration-for-apache-kafka-and-schema-registry-on-aiven-a3a48b8fc950