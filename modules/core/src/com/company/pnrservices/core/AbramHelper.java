package com.company.pnrservices.core;


import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AbramHelper {

    public static byte[] clearLast0(byte[] b) {
        for (int i = b.length - 1; i >= 0; i--) {
            if (b[i] != 0x00) {
                return Arrays.copyOfRange(b, 0, i + 1);
            }
        }
        return new byte[] {};
    }

    public static byte[] getBeforeAA(byte[] b) {
        for (int i = b.length - 1; i >= 0; i--) {
            if (b[i] == (byte) 0xAA && i > 0) {
                return Arrays.copyOfRange(b, 0, i);
            }
        }
        return b;
    }

    public static byte[] getBeforeTwoAA(byte[] b) {
        for (int i = 0;  i <= b.length - 1; i++) {
            if (b[i] == (byte) 0xAA && i > 0) {
                return Arrays.copyOfRange(b, 0, i);
            }
        }
        return b;
    }

    public static byte[] clearBeforeAA(byte[] b) {
        if (b.length > 0) {
            if (b[0] != (byte) 0xAA) {
                for (int i = 0; i <= b.length - 1; i++) {
                    if (b[i] == (byte) 0xAA && i > 0) {
                        return Arrays.copyOfRange(b, i, b.length);
                    }
                }
            }
        }
        return b;
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

    public static String deltaTimeFormat(LocalTime startTime, LocalTime endTime) {
        return formatDuration(Duration.between(endTime, startTime));
    }

    public static String formatDuration(Duration duration) {
        return String.format("%02d sec",
                Math.abs(duration.getSeconds()));
    }

//    public static String deltaDate(Date date1, Date date2) {
//        Duration d = Duration.between(date2.toInstant(), date1.toInstant());
//        return String.format("%1$2d:%2$2d:%3$2d.%4$3d", d.toHoursPart(), d.toMinutesPart(), d.toSecondsPart(), d.toMillisPart());
//    }

    public static boolean validReply(byte[] b) {
        boolean ret = false;
        if (b != null) {
            if (b.length > 0)
            if (b[0] == (byte) 0xAA) {
                b = getBeforeTwoAA(clearBeforeAA(clearLast0(b)));
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
