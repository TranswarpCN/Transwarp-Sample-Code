package io.transwarp.streamdatacheck;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hive.hbase.HBaseUtils;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

public class udtfCheckKerberos extends GenericUDTF {
    private PrimitiveObjectInspector stringOI = null;
    boolean result = true;
    private static HTable hTable;
    private static Logger log = LoggerFactory.getLogger(udfCheckKerberos.class);

    @Override
    public StructObjectInspector initialize(ObjectInspector[] args) throws UDFArgumentException {
        stringOI = (PrimitiveObjectInspector) args[0];

        List<String> fieldNames = new ArrayList<String>(2);
        List<ObjectInspector> fieldOIs = new ArrayList<ObjectInspector>(2);
        fieldNames.add("id");
        fieldNames.add("name");
        fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
        fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
        return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldOIs);
    }

    @Override
    public void process(Object[] record) throws HiveException {
        final String document = (String) stringOI.getPrimitiveJavaObject(record[0]);

        if (document == null) {
            return;
        }

        final String[] tokens = document.split(",");
        String[] results = tokens[1].split(" ");

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
                        Get get = new Get(Bytes.toBytes(tokens[0]));

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

        if (!result) {
            for (String r : results) {
                forward(new Object[]{tokens[0], r});
            }
        }
    }

    @Override
    public void close() throws HiveException {
        // do nothing
    }
}
