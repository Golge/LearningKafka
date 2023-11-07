# Exploring Conduktor Gateway

Let's create the virtual cluster Tom

```bash
userName=tom
userToken=`curl \
    --silent \
    --request POST "http://localhost:8888/admin/auth/v1beta1/tenants/$userName" \
    --user "superUser:superUser" \
    --header 'accept: application/json' \
    --header 'Content-Type: application/json' \
    --data-raw '{"lifeTimeSeconds": 7776000}'\
     | jq -r ".token"`
curl     --silent     --request POST "http://localhost:8888/admin/auth/v1beta1/tenants/$userName"     --user "superUser:superUser"     --header 'accept: application/json'     --header 'Content-Type: application/json'     --data-raw '{"lifeTimeSeconds": 7776000}'     | jq -r ".token"
```
```bash
echo  """
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="$userName" password="$userToken";
""" > clientConfig/$userName.properties
```
Let's look at its service account

`cat clientConfig/tom.properties`

`security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username=tom password=eyJhbGciOiJIUzUxMiJ9.eyJ1c2VybmFtZSI6InRvbSIsInRlbmFudCI6InRvbSIsImV4cCI6MTY5MzUxMTY3Mn0.4aA5rK_xgmpHDNdJ49tXR-3zYxnSJPNP3bbFPGs6sk2AoBD-hQhcyNMsg-A30o4e5jaqDAGBYwfyglGZ5XrG9A;
`

New virtual cluster = no topic

```bash 
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/tom.properties \
        --list
```

Success, there's no topic

Create `cars` topic

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/tom.properties \
        --topic cars \
        --replication-factor 1 \
        --partitions 1 \
        --create
```
Created topic `cars`.

Verify the topic is created

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/tom.properties \
        --list
```
`cars`

Success, the topic 'cars' has been created

Sending data in 'cars' topic

```bash
echo '{
    "type": "Ferrari",
    "color": "red",
    "price": 10000
}' | jq -c | docker compose exec -T kafka-client \
    kafka-console-producer  \
        --bootstrap-server gateway:6969 \
        --producer.config /clientConfig/tom.properties \
        --topic cars
```
```bash
echo '{
    "type": "RollsRoyce",
    "color": "black",
    "price": 9000
}' | jq -c | docker compose exec -T kafka-client \
    kafka-console-producer  \
        --bootstrap-server gateway:6969 \
        --producer.config /clientConfig/tom.properties \
        --topic cars
```
```bash
echo '{
    "type": "Mercedes",
    "color": "black",
    "price": 6000
}' | jq -c | docker compose exec -T kafka-client \
    kafka-console-producer  \
        --bootstrap-server gateway:6969 \
        --producer.config /clientConfig/tom.properties \
        --topic cars
```
Reading the data back

```bash
docker compose exec kafka-client \
    kafka-console-consumer  \
        --bootstrap-server gateway:6969 \
        --consumer.config /clientConfig/tom.properties \
        --topic cars \
        --from-beginning \
        --max-messages 3 | jq .
{
  "type": "Ferrari",
  "color": "red",
  "price": 10000
}
{
  "type": "RollsRoyce",
  "color": "black",
  "price": 9000
}
{
  "type": "Mercedes",
  "color": "black",
  "price": 6000
}
```

Processed a total of 3 messages

Perfect, we got the data back

Let's create another virtual cluster: `Emma`

```bash
userName=emma
userToken=$(curl \
    --silent \
    --request 'POST' "http://localhost:8888/admin/auth/v1beta1/tenants/$userName" \
    --user "superUser:superUser" \
    --header 'accept: application/json' \
    --header 'Content-Type: application/json' \
    --data-raw '{"lifeTimeSeconds": 7776000}'\
     | jq -r ".token")
curl     --silent     --request 'POST' "http://localhost:8888/admin/auth/v1beta1/tenants/$userName"     --user "superUser:superUser"     --header 'accept: application/json'     --header 'Content-Type: application/json'     --data-raw '{"lifeTimeSeconds": 7776000}'     | jq -r ".token"
```
```bash
echo  """
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="$userName" password="$userToken";
""" > clientConfig/$userName.properties
```
`Emma` is a brand new virtual cluster

```bash 
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/emma.properties \
        --list
```

Perfect, Emma has no topic: this cluster is perfectly isolated

Let's create the 'cars' topic to prove clusters are isolated

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/emma.properties \
        --topic cars \
        --replication-factor 1 \
        --partitions 1 \
        --create
```
Created `topic cars`.

Verify the topic is created

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/emma.properties \
        --list
```
cars

Success, the topic 'cars' has been created

Let's verify we are isolated from tom's 'cars' topic

```bash
docker compose exec kafka-client \
    kafka-console-consumer  \
        --bootstrap-server gateway:6969 \
        --consumer.config /clientConfig/emma.properties \
        --topic cars \
        --from-beginning \
        --timeout-ms 4000
```
[2023-06-02 19:55:45,712] ERROR Error processing message, terminating consumer process:  (kafka.tools.ConsoleConsumer$)
org.apache.kafka.common.errors.TimeoutException
Processed a total of 0 messages

Success, there is no data

Let's reveal the magic by listing the topics on the underlying Kafka

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server kafka1:9092 \
        --list | grep cars
