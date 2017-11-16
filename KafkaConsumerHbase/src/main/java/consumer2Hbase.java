import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class consumer2Hbase {

    public static Configuration configuration;


    public static void main(String[] args){
        confInit();
        String bootstrap = "172.16.140.204:9092,172.16.140.205:9092,172.16.140.206:9092";
        String topic = "kafkatest";
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
                    WriteHbase(record.value(),"test_kafka");

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }
    }

    private static void WriteHbase(String value,String outputtable) throws IOException {
        configuration.set(TableOutputFormat.OUTPUT_TABLE,outputtable );

        //HBaseAdmin admin=new HBaseAdmin(configuration);
        HTable hTable=new HTable(configuration,Bytes.toBytes(outputtable));

        String sep = ";";
        String msgArray[] = value.split(sep);

        Put put = new Put(Bytes.toBytes(msgArray[0].split(",")[0]));
        System.out.println(msgArray[0].split(",")[0]+":_key");

        for(int i=1;i<=2;i++){
            put.add(Bytes.toBytes("f"), Bytes.toBytes("q"+i), Bytes.toBytes(msgArray[i].split(",")[1]));
        }
        hTable.put(put);

    }

    public static void confInit(){
        configuration = HBaseConfiguration.create();
        //设置zooKeeper集群地址，也可以通过将hbase-site.xml导入classpath，也可以在程序里手动设置
        configuration.set("hbase.zookeeper.quorum", "172.16.140.204,172.140.205,172.16.140.206");
        //设置zookeeper连接端口，默认2181
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        configuration.set("zookeeper.znode.parent", "/hyperbase1");

        /*configuration.addResource(consumer2Hbase.class.getClassLoader().getResource("hbase-site.xml"));
        configuration.addResource(consumer2Hbase.class.getClassLoader().getResource("hdfs-site.xml"));
        configuration.addResource(consumer2Hbase.class.getClassLoader().getResource("core-site.xml"));*/
    }

    //造的测试数据
    /*create table test_kafka(col1 string,col2 string,col3 string) stored as hyperdrive;
    hbase.columns.mapping'=':key,f:q1,f:q2
    * key1,value1;key2,value2;key3,value3
    * key4,value4;key5,value5;key6,value6
    *
    * put 'test_kafka','key7','f:q1','value7'
    * put 'test_kafka','key7','f:q2','value8'
    * */


}
