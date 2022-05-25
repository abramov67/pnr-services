package com.company.pnrservices.core;

import com.sun.mail.iap.ByteArray;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.company.pnrservices.core.AbramHelper.*;
import static com.company.pnrservices.core.YodaRESTMethodsHelper.updaterForSM160REST;
import static java.lang.Thread.sleep;

public class MeterGSM {

    UUID id;
    String ip;
    int port;
    String TOKEN;
    String equipNumber;

    Socket socket;
    SocketAddress socketAddress;
    DataInputStream in;
    DataOutputStream out;
    int connectTimeOut = 60000;

    //Ответы
    byte[] replyGetMAC = null;
    byte[] replyCheckInfo = null;
    byte[] replyToDiscover = null;

    //Результаты обработки ответов и соединения
    public Date lastBootConnection = null;
    public Date lastActive = null;
    public String panID = null;
    public String networkPanID = null;
    public String channelNum = null;
    public Boolean isJoiningPermitted = null;
    public String versionNumber = null; //16
    public String boardVersion = null; //2
    public String bigVersionPO = null; //1
    public String smallVersionPO = null; //2
    public String optionNum = null; //1
    public String MAC = null;
    public List<String> toDiscoverMACList = new ArrayList();

    int index;
    int size;

    public MeterGSM(int index, int size, String ip, int port, String token, UUID id, String equipNumber) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.socketAddress = new InetSocketAddress(this.ip, this.port);
        this.TOKEN = token;
        this.index = index;
        this.size = size;
        this.equipNumber = equipNumber;
    }

    public void setResult() throws IOException {
        if (connect(2)) {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            lastBootConnection = new Date();
            if (checkInfo()) {
                channelNum = bytesToHex(ArrayUtils.toPrimitive(ArrayUtils.toArray(replyCheckInfo[4])));
                panID = bytesToHex(ArrayUtils.toPrimitive(ArrayUtils.addAll(
                        ArrayUtils.toArray(replyCheckInfo[6], replyCheckInfo[5])
                )));
                networkPanID = bytesToHex(ArrayUtils.toPrimitive(ArrayUtils.addAll(
                        ArrayUtils.toArray(
                                replyCheckInfo[7],
                                replyCheckInfo[8],
                                replyCheckInfo[9],
                                replyCheckInfo[10],
                                replyCheckInfo[11],
                                replyCheckInfo[12],
                                replyCheckInfo[13],
                                replyCheckInfo[14]
                        )
                )));
                isJoiningPermitted = setPermitToJoin();
                if (getMAC()) {
                    setMAC();
                    setVersion();
                }
            }
            if (toDiscover()) {
                lastActive = new Date();


            }
            saveToYodaREST();
        }
    }

    private void saveToYodaREST() {
        JSONObject jsn = new JSONObject();
        jsn.put("id", id.toString());
        jsn.put("ip", ip);
        jsn.put("lastBotConnection", dateTimeFormat(lastBootConnection));
        jsn.put("lastActive", dateTimeFormat(lastActive));
        jsn.put("panID", isnull(panID).toString());
        jsn.put("networkPanID", isnull(networkPanID).toString());
        jsn.put("channelNum", isnull(channelNum).toString());
        jsn.put("isJoiningPermitted", isnull(isJoiningPermitted).toString());
        jsn.put("versionNumber", "null");//isnull(versionNumber).toString()); пока закомментил, не понятный формат
        jsn.put("boardVersion", isnull(boardVersion).toString());
        jsn.put("bigVersionPO", isnull(bigVersionPO).toString());
        jsn.put("smallVersionPO", isnull(smallVersionPO).toString());
        jsn.put("optionNum", isnull(optionNum).toString());
        jsn.put("MAC", isnull(MAC).toString());
        jsn.put("toDiscoverMAC", toDiscoverMACList);
        jsn.put("option", "sm160");
        jsn.put("equipNumber", equipNumber);

        updaterForSM160REST(jsn.toString(), TOKEN);
    }

    private Object isnull(Object value) {
      if (value == null) return JSONObject.NULL; else return value;
    };


    private void setVersion() {
        String tmp = bytesToHex(replyGetMAC);
        try {
            byte[] sub = getByteSubArray(replyGetMAC, 12, 16);
            versionNumber = new String(sub, StandardCharsets.UTF_8);
            boardVersion = bytesToHex(getBoardVersion(replyGetMAC, 28, 2));
            bigVersionPO = bytesToHex(getBigVersionPO(replyGetMAC, 30, 1));
            smallVersionPO = bytesToHex(getSmallVersionPO(replyGetMAC, 31, 2));
            optionNum = bytesToHex(getOptionNum(replyGetMAC, 33, 1));
        } catch (Exception e) {
            System.out.println("!!!MeterGSM.setMac = " + tmp);
            e.printStackTrace();
        }
    }

    private byte[] getBoardVersion(byte[] array, int from, int count) {
        byte[] tmp = new ByteArray(array, from, count).getNewBytes();
        List<Byte> resultList = new ArrayList<>();
        byte[] resultBytes = null;
        for (int i = tmp.length - 1; i > 0; i--) {
            if ((tmp[i] != (byte) 0xFF)
                    && ((tmp[i] >= 0x20)
                    && (tmp[i] <= 0x7E))
            ) resultList.add(tmp[i]);
        }
        if (resultList.size() > 0) {
            resultBytes = new byte[resultList.size()];
            int i = 0;
            for (Byte b : resultList) {
                resultBytes[i] = b;
                i++;
            }
        }
        return resultBytes;
    }

    private byte[] getBigVersionPO(byte[] array, int from, int count) {
        byte[] tmp = new ByteArray(array, from, count).getNewBytes();
        List<Byte> resultList = new ArrayList<>();
        byte[] resultBytes = null;
        for (byte b : tmp) {
            if (b != (byte) 0xFF) resultList.add(b);
        }
        if (resultList.size()>0) {
            resultBytes = new byte[resultList.size()];
            int i = 0;
            for (Byte b : resultList) {
                resultBytes[i] = b;
                i++;
            }
        }
        return resultBytes;
    }

    private byte[] getSmallVersionPO(byte[] array, int from, int count) {
        byte[] tmp = new ByteArray(array, from, count).getNewBytes();
        if (tmp[1] != (byte) 0xFF && tmp[0] != (byte) 0xFF)
            return new byte[]{tmp[1], tmp[0]};
        else return null;
    }

    private byte[] getOptionNum(byte[] array, int from, int count) {
        return new ByteArray(array, from, count).getNewBytes();
    }

    private byte[] getByteSubArray(byte[] array, int from, int count) {
        byte[] tmp = new ByteArray(array, from, count).getNewBytes();
        List<Byte> resultList = new ArrayList<>();
        byte[] resultBytes = null;
        for (byte b : tmp) {
            if (b != (byte) 0xFF) resultList.add(b);
        }
        if (resultList.size()>0) {
            resultBytes = new byte[resultList.size()];
            int i = 0;
            for (Byte b : resultList) {
                resultBytes[i] = b;
                i++;
            }
        }
        return resultBytes;
    }

    private String setMAC() {
        String res = "";
        String tmp = bytesToHex(replyGetMAC);
        try {
            int ind = tmp.indexOf("F0D00");
            if (ind > 10) {
                MAC = tmp.substring(ind - 11, ind + 5);
            }
        } catch (Exception e) {
            System.out.println("!!!MeterGSM.setMac = " + tmp);
            e.printStackTrace();
        }
        return res;
    }

    private String extractMAC(byte[] b) {
        String res = "";
        String tmp = bytesToHex(b);
        try {
            int ind = tmp.indexOf("F0D00");
            if (ind > 10) {
                res = tmp.substring(ind - 11, ind + 5);
            }
        } catch (Exception e) {

        }
        return res;
    }

    private boolean setPermitToJoin() {
        boolean res = false;
        if (replyCheckInfo[15] != (byte) 0 ) {
            res = true;
        }
        return res;
    }

    public boolean toDiscover() {
        boolean ret = false;
        int cnt = 5;
        try {
            if (socket.isConnected()) {
                byte[] cmd = getCommand(7);
                out.write(cmd);
                out.flush();
                while (cnt > 0) {
                    replyToDiscover = readReply(16);
                    if (validReply(replyToDiscover))
                        toDiscoverMACList.add(extractMAC(replyToDiscover));
                    sleep(1000);
                    cnt--;
                }
            }
        } catch (Exception e) {
            System.out.println("!!!toDiscover exception: "+e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }

    public boolean checkInfo() {
        boolean ret = false;
        try
        {
            if (socket.isConnected()) {
                byte[] cmd = getCommand(9);
                out.write(cmd);
                out.flush();
                replyCheckInfo = readReply(17);
                ret = validReply(replyCheckInfo);
            }
        } catch (Exception e) {
            System.out.println("!!!checkInfo exception: "+e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }

    public boolean getMAC() {
        boolean ret = false;
        try
        {
            if (socket.isConnected()) {
                byte[] cmd = getCommand(19);
                out.write(cmd);
                out.flush();
                replyGetMAC = readReply( 35);
                ret = validReply(replyGetMAC);
            }
        } catch (Exception e) {
            System.out.println("!!!checkInfo exception: "+e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }

    private boolean connect(int conCnt) {
        boolean res = false;
        int i = 0;
        while (!res && i < conCnt) {
            try {
                socket = new Socket();
                socket.connect(socketAddress, connectTimeOut);
                res = true;
            } catch (Exception e) {
                //e.printStackTrace();
                //System.out.println("!!!MeterGSM.connect Ошибка i = " + i + ", ip = " + ((InetSocketAddress) socketAddress).getAddress() + ", message = " + e.getMessage());
                //i++;
                try { sleep(1000);}catch (InterruptedException ignored){}
            }
        }
        return res;
    }

    private byte[] readReply(int cntRead) throws IOException {
        byte[] b = new byte[cntRead];
        boolean run = true;
        int runCnt = 0;
        while (run && runCnt < 5) {
            try {
                sleep(1000);
            } catch (InterruptedException ignored) {
            }
            runCnt++;
            if (in.available() > 0) {
                in.read(b);
                if (b[0] == (byte) 0xAA) {
                    break;
                }
            }
        }
        return b;
    }

    private byte[] getCommand(int type) {
        byte[] ret = null;
        switch (type) {
            case(0): {
                ret = new byte[]{0x01, 0x00, 0x00};
                break;
            }
            case(7): {
                ret = new byte[]{0x01, 0x07, 0x00};
                break;
            }
            case(9): {
                ret = new byte[]{0x01, 0x09, 0x00};
                break;
            }
            case(19): {
                ret = new byte[]{0x01, 0x19, 0x08, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
                        (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
                break;
            }
        }
        return setAA(setCRC(ret));
    }

    private byte[] setAA(byte[] b) {
        ByteBuffer buf = ByteBuffer.wrap(new byte[b.length + 1]);
        buf.position(0);
        buf.put((byte) 0xAA);
        buf.position(1);
        buf.put(b);
        byte[] ret = buf.array();
        return ret;
    }

    private byte[] setCRC(byte[] b) {
        CRC8 crc = new CRC8();
        crc.reset();
        crc.update(b);
        byte[] ret = Arrays.copyOf(b, b.length + 1);
        ret[ret.length - 1] = (byte) crc.getValue();
        return ret;
    }

}
