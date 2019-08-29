package io.transwarp;

import org.apache.hadoop.hbase.thrift.generated.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import javax.security.sasl.SaslException;
import java.nio.ByteBuffer;
import java.util.*;

public class HBaseThriftTest {

    public static String  clusterIP = "172.26.0.5";
    public static int  clusterNodePort = 30171;

    public static Hbase.Client  hbaseClient = null;
    private String hbaseAddr = "";
    private Integer hbasePort = 0;
    private TTransport socket = null;
    private TProtocol protocol = null;
    protected static final String CHAR_SET = "UTF-8";


    public HBaseThriftTest(String addr, Integer port) throws SaslException {
        hbaseAddr = addr;
        hbasePort = port;
        socket = new TSocket(hbaseAddr, hbasePort);
        protocol = new TBinaryProtocol(socket, true, true);
        hbaseClient = new Hbase.Client(protocol);
    }

    public static void main(String[] args) throws TTransportException {
        HBaseThriftTest client = null;
        try {
            client = new HBaseThriftTest(clusterIP, clusterNodePort);
            client.openTransport();

            doCrudTest(client);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.closeTransport();
        }
    }

    public static void doCrudTest(HBaseThriftTest client) throws Exception {

        String tableName = "t2";
        String f1 = "f1";
        String f2 = "f2";
//        dropTable(tableName);
        createTable(tableName, f1, f2);
        listTables(client);
        putData(client,tableName);
        scanData(client,tableName);
        deleteData(client, tableName);
        dropTable(tableName);
        System.out.println("Done");

    }

    private static void listTables(HBaseThriftTest client) throws TException {
       for(ByteBuffer tableByteBuffer : hbaseClient.getTableNames()){
           System.out.println(new String(client.toBytes(tableByteBuffer)));
        }
    }

    private static void dropTable(String tableName) throws TException {
        hbaseClient.disableTable(getByteBuffer(tableName));
        hbaseClient.deleteTable(getByteBuffer(tableName));
    }

    private static void deleteData(HBaseThriftTest client, String tableName) throws TException {
        client.deleteRow(tableName, "r2");
        client.deleteCell(tableName, "r1", "f1:c2");
    }

    private static void scanData(HBaseThriftTest client, String tableName) throws TException {

        String startRow = "";
        int rowCnt = 1000;

        List<String> columns = new ArrayList<String>(0);
        Map<String, String> attributesTest = new HashMap<String, String>();
        int scannerID = client.scannerOpen(tableName, startRow, columns, attributesTest);
        try {
            List<TRowResult> scanResults = client.scannerGetList(scannerID,	rowCnt);
            while (scanResults != null && !scanResults.isEmpty()) {
                for (TRowResult rslt : scanResults) {
                    client.iterateResults(rslt);
                }
                scanResults = client.scannerGetList(scannerID, rowCnt);
            }
        } finally {
            client.scannerClose(scannerID);
        }
    }

    private static void putData(HBaseThriftTest client, String tableName) throws TException {
        String rowKey_R1 = "r1";
        Map<String, String> kvpUpdate_r1 = new HashMap<String, String>();
        kvpUpdate_r1.put("f1:c1", "val_20150618_0920_1");
        kvpUpdate_r1.put("f1:c1", "val_20150618_0920_1");
        kvpUpdate_r1.put("f1:c2", "val_20150618_0920_1");

        String rowKey_R2 = "r2";
        Map<String, String> kvpUpdate_r2 = new HashMap<String, String>();
        kvpUpdate_r2.put("f1:c3", "val_20150618_0920_2");
        kvpUpdate_r2.put("f1:c1", "val_20150618_0920_2");
        kvpUpdate_r2.put("f1:c3", "val_20150618_0920_2");

        client.updateRow(tableName, rowKey_R1, kvpUpdate_r1);
        client.updateRow(tableName, rowKey_R2, kvpUpdate_r2);
    }


    private static void createTable(String tableName, String f1, String f2) throws Exception {


        ByteBuffer tableNameByte = getByteBuffer(tableName);

        List<ColumnDescriptor> columnFamilies = new ArrayList<ColumnDescriptor>();

        ColumnDescriptor cd1 = new ColumnDescriptor();
        cd1.setName(getByteBuffer(f1));

        ColumnDescriptor cd2 = new ColumnDescriptor();
        cd2.setName(getByteBuffer(f2));

        columnFamilies.add(cd1);
        columnFamilies.add(cd2);

        hbaseClient.createTable(tableNameByte,columnFamilies);

    }

