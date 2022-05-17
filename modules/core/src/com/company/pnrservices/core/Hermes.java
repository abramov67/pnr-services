package com.company.pnrservices.core;

import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Deque;

import static com.company.pnrservices.core.YodaRESTMethodsHelper.getHermesParamsREST;
import static java.lang.Thread.sleep;

public class Hermes {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(HermesShell.class);

    String hermes_id;
    String hermes_ip, login, password;
    int port;
    Socket socket;
    SocketAddress socketAddress;
    DataInputStream in;
    DataOutputStream out;
    int connectTimeOut = 60000;
    Deque<byte[]> deque;



    public Hermes(String hermes_id, String token) throws IOException {
        JSONObject obj = getHermesParamsREST(hermes_id, token);
        this.hermes_ip = obj.get("ip").toString();
        this.port = Integer.parseInt(obj.get("port").toString());
        this.login = obj.get("user").toString();
        this.password = obj.get("pwd").toString();
        this.hermes_id = hermes_id;
        this.socketAddress = new InetSocketAddress(hermes_ip, port);
        this.socket = connect(3);
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

    }

    private Socket connect(int conCnt) {
        boolean res = false;
        int i = 0;
        Socket socket = null;
        while (!res && i < conCnt) {
            try {
                socket = new Socket();
                socket.connect(socketAddress, connectTimeOut);
                res = true;
            } catch (Exception e) {
                System.out.println("!!!Hermes.connect Ошибка i = " + i + ", ip = " + ((InetSocketAddress) socketAddress).getAddress() + ", message = " + e.getMessage());
                i++;
                try { sleep(1000);}catch (InterruptedException ignored){}
            }
        }
        return socket;
    }

    private byte[] readReply(int cntRead) throws IOException {
        byte[] b = new byte[cntRead];
        boolean run = true;
        int runCnt = 0;
        while (run && runCnt < 5) {
            if (in.available() > 0) {
                in.read(b);
//                if (b[0] != (byte) 0xAA) {
//                    return null;
//                }
                run = false;
            } else {
                try {sleep(1000);} catch(InterruptedException ignored){}
                runCnt++;
            }
        }
        return b;
    }


//    private byte[] readReply(int cntRead) throws IOException {
//        boolean run = true;
//        byte[] b;
//        int runCnt = 0;
//        while (run && runCnt < 5) {
//            if (in.available() > 0) {
//                while (!socket.isClosed()) {
//                    try {
//                        in.readFully(b);
//                    } catch (EOFException e) { //При эксепшене EOF(конец потока) закрываем его
//                        socket.close();
//                    }
////                if (b[0] != (byte) 0xAA) {
////                    return null;
////                }
//                run = false;
//            } else {
//                try {sleep(1000);} catch(InterruptedException ignored){}
//                runCnt++;
//            }
//        }
//        return b;
//    }


    private byte[] getCommand(int type) {
        byte[] ret = null;
        switch (type) {
            case(9): {
                ret = new byte[]{(byte) 0xAA, 0x01, 0x09, 0x00};
                break;
            }
        }
        return setCRC(ret);
    }

    private byte[] setCRC(byte[] b) {
        CRC8 crc = new CRC8();
        crc.reset();
        crc.update(b);
        byte[] ret = new byte[b.length];
        ret[ret.length-1] = (byte) crc.getValue();
        return ret;
    }

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

    public boolean checkInfo() {
        boolean ret = false;
        try
        {
            if (socket.isConnected()) {
                byte[] cmd = getCommand(9);
                out.write(cmd);
                out.flush();
                byte[] bytes = readReply( 40);
                System.out.println("!!!reply = "+bytesToHex(bytes));
                ret = true;
            } else {
                System.out.println("!!!Not connected to hermes_ip = " + hermes_ip);
            }
        } catch (Exception e) {
            System.out.println("!!!checkInfo exception: "+e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }

}
