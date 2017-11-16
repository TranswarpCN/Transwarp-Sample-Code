import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
import org.apache.hadoop.hbase.util.Base64;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import scala.Tuple3;

import java.io.IOException;
import java.io.Serializable;

public class SparkHBaseReader implements Serializable {

    private long minStamp = 0;
    private long maxStamp = 0;

    public SparkHBaseReader() {
        this.maxStamp = System.currentTimeMillis();
        this.minStamp = maxStamp - 10 * 60 * 1000;
    }

    public SparkHBaseReader(long maxStamp) {
        this.maxStamp = maxStamp;
        this.minStamp = maxStamp - 10 * 60 * 1000;
    }

    public SparkHBaseReader(long maxStamp, long duration) {
        this.maxStamp = maxStamp;
        this.minStamp = maxStamp - duration * 60 * 1000;
    }

    public void sparkProcess(JavaSparkContext jsc) throws IOException {
        // 配置conf对象，用于指定读取HBase表的相关信息
        Configuration conf = HBaseConfiguration.create();

        // 如果hbase-site.xml已经配置到程序执行的环境变量中，那么则不需要手动指定下面的几个参数
        // 以下演示未添加配置文件时访问hbase需要的参数，主要指定zk的连接ip、端口及hbase的根节点
        conf.set("hbase.zookeeper.quorum", "172.16.140.81,172.140.82,172.16.140.83");
        // 设置zookeeper连接端口，默认2181
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        conf.set("zookeeper.znode.parent", "/hyperbase1");

        // 指定读取HBase表的表名
        conf.set(TableInputFormat.INPUT_TABLE, "write_test3");

        // 配置scan对象，用于读取指定范围的数据，scan对象的更多用法可以参考HBase API，这里只
        // 针对本次场景演示了部分功能
        Scan scan = new Scan();
        // 通过addFamily和addColumn方法指定想要读取的列族和列，如果不指定则默认读取全部数据
        scan.addFamily(Bytes.toBytes("f1"));
        scan.addFamily(Bytes.toBytes("f2"));
        scan.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("name"));
        // 指定读取数据的timestamp范围，Spark会读取入库时间戳在这个时间段内的HBase表数据
        // 如本次场景中，每10分钟读取一次数据，则可以吧maxStamp设为程序启动时时间，minStamp
        // 设置为10分钟前时间。推荐使用者对时间范围设置一套合理的规则，以免产生时间间隙少取部分数据
        scan.setTimeRange(minStamp, maxStamp);
        // 将Scan对象转换为字符串形式，并传递给TableInputFormat
        String scanStr = convertScanToString(scan);
        conf.set(TableInputFormat.SCAN, scanStr);

        // 将配置好的TableInputFormat指定给spark，用于读取hbase数据并生成RDD，此步骤开始执行后
        // 则进入常规的Spark处理逻辑。需要注意的是，最初生成的RDD为KV类型，Key通常不需要处理，Value为
        // HBase的Result类型，可以通过RDD.values()方法直接获取全部value数据。这样RDD中的每一条数据
        // 都会对应HBase中的一条记录，例子中只调用了result对象的toString方法，实际使用时需要
        JavaRDD<Result> inputRdd = jsc.newAPIHadoopRDD(conf, TableInputFormat.class,
                ImmutableBytesWritable.class, Result.class)
                .values();

        // 以下步骤演示了从Result类型中提取字段内容并将结果数据作为元组返回，需注意取数据时的类型应
        // 跟入库时类型保持一致
        JavaRDD<String> resultRdd = inputRdd.map(new Function<Result, String>() {
            @Override
            public String call(Result result) throws Exception {

                Tuple3 data = new Tuple3<>(result.getRow(),
                        Bytes.toString(result.getValue(Bytes.toBytes("f1"), Bytes.toBytes("name"))),
                        Bytes.toInt(result.getValue(Bytes.toBytes("f2"), Bytes.toBytes("age"))));
                return data.toString();
            }
        });
        // print结果rdd第一条记录查看结果
        System.out.println(resultRdd.first());
    }

    private String convertScanToString(Scan scan) throws IOException {
        ClientProtos.Scan proto = ProtobufUtil.toScan(scan);
        return Base64.encodeBytes(proto.toByteArray());
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
                .setAppName("Spark HBase Demo")
                .setMaster("local[2]");
        JavaSparkContext jsc = new JavaSparkContext(conf);

        // 指定读取数据的时间间隔并启动处理任务
        SparkHBaseReader reader = new SparkHBaseReader(1507995559760L, 10);
        reader.sparkProcess(jsc);

    }
}
