package io.transwarp.dfsclient;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.*;

public class UploadFile {
    // 通过Java API上传文件
    public static void main(String[] args) throws IOException {
        Configuration conf = new Configuration();
        conf.addResource("core-site.xml");
        conf.addResource("hdfs-site.xml");
        conf.addResource("yarn-site.xml");
        // 没开kerberos，注释下面两行
        UserGroupInformation.setConfiguration(conf);
        UserGroupInformation.loginUserFromKeytab("hdfs@TDH","E:\\星环\\任务\\2016年11月28日\\hdfs.keytab");
        String localFile = "E:\\星环\\yarn-site.xml";
        InputStream in = new BufferedInputStream(new FileInputStream(localFile));
        Path p = new Path( "/tmp/yarn-site.xml");
        FileSystem fs = p.getFileSystem(conf);
        OutputStream out = fs.create(p);
        IOUtils.copyBytes(in, out, conf);
        fs.close();
        IOUtils.closeStream(in);
    }
}
