package io.gib.demos.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithKeys {
    private static final Logger log = LoggerFactory.getLogger(ProducerDemoWithKeys.class);
    public static void main(String[] args) {
        log.info("I am a Kafka producer");

        var props = new Properties();
        props.put("bootstrap.servers", "127.0.0.1:9092");
        //props.put("bootstrap.servers", "creative-dove-13738-us1-kafka.upstash.io:9092");
        //props.put("sasl.mechanism", "SCRAM-SHA-256");
        //props.put("security.protocol", "SASL_SSL");
        //props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"Y3JlYXRpdmUtZG92ZS0xMzczOCQ27m3t0uKybSUVYz1Ghp8QdUARiadQvV_7HPE\" password=\"YmRjMWRjNzItYWU0NS00N2U2LTgxYjEtZmFkMDM5Y2FjNDlm\";");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        for (int j=0; j<2; j++){
            for (int i=0; i<10; i++ ) {

                // create a producer record

                String topic = "demo_java";
                String value = "hello world " + Integer.toString(i);
                String key = "id_" + Integer.toString(i);

                ProducerRecord<String, String> producerRecord =
                        new ProducerRecord<>(topic, key, value);

                // send data - asynchronous
                producer.send(producerRecord, new Callback() {
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        // executes every time a record is successfully sent or an exception is thrown
                        if (e == null) {
                            // the record was successfully sent
                            log.info("Key:" + key + " | Partition: " + recordMetadata.partition());
                        } else {
                            log.error("Error while producing", e);
                        }
                    }
                });
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



        producer.flush();
        producer.close();

    }

}
