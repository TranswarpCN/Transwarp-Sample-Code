package io.transwarp.batchinsert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class SQLOperation {

    // inceptor连接实例
    static ConnectorPools connectorPools;
    static Connection connection = connectorPools.getConnection();

    // 获取文件列表
    static List<File> filesQueue = new LinkedList<File>();

    // 读取文件的线程
    static WorkThread workThread[] = new WorkThread[Integer.parseInt(Constant.readFileThreadNum)];

    // 含有占位符的sql插入语句，例如 insert into table1(....) values(?,?,?,?...)
    static String insertSql = "";

    public void close(){
        connectorPools.backConnection(connection);
    }

    /**
     * 向Hyperbase批量插入
     * @param sql 需要执行的sql语句，如insert into tableName1(f1,f2...) as select ..as struct, from tableName2
     * @return 是否执行成功
     */
    public Boolean HyperbaseBatchInsertWithSql(String sql) {
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 构造建表语句，并返回；构造sql插入语句
     * @param tableName 目标表的名称
     * @param structFields 组成hbase rowkey的字段，rowkey的顺序按照这个数组的顺序
     * @param fields 当前表的所有字段组成的数组
     * @param isStruct 是否需要指定字段，不指定默认按照第一个字段作为rowkey
     * @return 返回表的创建语句
     */
    private String GenerateTableSql(String tableName, String[] structFields,
                                    String[] fields, Boolean isStruct) {
        String sqlString = isStruct ?
                "create table if not exists %s (key struct< %s >,%s ) stored by \'org.apache.hadoop.hive.hbase.HBaseStorageHandler\' ;"
                : "create table if not exists %s ( %s ) stored by \'org.apache.hadoop.hive.hbase.HBaseStorageHandler\' ;";
        String inSql = "insert into  table(%s) valuse(%s);";
        String struct = "";

        String field = "";
        String placeHolder = "";
        for (String s : fields) {
            field += s + " String,";
            placeHolder += " ?,";
        }

        field = field.substring(0, field.lastIndexOf(","));
        placeHolder = field.substring(0, placeHolder.lastIndexOf(","));

        String.format(inSql, field, placeHolder);
        insertSql = inSql;

        if (isStruct) {
            for (String s : structFields) {
                struct += s + ":String ,";
            }
            struct = struct.substring(0, struct.lastIndexOf(","));
            String.format(sqlString, tableName, struct, field);
        } else {
            String.format(sqlString, tableName, field);
        }
        return sqlString;
    }

    /**
     * 将给定的文件插入到表
     * @param tableName 目标表的名称
     * @param inputPath 文件存放路径，本地路径，可以是文件也可以是目录
     * @param fields 当前表的所有字段组成的数组
     * @param isCreateTable 是否需要创建表
     * @return 是否执行成功
     */
    public Boolean HyperbaseBatchInsertWithoutStructRowKey(String tableName, String inputPath,
                                                           String[] fields, Boolean isCreateTable) {
        if (!isExitsTable(tableName) && !isCreateTable) {
            System.out.println("table is not exists");
            System.exit(1);
        }
        if (isCreateTable) {
            try {
                connection.createStatement().execute(GenerateTableSql(tableName,null, fields, false));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        BatchInsert(inputPath);
        return true;
    }

    /**
     * 将给定的文件插入到表
     * @param tableName 目标表的名称
     * @param inputPath 文件存放路径，本地路径，可以是文件也可以是目录
     * @param structFields 组成hbase rowkey的字段，rowkey的顺序按照这个数组的顺序
     * @param fields 当前表的所有字段组成的数组
     * @param isCreateTable 是否需要创建表
     * @return 是否执行成功
     */
    public Boolean HyperbaseBatchInsertWithStructRowKey(String tableName, String inputPath,
                                                        String[] structFields, String[] fields,
                                                        Boolean isCreateTable) {
        if (!isExitsTable(tableName) && !isCreateTable) {
            System.out.println("table is not exists");
            System.exit(1);
        }

        if (isCreateTable) {
            try {
                connection.createStatement().execute(GenerateTableSql(tableName, structFields, fields, true));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        BatchInsert(inputPath);

        return false;
    }

    /**
     * 启动多个线程读取文件，将内容加入到inceptor
     * @inputPath 文件存放路径，本地路径，可以是文件也可以是目录
     * @return 是否执行成功
     */
    private Boolean BatchInsert(String inputPath) {
        filesQueue = getFilesList(new File(inputPath));
        for (int i = 0; i < Integer.parseInt(Constant.readFileThreadNum); i++) {
            workThread[i] = new WorkThread();
            workThread[i].start();
        }
        return true;
    }

    /**
     * 回去文件列表
     * @srcFile 文件对象
     */
    private static List<File> getFilesList(File srcFile) {
        List<File> fileList = new LinkedList<File>();
        if (srcFile.isDirectory()) {
            fileList.addAll(getFilesList(srcFile));
        }
        if (srcFile.isFile()) {
            fileList.add(srcFile);
        }
        return fileList;
    }

    /**
     * 判断给定的表名臣在数据库中是否存在
     * @tableName 表名称
     */
    private static Boolean isExitsTable(String tableName) {

        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet result = meta.getTables(null, null, tableName, null);
            if (result.next())
                return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 工作进程
    private class WorkThread extends Thread {
        // 该工作线程是否有效，用于结束该工作线程
        private boolean isRunning = true;

        // 关键所在，如果任务队列不空，则取出任务执行，若任务队列空，则等待
        @Override
        public void run() {
            File file;
            while (isRunning) {// 注意，若线程无效则自然结束run方法
                synchronized (filesQueue) {
                    while (isRunning && filesQueue.isEmpty()) {// 队列为空
                        try {
                            filesQueue.wait(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!filesQueue.isEmpty())
                        readFileByLine(filesQueue.remove(0));// 取出任务
                }
            }
        }

        /*
         * 按照行读取文件
         * flie:需要读取的文件
         */
        public void readFileByLine(File file) {

            BufferedReader reader = null;
            Connection con = null;
            try {
                con = connectorPools.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement(insertSql);
                System.out.println("以行为单位读取文件内容，一次读一整行：");
                reader = new BufferedReader(new FileReader(file));
                String tempString = null;
                String[] fields = null;
                int line = 1;
                // 一次读入一行，直到读入null为文件结束
                while ((tempString = reader.readLine()) != null) {
                    // 显示行号
                    System.out.println("line " + line + ": " + tempString);
                    line++;
                    fields = tempString.split(Constant.fileSeparator);
                    for (int i = 0; i < Integer.parseInt(Constant.fieldsNum); i++) {
                        preparedStatement.setString(1, fields[i]);
                    }
                    preparedStatement.addBatch();

                    if (line % Integer.parseInt(Constant.batchSize) == 0) {
                        preparedStatement.executeBatch();
                    }
                }
                preparedStatement.executeBatch();

                reader.close();
                connectorPools.backConnection(con);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                    }
                }
                if(con != null){
                    connectorPools.backConnection(con);
                }
            }
        }

        // 停止工作，让该线程自然执行完run方法，自然结束
        public void stopWorker() {
            isRunning = false;
        }
    }

    // 执行一条有返回值的sql
    public ResultSet Select(String sql) {
        try {
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
