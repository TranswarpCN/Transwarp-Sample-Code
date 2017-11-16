package io.transwarp.objectstore;

import java.security.MessageDigest;

public class MD5Util {
    private static MessageDigest md5 = null;
    private static StringBuffer digestBuffer = null;

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
            digestBuffer = new StringBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // md5函数
    public static String md5crypt(String s) {
        System.out.println(s);
        digestBuffer.setLength(0);
        byte abyte0[] = md5.digest(s.getBytes());
        for (int i = 0; i < abyte0.length; i++)
            digestBuffer.append(toHex(abyte0[i]));

        return digestBuffer.toString();
    }

    // 辅助函数
    private static String toHex(byte one) {
        String HEX = "0123456789ABCDEF";
        char[] result = new char[2];
        result[0] = HEX.charAt((one & 0xf0) >> 4);
        result[1] = HEX.charAt(one & 0x0f);
        return new String(result);
    }
}
