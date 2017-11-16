package io.transwarp.kafkaConsumer;

import java.util.Properties;

public class kafkaProperties {
    private Properties properties = new Properties();

    // 加载kafka配置
    public kafkaProperties() {
        Constant constant = new Constant();

        /*if (constant.OPEN_KERBEROS.equals("true")) {
            AuthenticationManager.setAuthMethod("kerberos");
            AuthenticationManager.login(constant.KERBEROS_USER, constant.KEYTAB);
        }*/

        //0.82版本参数
        //properties.put("zookeeper.connect", constant.ZK_CONNECT);
        //0.10版本参数
        properties.put("bootstrap.servers",constant.BOOTSTRAP_SERVER);
        properties.put("group.id", constant.GROUP_ID);
        properties.put("zookeeper.session.timeout.ms", "400");
        properties.put("zookeeper.sync.time.ms", "200");
        properties.put("auto.commit.interval.ms", "1000");
        properties.put("auto.offset.reset","earliest");
        properties.put("session.timeout.ms", "30000");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("key.deserializer.encoding", "UTF8");
        properties.put("value.deserializer.encoding", "UTF8");
    }

    public Properties properties() {
        return properties;
    }

}
