package io.transwarp.kafkaDecoder;

import io.transwarp.streaming.sql.api.decoder.ArrayDecoder;
import kafka.utils.VerifiableProperties;






//这个decoder实现的逻辑是针对传过来的keyvalue键值对拆分取value
//key,value;key,value;key,value;key,value;这样的格式

public class KeyValueDecoder extends ArrayDecoder<byte[]> {
    public KeyValueDecoder(VerifiableProperties properties){
        super(properties);
    }


    @Override
    public byte[][] arrayFromBytes(byte[] msgBytes) {
        System.out.println("starteddecoder");
        String msgString = new String(msgBytes);
        StringBuilder stringBuilder = new StringBuilder();
        String sep = ";";
        try {
            String msgArray[] = msgString.split(sep);
            int column_length = msgArray.length;

            for (int i = 0; i < column_length; i++) {
                //注意字段之间有分隔符
                stringBuilder.append(msgArray[i].split(",")[1] + sep);
            }
            //System.out.println(stringBuilder.toString());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return new byte[][] {
                stringBuilder.toString().getBytes()
        };
    }

    public static void main(String args[]){
        System.out.println(
                new KeyValueDecoder(new VerifiableProperties()).
                        arrayFromBytes("key,value1;key,value2;key,value3;key,value".getBytes())
        );

    }
}
