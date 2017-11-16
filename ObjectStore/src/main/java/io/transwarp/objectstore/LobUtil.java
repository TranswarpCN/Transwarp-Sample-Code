package io.transwarp.objectstore;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.protobuf.generated.HyperbaseProtos;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hyperbase.client.HyperbaseAdmin;
import org.apache.hadoop.hyperbase.datatype.TypeException;
import org.apache.hadoop.hyperbase.secondaryindex.IndexedColumn;
import org.apache.hadoop.hyperbase.secondaryindex.LOBIndex;

import java.io.IOException;

public class LobUtil {
    private HyperbaseAdmin admin;
    private String family1 = "file";
    private String f1_q1 = "filename";
    private String family2 = "data";
    private String f2_q1 = "bytes";
    private String indexName = "index";
    private int flushSize;
    private Configuration conf;
    private Constant constant;

    // 构造函数
    public LobUtil(HyperbaseAdmin admin, Configuration conf) {
        // TODO Auto-generated constructor stub
        this.admin = admin;
        this.conf = conf;
    }

    // 创建Hyperbase表
    public void createLobTable(String tableName, int size){
        flushSize = size;
        try {
            if(admin.tableExists(TableName.valueOf(tableName))){
                System.out.println("Table already exists!!---" + tableName);
                return;
            }
            HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
            tableDescriptor.addFamily(new HColumnDescriptor(family1));

            HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(family2);
            hColumnDescriptor.setValue(HConstants.HREGION_MEMSTORE_COLUMNFAMILY_FLUSH_SIZE, String.valueOf(flushSize));
            tableDescriptor.addFamily(hColumnDescriptor);

            constant = new Constant();

            int r = Integer.valueOf(constant.REGION_NUM);
            int t = Integer.valueOf(constant.THREAD_NUM);
            int tp = Integer.valueOf(constant.THREAD_POOL_SIZE);

            byte[][] splitKeys = new byte[r-1][];
            if (t == tp) {
                if (r == t) {
                    for (int i = 0; i < r-1; ++i) {
                        splitKeys[i] = Bytes.toBytes(String.valueOf((i+1) + "tt0"));
                    }
                } else if (r > t) {
                    double tmp = r / t;
                    for (int i = 0; i < r-1; ++i) {
                        splitKeys[i] = Bytes.toBytes(String.valueOf((int) ((i+1)*tmp) + "tt0"));
                    }
                } else {
                    for (int i = 0; i < r-1; ++i) {
                        splitKeys[i] = Bytes.toBytes(String.valueOf(i+1) + "tt0");
                    }
                }
            } else if (t > tp) {
                int min = t >= tp ? tp : t;
                if (r == min) {
                    for (int i = 0; i < r-1; ++i) {
                        splitKeys[i] = Bytes.toBytes(String.valueOf((i+1) + "tt0"));
                    }
                } else if (r > min) {
                    double tmp = r / min;
                    for (int i = 0; i < r-1; ++i) {
                        splitKeys[i] = Bytes.toBytes(String.valueOf((int) ((i+1)*tmp) + "tt0"));
                    }
                } else {
                    for (int i = 0; i < r-1; ++i) {
                        splitKeys[i] = Bytes.toBytes(String.valueOf(i+1) + "tt0");
                    }
                }
            } else {
                int min = t >= tp ? tp : t;
                if (r == min) {
                    for (int i = 0; i < r-1; ++i) {
                        splitKeys[i] = Bytes.toBytes(String.valueOf((i+1) + "tt0"));
                    }
                } else if (r > min) {
                    double tmp = r / min;
                    for (int i = 0; i < r-1; ++i) {
                        splitKeys[i] = Bytes.toBytes(String.valueOf((int) ((i+1)*tmp) + "tt0"));
                    }
                } else {
                    for (int i = 0; i < r-1; ++i) {
                        splitKeys[i] = Bytes.toBytes(String.valueOf(i+1) + "tt0");
                    }
                }
            }
            admin.createTable(tableDescriptor, null, splitKeys);
            addLOB(TableName.valueOf(tableName), family1, f1_q1, indexName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 创建LOB Index
    private void addLOB(TableName table, String indexdFamily, String indexdQualifer, String LOBfamily) {
        try {
            HyperbaseProtos.SecondaryIndex.Builder LOBbuilder =
                    HyperbaseProtos.SecondaryIndex.newBuilder();
            LOBbuilder.setClassName(LOBIndex.class.getName());
            LOBbuilder.setUpdate(true);
            LOBbuilder.setDcop(true);
            IndexedColumn column = new IndexedColumn(Bytes.toBytes(indexdFamily), Bytes.toBytes(indexdQualifer));
            LOBbuilder.addColumns(column.toPb());
            admin.addLob(table, new LOBIndex(LOBbuilder.build()), Bytes.toBytes(LOBfamily), false, 1);
        } catch (TypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 上传对象到LOB
    public void putLob(String tableName, String row, String filename, byte[] fileData){
        byte[] rowkey = Bytes.toBytes(row);
        try {
            HTable htable = new HTable(conf, tableName);
            Put put = new Put(rowkey);
            put.add(Bytes.toBytes(family1), Bytes.toBytes(f1_q1), Bytes.toBytes(filename));
            put.add(Bytes.toBytes(family2), Bytes.toBytes(f2_q1), fileData);
            htable.put(put);
            htable.flushCommits();
            htable.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}