```
emmacars
tomcars

We enfore isolation by added prefixes on topics, consumer-groups, acl and transactions

Let's get back to tom's cluster

Let's create a virtual topic so we get to only see red cars

```bash
docker compose exec kafka-client \
    curl \
        --silent \
        --request POST "gateway:8888/topicMappings/tom/red_cars" \
        --user "superUser:superUser" \
        --header 'Content-Type: application/json' \
        --data-raw '{
                        "topicName": "tomcars",
                        "isVirtual": true
                    }'
```
"SUCCESS"
```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/tom.properties \
        --topic red_cars \
        --replication-factor 1 \
        --partitions 1 \
        --create
```
WARNING: Due to limitations in metric names, topics with a period ('.') or underscore ('_') could collide. To avoid issues it is best to use either, but not both.
Created topic `red_cars`

Let's ask gateway to do a sql projection and restriction clause

`sql="SELECT '$.type' as type, '$.price' as price FROM red_cars WHERE '$.color' = 'red'"`

```bash
echo docker compose exec kafka-client \
    curl \
        --silent \
        --request POST "gateway:8888/tenant/tom/feature/sql-filter" \
        --user "superUser:superUser" \
        --header 'Content-Type: application/json' \
        --data-raw '{
            "config": {
                "statement": "'$sql'"
            },
            "direction": "RESPONSE",
            "apiKeys": "FETCH"
        }'
```
```bash
docker compose exec kafka-client curl --silent --request POST gateway:8888/tenant/tom/feature/sql-filter --user superUser:superUser --header Content-Type: application/json --data-raw {
            "config": {
                "statement": "SELECT '$.type' as type, '$.price' as price FROM red_cars WHERE '$.color' = 'red'"
            },
            "direction": "RESPONSE",
            "apiKeys": "FETCH"
        }
```
Let's make sure we only get red cars

```bash
docker compose exec kafka-client \
    kafka-console-consumer  \
        --bootstrap-server gateway:6969 \
        --consumer.config /clientConfig/tom.properties \
        --topic red_cars \
        --from-beginning \
        --max-messages 1 | jq .
{
  "type": "Ferrari",
  "color": "red",
  "price": 10000
}
```
Processed a total of 1 messages

Perfect, we are getting only red cars!

Wait, 'cars' topic is badly configured!

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/tom.properties \
        --topic cars \
        --describe
```
Topic: cars     PartitionCount: 1       ReplicationFactor: 1    Configs:
        Topic: cars     Partition: 0    Leader: 1       Replicas: 1     Isr: 1

Replication factor is 1? This is bad: we can lose data!

Let's make sure this problem is never repeated

While we're at it, let's make sure we don't abuse partitions either

```bash
docker compose exec kafka-client \
    curl \
        --silent \
        --request POST "gateway:8888/tenant/tom/feature/guard-create-topics" \
        --user "superUser:superUser" \
        --header 'Content-Type: application/json' \
        --data-raw '{
            "config": {
                "minReplicationFactor": 2,
                "maxReplicationFactor": 2,
                "minNumPartition": 1,
                "maxNumPartition": 3
            },
            "direction": "REQUEST",
            "apiKeys": "CREATE_TOPICS"
        }'
```
"SUCCESS"
Testing our policy

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/tom.properties \
        --topic cars_with_bad_replication_factor_and_many_partitions \
        --replication-factor 1 \
        --partitions 100 \
        --create
```
WARNING: Due to limitations in metric names, topics with a period ('.') or underscore ('_') could collide. To avoid issues it is best to use either, but not both.
Error while executing topic command : Request parameters do not satisfy the configured policy. Number partitions is '100', must not be greater than 3. Replication factor is '1', must not be less than 2
[2023-06-02 19:56:41,281] ERROR org.apache.kafka.common.errors.PolicyViolationException: Request parameters do not satisfy the configured policy. Number partitions is '100', must not be greater than 3. Replication factor is '1', must not be less than 2
 (kafka.admin.TopicCommand$)

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/tom.properties \
        --topic roads \
        --replication-factor 2 \
        --partitions 100 \
        --create
```
Error while executing topic command : Request parameters do not satisfy the configured policy. Number partitions is '100', must not be greater than 3
[2023-06-02 19:56:42,487] ERROR org.apache.kafka.common.errors.PolicyViolationException: Request parameters do not satisfy the configured policy. Number partitions is '100', must not be greater than 3
 (kafka.admin.TopicCommand$)

Topic creation is `denied` by our policy

Let's now create a topic that is within our policy

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/tom.properties \
        --topic roads \
        --replication-factor 2 \
        --partitions 3 \
        --create
```
Created topic roads.

Perfect, it has been created

Let's make sure we enforce policies when we alter topics too

Let's enforce that retention can only be between 1 and 5 days

```bash
docker compose exec kafka-client \
    curl \
        --silent \
        --request POST "gateway:8888/tenant/tom/feature/guard-alter-configs" \
        --user "superUser:superUser" \
        --header 'Content-Type: application/json' \
        --data-raw '{
            "config": {
                "minRetentionMs": 86400000,
                "maxRetentionMs": 432000000
            },
            "direction": "REQUEST",
            "apiKeys": "ALTER_CONFIGS"
        }'
```
"SUCCESS"
Update `cars` with a retention of `60 days`

```bash
docker compose exec kafka-client \
    kafka-configs \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/tom.properties \
        --topic roads \
        --add-config retention.ms=5184000000 \
        --alter
