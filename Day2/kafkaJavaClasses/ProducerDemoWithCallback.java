package io.gib.demos.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithCallback {
    private static final Logger log = LoggerFactory.getLogger(ProducerDemoWithCallback.class);
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
        props.put("batch.size", "400"); // set batch size to a low number
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        for (int j=0; j<10; j++){

            for (int i=0; i<30; i++){
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>("demo_java", "my value " + i);

                producer.send(producerRecord, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception e) {
                        // executes everytime a record successfully sent or an exception is thrown
                        if (e == null){
                            // successfully event is sent
                            log.info("Received new metadata \n" +
                                    "Topic: " + metadata.topic() + "\n" +
                                    "Partition: " + metadata.partition() + "\n" +
                                    "Offset: " + metadata.offset() + "\n" +
                                    "Timestamp: " + metadata.timestamp() );
                        } else {
                            log.error("Error while producing", e);
                        }
                    }
                });

            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        producer.flush();
        producer.close();

    }

}
