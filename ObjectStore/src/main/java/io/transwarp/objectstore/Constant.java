package io.transwarp.objectstore;

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

    // 配置中常量
    String HBASE_TABLE_NAME;
    String REGION_NUM;
    String MODE;
    String KERBEROS_WITH_KEYTAB_USER;
    String KERBEROS_KEYTAB;
    String KERBEROS_WITH_PASSWD_USER;
    String KERBEROS_PASSWD;
    String UPLOAD_DIR;
    String DOWNLOAD_DIR;
    String FLUSH_SIZE;
    String THREAD_POOL_SIZE;
    String THREAD_NUM;

    // 构造函数
    public Constant() {
        this.HBASE_TABLE_NAME = configuration.get("hbase_table_name");
        this.REGION_NUM = configuration.get("region_num");
        this.MODE = configuration.get("mode");
        this.KERBEROS_WITH_KEYTAB_USER = configuration.get("kerberos_with_keytab_user");
        this.KERBEROS_KEYTAB = configuration.get("kerberos_keytab");
        this.KERBEROS_WITH_PASSWD_USER = configuration.get("kerberos_with_passwd_user");
        this.KERBEROS_PASSWD = configuration.get("kerberos_passwd");
        this.UPLOAD_DIR = configuration.get("upload_dir");
        this.DOWNLOAD_DIR = configuration.get("download_dir");
        this.FLUSH_SIZE = configuration.get("flush_size");
        this.THREAD_POOL_SIZE = configuration.get("thread_pool_size");
        this.THREAD_NUM = configuration.get("thread_num");
    }
}
