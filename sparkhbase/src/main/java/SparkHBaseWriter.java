import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SparkHBaseWriter implements Serializable {

    private String outputTable;

    public SparkHBaseWriter(String outputTable) {
        this.outputTable = outputTable;
    }

    public void sparkProcess(JavaSparkContext jsc) throws IOException {
        Configuration conf = HBaseConfiguration.create();
        //设置zooKeeper集群地址，也可以通过将hbase-site.xml导入classpath，也可以在程序里手动设置
        conf.set("hbase.zookeeper.quorum", "172.16.140.81,172.140.82,172.16.140.83");
        //设置zookeeper连接端口，默认2181
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        conf.set("zookeeper.znode.parent", "/hyperbase1");
        conf.set(TableOutputFormat.OUTPUT_TABLE, outputTable);

        // 如果表不存在则创建表，并为HBase表添加列族f1、f2
        HBaseAdmin admin = new HBaseAdmin(conf);
        if (!admin.isTableAvailable(outputTable)) {
            HTableDescriptor tableDesc = new HTableDescriptor(TableName.valueOf(outputTable));
            admin.createTable(tableDesc);
            HColumnDescriptor column1 = new HColumnDescriptor("f1");
            HColumnDescriptor column2 = new HColumnDescriptor("f2");
            admin.addColumn(outputTable, column1);
            admin.addColumn(outputTable, column2);
        }

        // 配置写入HBase JobConf，指定写入HBase的OutputFormat
        JobConf jobConf = new JobConf(conf);
        jobConf.setOutputKeyClass(ImmutableBytesWritable.class);
        jobConf.setOutputValueClass(Result.class);
        Job job = new Job(jobConf);
        job.setOutputFormatClass(TableOutputFormat.class);

        // 构造测试数据，测试创建HBase表并使用Spark写入HBase
        List<String> dummyData = new ArrayList<String>();
        dummyData.add("1,jack,15");
        dummyData.add("2,Lily,16");
        dummyData.add("3,mike,16");
        JavaRDD<String> outputData = jsc.parallelize(dummyData, 2);


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

    public static void kerberosLogin(String user, String keytabFile) {
        try {
            UserGroupInformation.loginUserFromKeytab(user, keytabFile);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String[] args) throws IOException {
        // 配置安全验证，keytab文件需要放到代码执行所在的机器，容器中执行则将keytab拷贝到
        // 对应容器，并配置容器内目录
        kerberosLogin("hbase/tdh-81@TDH", "D:\\hyperbase.keytab");

        // 创建sparkcontext，可根据需求指定不同的master，如yarn-client
        SparkConf conf = new SparkConf()
                .setAppName("Spark HBase Writer")
                .setMaster("local[2]");
        JavaSparkContext jsc = new JavaSparkContext(conf);

        new SparkHBaseWriter("test_table").sparkProcess(jsc);
    }
}
