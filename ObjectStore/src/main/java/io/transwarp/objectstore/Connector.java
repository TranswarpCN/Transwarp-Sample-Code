package io.transwarp.objectstore;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hyperbase.client.HyperbaseAdmin;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

public class Connector {

    private Configuration configuration;
    private HBaseAdmin hBaseAdmin;
    private HyperbaseAdmin hyperbaseAdmin;

    // 加载配置
    public Connector() {
        Configuration HBASE_CONFIG = new Configuration();
        Constant constant = new Constant();
        HBASE_CONFIG.addResource("hbase-site.xml");
        HBASE_CONFIG.addResource("core-site.xml");
        HBASE_CONFIG.addResource("hdfs-site.xml");
        configuration = HBaseConfiguration.create(HBASE_CONFIG);

        try {
            if (constant.MODE.equals("kerberos")) {
                UserGroupInformation.setConfiguration(configuration);
//                UserGroupInformation.loginUserFromPassword(constant.KERBEROS_USER,constant.KERBEROS_PASSWD);
                UserGroupInformation.loginUserFromKeytab(constant.KERBEROS_WITH_KEYTAB_USER,constant.KERBEROS_KEYTAB);
            }
            hBaseAdmin = new HBaseAdmin(configuration);
            hyperbaseAdmin = new HyperbaseAdmin(configuration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取HyperbaseAdmin对象
    public HyperbaseAdmin getHyperbaseAdmin() {
        return hyperbaseAdmin;
    }

    // 获取配置
    public Configuration getConfiguration() {
        return configuration;
    }

    // 关闭HBaseAdmin/HyperbaseAdmin对象
    public void close() {
        try {
            hBaseAdmin.close();
            hyperbaseAdmin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 检测是否连接上Hyperbase
    public void checkConnection() {
        try {
            if (!hBaseAdmin.tableExists("xxxxxxxx")) {
                System.out.println("hbase connected succed");
                return;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("hbase connected failed");
    }

    // 主函数，检测是否连接上Hyperbase
    public static void main(String[] args) {
        Connector connector = new Connector();
        connector.checkConnection();
    }
}
