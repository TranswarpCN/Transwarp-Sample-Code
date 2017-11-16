package kafkaProducertdh5;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class kafkaProducer010 {
    //TDH5.0采用了kafka0.82,支持了新版的api，老版本的api可用但是新版本的包不会适配安全部分，如果要使用安全需要采用新版api

    public static void main(String[] args){
        // 配置连接kafka参数，访问开安全后的kafka，相关参数也在此添加
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

        KafkaProducer<String,String> kp=new KafkaProducer<String, String>(kafkaParams);
        kp.send(new ProducerRecord<String, String>("topic","25443523,sadasd,aefewf  wd a"));

    }



}
