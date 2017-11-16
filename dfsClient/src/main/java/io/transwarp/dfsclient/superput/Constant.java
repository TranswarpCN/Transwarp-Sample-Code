package io.transwarp.dfsclient.superput;

import org.apache.hadoop.conf.Configuration;

public class Constant {
    private static Configuration configuration = new Configuration();

    // 加载配置文件
    static {
        try {
            configuration.addResource("setup.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 定义常量
    String LOCAL_DATA_DIR;
    String HDFS_DATA_DIR;
    String COMPRESSION_SWITCHER;
    String BUFFER_SIZE;
    String COMPRESSION_CODEC;
    String DELETE_LOCAL_DATA;
    String FILE_TYPE_FILTER;
    String THREAD_POOL_SIZE;
    String THREAD_NUM;
    String OPEN_KERBEROS;
    String KERBEROS_USER;
    String KEYTAB;

    public Constant() {
        this.LOCAL_DATA_DIR = configuration.get("local_data_dir");
        this.HDFS_DATA_DIR = configuration.get("hdfs_data_dir");
        this.COMPRESSION_SWITCHER = configuration.get("compression_switcher");
        this.BUFFER_SIZE = configuration.get("buffer_size");
        this.COMPRESSION_CODEC = configuration.get("compression_codec");
        this.DELETE_LOCAL_DATA = configuration.get("delete_local_data");
        this.FILE_TYPE_FILTER = configuration.get("file_type_filter");
        this.THREAD_POOL_SIZE = configuration.get("thread_pool_size");
        this.THREAD_NUM = configuration.get("thread_num");
        this.OPEN_KERBEROS = configuration.get("open_kerberos");
        this.KERBEROS_USER = configuration.get("kerberos_user");
        this.KEYTAB = configuration.get("keytab");
    }
}
