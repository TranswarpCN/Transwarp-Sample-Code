package io.transwarp.batchinsert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// JDBC 初始化，获取连接数据库连接
public class ConnectorSingleton {
    // 连接实例
    private static ConnectorSingleton uniqueInstanceConnectorSingleton = null;
    // 私有构造函数，反射加载驱动类
    private ConnectorSingleton() {
        try {
            Class.forName(Constant.configuration.get(Constant.driverName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 获取JDBC连接实例
    public static  ConnectorSingleton getConnectorSingleton(){
        if (uniqueInstanceConnectorSingleton == null){
            uniqueInstanceConnectorSingleton = new ConnectorSingleton();
        }
        return uniqueInstanceConnectorSingleton;
    }

    // 通过连接实例获取一个数据库连接
    public Connection getConnection(){
        Connection connection = null;
        try {
            if (Constant.mode.toLowerCase().equals(Constant.MODE.KERBEROS.key)) {
                connection = DriverManager.getConnection(Constant.configuration.get(Constant.kerberosJdbcUrl));
            } else if (Constant.mode.toLowerCase().equals(Constant.MODE.LDAP.key)) {
                connection = DriverManager.getConnection(Constant.configuration.get(Constant.LDAPJdbcUrl),
                        Constant.configuration.get(Constant.LDAPName), Constant.configuration.get(Constant.LDAPPasswd));
            } else if (Constant.mode.toLowerCase().equals(Constant.MODE.SIMPLE.key)) {
                connection = DriverManager.getConnection(Constant.configuration.get(Constant.simpleJdbcUrl));
            } else {
                //使用自定异常类
                throw new BatchInsertException("不能获取一个有效的Transwarp Incepor 连接");
            }
        } catch(BatchInsertException bie){
            bie.printStackTrace();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
