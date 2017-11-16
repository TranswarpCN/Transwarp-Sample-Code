package io.transwarp.kafkaDecoder;

import io.transwarp.streaming.sql.api.decoder.ArrayDecoder;
import kafka.utils.VerifiableProperties;

import java.util.Date;

/* kafka里的测试数据,倒数第三列为状态字段，倒数第二为日期字段，倒数第一为时间字段
 * 1,1,1,1,t1,t2,A,1451577600,0
 * 1,1,1,1,t1,t2,B,1451577600,1
 * 1,1,1,1,t1,t2,A,1451577600,2
 */
public class TimeFormatDecoder extends ArrayDecoder<byte[]> {
    public TimeFormatDecoder(VerifiableProperties properties) {
        super(properties);
    }

    /*
     * 在arrayFromBytes方法里实现decoder的逻辑。注意点：
     * 1.输入类型为byte[]，输出类型byte[][]
     * 2.确保，输入kafka的数据符合该方法的解析格式；输出对应create stream指定的格式
     */
    @Override
    public byte[][] arrayFromBytes(byte[] msgBytes) {
        String msgString = new String(msgBytes);
        StringBuilder stringBuilder = new StringBuilder();
        String sep = ",";
        try {
            String msgArray[] = msgString.split(sep);
            int column_length = msgArray.length;
            int dateValue = Integer.parseInt(msgArray[column_length - 2]);
            int timeValue = Integer.parseInt(msgArray[column_length - 1]);
            long timestamp = dateValue + timeValue;
            Date ftime = new java.sql.Date(timestamp);

            //状态字段映射到数量值
            char statusChar = msgArray[column_length - 3].charAt(0);
            int statusValue;
            switch(statusChar) {
                case 'A' :
                    statusValue = 1;
                    break;
                case 'B' :
                    statusValue = 2;
                    break;
                default :
                    statusValue = 0;
            }

            for (int i = 0; i < column_length; i++) {
                //注意字段之间有分隔符
                stringBuilder.append(msgArray[i] + sep);
            }
            stringBuilder.append(statusValue + sep);
            stringBuilder.append(ftime);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return new byte[][] {
                stringBuilder.toString().getBytes()
        };
    }
}
