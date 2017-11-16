package io.transwarp.kafkaConsumer;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class Consumer {
    public static void main(String[] args) {
        System.out.println("begin consumer");
        connectionKafka();
        System.out.println("finish consumer");
    }

    @SuppressWarnings("resource")
    public static void connectionKafka() {

        Properties props = new kafkaProperties().properties();
        System.out.println(props.getProperty("bootstrap.servers"));
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        /*consumer.subscribe(Arrays.asList(new Constant().TOPIC));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            *//*try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*//*
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(), record.value());
            }
        }*/
    }

}


