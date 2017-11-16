package io.transwarp.webhdfs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

public class RemoteShellTool {
    private Connection conn;
    private String ipAddr;
    private String charset = Charset.defaultCharset().toString();
    private String userName;
    private String password;

    public RemoteShellTool(String ipAddr, String userName, String password,
                           String charset) {
        this.ipAddr = ipAddr;
        this.userName = userName;
        this.password = password;
        if (charset != null) {
            this.charset = charset;
        }
    }

    public boolean login() throws IOException {
        conn = new Connection(ipAddr);
        conn.connect(); // 连接
        return conn.authenticateWithPassword(userName, password); // 认证
    }

    public String exec(String cmds) {
        InputStream in = null;
        String result = "";
        try {
            if (this.login()) {
                Session session = conn.openSession();
                session.execCommand(cmds);
                in = session.getStdout();
                result = this.processStdout(in, this.charset);
                session.close();
                conn.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return result;
    }

    public String processStdout(InputStream in, String charset) {

        byte[] buf = new byte[1024];
        StringBuffer sb = new StringBuffer();
        try {
            while (in.read(buf) != -1) {
                sb.append(new String(buf, charset));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        RemoteShellTool tool = new RemoteShellTool("172.16.2.96", "root", "123456", "utf-8");
        String cmd0 = "kdestroy ;" +
                "rm -rf hdfs.keytab ;" +
                "kadmin.local -q \"xst -norandkey -k hdfs.keytab hdfs\"";

        String cmd = "kinit -kt hdfs.keytab hdfs ;" +
                "curl -s --negotiate -u: \"http://172.16.2.96:50070/webhdfs/v1/?op=GETDELEGATIONTOKEN\"";

        tool.exec(cmd0);
        String result =  tool.exec(cmd);
        String token = result.substring(result.indexOf("\":\"")+3,result.indexOf("\"}}"));
        System.out.println(token);
//        String result = tool.exec("kadmin.local -q \"xst -norandkey -k hdfs.keytab hdfs\"");
//        System.out.print(result);
//        String result = tool.exec("kinit -kt hdfs.keytab hdfs ;curl -s --negotiate -u: \"http://172.16.2.96:50070/webhdfs/v1/?op=GETDELEGATIONTOKEN\"");
//        System.out.print(result);
    }
}
