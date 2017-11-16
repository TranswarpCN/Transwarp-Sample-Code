package io.transwarp.batchinsert;

import org.apache.hadoop.conf.Configuration;

// 在Batchinsert实例中需要使用的常量，在配置文件set.xml中应该有相应的配置
public final  class Constant {
    // 配置文件类，主要是set.xml等
    public static Configuration configuration = new Configuration();
    // 驱动类名称
    public static String driverName;
    // inceptor的连接模式，一般有simple，ldap
    public static String mode;
    // 当配置连接Kerberos认证的Inceptor时，数据库连接字符串
    public static String kerberosJdbcUrl;
    // 当配置连接LDAP认证的Inceptor时，数据库连接字符串
    public static String LDAPJdbcUrl;
    // 当配置连接LDAP认证的Inceptor时，用户名称对应的用户密码
    public static String LDAPPasswd;
    // 当配置连接LDAP认证的Inceptor时，用户名称
    public static String LDAPName;
    // 在简单模式下连接inceptor的url
    public static String simpleJdbcUrl;
    // 数据库连接线程池的大小
    public static String connectionsNum;
    // 在入库时，每个批次的大小
    public static String batchSize;
    // 文件的字段分隔符
    public static String fileSeparator;
    // 文件的字段数量
    public static String fieldsNum;
    // 设置读取文件的线程数量
    public static String readFileThreadNum;;

    // 构造函数，加载配置文件，初始化常量变量
    private  Constant() {
        configuration.addResource("conf/setup.xml");
        driverName = configuration.get("driverName");
        mode = configuration.get("mode");
        kerberosJdbcUrl = configuration.get("kerberosJdbcUrl");
        LDAPJdbcUrl = configuration.get("LDAPJdbcUrl");
        LDAPPasswd = configuration.get("LDAPPasswd");
        LDAPName = configuration.get("LDAPName");
        simpleJdbcUrl = configuration.get("simpleJdbcUrl");
        connectionsNum = configuration.get("connectionsNum");
        fileSeparator = configuration.get("fileSeparator");
        fieldsNum = configuration.get("fieldsNum");
        readFileThreadNum = configuration.get("readFileThreadNum");
    }

    // 连接模式的枚举
    public static enum MODE {
        KERBEROS("kerberos", 1), LDAP("ldap", 2), SIMPLE("simple", 3);
        String key;
        int value;

        MODE(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }
}
