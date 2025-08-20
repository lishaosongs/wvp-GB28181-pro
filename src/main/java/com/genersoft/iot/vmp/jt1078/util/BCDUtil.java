package com.genersoft.iot.vmp.jt1078.util;

/**
 * BCD码转换
 */
public class BCDUtil {

    public static String transform(byte[] bytes) {
        if (bytes.length == 0) {
            return null;
        }
        // BCD
        StringBuilder stringBuffer = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            // 每次取出四位的值，一个byte是八位，第一取出高四位，第二次取出低四位，
            // 这里也可以先 & 0xf0再右移4位，0xf0二进制为11110000，与运算后，可以得到高4位是值，低四位清零的结果
            stringBuffer.append((byte) ((bytes[i]  >>> 4 & 0xf)));
            stringBuffer.append((byte) (bytes[i] & 0x0f));
        }
        return stringBuffer.toString();
    }

    /**
     * 字符串转BCD码
     * 来自： https://www.cnblogs.com/ranandrun/p/BCD.html
     * @param asc ASCII字符串
     * @return BCD
     */
    public static byte[] strToBcd(String asc) {
        int len = asc.length();
        int mod = len % 2;
        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }
        byte abt[] = new byte[len];
        if (len >= 2) {
            len >>= 1;
        }
        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;
        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }
            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }
}
