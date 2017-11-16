package io.transwarp.kafkaProducer;

import org.apache.hadoop.conf.Configuration;

public class Constant {
    private static Configuration configuration = new Configuration();

    // 加载配置文件
    static {
        try {
            configuration.addResource("setup.xml");
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    // 定义常量
    String METADATA_BROKER_LIST;
    String SERIALIZER_CLASS;
    String KEY_SERIALIZER_CLASS;
    String REQUEST_REQUIRED_ACKS;
    String TOPIC_NAME;
    String THREAD_POOL_SIZE;
    String THREAD_NUM;
    String FILE_FOLDERS;
    String BATCH_SIZE;
    String OPEN_KERBEROS;
    String KERBEROS_USER;
    String KEYTAB;

    public Constant() {
        this.METADATA_BROKER_LIST = configuration.get("metadata_broker_list");
        this.SERIALIZER_CLASS = configuration.get("serializer_class");
        this.KEY_SERIALIZER_CLASS = configuration.get("key_serializer_class");
        this.REQUEST_REQUIRED_ACKS = configuration.get("request_required_acks");
        this.TOPIC_NAME = configuration.get("topic_name");
        this.THREAD_POOL_SIZE = configuration.get("thread_pool_size");
        this.THREAD_NUM = configuration.get("thread_num");
        this.FILE_FOLDERS = configuration.get("file_folders");
        this.BATCH_SIZE = configuration.get("batch_size");
        this.OPEN_KERBEROS = configuration.get("open_kerberos");
        this.KERBEROS_USER = configuration.get("kerberos_user");
        this.KEYTAB = configuration.get("keytab");
    }
}