```
Error while executing config command with args '--bootstrap-server gateway:6969 --command-config /clientConfig/tom.properties --topic roads --add-config retention.ms=5184000000 --alter'
java.util.concurrent.ExecutionException: org.apache.kafka.common.errors.PolicyViolationException: Request parameters do not satisfy the configured policy. retention.ms is '5184000000', must not be greater than 432000000
        at java.base/java.util.concurrent.CompletableFuture.reportGet(CompletableFuture.java:395)
        at java.base/java.util.concurrent.CompletableFuture.get(CompletableFuture.java:2022)
        at org.apache.kafka.common.internals.KafkaFutureImpl.get(KafkaFutureImpl.java:180)
        at kafka.admin.ConfigCommand$.alterConfig(ConfigCommand.scala:359)
        at kafka.admin.ConfigCommand$.processCommand(ConfigCommand.scala:326)
        at kafka.admin.ConfigCommand$.main(ConfigCommand.scala:97)
        at kafka.admin.ConfigCommand.main(ConfigCommand.scala)
Caused by: org.apache.kafka.common.errors.PolicyViolationException: Request parameters do not satisfy the configured policy. retention.ms is '5184000000', must not be greater than 432000000

Altering the topic is denied by our policy

Update `cars` with a retention of `3 days`

```bash
docker compose exec kafka-client \
    kafka-configs \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/tom.properties \
        --topic roads \
        --add-config retention.ms=259200000 \
        --alter
```
Completed updating config for topic roads.

Topic updated successfully

You can have many different policies, even on produce!

```bash
docker compose exec kafka-client \
    curl \
        --silent \
        --request POST "gateway:8888/tenant/tom/feature/guard-produce" \
        --user "superUser:superUser" \
        --header 'Content-Type: application/json' \
        --data-raw '{
                "config": {
                    "acks" : [-1],
                    "compressions": ["ZSTD", "LZ4"]
                },
                "direction": "REQUEST",
                "apiKeys": "PRODUCE"
            }'
```
"SUCCESS"
Let's try sending again data

```bash
echo '{
    "type": "Fiat",
    "color": "red",
    "price": 200
}' | jq -c | docker compose exec -T kafka-client \
    kafka-console-producer  \
        --bootstrap-server gateway:6969 \
        --producer.config /clientConfig/tom.properties \
        --request-required-acks 1 \
        --topic cars
```
[2023-06-02 19:57:26,616] ERROR Error when sending message to topic cars with key: null, value: 41 bytes with error: (org.apache.kafka.clients.producer.internals.ErrorLoggingCallback)
org.apache.kafka.common.errors.PolicyViolationException: Request parameters do not satisfy the configured policy. Invalid value for 'acks': 1. Valid value is one of the values: -1. Invalid value for 'compressions': none. Valid value is one of the values: ZSTD, LZ4

Message is denied by our policy

Let's try sending again data and setting compression

```bash
echo '{
    "type": "Fiat",
    "color": "red",
    "price": 200
}' | jq -c | docker compose exec -T kafka-client \
    kafka-console-producer  \
        --bootstrap-server gateway:6969 \
        --producer.config /clientConfig/tom.properties \
        --topic cars \
        --compression-codec zstd
```
Message accepted

Let's remove this policy

```bash
docker compose exec kafka-client curl \
    --silent \
    --request DELETE "gateway:8888/tenant/tom/feature/guard-produce/apiKeys/PRODUCE/direction/REQUEST" \
    --user "superUser:superUser"
```
"SUCCESS"
Messages have no context, so it's hard to help the right teams

As an admin, I would like to add headers to messages

```bash
docker compose exec kafka-client \
    curl \
        --silent \
        --request POST "gateway:8888/tenant/tom/feature/inject-header" \
        --user "superUser:superUser" \
        --header 'Content-Type: application/json' \
        --data-raw '{
            "config": {
                "topic": "roads",
                "keys": {
                "X-USER-IP": "{{userIp}}",
                "X-TENANT": "{{tenant}}"
                }
            },
            "direction": "REQUEST",
            "apiKeys": "PRODUCE"
        }'
```
"SUCCESS"
Let's send data in 'road' without header

```bash
echo '{
    "town": "Paris",
    "road": "Avenue des champs Elysées"
}' | jq -c | docker compose exec -T kafka-client \
    kafka-console-producer  \
        --bootstrap-server gateway:6969 \
        --producer.config /clientConfig/tom.properties \
        --topic roads
```
Let's consume it back to showcase headers have been added

```bash
docker compose exec kafka-client \
    kafka-console-consumer \
        --bootstrap-server gateway:6969 \
        --consumer.config /clientConfig/tom.properties \
        --topic roads \
        --from-beginning \
        --max-messages 1 \
        --property print.headers=true
```
X-USER-IP:172.23.0.5,X-TENANT:tom       {"town":"Paris","road":"Avenue des champs Elysées"}
Processed a total of 1 messages

Headers have been added, I now know whom to help!

Some fields need to be protected

Let's **mask** the `road` field

```bash
docker compose exec kafka-client \
    curl \
        --silent \
        --request POST "gateway:8888/tenant/tom/feature/data-masking" \
        --user "superUser:superUser" \
        --header 'Content-Type: application/json' \
        --data-raw '{
            "config": {
                "policies": [
                    {
                        "name": "Mask road",
                        "rule": {
                            "type": "MASK_ALL",
                            "maskingString": "********"
                        },
                        "fields": [
                            "road"
                        ]
                    }
                ]
            },
            "direction": "RESPONSE",
            "apiKeys": "FETCH"
        }'
