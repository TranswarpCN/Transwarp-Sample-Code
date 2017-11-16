package io.transwarp.keywordsearch;

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

    static String CURRENT_PATH = configuration.get("current_path");
    static String DIRECTORY_PATH = configuration.get("directory_path");
    static String DRIVER_NAME = configuration.get("driver_name");
    static String MODE = configuration.get("mode");
    static String SIMPLE_JDBC_URL = configuration.get("simple_jdbc_url");
    static String LDAP_JDBC_URL = configuration.get("ldap_jdbc_url");
    static String LDAP_NAME = configuration.get("ldap_name");
    static String LDAP_PASSWD = configuration.get("ldap_passwd");
    static String KERBEROS_JDBC_URL = configuration.get("kerberos_jdbc_url");
    static String PARAMETER = configuration.get("parameter");
    static String CREATE_EXTERNAL_TABLE = configuration.get("create_external_table");
    static String SEARCH_SQL = configuration.get("search_sql");
    static String THREAD_POOL_SIZE = configuration.get("thread_pool_size");
    static String THREAD_NUM = configuration.get("thread_num");
}
