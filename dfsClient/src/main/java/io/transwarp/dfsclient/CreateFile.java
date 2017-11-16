package io.transwarp.dfsclient;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

public class CreateFile {
    public static void main(String[] args) throws IOException {
        // 通过Java API创建文件
        String rootPath = "hdfs://nameservice1";
        Path p = new Path(rootPath + "/tmp/file.txt");
        Configuration conf = new Configuration();
        conf.addResource("core-site.xml");
        conf.addResource("hdfs-site.xml");
        conf.addResource("yarn-site.xml");
        // 没开kerberos，注释下面两行
        UserGroupInformation.setConfiguration(conf);
        UserGroupInformation.loginUserFromKeytab("hdfs@TDH","E:\\星环\\hdfs.keytab");
        FileSystem fs = p.getFileSystem(conf);
        fs.create(p);
        fs.close();
    }
}