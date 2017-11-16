package io.transwarp.kafkaProducer;

public class SampleCode {
    // 主函数
    public static void main(String[] args) {
        //4. ./kafka-create-topic.sh --zookeeper 172.16.2.93:2181 --partition 3 --topic tmp_kafka_1st --broker 172.16.2.93 172.16.2.94 172.16.2.95
        //5.0  ./kafka-topics.sh --create --zookeeper 172.16.2.61:2181,172.16.2.62:2181,172.16.2.64:2181  --replication-factor 3 --partition 1 --topic kafka1st

        kafkaProducer kafkaProducer = new kafkaProducer();
        kafkaProducer.go();
    }
}
