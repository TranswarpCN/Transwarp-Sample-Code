package io.transwarp.kafkaConsumer;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;



public class KafkaTest {
    private static FSDataOutputStream  hdfsOutStream;
    private static Configuration conf;
    public static FileSystem fs ;
    private static Path path;



    public static void main(String[] args) {
        String bootstrap = "172.16.140.21:9092,172.16.140.22:9092,172.16.140.23:9092";
        String topic = "kafka1st";
        String group = "mytest003";
        String offset = "earliest";

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrap);
        props.put("group.id", group);
        props.put("enable.auto.commit", "true");  //自动commit
        props.put("zookeeper.session.timeout.ms", "5000");
        props.put("zookeeper.connection.timeout.ms","10000");
        props.put("auto.offset.reset", offset);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.deserializer.encoding", "UTF8");
        props.put("value.deserializer.encoding", "UTF8");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));
        while(true) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : records) {
                try {
                    System.out.println(record.value());
                    WriteFile(getFSDataOutputStream(),record.value());

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }

    }
    public static Configuration HdfsConfigInit(){
        conf = new Configuration();
        conf.addResource(KafkaTest.class.getClassLoader().getResource("core-site.xml"));
        //conf.addResource(hdfs_putanddown.class.getClassLoader().getResource("yarn-site.xml"));
        conf.addResource(KafkaTest.class.getClassLoader().getResource("hdfs-site.xml"));
        conf .set("dfs.client.block.write.replace-datanode-on-failure.policy" ,"NEVER" );
        conf .set("dfs.client.block.write.replace-datanode-on-failure.enable" ,"true" );

        return conf;
    }

    public static FSDataOutputStream getFSDataOutputStream(){
        return hdfsOutStream;
    }

    public static void WriteFile(FSDataOutputStream hdfsOutStream, String message) throws IOException {
        path=new Path(new String("/tmp/kafkatest/kafkamessage1"));
        HdfsConfigInit();

        try {
            fs=path.getFileSystem(conf);
            if(fs.exists(path)){
                hdfsOutStream=fs.append(path);
                hdfsOutStream.write(message.getBytes());
                hdfsOutStream.write("\n".getBytes());
                hdfsOutStream.hflush();
                hdfsOutStream.close();
            }
            else{
                hdfsOutStream=fs.create(path);
                hdfsOutStream.write(message.getBytes());
                hdfsOutStream.write("\n".getBytes());
                hdfsOutStream.hflush();
                hdfsOutStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
