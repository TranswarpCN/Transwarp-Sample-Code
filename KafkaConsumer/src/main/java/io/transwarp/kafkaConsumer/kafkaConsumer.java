package io.transwarp.kafkaConsumer;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.HdfsConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class kafkaConsumer {


    public static Configuration conf;
    public static FileSystem fs ;
    private static FSDataOutputStream  hdfsOutStream;
    private int max_sync = 1000;
    private int current_sync = 0;
    private String lock;
    private static Path path;


    public kafkaConsumer() {

    }

    // 线程对象
    private  class Task implements Runnable {
        KafkaStream<byte[], byte[]> stream;

        Task(KafkaStream<byte[], byte[]> stream) {
            this.stream = stream;
        }

        public void run() {
            ConsumerIterator<byte[], byte[]> it = stream.iterator();
            while (it.hasNext()) {
                //byte[] message=it.next().message();
                MessageAndMetadata<byte[],byte[]> next = it.next();
                System.out.println(Thread.currentThread().getName() + ": partition[" + next.partition() + "],"
                        + "offset[" + next.offset() + "], " + new String(next.message()));
                try {
                    synchronized (lock) {
                        WriteFile(getFSDataOutputStream(), new String(next.message()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 创建线程池，执行kafka消费者
    public void go() {
        Constant constant = new Constant();
        kafkaProperties kafkaProperties = new kafkaProperties();
        ConsumerConfig config = new ConsumerConfig(kafkaProperties.properties());

        ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(constant.THREAD_POOL_SIZE));

        String topic = constant.TOPIC;
//        Task[] tasks = new Task[Integer.parseInt(constant.THREAD_NUM)];
        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, new Integer(constant.THREAD_NUM));
        ConsumerConnector consumer = kafka.consumer.Consumer.createJavaConsumerConnector(config);
        StringDecoder keyDecoder = new StringDecoder(new VerifiableProperties());
        StringDecoder valueDecoder = new StringDecoder(new VerifiableProperties());
        Map<String, List<KafkaStream<String, String>>> consumerMap = consumer.createMessageStreams(topicCountMap,keyDecoder,valueDecoder);
        List<KafkaStream<String, String>> streams = consumerMap.get(topic);

        for (KafkaStream stream : streams) {
            executorService.submit(new Task(stream));
        }

        executorService.shutdown();
    }
    public static FSDataOutputStream getFSDataOutputStream(){
        return hdfsOutStream;
    }

    public static void init(){
            conf = new HdfsConfiguration();
            //String url = "hdfs://Master:9000/test" ;
            conf.addResource(kafkaConsumer.class.getClassLoader().getResource("core-site.xml"));
            //conf.addResource(hdfs_putanddown.class.getClassLoader().getResource("yarn-site.xml"));
            conf.addResource(kafkaConsumer.class.getClassLoader().getResource("hdfs-site.xml"));
            conf .set("dfs.client.block.write.replace-datanode-on-failure.policy" ,"NEVER" );
            conf .set("dfs.client.block.write.replace-datanode-on-failure.enable" ,"true" );
    }

    public void WriteFile(FSDataOutputStream hdfsOutStream, String message) throws IOException {
        path=new Path(new String("/tmp/kafkatest/kafkamessage1"));

        try {
            fs=path.getFileSystem(conf);
            hdfsOutStream=fs.create(path);
            hdfsOutStream.write(message.getBytes());
            hdfsOutStream.write("\n".getBytes());
            current_sync++;
            if (current_sync >= max_sync) {
                hdfsOutStream.sync();
            }
            hdfsOutStream.hflush();
            hdfsOutStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
