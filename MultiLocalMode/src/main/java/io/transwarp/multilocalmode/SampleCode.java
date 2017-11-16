package io.transwarp.multilocalmode;

import java.util.ArrayList;
import java.util.List;

public class SampleCode {
    public static int hostNum;
    public static int hostnth;

    // 获取节点数量
    public static int getHostNums() {
        if (Constant.MODE.equals("kerberos")) {
            String[] conns = Constant.KERBEROS_JDBC_URL.split("|");
            return conns.length;
        } else if (Constant.MODE.equals("LDAP")) {
            String[] conns = Constant.LDAP_JDBC_URL.split(";");
            return conns.length;
        } else if (Constant.MODE.equals("simple")) {
            String[] conns = Constant.SIMPLE_JDBC_URL.split(";");
            return conns.length;
        } else {
            return 0;
        }
    }

    // 主函数
    public static void main(String[] args) {
        hostNum = getHostNums();
        if (hostNum == 0) {
            System.exit(0);
        }
        List<Manager> threads = new ArrayList<Manager>();

        for (int k = 0; k < hostNum; ++k) {
            hostnth = k;
            threads.add(new Manager(hostnth));
        }

        for(Manager manager : threads) {
            manager.go();
        }
    }
}
