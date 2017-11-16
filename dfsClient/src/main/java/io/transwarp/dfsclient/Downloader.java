package io.transwarp.dfsclient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.*;
import java.net.URI;

public class Downloader {
    // 通过Java API下载文件
    private static Log LOG = LogFactory.getLog(Downloader.class);
    private String src;
    private static Configuration conf;
    public static DistributedFileSystem dfs;
    static {
        conf = new HdfsConfiguration();
        conf.addResource("core-site.xml");
        conf.addResource("hdfs-site.xml");
        conf.addResource("yarn-site.xml");
        dfs = new DistributedFileSystem();
        try {
            // 没开kerberos，注释下面两行
            UserGroupInformation.setConfiguration(conf);
            UserGroupInformation.loginUserFromKeytab("hdfs@TDH","E:\\星环\\hdfs.keytab");
            dfs.initialize(new URI(conf.get("fs.defaultFS")), conf);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public Downloader(String src) {
        this.src = src;
    }

    public void download(String dest) {
        Path path = new Path(src);
        File file = new File(dest);
        file.mkdirs();
        try {
            if (dfs.isFile(path)) {
                innerDownloadFile(src, dest);
            } else {
                innerDownloadDir(src, dest);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void innerDownloadFile(String src, String dest) {
        Path path = new Path(src);
        try {
            if (dfs.exists(path)) {
                File file = new File(dest + File.separator + path.getName());
                file.createNewFile();
                InputStream in = dfs.open(path);
                OutputStream out = new FileOutputStream(file);
                IOUtils.copyBytes(in, out, conf);
                in.close();
                out.close();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void innerDownloadDir(String src, String dest) {

        Path path = new Path(src);
        File file = new File(dest + File.separator + path.getName());
        file.mkdirs();
        try {
            FileStatus[] fss = dfs.listStatus(path);
            for (int i = 0; i < fss.length; i++) {
                if (fss[i].isFile()) {
                    innerDownloadFile(fss[i].getPath().toString(), dest
                            + File.separator + path.getName());
                } else {

                    innerDownloadDir(fss[i].getPath().toString(),
                            file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        Downloader downloader = new Downloader("/tmp/yarn-site.xml");
        downloader.download("E:\\星环");
    }
}