```
"SUCCESS"
Let's **verify** the `road` field has been masked

```bash
docker compose exec kafka-client \
    kafka-console-consumer \
        --bootstrap-server gateway:6969 \
        --consumer.config /clientConfig/tom.properties \
        --topic roads \
        --from-beginning \
        --max-messages 1 | jq .
{
  "town": "Paris",
  "road": "********"
}
```
Processed a total of 1 messages

'road' field is masked

Let's remove datamasking

```bash
docker compose exec kafka-client \
    curl \
        --silent \
        --request DELETE "gateway:8888/tenant/tom/feature/data-masking/apiKeys/FETCH/direction/RESPONSE" \
        --user "superUser:superUser"
```
"SUCCESS"
Let's verify the 'road' field is no more masked

```bash
docker compose exec kafka-client \
  kafka-console-consumer \
    --bootstrap-server gateway:6969 \
    --consumer.config /clientConfig/tom.properties \
    --topic roads \
    --from-beginning \
    --max-messages 1 | jq .
{
  "town": "Paris",
  "road": "Avenue des champs Elysées"
}
```
Processed a total of 1 messages

'road' field is no more masked

Data masking is the first step before encryption

So let's encrypt instead of masking. It is safer.

```bash
docker compose exec kafka-client \
    curl \
        --silent \
        --request POST "gateway:8888/tenant/tom/feature/encryption" \
        --user "superUser:superUser" \
        --header 'Content-Type: application/json' \
        --data-raw '{
            "config": {
                "topic": "roads",
                "fields": [ {
                    "fieldName": "road",
                    "keySecretId": "secret-key-road",
                    "algorithm": {
                        "type": "TINK/AES_GCM",
                        "kms": "TINK/KMS_INMEM"
                    }
                }]
            },
            "direction": "REQUEST",
            "apiKeys": "PRODUCE"
        }'
```
"SUCCESS"
Let's send clear data

```bash
echo '{
    "town": "Dubai",
    "road": "Sheik Zayed Road"
}' | jq -c | docker compose exec -T kafka-client \
    kafka-console-producer  \
        --bootstrap-server gateway:6969 \
        --producer.config /clientConfig/tom.properties \
        --topic roads
```
Let's read our data back to make sure it is encrypted

```bash
docker compose exec kafka-client \
  kafka-console-consumer \
    --bootstrap-server gateway:6969 \
    --consumer.config /clientConfig/tom.properties \
    --topic roads \
    --from-beginning \
    --max-messages 2 | jq .
{
  "town": "Paris",
  "road": "Avenue des champs Elysées"
}
{
  "town": "Dubai",
  "road": "AWU77bQVHsZQ8m3xewG5V7r5+qIRW3L8vpuwwDuaSvwDHx/CW5o8EOAfrYsaMuO5foimF58VZf8="
}
```
Processed a total of 2 messages

`road` field is encrypted

Let's now add decryption

```bash
docker compose exec kafka-client \
    curl \
        --silent \
        --request POST "gateway:8888/tenant/tom/feature/decryption" \
        --user "superUser:superUser" \
        --header 'Content-Type: application/json' \
        --data-raw '{
            "config": {
                "topic": "roads",
                "fields": [ {
                    "fieldName": "road",
                    "keySecretId": "secret-key-road",
                    "algorithm": {
                        "type": "TINK/AES_GCM",
                        "kms": "TINK/KMS_INMEM"
                    }
                }]
            },
            "direction": "RESPONSE",
            "apiKeys": "FETCH"
        }'
```
"SUCCESS"
Let's read it back, it should now be decrypted

```bash
docker compose exec kafka-client \
    kafka-console-consumer \
        --bootstrap-server gateway:6969 \
        --consumer.config /clientConfig/tom.properties \
        --topic roads \
        --from-beginning \
        --max-messages 2 | jq .
{
  "town": "Paris",
  "road": "Avenue des champs Elysées"
}
{
  "town": "Dubai",
  "road": "Sheik Zayed Road"
}
```
Processed a total of 2 messages

'road' field is decrypted

Encryption also work with Schema Registry

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/tom.properties \
        --topic customers \
        --replication-factor 2 \
        --partitions 3 \
        --create
```
Created topic customers.

Let's add encryption

```bash
docker compose exec kafka-client curl \
    --silent \
    --request POST "gateway:8888/tenant/tom/feature/encryption" \
    --user "superUser:superUser" \
    --header 'Content-Type: application/json' \
    --data-raw '{
        "config": {
            "topic": "customers",
            "fields": [ {
                "fieldName": "password",
                "keySecretId": "secret-key-password",
                "algorithm": {
                    "type": "TINK/AES_GCM",
                    "kms": "TINK/KMS_INMEM"
                }
            },
            {
                "fieldName": "visa",
                "keySecretId": "secret-key-visaNumber",
                "algorithm": {
                    "type": "TINK/AES_GCM",
                    "kms": "TINK/KMS_INMEM"
                }
            }]
        },
        "direction": "REQUEST",
        "apiKeys": "PRODUCE"
    }'
```
"SUCCESS"
Let's send Json Schema

```bash
echo '{
    "name": "tom",
    "username": "tom@conduktor.io",
    "password": "motorhead",
    "visa": "#abc123",
    "address": "Chancery lane, London"
}' | jq -c | docker compose exec -T schema-registry \
    kafka-json-schema-console-producer  \
        --bootstrap-server gateway:6969 \
        --producer.config /clientConfig/tom.properties \
        --topic customers \
        --property value.schema='{
            "title": "Customer",
            "type": "object",
            "properties": {
                "name": { "type": "string" },
                "username": { "type": "string" },
                "password": { "type": "string" },
                "visa": { "type": "string" },
                "address": { "type": "string" }
            }
        }'
```
Let's make sure it is encrypted