    public void deleteRow(String table, String rowKey) throws TException {
        ByteBuffer tableName = getByteBuffer(table);
        ByteBuffer row = getByteBuffer(rowKey);
        hbaseClient.deleteAllRow(tableName, row, getAttributesMap(new HashMap<String, String>()));
    }

    public void deleteCell(String table, String rowKey, String column) throws TException {
        List<String> columns = new ArrayList<String>(1);
        columns.add(column);
        deleteCells(table, rowKey, columns);
    }

    public void deleteCells(String table, String rowKey, List<String> columns) throws TException {
        boolean writeToWal = false;
        List<Mutation> mutations = new ArrayList<Mutation>();
        for (String column : columns) {
            mutations.add(new Mutation(false, getByteBuffer(column), null, writeToWal));
        }
        ByteBuffer tableName = getByteBuffer(table);
        ByteBuffer row = getByteBuffer(rowKey);
        hbaseClient.mutateRow(tableName, row, mutations, getAttributesMap(new HashMap<String, String>()));
    }

    public void updateRow(String table, String rowKey, Map<String, String> rowData) throws TException {
        boolean writeToWal = false;
        Map<String, String> attributes = new HashMap<String, String>();
        List<Mutation> mutations = new ArrayList<Mutation>();

        for(Map.Entry<String, String> entry : rowData.entrySet()) {
            mutations.add(new Mutation(false, getByteBuffer(entry.getKey()), getByteBuffer(entry.getValue()), writeToWal));
        }
        Map<ByteBuffer, ByteBuffer> wrappedAttributes = getAttributesMap(attributes);
        ByteBuffer tableName = getByteBuffer(table);
        ByteBuffer row = getByteBuffer(rowKey);
        hbaseClient.mutateRow(tableName, row, mutations, wrappedAttributes);
    }

    public void iterateResults(TRowResult result) {
        Iterator<Map.Entry<ByteBuffer, TCell>> iter = result.columns.entrySet().iterator();
        System.out.println("RowKey:" + new String(result.getRow()));
        while (iter.hasNext()) {
            Map.Entry<ByteBuffer, TCell> entry = iter.next();
            System.out.println("\tCol=" + new String(toBytes(entry.getKey())) + ", Value=" + new String(entry.getValue().getValue()));
        }
    }

    private static Map<ByteBuffer, ByteBuffer> getAttributesMap(Map<String, String> attributes) {
        Map<ByteBuffer, ByteBuffer> attributesMap = null;
        if(attributes != null && !attributes.isEmpty()) {
            attributesMap = new HashMap<ByteBuffer, ByteBuffer>();
            for(Map.Entry<String, String> entry : attributes.entrySet()) {
                attributesMap.put(getByteBuffer(entry.getKey()), getByteBuffer(entry.getValue()));
            }
        }
        return attributesMap;
    }

    public List<TRowResult> scannerGetList(int id, int nbRows)throws TException {
        return hbaseClient.scannerGetList(id, nbRows);
    }

    public List<TRowResult> scannerGet(int id) throws TException {
        return hbaseClient.scannerGetList(id, 1);
    }

    public int scannerOpen(String table, String startRow, List<String> columns, Map<String, String> attributes) throws TException {
        ByteBuffer tableName = getByteBuffer(table);
        List<ByteBuffer> blist = getColumnsByte(columns);
        Map<ByteBuffer, ByteBuffer> wrappedAttributes = getAttributesMap(attributes);
        return hbaseClient.scannerOpen(tableName, getByteBuffer(startRow), blist, wrappedAttributes);
    }

    public void scannerClose(int id) throws TException {
        hbaseClient.scannerClose(id);
    }

    public List<ByteBuffer> getColumnsByte(List<String> columns) {
        List<ByteBuffer> blist = new ArrayList<ByteBuffer>();
        for(String column : columns) {
            blist.add(getByteBuffer(column));
        }
        return blist;
    }

    public byte[] toBytes(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.limit()];
        for (int i = 0; i < buffer.limit(); i++) {
            bytes[i] = buffer.get();
        }
        return bytes;
    }

    public static ByteBuffer getByteBuffer(String str) {
        return ByteBuffer.wrap(str.getBytes());
    }

    public void openTransport() throws TTransportException {
        if (socket != null) {
            socket.open();
        }
    }

    public void closeTransport() throws TTransportException {
        if (socket != null) {
            socket.close();
        }
    }
}