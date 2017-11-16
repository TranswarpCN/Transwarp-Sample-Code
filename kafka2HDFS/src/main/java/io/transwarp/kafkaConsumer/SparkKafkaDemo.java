package io.transwarp.kafkaConsumer;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.hadoop.security.UserGroupInformation;


public class SparkKafkaDemo {

    public static void main(String[] args) throws InterruptedException {

        // 创建SparkConf，根据需求指定Master为local或yarn模式
        SparkConf conf = new SparkConf().setAppName("Spark Kafka Demo").setMaster("local[2]");
        // 创建StreamingContext，同时指定流处理时间间隔，可用以下两种方式指定，以2分钟为例
        //JavaStreamingContext ssc = new JavaStreamingContext(conf, new Duration(120000));
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.minutes(1));

        // 配置Spark连接kafka参数，访问开安全后的kafka，相关参数也在此添加
        Map<String, Object> kafkaParams = new HashMap<>();
        // kafka 0.10版本offset不通过zookeeper保存，此处bootstrap参数直连kafka
        kafkaParams.put("bootstrap.servers", "tdh-81:9092,tdh-82:9092,tdh-83:9092");
        // 根据kafka内存储数据格式指定key和value的解析方法
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        // 指定group id
        kafkaParams.put("group.id", "test3");
        // 若要从kafka topic开头出读取数据，则将此参数置为earliest
        kafkaParams.put("auto.offset.reset", "earliest");


        //安全相关参数
        kafkaParams.put("ssl.key.password","123456");
        kafkaParams.put("sasl.mechanism","GSSAPI");
        kafkaParams.put("security.protocol","SASL_PLAINTEXT");
        kafkaParams.put("sasl.kerberos.service.name","kafka");
        String sasa = "com.sun.security.auth.module.Krb5LoginModule required  " +
                "useKeyTab=true " +
                "storeKey=true  " +
                "keyTab=\"D:\\kafka.keytab\" " +
                "principal=\"kafka/tdh-81@TDH\";";
        kafkaParams.put("sasl.jaas.config", sasa);
        System.out.println(sasa);

        // 配置所需消费kafka队列名称，可以配置多个
        // Collection<String> topics = Arrays.asList("topic1", "topic2");
        Collection<String> topics = Arrays.asList("kafka1st");

        // 创建DStream
        JavaInputDStream<ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(
                        ssc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
                );

        //加载hdfs的配置
        Configuration hdfsconf=new Configuration();
        hdfsconf.addResource(SparkKafkaDemo.class.getClassLoader().getResource("core-site.xml"));
        //conf.addResource(hdfs_putanddown.class.getClassLoader().getResource("yarn-site.xml"));
        hdfsconf.addResource(SparkKafkaDemo.class.getClassLoader().getResource("hdfs-site.xml"));
        UserGroupInformation.setConfiguration(hdfsconf);

        try{
            UserGroupInformation.loginUserFromKeytab("kafka/tdh-81@TDH","D:\\kafka.keytab");
        }catch (IOException e){
            e.printStackTrace();
        }
        // 对DStream执行业务所需的转换，下面以取kafka数据中的value字段为例
        JavaDStream<String> result = stream.map(new Function<ConsumerRecord<String, String>, String>() {
            @Override
            public String call(ConsumerRecord<String, String> record) throws Exception {
                return record.value();
            }
        });

        // 调用foreachRDD action将数据存储到HDFS，如果没有将数据按自定义规则划分存储目录的需求，
        // 则可以直接调用dstream.saveAsTextFile或saveAsHadoopFile等方法
        result.foreachRDD(new VoidFunction2<JavaRDD<String>, Time>() {

            // 此方法用于计算当前批次数据该写入那个文件夹中，这里简单通过毫秒数进行区分
            private String computeOutputDir(Time time) {
                return time.milliseconds() + "";
            }

            @Override
            public void call(JavaRDD<String> rdd, Time time) throws Exception {
                //rdd.saveAsTextFile("hdfs://172.16.140.25:8020/spark/test2/" + computeOutputDir(time));
                rdd.saveAsTextFile("hdfs://172.16.140.81:8020/spark/test2/" + computeOutputDir(time));
            }
        });

        // 启动SparkStreaming，并等待程序被终止
        ssc.start();
        ssc.awaitTermination();
    }
}
