package io.transwarp.kafkaProducer;

//import kafka.auth.AuthenticationManager;

import java.util.Properties;

public class kafkaProperties {
    private Properties properties = new Properties();

    // 构造函数
    public kafkaProperties() {
        Constant constant = new Constant();

      /*  if (constant.OPEN_KERBEROS.equals("true")) {
            AuthenticationManager.setAuthMethod("kerberos");
            AuthenticationManager.login(constant.KERBEROS_USER, constant.KEYTAB);
        }*/

        properties.put("metadata.broker.list",constant.METADATA_BROKER_LIST);
        properties.put("serializer.class", constant.SERIALIZER_CLASS);
        properties.put("key.serializer.class", constant.KEY_SERIALIZER_CLASS);
        properties.put("request.required.acks", constant.REQUEST_REQUIRED_ACKS);
    }

    public Properties properties() {
        return properties;
    }
}
