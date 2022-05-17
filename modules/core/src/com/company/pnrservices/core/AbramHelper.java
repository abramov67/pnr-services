package com.company.pnrservices.core;


import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class AbramHelper {

    public static byte[] clearLast(byte[] b) {
        for (int i = b.length - 1; i >= 0; i--) {
            if (b[i] != 0x00) {
               // System.out.println("!!!before clear = "+bytesToHex(b)+", after clear = "+bytesToHex(Arrays.copyOfRange(b, 0, i + 1)));
                return Arrays.copyOfRange(b, 0, i + 1);
            }
        }
        return new byte[] {};
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return null;
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String dateTimeFormat(Date tm) {
        if (tm == null) return "null";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return new SimpleDateFormat(pattern).format(tm);
    }

    public static boolean validReply(byte[] b) {
        boolean ret = false;
        if (b != null) {
            if (b.length > 0)
            if (b[0] == (byte) 0xAA) {
                ret = isCRC(b);
            }
        }
        return ret;
    }

    private static boolean isCRC(byte[] b) {
        boolean ret = false;
        if (b != null) {
            byte crc1 = getCRC(Arrays.copyOfRange(b, 1, b.length -1));
            byte crc2 = b[b.length - 1];
            ret = byteCompare(crc1, crc2);
        }
        return ret;
    }

    private static boolean byteCompare(byte b1, byte b2) {
        return b1 == b2;
    }

    private static byte getCRC(byte[] b) {
        CRC8 crc = new CRC8();
        crc.reset();
        crc.update(b);
        return (byte) crc.getValue();
    }

}
