package io.gib.demos.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemo {
    private static final Logger log = LoggerFactory.getLogger(ProducerDemo.class);
    public static void main(String[] args) {
        log.info("I am a Kafka producer");

        // create Producer Properties
        var props = new Properties();

        // connection to Conduktor & Localhost
        props.put("bootstrap.servers", "localhost:9092");
        //props.put("bootstrap.servers", "creative-dove-13738-us1-kafka.upstash.io:9092");
        //props.put("sasl.mechanism", "SCRAM-SHA-256");
        // props.put("security.protocol", "SASL_SSL");
        //props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"Y3JlYXRpdmUtZG92ZS0xMzczOCQ27m3t0uKybSUVYz1Ghp8QdUARiadQvV_7HPE\" password=\"YmRjMWRjNzItYWU0NS00N2U2LTgxYjEtZmFkMDM5Y2FjNDlm\";");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // create producer object from KafkaProducer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // create the first record to a topic :)
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("demo_java", "my value");

        producer.send(producerRecord);
        producer.flush();
        producer.close();

    }

}
