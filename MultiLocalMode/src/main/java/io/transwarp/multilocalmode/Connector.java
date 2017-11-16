package io.transwarp.multilocalmode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connector {
    // 加载驱动
    static {
        try {
            Class.forName(Constant.DRIVER_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取连接
    public static Connection getConnection(int hostwho) {
        Connection connection = null;
        try {
            if (Constant.MODE.equals("simple")) {
                connection = DriverManager.getConnection(Constant.SIMPLE_JDBC_URL.split(";")[hostwho]);
            } else if (Constant.MODE.equals("LDAP")) {
                connection = DriverManager.getConnection(Constant.LDAP_JDBC_URL.split(";")[hostwho],
                        Constant.LDAP_NAME.split(";")[hostwho], Constant.LDAP_PASSWD.split(";")[hostwho]);
            } else {
                connection = DriverManager.getConnection(Constant.KERBEROS_JDBC_URL.split("|")[hostwho]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
