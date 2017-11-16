package io.transwarp.dfsclient.superput;

import org.apache.hadoop.conf.Configuration;

public class hdfsProperties {
    static Configuration configuration = new Configuration();
    // 加载配置文件
    static {
        try {
            configuration.addResource("core-site.xml");
            configuration.addResource("hdfs-site.xml");
            configuration.addResource("yarn-site.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Configuration getConf() {
        return configuration;
    }
}
