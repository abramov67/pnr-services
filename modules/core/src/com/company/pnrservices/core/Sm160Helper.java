package com.company.pnrservices.core;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import com.company.pnrservices.core.Sm160Helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.apache.poi.util.HexDump.byteToHex;

@Component("pnrservices_Sm160Helper")
public class Sm160Helper {

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

//    public static byte[] hexStringToByteArray(String s) {
//        int len = s.length();
//        byte[] data = new byte[len / 2];
//        for (int i = 0; i < len; i += 2) {
//            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//                    + Character.digit(s.charAt(i+1), 16));
//        }
//        return data;
//    }

    public static class MapCheckSm160Sim {
        public String equip_number;
        public UUID ne_id;
        public String sim_ip;
        public UUID sim_id;
        public String sim_type;
        public String res_name;
        public Integer tp_number;
        public String mac;
        public String network_pan_id = null;
        public Integer channel_num = null;
    }

    public static class WorkSm160Thread extends Thread {
        UUID neq_id;
        List<MapCheckSm160Sim> ip_map;
        CheckInfo ci;

        public WorkSm160Thread(UUID p_neq_id, List<MapCheckSm160Sim> p_ip_map) {
            this.neq_id = p_neq_id;
            this.ip_map = p_ip_map;
        }

        @Override
        public void run() {
            super.run();
            work_sm160();
        }


        static public class CheckInfo {
            public String ip;
            public UUID neq_id;
            public String mac;
            public String network_pan_id;
            public int channel_num;
            public String chanel; //1 байт
            public String panId; //2 байта
            public String extPanId; //8 байт
            public boolean permitToJoin; //1 байт
            public byte[] data; //с АА и CRC гна конце
            public byte[] clrData; //без АА и CRC гна конце
            public int crcCalc; // 1 байт
            public int crcReply; // 1 байт
            public boolean analyse = false;

            public CheckInfo(byte[] bytes, String ip, UUID neq_id, String mac, String network_pan_id, int channel_num) {
                this.ip = ip;
                this.neq_id = neq_id;
                this.data = bytes;
                this.mac = mac;
                this.network_pan_id = network_pan_id;
                this.channel_num = channel_num;
            }

            public void parse() {
                this.chanel = byteToHex(data[1]).substring(2);
                this.panId = bytesToHex(ArrayUtils.toPrimitive(ArrayUtils.addAll(ArrayUtils.toArray(data[3], data[2]))));
                this.extPanId = setMAC();
                this.permitToJoin = setPermitToJoin();
                this.clrData = setClearData();
                setCRC();
                setAnalyse();
            }

            private String setMAC() {
                String res = "";
                String tmp = bytesToHex(data);
                try {
                    int ind = tmp.indexOf("F0D00");
                    if (ind > 10) {
                        res = tmp.substring(ind - 11, ind + 5);
                    }
                } catch (Exception e) {
                    System.out.println("!!!tmp = " + tmp);
                    e.printStackTrace();
                }
                return res;
            }

            private boolean setPermitToJoin() {
                boolean res = false;
                if (data[15] != (byte) 0 ) {
                    res = true;
                }
                return res;
            }

            private byte[] setClearData() {
                return clearDoubleAA(clearBytes(data));
            }

            private void setCRC() {
                byte[] clrEnd = ArrayUtils.subarray(clrData, 1, clrData.length - 1);
                if(clrData.length > 0) {
                    crcReply = clrData[clrData.length - 1];
                    crcCalc = getCRC(clrEnd);
                }
            }

            public boolean setAnalyse() {
                boolean res = false;
                if (crcReply == crcCalc) {
                    if (
                            chanel.length() > 0
                            &&
                            panId.length() > 0
                            &&
                            extPanId.length() > 0
                            //&& permitToJoin

                    ) {
                        analyse = true;
                    }
                }

                return res;
            }

        } //CheckInfo

        public void work_sm160() {
            int port = 10001;
           for (Iterator<MapCheckSm160Sim> it = ip_map.iterator(); it.hasNext(); ) {
                MapCheckSm160Sim sim = it.next();
                //boolean res = sm160_manage(sim.sim_ip, neq_id, sim.equip_number, sim.ne_id, sim.res_name, sim.tp_number);
                boolean res = checkInfo(sim.sim_ip, port, neq_id, sim.mac, sim.network_pan_id, sim.channel_num);
                if (!res) {//если не получили network_info, переходим ко второй sim карте, если она есть
                    continue;
                } else {

                    break;
                }
            }
        }

//        private boolean sm160_manage(String ip_addr, UUID neq_id, String equip_number, UUID ne_id, String res_name, Integer tp_number) {
//            int port = 10001;
//            return checkInfo(ip_addr, port, neq_id);
//            if (sm160.sm160_opros.replyString.equals("")) return false; else return true;

//            result = execute_sm160(is_discover=False,
//                    attempts=5,
//                    att_discover=0, sm160=sm160, neq_id=neq_id)
//            if not result:
//            return False
//    //# если получили network_info то запускаем discover
//            execute_sm160(is_discover=True, attempts=5,
//                    att_discover=3, sm160=sm160, neq_id=neq_id)
//            return True
//        }

