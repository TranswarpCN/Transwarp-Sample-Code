package io.transwarp.kafkaConsumer;

import org.apache.hadoop.conf.Configuration;

public class Constant {
    private static Configuration configuration = new Configuration();

    // 加载配置
    static {
        try {
            configuration.addResource("setup.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 定义配置常量
    String ZK_CONNECT;
    String GROUP_ID;
    String TOPIC;
    String THREAD_POOL_SIZE;
    String THREAD_NUM;
    String OPEN_KERBEROS;
    String KERBEROS_USER;
    String KEYTAB;
    String BOOTSTRAP_SERVER;

    // 构造函数
    public Constant() {
        this.ZK_CONNECT = configuration.get("zk_connect");
        this.BOOTSTRAP_SERVER=configuration.get("bootstrap_servers");
        this.GROUP_ID = configuration.get("group_id");
        this.TOPIC = configuration.get("topic");
        this.THREAD_POOL_SIZE = configuration.get("thread_pool_size");
        this.THREAD_NUM = configuration.get("thread_num");
        this.OPEN_KERBEROS = configuration.get("open_kerberos");
        this.KERBEROS_USER = configuration.get("kerberos_user");
        this.KEYTAB = configuration.get("keytab");
    }
}
