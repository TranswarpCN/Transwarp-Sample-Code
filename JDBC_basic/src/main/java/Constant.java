import org.apache.hadoop.conf.Configuration;

/**
 * Created by Xiaolin on 2017/4/28.
 */
public class Constant {
    // 配置文件类，主要是conf.xml等
    private static Configuration configuration = new Configuration();
    // 驱动类名称
    static String driverName;
    // inceptor的连接模式，一般有simple，ldap，kerberos
    static String mode;
    // 数据库连接字符串
    static String JDBC_URL;
    // LDAP模式下的用户名
    static String UserName;
    // LDAP模式下的密码
    static String Password;
    // Kerberos模式下的额外连接参数
    static String Kerberos_Parameter;
    // 需要执行的SQL语句
    static String RunSQL;

    Constant(){
        configuration.addResource("conf.xml");
        driverName=configuration.get("driverName");
        mode=configuration.get("mode").toLowerCase();
        JDBC_URL=configuration.get("JDBC_URL");
        UserName=configuration.get("UserName");
        Password=configuration.get("Password");
        Kerberos_Parameter=configuration.get("Kerberos_Parameter");
        RunSQL=configuration.get("RunSQL");
    }
}
