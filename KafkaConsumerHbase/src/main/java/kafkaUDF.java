import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.RetriesExhaustedWithDetailsException;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.io.IOException;
import java.io.InterruptedIOException;

public class kafkaUDF extends UDF{
    public static Configuration configuration;
    public static String outputtable="test_kafka";

    public static void evaluate(String record) throws IOException{
        confInit();
        configuration.set(TableOutputFormat.OUTPUT_TABLE,outputtable );

        //HBaseAdmin admin=new HBaseAdmin(configuration);
        HTable hTable=new HTable(configuration, Bytes.toBytes(outputtable));

        String sep = ";";
        String msgArray[] = record.split(sep);

        Put put = new Put(Bytes.toBytes(msgArray[0].split(",")[0]));
        //System.out.println(msgArray[0].split(",")[0]+":_key");

        for(int i=1;i<=2;i++){
            put.add(Bytes.toBytes("f"), Bytes.toBytes("q"+i), Bytes.toBytes(msgArray[i].split(",")[1]));
        }
        try {
            hTable.put(put);
        } catch (InterruptedIOException e) {
            e.printStackTrace();
        } catch (RetriesExhaustedWithDetailsException e) {
            e.printStackTrace();
        }

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

    public static void main(String[] args){
        try {
            evaluate("key4,value4;key5,value5;key6,value6");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