```bash
docker compose exec schema-registry \
    kafka-json-schema-console-consumer \
        --bootstrap-server gateway:6969 \
        --consumer.config /clientConfig/tom.properties \
        --topic customers \
        --from-beginning \
        --max-messages 1 | jq .
{
  "name": "tom",
  "username": "tom@conduktor.io",
  "password": "AU6XrIdBrhtqoSD4fBBuUdL5WWPW97kkjyVPmaufhQBEBwjpF2w+OypdaMixBS5zkw==",
  "visa": "AUg/wJdsPnPSrvYYIQFfwIGq2/DLkvM+b1IngN917h3VSmXCKbbiEd/vYFa9ON0=",
  "address": "Chancery lane, London"
}
```
Processed a total of 1 messages

'user' and 'password' fields are encrypted

Let's add decryption now

```bash
docker compose exec kafka-client \
    curl \
        --silent \
        --request POST "gateway:8888/tenant/tom/feature/decryption" \
        --user "superUser:superUser" \
        --header 'Content-Type: application/json' \
        --data-raw '{
            "config": {
                "topic": "customers",
                "fields": [ {
                    "fieldName": "password",
                    "keySecretId": "secret-key-password",
                    "algorithm": {
                        "type": "TINK/AES_GCM",
                        "kms": "TINK/KMS_INMEM"
                    }
                },
                {
                    "fieldName": "visa",
                    "keySecretId": "secret-key-visaNumber",
                    "algorithm": {
                        "type": "TINK/AES_GCM",
                        "kms": "TINK/KMS_INMEM"
                    }
                }]
            },
            "direction": "RESPONSE",
            "apiKeys": "FETCH"
        }'
```
"SUCCESS"
Let's make sure it is decrypted

```bash
docker compose exec schema-registry \
    kafka-json-schema-console-consumer \
        --bootstrap-server gateway:6969 \
        --consumer.config /clientConfig/tom.properties \
        --topic customers \
        --from-beginning \
        --max-messages 1 | jq .
{
  "name": "tom",
  "username": "tom@conduktor.io",
  "password": "motorhead",
  "visa": "#abc123",
  "address": "Chancery lane, London"
}
```
Processed a total of 1 messages

'user' and 'password' fields are decrypted

Kafka is notoriously bad with large messages

Let's create a topic for large messages, let's fix that too

```bash
docker compose exec kafka-client \
  kafka-topics \
    --bootstrap-server gateway:6969 \
    --command-config /clientConfig/tom.properties \
    --topic large_message \
    --replication-factor 2 \
    --partitions 1 \
    --create
```
WARNING: Due to limitations in metric names, topics with a period ('.') or underscore ('_') could collide. To avoid issues it is best to use either, but not both.
Created topic large_message.

Let's create a fake S3 with minio

```bash
docker compose exec aws \
    mkdir -p /root/.aws/
```
```bash
minio_credentials=$(mktemp)
mktemp
echo """
[minio]
aws_access_key_id = minio
aws_secret_access_key = minio123
""" > $minio_credentials
```
```bash
docker compose cp \
    $minio_credentials aws:/root/.aws/credentials
```
[+] Copying 1/0
 ✔ gateway-story-aws-1 copy /var/folders/57/rwdtg04n3fn1dmrg8r5b_q500000gn/T/tmp.VHaFKTzS to gateway-story-aws-1:/root/.aws/credentials Copied0.0s

Creating a S3 bucket

```bash
docker compose exec aws \
    aws \
        --profile minio \
        --endpoint-url=http://minio:9000 \
        --region eu-south-1 \
        s3api create-bucket \
            --bucket bucket
{
    "Location": "/bucket"
}
```
Let's ask gateway to offload large messages to S3

```bash
docker compose exec kafka-client \
    curl \
        --silent \
        --request POST "gateway:8888/tenant/tom/feature/s3-reference" \
        --user "superUser:superUser" \
        --header 'Content-Type: application/json' \
        --data-raw '{
            "config": {
                "topic": "large_message",
                "s3Config": {
                    "accessKey": "minio",
                    "secretKey": "minio123",
                    "bucketName": "bucket",
                    "region": "eu-south-1",
                    "uri": "http://minio:9000",
                    "localDiskDirectory": "/tmp"
                }
            },
            "direction": "REQUEST",
            "apiKeys": "PRODUCE"
        }'
```
"SUCCESS"
Let's create a large message

```bash
docker compose exec kafka-client \
    bash -c "openssl rand -hex $((20*1024*1024/2)) > large_message.bin"
```
```bash
docker compose exec kafka-client \
    bash -c "cat large_message.bin | wc -c  | numfmt --to=iec-i"
```
21Mi

Let's send this large message

```bash
docker compose exec kafka-client \
    kafka-producer-perf-test \
        --topic large_message \
        --throughput -1 \
        --num-records 1 \
        --producer.config /clientConfig/tom.properties \
        --payload-file large_message.bin \
        --producer-props \
            acks=all \
            bootstrap.servers=gateway:6969 \
            max.request.size=$((25*1024*1024)) \
            buffer.memory=$((25*1024*1024))
```
Reading payloads from: /home/appuser/large_message.bin
Number of messages read: 1
1 records sent, 0.576701 records/sec (11.53 MB/sec), 1727.00 ms avg latency, 1727.00 ms max latency, 1727 ms 50th, 1727 ms 95th, 1727 ms 99th, 1727 ms 99.9th.

