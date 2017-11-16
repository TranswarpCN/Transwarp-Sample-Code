package io.transwarp.dfsclient;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

public class Delete {
    // 通过Java API删除文件
    public static void main(String[] args) {
        String rootPath = "hdfs://nameservice1";
        Path p = new Path(rootPath + "/tmp/file.txt");
        Configuration conf = new Configuration();
        conf.addResource("core-site.xml");
        conf.addResource("hdfs-site.xml");
        conf.addResource("yarn-site.xml");
        try {
            // 没开kerberos，注释下面两行
            UserGroupInformation.setConfiguration(conf);
            UserGroupInformation.loginUserFromKeytab("hdfs@TDH","E:\\星环\\hdfs.keytab");
            FileSystem fs = p.getFileSystem(conf);
            boolean b = fs.delete(p, true);
            System.out.println(b);
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}