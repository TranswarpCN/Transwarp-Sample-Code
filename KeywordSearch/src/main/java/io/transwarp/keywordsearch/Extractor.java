package io.transwarp.keywordsearch;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.*;

public class Extractor {
    // 获取文件夹
    private static File[] showFolder(String path) {
        File file = new File(path);
        return file.listFiles();
    }
    //tika的使用是自定义解析规则，这个可以根据自己的文档来定义自己的解析规则，这里定义个基础版本的
    // pdf, word 转化为 txt，输出格式为完整路径名 | 本文档第几段文本 | 本文档本段文本
    private static void extract(String curPath, String fileName) {
        InputStream is = null;

        try {
            is = new BufferedInputStream(new FileInputStream(new File(fileName)));
            Parser parser = new AutoDetectParser();
            ContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            parser.parse(is, handler, metadata, new ParseContext());

            String s = handler.toString();
            s = s.replace("\t","\uF06C");
            s = s.replace(" "," \uF06C");
            s = s.replace("\n","\uF06C");
            s = s.replace("\r","\uF06C");
            s = s.replace("|","\uF06C");
            //s = s.replace("\uF06C"," ");
            s = s.replace("\uF0D8","\uF06C");
            s = s.replace("\uF020","\uF06C");
            s = s.replace("\uF0B7","\uF06C");
            s = s.replace("..","..\uF06C");
            s = s.replace(",",",\uF06C");
            s = s.replace("，",",\uF06C");
            s = s.replace("。","。\uF06C");
            s = s.replace(";",";\uF06C");
            s = s.replace("；","；\uF06C");
            s = s.replace("“","“\uF06C");
            s = s.replace("”","”\uF06C");
            s = s.replace("\"","\"\uF06C");
            s = s.replace("：","：\uF06C");
            s = s.replace("）","）\uF06C");
            s = s.replace("（","（\uF06C");
            s = s.replace(")",")\uF06C");
            s = s.replace("(","(\uF06C");
            s = s.replace("[","[\uF06C");
            s = s.replace("]","]\uF06C");
            s = s.replace("【","【\uF06C");
            s = s.replace("】","】\uF06C");
            s = s.replace("{","{\uF06C");
            s = s.replace("}","}\uF06C");
            s = s.replace("、","、\uF06C");
            s = s.replace("->","->\uF06C");
            s = s.replace("--","--\uF06C");
            System.out.println(s.length());
            String[] arr = s.split("\uF06C");
            int n = arr.length;

            FileWriter fw = new FileWriter(curPath + "\\output.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            String str = "";
            int rownum = 0;
            for (int i = 0; i < n; ++i) {
                str = str.replace("   ","");
                if (str.length() + arr[i].length() > 150) {
                    rownum += 1;
                    bw.write(fileName + "|" + rownum + "|" + str  + "\r\n");
                    str = arr[i];
                } else {
                    str += arr[i];
                }
            }
            rownum += 1;
            bw.write(fileName + "|" + rownum + "|" + str + "\r\n");
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 文档转化主函数
    public static void main(String... args) {
        String curPath = Constant.CURRENT_PATH;
        String dirPath = Constant.DIRECTORY_PATH;
        File[] files = showFolder(curPath);
        for (File file : files) {
            System.out.print(file.toString());
            String f = file.toString();
            System.out.print(f.toString());
            extract(dirPath, f);
        }
    }
}