Let's read the message back

```bash
docker compose exec kafka-client \
    kafka-console-consumer  \
        --bootstrap-server gateway:6969 \
        --consumer.config /clientConfig/tom.properties \
        --topic large_message \
        --from-beginning \
        --max-messages 1
```
large_message/fcf37fcb-4c82-4bfa-b989-1873a1c290ef
Processed a total of 1 messages

There's only a reference, your Kafka is safe, fast and cheap!

Let's look in S3

```bash
docker compose exec aws \
    aws \
        --profile minio \
        --endpoint-url=http://minio:9000 \
        --region eu-south-1 \
        s3 \
            ls s3://bucket --recursive --human-readable
```
2023-06-02 20:00:00   20.0 MiB large_message/fcf37fcb-4c82-4bfa-b989-1873a1c290ef

The message is now in S3 instead of Kafka

Let's ask Gateway to rehydrate from S3

```bash
docker compose exec kafka-client \
    curl \
        --silent \
        --request POST "gateway:8888/tenant/tom/feature/s3-reference" \
        --user "superUser:superUser" \
        --header 'Content-Type: application/json' \
        --data-raw '{
            "config": {
                "topic": "large_message",
                "s3Config": {
                    "accessKey": "minio",
                    "secretKey": "minio123",
                    "bucketName": "bucket",
                    "region": "eu-south-1",
                    "uri": "http://minio:9000",
                    "localDiskDirectory": "/tmp"
                }
            },
            "direction": "RESPONSE",
            "apiKeys": "FETCH"
        }'
```
"SUCCESS"
Let's make sure we can read the large message back

```bash
docker compose exec kafka-client \
    kafka-console-consumer  \
        --bootstrap-server gateway:6969 \
        --consumer.config /clientConfig/tom.properties \
        --topic large_message \
        --from-beginning \
        --max-messages 1 \
            | wc -c | numfmt --to=iec-i
```
Processed a total of 1 messages
     21Mi

Perfect, I have my large message back

Let's remove Kafka's partitions limitations

Let's count the physical number of partitions in our target cluster

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server kafka1:9092 \
        --describe \
            | awk 'BEGIN{ sum=0} {sum+=$6} END{print "Total partitions: " sum}'
```
Total partitions: 174

Let's remove our topic-create policy first

```bash
docker compose exec kafka-client curl \
    --silent \
    --request DELETE "gateway:8888/tenant/tom/feature/guard-create-topics/apiKeys/CREATE_TOPICS/direction/REQUEST" \
    --user "superUser:superUser"
```
"SUCCESS"
Let's concentrate all topics starting with unlimited on a single physical topic

```bash
docker compose exec kafka-client \
    curl \
        --silent \
        --request POST "gateway:8888/topicMappings/tom/unlimited.*" \
        --user "superUser:superUser" \
        --header 'Content-Type: application/json' \
        --data-raw '{
                        "topicName": "concentrated",
                        "isVirtual": true
                    }'
```
"SUCCESS"
Creating a topic with 2000 virtual partitions

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/tom.properties \
        --topic unlimited_2000 \
        --replication-factor 2 \
        --partitions 2000 \
        --create
```
WARNING: Due to limitations in metric names, topics with a period ('.') or underscore ('_') could collide. To avoid issues it is best to use either, but not both.
Created topic unlimited_2000.

```bash
echo '"message in unlimited_2000"' \
    | docker compose exec -T kafka-client \
        kafka-console-producer  \
            --bootstrap-server gateway:6969 \
            --producer.config /clientConfig/tom.properties \
            --topic unlimited_2000
```
```bash
docker compose exec kafka-client \
    kafka-console-consumer  \
        --bootstrap-server gateway:6969 \
        --consumer.config /clientConfig/tom.properties \
        --topic unlimited_2000 \
        --from-beginning \
        --max-messages 1 | jq .
```
"message in unlimited_2000"
Processed a total of 1 messages

Creating a topic with 50000 virtual partitions

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/tom.properties \
        --topic unlimited_50000 \
        --replication-factor 2 \
        --partitions 50000 \
        --create
```
WARNING: Due to limitations in metric names, topics with a period ('.') or underscore ('_') could collide. To avoid issues it is best to use either, but not both.
Created topic unlimited_50000.

```bash
echo '"message in unlimited_50000"' \
    | docker compose exec -T kafka-client \
        kafka-console-producer  \
            --bootstrap-server gateway:6969 \
            --producer.config /clientConfig/tom.properties \
            --topic unlimited_50000
```
```bash
docker compose exec kafka-client \
    kafka-console-consumer  \
        --bootstrap-server gateway:6969 \
        --consumer.config /clientConfig/tom.properties \
        --topic unlimited_50000 \
        --from-beginning \
        --max-messages 1 | jq .
```
"message in unlimited_2000"
Processed a total of 1 messages

Let's count again the physical number of partitions

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server kafka1:9092 \
        --describe \
            | awk 'BEGIN{ sum=0} {sum+=$6} END{print "Total partitions: " sum}'
```
Total partitions: 182

While we created 52000 virtual partitions, our physical cluster only added a few partitions

Let's reveal the magic by reading directly the concentrated topic on the physical cluster

```bash
docker compose exec kafka-client \
    kafka-console-consumer \
        --bootstrap-server kafka1:9092 \
        --topic concentrated \
        --property print.headers=true \
        --property print.partition=true \
        --from-beginning \
        --max-messages 2
```
Partition:2     PDK_originalPartition:32816,PDK_originalTopic:unlimited_50000   "message in unlimited_50000"
Partition:0     PDK_originalPartition:171,PDK_originalTopic:unlimited_2000      "message in unlimited_2000"
Processed a total of 2 messages

