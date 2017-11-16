package io.transwarp.objectstore;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import static io.transwarp.objectstore.MD5Util.md5crypt;

public class DownloadData {
    private Connector connector;
    private Configuration configuration;
    private Constant constant;
    private MD5Util md5Util;

    // 构造函数
    public DownloadData() {
        connector = new Connector();
        constant = new Constant();
        configuration = connector.getConfiguration();
    }

    // 获取文件
    public void getFile(String fileName) {
        try {
            HTable hTable = new HTable(configuration, constant.HBASE_TABLE_NAME);
            String fileNameMD5 = md5crypt(fileName);

            String result_filename;
            byte[] result_filedata;
            int result_fileCount=0;

            byte [] startRowKey = Bytes.toBytes(fileNameMD5);
            byte [] stopRowKey = Bytes.toBytes(fileNameMD5+"9");

            Scan scan = new Scan();
            scan.setStartRow(startRowKey);
            scan.setStopRow(stopRowKey);
            ResultScanner scanner = hTable.getScanner(scan);
            for (Result r : scanner) {
                result_filename = Bytes.toString(r.getValue(Bytes.toBytes("file"), Bytes.toBytes("filename")));
                //for_windows filename  linux注释下边两行
                String[] filename_array=fileName.split("\\\\");
                String targetfileName=filename_array[filename_array.length-1];
                //-------
                if (fileName.compareTo(result_filename) == 0){
                    result_fileCount++;
                    result_filedata = r.getValue(Bytes.toBytes("data"), Bytes.toBytes("bytes"));
                    //下面方法中的变量：linux-->fileName,windows-->targetfileName
                    FileUtil.byte2File(result_filedata, constant.DOWNLOAD_DIR, targetfileName);
                }
            }
            if(result_fileCount != 0)
                System.out.println("Download "+ result_fileCount + " files");
            else
                System.out.println("Not found:"+fileName);

            hTable.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 关闭连接
    public void disConnect() {
        connector.close();
    }

    // 主函数
    public static void main(String[] args) {
        DownloadData downloadData = new DownloadData();
        // 下载时，更改文件名
        downloadData.getFile("D:\\tanliming\\Transwarp-Sample-Code\\ObjectStore\\src\\main\\resources\\picture\\beizhai.jpg");
        downloadData.disConnect();
    }
}
