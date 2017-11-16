package io.transwarp.inceptorudf;

import org.apache.hadoop.hive.ql.exec.UDF;

// Inceptor UDF 单行输入单行输出，主要解决的问题是单行数据的转换问题
public class InceptorUDF  extends UDF{
    // 将IP地址转换成数字表示
    public static long evaluate(String ipNum) {
        String[] arr = ipNum.split("\\.");
        if (arr.length == 4) {
            return Integer.parseInt(arr[0]) * 256 * 256 * 256l +
                    Integer.parseInt(arr[1]) * 256 * 256l +
                    Integer.parseInt(arr[2]) * 256l +
                    Integer.parseInt(arr[3]);
        } else {
            return 0l;
        }
    }
    public static void main (String args[]){
        InceptorUDF test=new InceptorUDF();
        System.out.print(test.evaluate("14.14.14.14"));
    }
}