Ok, but what's the cost in latency and throughput?

Let's look at latency without Gateway first

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server kafka1:9092 \
        --topic latency \
        --replication-factor 2 \
        --partitions 1 \
        --create
```
Created topic latency.

```bash
docker compose exec kafka-client \
    kafka-run-class kafka.tools.EndToEndLatency \
        kafka1:9092 latency 10000 all 255
```
0       36.278834
1000    1.240625
2000    0.960375
3000    0.84475
4000    0.8845
5000    0.841667
6000    0.898792
7000    0.672584
8000    0.691042
9000    0.686625
Avg latency: 1.0493 ms

Percentiles: 50th = 0, 99th = 3, 99.9th = 7

Let's look at latency with Gateway

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/tom.properties \
        --topic latency \
        --replication-factor 2 \
        --partitions 1 \
        --create
```
Created topic latency.

```bash
docker compose exec kafka-client \
    kafka-run-class kafka.tools.EndToEndLatency \
        gateway:6969 latency 10000 all 255 /clientConfig/tom.properties
```
0       90.005375
1000    2.862
2000    3.8332089999999996
3000    2.184
4000    2.199333
5000    2.053541
6000    2.200625
7000    1.801
8000    1.550375
9000    1.535166
Avg latency: 2.6859 ms

Percentiles: 50th = 2, 99th = 9, 99.9th = 29

Gateway is adding a mere 1ms to the overall latency

Let's look at throughput without Gateway

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server kafka1:9092 \
        --topic perf-test \
        --replication-factor 2 \
        --partitions 1 \
        --create
```
Created topic perf-test.

```bash
docker compose exec kafka-client \
    kafka-producer-perf-test \
        --topic perf-test \
        --throughput -1 \
        --num-records 2500000 \
        --record-size 255 \
        --producer-props bootstrap.servers=kafka1:9092
```
607112 records sent, 121422.4 records/sec (29.53 MB/sec), 781.6 ms avg latency, 1050.0 ms max latency.
718153 records sent, 143630.6 records/sec (34.93 MB/sec), 871.4 ms avg latency, 926.0 ms max latency.
733891 records sent, 146778.2 records/sec (35.69 MB/sec), 847.5 ms avg latency, 887.0 ms max latency.
2500000 records sent, 138611.665558 records/sec (33.71 MB/sec), 843.26 ms avg latency, 1050.00 ms max latency, 866 ms 50th, 987 ms 95th, 1041 ms 99th, 1048 ms 99.9th.

Let's look at throughput with Gateway

```bash
docker compose exec kafka-client \
    kafka-topics \
        --bootstrap-server gateway:6969 \
        --command-config /clientConfig/tom.properties \
        --topic perf-test \
        --replication-factor 2 \
        --partitions 1 \
        --create
```
Created topic perf-test.

```bash
docker compose exec kafka-client \
    kafka-producer-perf-test \
        --topic perf-test \
        --throughput -1 \
        --num-records 2500000 \
        --record-size 255 \
        --producer-props bootstrap.servers=gateway:6969 \
        --producer.config /clientConfig/tom.properties
```
599265 records sent, 119829.0 records/sec (29.14 MB/sec), 790.8 ms avg latency, 1012.0 ms max latency.
689849 records sent, 137969.8 records/sec (33.55 MB/sec), 896.2 ms avg latency, 958.0 ms max latency.
692289 records sent, 138457.8 records/sec (33.67 MB/sec), 908.6 ms avg latency, 1001.0 ms max latency.
2500000 records sent, 133290.680316 records/sec (32.41 MB/sec), 876.53 ms avg latency, 1012.00 ms max latency, 901 ms 50th, 970 ms 95th, 996 ms 99th, 1009 ms 99.9th.

Gateway is almost on par regarding throughput

But can I monitor it?

```bash
docker compose exec kafka-client \
    curl \
        --silent \
        --request GET "gateway:8888/metrics" \
        --user "superUser:superUser"
