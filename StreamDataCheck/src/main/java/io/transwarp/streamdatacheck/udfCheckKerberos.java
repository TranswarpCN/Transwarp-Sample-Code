package io.transwarp.streamdatacheck;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hive.hbase.HBaseUtils;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivilegedAction;

public class udfCheckKerberos extends UDF {
    static boolean result = false;
    private static HTable hTable;
    private static Logger log = LoggerFactory.getLogger(udfCheckKerberos.class);

    public Boolean evaluate(final String rowkey) {
        try {
            UserGroupInformation ugi =
                    UserGroupInformation.loginUserFromKeytabAndReturnUGI("hbase@TDH", "/mnt/disk1/hbase.keytab");
            /*
            UserGroupInformation ugi =
                    UserGroupInformation.loginUserFromPasswordAndReturnUGI("hbase","123456");
            */
            ugi.doAs(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    try {
                        Configuration HBASE_CONFIG = new Configuration();
                        Configuration configuration = HBaseConfiguration.create(HBASE_CONFIG);
                        JobConf conf = new JobConf(configuration);

                        hTable = HBaseUtils.createHTable(conf, "bi");
                        Get get = new Get(Bytes.toBytes(rowkey));

                        result = hTable.exists(get);
                        return result;

                    } catch (Exception e) {
                        log.error(e.toString());
                    }
                    return result;
                }

            });
        } catch (Exception e) {
            log.error(e.toString());
        }
        return result;
    }
}
