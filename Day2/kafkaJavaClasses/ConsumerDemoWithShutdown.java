package io.gib.demos.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemoWithShutdown {
    private static final Logger log = LoggerFactory.getLogger(ConsumerDemoWithShutdown.class);
    public static void main(String[] args) {
        log.info("I am a Kafka consumer");

        String groupId = "my-java-app";
        String topic = "demo_java";

        // create Producer Properties
        var props = new Properties();

        // connection to Conduktor & Localhost
        props.put("bootstrap.servers", "127.0.0.1:9092");
        //props.put("bootstrap.servers", "creative-dove-13738-us1-kafka.upstash.io:9092");
        //props.put("sasl.mechanism", "SCRAM-SHA-256");
        //props.put("security.protocol", "SASL_SSL");
        //props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"Y3JlYXRpdmUtZG92ZS0xMzczOCQ27m3t0uKybSUVYz1Ghp8QdUARiadQvV_7HPE\" password=\"YmRjMWRjNzItYWU0NS00N2U2LTgxYjEtZmFkMDM5Y2FjNDlm\";");

        // create consumer configs
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("group.id", groupId);
        props.put("auto.offset.reset", "earliest"); // none-earliest-latest

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // get a reference to the main thread
        final Thread mainThread = Thread.currentThread();
        // add the shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run() {
                log.info("Detected a shutdown, exit by calling consumer.wakeup()");
                consumer.wakeup();

                // join the main thread to allow the execution of the code in the main thread
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            // subscribe to a topic
            consumer.subscribe(Arrays.asList(topic));

            // poll for the data
            while (true) {

                //log.info("Polling");

                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record : records) {
                    log.info("Key: " + record.key() + ", Value: " + record.value());
                    log.info("Partition: " + record.partition() + ", Offset: " + record.offset());
                }
            }
        } catch (WakeupException e) {
            log.info("Consumer is starting to shut down");
        } catch (Exception e){
            log.error("unexpected exception in the consumer", e);
        } finally {
            consumer.close(); // close the consumer & commit offset
            log.info("The consumer is gracefully shutdown");
        }
    }

}
