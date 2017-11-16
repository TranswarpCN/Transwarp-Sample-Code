package io.transwarp.streamdatacheck;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

import java.util.ArrayList;
import java.util.List;

public class udtfCheck extends GenericUDTF {
    private PrimitiveObjectInspector stringOI = null;
    boolean result = true;
    private static Configuration conf = HBaseConfiguration.create();
    private static HTable hTable;

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

        String[] tokens = document.split(",");
        String[] results = tokens[1].split(" ");

        try {
            hTable = new HTable(conf, "bi");
            Get get = new Get(Bytes.toBytes(tokens[0]));
            result = hTable.exists(get);
        } catch (Exception e) {
            e.printStackTrace();
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