```
`
#HELP jvm_buffer_memory_used_bytes An estimate of the memory that the Java virtual machine is using for this buffer pool
#TYPE jvm_buffer_memory_used_bytes gauge
jvm_buffer_memory_used_bytes{id="mapped - 'non-volatile memory'",} 0.0
jvm_buffer_memory_used_bytes{id="mapped",} 0.0
jvm_buffer_memory_used_bytes{id="direct",} 3.9149942E7
#HELP jvm_memory_committed_bytes The amount of memory in bytes that is committed for the Java virtual machine to use
#TYPE jvm_memory_committed_bytes gauge
jvm_memory_committed_bytes{area="nonheap",id="CodeHeap 'profiled nmethods'",} 1.8874368E7
jvm_memory_committed_bytes{area="heap",id="G1 Survivor Space",} 1048576.0
jvm_memory_committed_bytes{area="heap",id="G1 Old Gen",} 1.00663296E8
jvm_memory_committed_bytes{area="nonheap",id="Metaspace",} 8.2378752E7
jvm_memory_committed_bytes{area="nonheap",id="CodeHeap 'non-nmethods'",} 2555904.0
jvm_memory_committed_bytes{area="heap",id="G1 Eden Space",} 1.11149056E8
jvm_memory_committed_bytes{area="nonheap",id="Compressed Class Space",} 1.0551296E7
jvm_memory_committed_bytes{area="nonheap",id="CodeHeap 'non-profiled nmethods'",} 9699328.0
#HELP proxy_latency_request_response_seconds
#TYPE proxy_latency_request_response_seconds histogram
proxy_latency_request_response_seconds{quantile="0.3",} 9.8304E-4
proxy_latency_request_response_seconds{quantile="0.5",} 0.001998848
proxy_latency_request_response_seconds{quantile="0.95",} 0.002981888
proxy_latency_request_response_seconds{quantile="0.99",} 0.005210112
proxy_latency_request_response_seconds_bucket{le="0.001",} 31207.0
proxy_latency_request_response_seconds_bucket{le="0.001048576",} 31207.0
proxy_latency_request_response_seconds_bucket{le="0.001398101",} 31207.0
proxy_latency_request_response_seconds_bucket{le="+Inf",} 61326.0
proxy_latency_request_response_seconds_count 61326.0
proxy_latency_request_response_seconds_sum 98.932
#HELP proxy_latency_request_response_seconds_max
#TYPE proxy_latency_request_response_seconds_max gauge
proxy_latency_request_response_seconds_max 0.732
#HELP jvm_gc_live_data_size_bytes Size of long-lived heap memory pool after reclamation
#TYPE jvm_gc_live_data_size_bytes gauge
jvm_gc_live_data_size_bytes 8.1592832E7
#HELP vertx_http_server_requests_total Number of processed requests
#TYPE vertx_http_server_requests_total counter
vertx_http_server_requests_total{code="200",method="POST",route="/tenant/:tenant/feature/:feature",} 11.0
vertx_http_server_requests_total{code="200",method="POST",route="/topicMappings/:tenant/:regex",} 2.0
vertx_http_server_requests_total{code="200",method="DELETE",route="/tenant/:tenant/feature/:feature/apiKeys/:apiKeys/direction/:direction",} 3.0
vertx_http_server_requests_total{code="200",method="POST",route="/admin/>/auth/v1beta1/tenants/{tenantId}",} 2.0
#HELP jvm_classes_loaded_classes The number of classes that are currently loaded in the Java virtual machine
#TYPE jvm_classes_loaded_classes gauge
jvm_classes_loaded_classes 15684.0
#HELP vertx_pool_completed_total Number of elements done with the resource
#TYPE vertx_pool_completed_total counter
vertx_pool_completed_total{pool_type="worker",} 1.0
#HELP process_cpu_usage The "recent cpu usage" for the Java Virtual Machine process
#TYPE process_cpu_usage gauge
process_cpu_usage 0.11548622468235
#HELP vertx_pool_queue_time_seconds_max Time spent in queue before being processed
#TYPE vertx_pool_queue_time_seconds_max gauge
vertx_pool_queue_time_seconds_max{pool_type="worker",} 0.0
#HELP vertx_pool_queue_time_seconds Time spent in queue before being processed
#TYPE vertx_pool_queue_time_seconds summary
vertx_pool_queue_time_seconds_count{pool_type="worker",} 1.0
vertx_pool_queue_time_seconds_sum{pool_type="worker",} 0.002171292
#HELP system_cpu_usage The "recent cpu usage" of the system the application is running in
#TYPE system_cpu_usage gauge
system_cpu_usage 0.3341541705125904
#HELP proxy_upstreamio_nodes
#TYPE proxy_upstreamio_nodes gauge
proxy_upstreamio_nodes{name="upStreamResource-3-1",threadId="39",} 2.0
proxy_upstreamio_nodes{name="upStreamResource-3-2",threadId="40",} 2.0
proxy_upstreamio_nodes{name="upStreamResource-3-3",threadId="41",} 2.0
proxy_upstreamio_nodes{name="upStreamResource-3-4",threadId="42",} 2.0
#HELP jvm_memory_max_bytes The maximum amount of memory in bytes that can be used for memory management
#TYPE jvm_memory_max_bytes gauge
jvm_memory_max_bytes{area="nonheap",id="CodeHeap 'profiled nmethods'",} 1.22908672E8
jvm_memory_max_bytes{area="heap",id="G1 Survivor Space",} -1.0
jvm_memory_max_bytes{area="heap",id="G1 Old Gen",} 2.059403264E9
jvm_memory_max_bytes{area="nonheap",id="Metaspace",} -1.0
jvm_memory_max_bytes{area="nonheap",id="CodeHeap 'non-nmethods'",} 5836800.0
jvm_memory_max_bytes{area="heap",id="G1 Eden Space",} -1.0
jvm_memory_max_bytes{area="nonheap",id="Compressed Class Space",} 1.073741824E9
jvm_memory_max_bytes{area="nonheap",id="CodeHeap 'non-profiled nmethods'",} 1.22
#HELP jvm_threads_live_threads The current number of live threads including both daemon and non-daemon threads
#TYPE jvm_threads_live_threads gauge
jvm_threads_live_threads 53.0
.
.
.
jvm_buffer_total_capacity_bytes{id="mapped - 'non-volatile memory'",} 0.0
jvm_buffer_total_capacity_bytes{id="mapped",} 0.0
jvm_buffer_total_capacity_bytes{id="direct",} 3.9158044E7
`
Yes, you have a prometheus endpoint

Does Gateway scale?

Yes, because it is stateless

Ok, but is it resilient?

Yes! Resiliency is backed diretly in the Apache Kafka protocol!

Conduktor Gateway is ready to serve you

Conduktor Gateway helps you reinvent your Kafka experience