        public boolean checkInfo(String ip, int port, UUID neq_id, String mac, String network_pan_id, int channel_num) {
            DataInputStream in;
            DataOutputStream out;
            byte[] bytes;
            Socket socket = null;
            try
             {

                 SocketAddress socketAddress = new InetSocketAddress(ip, port);
                 socket = connect(socketAddress, 3);
                if (socket.isConnected()) {
                    in = new DataInputStream(socket.getInputStream());
                    out = new DataOutputStream(socket.getOutputStream());
                    byte[] cmd = getCommand();
                    out.write(cmd);
                    out.flush();
                    bytes = readReply09(out, in, 40);
                    if (bytes != null && bytes.length > 0) {
                        byte[] clrBytes = clearBytes(bytes);
                        clrBytes = clearDoubleAA(clrBytes);
                        boolean ret = checkCRC(clrBytes);
                        if (ret) {

                            ci = new CheckInfo(bytes, ip, neq_id, mac, network_pan_id, channel_num);
                            ci.parse();
                            ci.setAnalyse();
                            System.out.println("!!!end wait ip = " + ci.ip
                                    + ", neq_id = " + ci.neq_id
                                    + ", channel = " + ci.chanel
                                    + ", yoda_channel_num = " + ci.channel_num
                                    + ", panId = " + ci.panId
                                    + ", yoda_panId = " + ci.network_pan_id
                                    + ", extPanId = " + ci.extPanId
                                    + ", yoda_mac = " + ci.mac
                                    + ", permitToJoin = " + ci.permitToJoin
                                    + ", ret = " + ret
                                    + ", crcCalc = " + byteToHex(ci.crcCalc)
                                    + ", crcReply = " + byteToHex(ci.crcReply)
                                    + "\n, replyBytes = " + bytesToHex(ci.data)
                                    + "\n, clrBytes = " + bytesToHex(ci.clrData)
                            );
                            System.out.println("!!!------------------------------------------------");
                            if (ci.analyse) {
                                finalUpdate();
                            }
                        }
                        return ret;
                    } else {
                        System.out.println("!!!No reply from ip = " + ip);
                        System.out.println("!!!------------------------------------------------");
                    }
                }

                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            finally {
                    try {
                        socket.close();
                    } catch (IOException ignored){}
                }

        }

        private void finalUpdate() {

        };

        private static Socket connect(SocketAddress sa, int conCnt) throws InterruptedException {
            boolean res = false;
            int i = 0;
            Socket socket = null;
            while (!res && i < conCnt) {
                try {
                    socket = new Socket();
                    socket.connect(sa, 10000);
                    res = true;
                } catch (Exception e) {
                    System.out.println("!!!Ошибка i = " + i + ", ip = " + ((InetSocketAddress) sa).getAddress() + ", message = " + e.getMessage());
                    i++;
                    sleep(500);
                }
            }
            return socket;
        }

        private static byte[] clearBytes(byte[] bytes) {
            int i = bytes.length - 1;
            while (bytes[i] == (byte) 0) {
                i = i - 1;
                if (i == 0 ) break;
            }
            bytes = trimEnd(bytes, i);
            return bytes;
        }

        private static byte[] trimEnd(byte[] b, int newLength) {
            b = Arrays.copyOf(b, newLength + 1);
            return b;
        }

        private static byte[] clearDoubleAA(byte[] b) {
            int i = ArrayUtils.indexOf(b, (byte) 0xAA, 1);
            if (i > -1) {
                return ArrayUtils.subarray(b, 0, i);
            }
            return b;
        }

        private static boolean checkCRC(byte[] bytes) {
            boolean ret = false;
            if (bytes.length >= 3) {
                try {
                    byte crcReply = bytes[bytes.length - 1];
                    byte[] bytesPrepared = ArrayUtils.subarray(bytes, 1, bytes.length - 1);
                    byte crcCalc = getCRC(bytesPrepared);
                    if (crcReply == crcCalc) {
                        ret = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return ret;
        }

        private static byte[] readReply09(DataOutputStream out, DataInputStream in, int cntRead) throws Exception {
            byte[] b = new byte[cntRead];
            boolean run = true;
            int runCnt = 0;
            int cnt;
            while (run && runCnt < 5) {
                if (in.available() > 0) {
                    in.read(b);
                    if (b[0] != (byte) 0xAA) {
                        return null;
                    }
                        run = false;
                } else {
                    sleep(1000);
                    runCnt++;
                }
            }
            return b;
        }

        private static byte[] getCommand() {
            return new byte[]{(byte) 0xAA, 0x01, 0x09, 0x00, (byte) 0xB5};
        }

        private static byte getCRC(byte[] b) {
            CRC8 crc = new CRC8();
            crc.reset();
            crc.update(b);
            return (byte) crc.getValue();
        }

    } //WorkSm160Thread


//    private byte[] getCommandCheckMac() {
//        return new byte[]{(byte) 0xAA, 0x01, 0x19, 0x08,
//                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
//                (byte) 0x3D};
//    }
//
//    public long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
//        long diffInMillies = date2.getTime() - date1.getTime();
//        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
//    }
//
//        private byte[] readReply(DataOutputStream out, DataInputStream in) throws IOException {
//            final byte aa = (byte) 170;
//            byte[] b = new byte[1];
//            List<Byte> list = new ArrayList<>();
//            int cnt;
//            while (true){
//                cnt = (in.read(b));
//                if (new String(b) == "") {
//                    out.write((byte)0x00);
//                    out.flush();
//                    System.out.println("!!! Send 0x00");
//                } else {
//                    if (b[0] == aa) {
//                        break;
//                    }
//                    list.add(b[0]);
//                }
//            }
//            return Bytes.toArray(list);
//        }

}
