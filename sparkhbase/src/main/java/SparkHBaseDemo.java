import javassist.expr.Cast;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.security.UserGroupInformation;



public class SparkHBaseDemo {

    public static JavaRDD<String> readFromHBase(JavaSparkContext jsc, String inputTable) {
        try {
            UserGroupInformation.loginUserFromKeytab(
                    "hbase/tdh-81@TDH", "D:\\hyperbase.keytab");
        }catch (IOException e){
            e.printStackTrace();
        }
        Configuration conf = HBaseConfiguration.create();
        //设置zooKeeper集群地址，也可以通过将hbase-site.xml导入classpath，也可以在程序里手动设置
        /*conf.set("hbase.zookeeper.quorum", "172.16.140.81,172.140.82,172.16.140.83");
        //设置zookeeper连接端口，默认2181
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        conf.set("zookeeper.znode.parent", "/hyperbase1");*/
        conf.set(TableInputFormat.INPUT_TABLE, inputTable);
        conf.addResource(SparkHBaseDemo.class.getClassLoader().getResource("hbase-site.xml"));
        conf.addResource(SparkHBaseDemo.class.getClassLoader().getResource("hdfs-site.xml"));
        conf.addResource(SparkHBaseDemo.class.getClassLoader().getResource("core-site.xml"));

        UserGroupInformation.setConfiguration(conf);


        return jsc.newAPIHadoopRDD(conf, TableInputFormat.class,
                ImmutableBytesWritable.class, Result.class)
                .values().map(new Function<Result, String>() {
                    @Override
                    public String call(Result result) throws Exception {
                        return result.toString();
                    }
                });
    }

    public static void writeToHBase(String outputTable, JavaRDD<String> outputData) throws IOException {
        UserGroupInformation.loginUserFromKeytab(
                "hbase/tdh-81@TDH","D:\\hyperbase.keytab");

        Configuration conf = HBaseConfiguration.create();
        /*//设置zooKeeper集群地址，也可以通过将hbase-site.xml导入classpath，也可以在程序里手动设置
        conf.set("hbase.zookeeper.quorum", "172.16.140.81,172.140.82,172.16.140.83");
        //设置zookeeper连接端口，默认2181
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        conf.set("zookeeper.znode.parent", "/hyperbase1");*/

        conf.addResource(SparkHBaseDemo.class.getClassLoader().getResource("hbase-site.xml"));
        conf.addResource(SparkHBaseDemo.class.getClassLoader().getResource("hdfs-site.xml"));
        conf.addResource(SparkHBaseDemo.class.getClassLoader().getResource("core-site.xml"));
        UserGroupInformation.setConfiguration(conf);


        // 如果表不存在则创建表，并为HBase表添加列族f1、f2
        HBaseAdmin admin = new HBaseAdmin(conf);
        if (!admin.isTableAvailable(outputTable)) {
            HTableDescriptor tableDesc = new HTableDescriptor(TableName.valueOf(outputTable));
            HColumnDescriptor column1 = new HColumnDescriptor("f1");
            HColumnDescriptor column2 = new HColumnDescriptor("f2");

            tableDesc.addFamily(column1);
            tableDesc.addFamily(column2);
            admin.createTable(tableDesc);
            //admin.addColumn(outputTable, column1);
            //admin.addColumn(outputTable, column2);

        }

        // 配置写入HBase JobConf，指定写入HBase的OutputFormat
        conf.set(TableOutputFormat.OUTPUT_TABLE, outputTable);
        JobConf jobConf = new JobConf(conf);
        jobConf.setOutputKeyClass(ImmutableBytesWritable.class);
        jobConf.setOutputValueClass(Result.class);
        Job job = new Job(jobConf);
        job.setOutputFormatClass(TableOutputFormat.class);

        // 将原始数据提取有用字段，转换为kv类型的RDD，调用saveAsNewAPIHadoopDataset写入HBase
        outputData.mapToPair(new PairFunction<String, ImmutableBytesWritable, Put>() {
            @Override
            public Tuple2<ImmutableBytesWritable, Put> call(String s) throws Exception {
                String[] arr = s.split(",");
                Put put = new Put(Bytes.toBytes(Integer.parseInt(arr[0])));
                put.add(Bytes.toBytes("f1"), Bytes.toBytes("name"), Bytes.toBytes(arr[1]));
                put.add(Bytes.toBytes("f2"), Bytes.toBytes("age"), Bytes.toBytes(Integer.parseInt(arr[2])));
                return new Tuple2<ImmutableBytesWritable, Put>(new ImmutableBytesWritable(), put);
            }
        }).saveAsNewAPIHadoopDataset(job.getConfiguration());
    }

    public static void main(String[] args) throws IOException {

        //--------------------读数据-------------------
        SparkConf conf = new SparkConf()
                .setAppName("Spark HBase Demo")
                .setMaster("local[2]");
        JavaSparkContext jsc = new JavaSparkContext(conf);

        // 读取指定表数据，并打印第一条数据
        JavaRDD<String> spark_test = readFromHBase(jsc, "write_test3");
        System.out.println(spark_test.first());

        //--------------------写数据-------------------
        // 构造测试数据，测试创建HBase表并使用Spark写入HBase
        /*List<String> dummyData = new ArrayList<String>();
        dummyData.add("1,jack,15");
        dummyData.add("2,Lily,16");
        dummyData.add("3,mike,16");
        JavaRDD<String> dummyRDD = jsc.parallelize(dummyData, 2);
        writeToHBase("write_test3", dummyRDD);*/
    }
}
