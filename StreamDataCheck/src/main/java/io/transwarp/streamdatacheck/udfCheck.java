package io.transwarp.streamdatacheck;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hive.ql.exec.UDF;

public class udfCheck extends UDF {
    static boolean  result = true ;

    private static Configuration conf = HBaseConfiguration.create();

    private static HTable hTable;
    public static boolean evaluate(String rowkey) {
        try {
            hTable = new HTable(conf, "bi");
            Get get = new Get(Bytes.toBytes(rowkey));
            result = hTable.exists(get);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
