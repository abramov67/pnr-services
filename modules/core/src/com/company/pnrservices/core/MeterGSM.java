package com.company.pnrservices.core;

import com.company.pnrservices.service.SM160LogInfoService;
import com.company.pnrservices.service.UpdateTopologyServiceBean;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.haulmont.cuba.core.global.AppBeans;
import com.sun.mail.iap.ByteArray;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.company.pnrservices.core.AbramHelper.*;
import static com.company.pnrservices.core.Sm160Helper.*;
import static com.company.pnrservices.core.YodaRESTMethodsHelper.*;
import static java.lang.Thread.sleep;

public class MeterGSM {
    private static final Logger log = LoggerFactory.getLogger(UpdateTopologyServiceBean.class);

    SM160LogInfoService sm160LogInfoService = AppBeans.get(SM160LogInfoService.class);


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
    public byte[] replyGetMAC = null;
    public byte[] replyCheckInfo = null;
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
    public List<String> discoverReplyBuffer = new ArrayList();
    public String resultStr = "";
    private Set<String> discoverMACs = new HashSet<>();
    private LocalTime startTime = LocalTime.now();
    private String logId;
    private Long discoverSec;

    private boolean saveToYoda;

    int index;
    int size;

    public boolean saveResult = true;
    public boolean forSingle;


    public MeterGSM(int index, int size, String ip, int port, String token, UUID id, String equipNumber, String logId, boolean saveToYoda, long discoverSec) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.socketAddress = new InetSocketAddress(this.ip, this.port);
        this.TOKEN = token;
        this.index = index;
        this.size = size;
        this.equipNumber = equipNumber;
        this.logId = logId;
        this.saveToYoda = saveToYoda;
        this.discoverSec = discoverSec;
    }

    public MeterGSM(int index, int size, String ip, int port, String equipNumber, String logId, boolean saveToYoda, String token, long discoverSec) {
        this.ip = ip;
        this.port = port;
        this.socketAddress = new InetSocketAddress(this.ip, this.port);
        this.index = index;
        this.size = size;
        this.equipNumber = equipNumber;
        this.logId = logId;
        this.saveToYoda = saveToYoda;
        this.TOKEN = token;
        this.discoverSec = discoverSec;
    }

    public void setResult() throws IOException {
        boolean cnt = connectNew2(2);
        if (cnt) {
            try {
                logSm160Operations(logId, "setResult start", null, null);
                lastBootConnection = new Date();
                if (checkInfoOpros(7)) {// checkInfo(5)) {
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
                    logSm160Operations(logId, "checkInfo result",
                            "channelNum = " + isNullStr(channelNum)
                                    + ", panID = " + isNullStr(panID)
                                    + ", networkPanID = " + isNullStr(networkPanID)
                                    + ", JoiningPermitted = " + isJoiningPermitted.toString(),
                            null);
                    sm160LogInfoService.upsertStoredProc(
                            equipNumber,
                            panID,
                            Integer.parseInt(channelNum, 16),
                            isJoiningPermitted
                    );
                }

                if (getMAC()) {
                    setMAC();
                    setVersion();
                    logSm160Operations(logId, "getMAC result",
                            "MAC = " + isNullStr(MAC)
                                    + ", \nversionNumber = " + isNullStr(versionNumber)
                                    + ", \nboardVersion = " + isNullStr(boardVersion)
                                    + ", \nbigVersionPO = " + isNullStr(bigVersionPO)
                                    + ", \nsmallVersionPO = " + isNullStr(smallVersionPO)
                                    + ", \noptionNum = " + isNullStr(optionNum),
                            null);

                }

                if (toDiscoverPart()) {
                    generateToDiscoverMACs();
                }

                if (discoverMACs.size() == 0) discoverMACs.add(equipNumber);

                saveToYodaREST();

            } finally {
                logSm160Operations(logId, "setResult end", null, null);
                disconnect(3);
                System.out.println(dateTimeFormat(new Date())+"/"+index+"/"+size+"/"+deltaTimeFormat(startTime, LocalTime.now())
                        +" !!!finally ip = "+ip+", macs.size = "+discoverMACs.size()+", MAC = "+MAC);
            }
        }
    }

    public boolean generateToDiscoverMACs() {
        resultStr = String.join("", discoverReplyBuffer);
//        parseDiscoverBuffer();
//        toDiscoverMACList.forEach(s ->
//        {
//            String mac = extractMACStr(s);
//            if (!mac.equals("")) {
//                discoverMACs.add(mac);
//            }
//        });

        Long cntF0D00 = extractF0D00macs();
        logSm160Operations(logId, "toDiscoverNew F0D00", "cnt = "+cntF0D00, null);
        Long cntE1E00 = extractE1E00macs();
        logSm160Operations(logId, "toDiscoverNew E1E00", "cnt = "+cntE1E00, null);
        Long cntFEFF = extractFEFFmacs();
        logSm160Operations(logId, "toDiscoverNew FEFF", "cnt = "+cntFEFF, null);

        lastActive = new Date();

        discoverMACs.forEach(mac -> {
            logSm160Discovery(logId, mac, null);
            logSm160Operations(logId, "toDiscoverNew mac", mac, null);
        });
        return true;
    }

    public void parseDiscoverBuffer() {
        toDiscoverMACList = Arrays.asList(resultStr.split("AA01"));
    }

    private Long extractF0D00macs() {
        int ret = 0;
        String findSubstr = "F0D00";
        int indFrom = 11;
        int indTo = 5;
        int ind = resultStr.indexOf(findSubstr);
        while ( ((ind - indFrom) >= 0) ) {
            discoverMACs.add(resultStr.substring(ind - indFrom, ind + indTo));
            ind = resultStr.indexOf(findSubstr, ind + indTo - 1);
            if (ind >= 0) ret++;
        }
        return discoverMACs.stream().filter(mac -> mac.contains(findSubstr)).count();
    }

    private Long extractE1E00macs() {
        int ret = 0;
        String findSubstr = "E1E00";
        int indFrom = 11;
        int indTo = 5;
        int ind = resultStr.indexOf(findSubstr);
        while ( ((ind - indFrom) >= 0) ) {
            discoverMACs.add(resultStr.substring(ind - indFrom, ind + indTo));
            ind = resultStr.indexOf(findSubstr, ind + indTo - 1);
            if (ind >= 0) ret++;

        }
        return discoverMACs.stream().filter(mac -> mac.contains(findSubstr)).count();
    }

    private Long extractFEFFmacs() {
        int ret = 0;
        String findSubstr = "FEFF";
        int indFrom = 6;
        int indTo = 4 + 6;
        int ind = resultStr.indexOf(findSubstr);
        while ( ((ind - indFrom) >= 0) && ((indTo + ind) <= (resultStr.length() - 1))) {
            discoverMACs.add(resultStr.substring(ind - indFrom, ind + indTo));
            ind = resultStr.indexOf(findSubstr, ind + indTo - 1);
            if (ind >= 0) ret++;
        }
        return discoverMACs.stream().filter(mac -> mac.contains(findSubstr)).count();
    }

    public String isNullStr(String value) {
        return value == null ? "null" : value;
    }

    public JSONObject saveToYodaREST() {
        logSm160Operations(logId, "saveToYodaREST start",null, null);

        JSONObject jsn = new JSONObject();
        jsn.put("id", isnull(id).toString());
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
        jsn.put("toDiscoverMAC", discoverMACs);
        jsn.put("option", "sm160");
        jsn.put("equipNumber", equipNumber);

        if (saveResult) {
            if (saveToYoda) updaterForSM160REST(jsn.toString(), TOKEN);
            else updaterForSM160REST(jsn.toString(), TOKEN);//updaterForSM160RESTSingle(jsn.toString(), TOKEN);
            logSm160Operations(logId, "saved to yoda end, saveToYoda = "+saveToYoda, jsn.toString(), null);
        }
        logSm160Operations(logId, "saveToYodaREST end", jsn.toString(), null);
        return jsn;
    }

    public Object isnull(Object value) {
      if (value == null) return JSONObject.NULL; else return value;
    }


    public boolean setVersion() {
        boolean ret = false;
        String tmp = bytesToHex(replyGetMAC);
        try {
            if (replyGetMAC.length < 35) {
                logSm160Operations(logId, "MeterGSM.setVersion replyGetMAC.length < 35", tmp, null);
            } else {
                byte[] sub = getByteSubArray(replyGetMAC, 12, 16);
                versionNumber = new String(sub, StandardCharsets.UTF_8);
                boardVersion = bytesToHex(getBoardVersion(replyGetMAC, 28, 2));
                bigVersionPO = bytesToHex(getBigVersionPO(replyGetMAC, 30, 1));
                smallVersionPO = bytesToHex(getSmallVersionPO(replyGetMAC, 31, 2));
                optionNum = bytesToHex(getOptionNum(replyGetMAC, 33, 1));
                ret = true;
            }
        } catch (Exception e) {
            logSm160Operations(logId, "exception MeterGSM.setVersion", tmp, Arrays.toString(e.getStackTrace()));
            System.out.println("!!!MeterGSM.setVersion exception: "+e.getMessage()+", tmp == " + tmp);
            e.printStackTrace();
        }
        return ret;
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

    public String setMAC() {
        String res = "";
        String tmp = bytesToHex(replyGetMAC);
        try {
            int ind = tmp.indexOf("F0D00");
            if (ind > 10) {
                MAC = tmp.substring(ind - 11, ind + 5);
                discoverMACs.add(MAC);
                res = MAC;
            } else {
                ind = tmp.indexOf("FEFF");
                if (ind > 5) {
                    //log.info("!!!FEFF MAC reply = "+tmp);
                    MAC = tmp.substring(ind - 6, ind + 4 + 6);
                    //log.info("!!!MAC = "+MAC);
                    discoverMACs.add(MAC);
                    res = MAC;
                } else {
                    ind = tmp.indexOf("E1E00");
                    if (ind > 10) {
                        MAC = tmp.substring(ind - 11, ind + 5);
                        discoverMACs.add(MAC);
                        res = MAC;
                    }
                }
            }
        } catch (Exception e) {
            log.error("!!!setMAC Exception tmp = "+tmp+", message = "+e.getMessage()+", stackTrace = "+Arrays.toString(e.getStackTrace()));
            log.error("!!!setMAC Exception stackTrace " + Arrays.toString(e.getStackTrace()));
            System.out.println("!!!MeterGSM.setMac = " + tmp);
            e.printStackTrace();
        }
        return res;
    }

    public String extractMACStr(String s) {
        String res = "";
        try {
            int ind = s.indexOf("F0D00");
            if (ind > 10) {
                res = s.substring(ind - 11, ind + 5);
            } else {
                ind = s.indexOf("FEFF");
                if (ind > 5) {
                    //log.info("!!!extractMAC MAC reply = "+s);
                    res = s.substring(ind - 6, ind + 4 + 6);
                    //log.info("!!!extractMAC = "+res);
                } else {
                    ind = s.indexOf("E1E00");
                    if (ind > 10) {
                        //log.info("!!!extractMAC E1E00 reply = " + s);
                        res = s.substring(ind - 11, ind + 5);
                        //log.info("!!!extractMAC E1E00 = " + res);
                    }
                }
            }
        } catch (Exception e) {
            log.error("!!!extractMAC Exception tmp = "+s+", message = "+e.getMessage());
            log.error("!!!extractMAC Exception stackTrace " + Arrays.toString(e.getStackTrace()));
            System.out.println("!!!MeterGSM.setMac = " + s);
            e.printStackTrace();
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

//    private void waitDiscover(long seconds) {
//        LocalDateTime startTime = LocalDateTime.now();
//        while (startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS) <= seconds) {
//        }
//    }

    private boolean sendCommandDiscovery() {
        boolean ret = false;
        byte[] cmd = getCommand(7);
        try {
            out.write(cmd);
            out.flush();
            if (forSingle) System.out.println("!!!toDiscoverPart sendCommand = "+bytesToHex(cmd));
            logSm160Operations(logId, "toDiscoverPart sendCommand",bytesToHex(cmd),null);
            ret = true;
        } catch (IOException e) {
            logSm160Operations(logId, "toDiscoverPart sendCommand exception", e.getMessage(), Arrays.toString(e.getStackTrace()));
            System.out.println("!!!toDiscoverPart sendCommand exception message = " + e.getMessage());
        }
        return ret;
    }

    private boolean isConnect() {
        boolean ret = false;
        if (!socket.isClosed() && socket.isConnected()) {
            ret = true;
        }
        return ret;
    }

    public boolean toDiscoverPart() throws IOException {
        int reconnectCnt = 0;
        int reconnectCntMax = 5;
        long forcedReconnectSec = 30;
        boolean forcedReconnected = false;
        boolean ret = false;
        LocalDateTime startTime = LocalDateTime.now();
        int discoverPartSec = 20;
        boolean allGood = true;

        logSm160Operations(logId, "toDiscoverPart wait",null,null);
        try { sleep(20000);} catch (InterruptedException ignore) {
            System.out.println("!!!wait exception message = "+ignore.getMessage());
            logSm160Operations(logId, "toDiscoverPart wait exception",
                    "message: "+ignore.getMessage(), Arrays.toString(ignore.getStackTrace()));
        }
        logSm160Operations(logId, "toDiscoverPart start",null,null);
        while (startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS) <= discoverSec) {
            if (!forcedReconnected) {
                if (startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS) > forcedReconnectSec) {
                    forcedReconnected = true;
                    allGood = connectNew2(1);
                    if (forSingle) System.out.println("!!!toDiscoverPart reconnect forced after sec = "+forcedReconnectSec+", allGood = "+allGood);
                    logSm160Operations(logId, "toDiscoverPart reconnect forced", "after sec = "+forcedReconnectSec+", allGood = "+allGood, null);
                }
            }
            if (reconnectCnt >= reconnectCntMax) break;
            if (isConnect() && allGood) {
                if (sendCommandDiscovery()) {
                    LocalDateTime startPartTime = LocalDateTime.now();
                    while (startPartTime.until(LocalDateTime.now(), ChronoUnit.SECONDS) <= discoverPartSec) {
                        replyToDiscover = readReplyDiscover(40);
                        String reply = bytesToHex(replyToDiscover);
                        String message = "sec run = "+startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS)
                                + ", part = " + startPartTime.until(LocalDateTime.now(), ChronoUnit.SECONDS);
                        if (forSingle) {
                            System.out.println("!!!message = "+message+", reply = "+reply);
                        }
                        if (!isZeroAll(replyToDiscover)) {
                            logSm160DiscoveryReply(logId, reply, message);
                            discoverReplyBuffer.add(bytesToHex(replyToDiscover));
                            ret = true;
                        }
                    }
                } else {
                    reconnectCnt++;
                    System.out.println("!!!toDiscoverPart reconnectCmd reconnectCnt = "+reconnectCnt+", ip = "+ip);
                    //try { sleep(2000); } catch (InterruptedException ignore) {}
                    logSm160Operations(logId, "toDiscoverPart reconnect sendCmd", null, null);
                    allGood = connectNew2(1);
                }
            } else {
                reconnectCnt++;
                //try { sleep(2000); } catch (InterruptedException ignore) {}
                System.out.println("!!!toDiscoverPart reconnect reconnectCnt = "+reconnectCnt+", ip = "+ip);
                logSm160Operations(logId, "toDiscoverPart reconnect",null,null);
                allGood = connectNew2(1);
            }
        }
        logSm160DiscoveryReply(logId, null, "toDiscover end ret = "+ret);
        return ret;
    }

//    public boolean toDiscover() {
//        logSm160Operations(logId, "toDiscover wait",null,null);
//        try { sleep(5000);} catch (InterruptedException ignore) {
//            System.out.println("!!!wait exception message = "+ignore.getMessage());
//            logSm160Operations(logId, "toDiscover wait exception",
//                    "message: "+ignore.getMessage(), Arrays.toString(ignore.getStackTrace()));
//        }
//        logSm160Operations(logId, "toDiscover start",null,null);
//        boolean ret = false;
//        LocalDateTime startTime = LocalDateTime.now();
//        try {
//            if (socket.isConnected() && !socket.isClosed()) {
//                byte[] cmd = getCommand(7);
//                out.write(cmd);
//                out.flush();
//                //System.out.println("!!!sec start = "+startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS));
//
//                while (startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS) <= discoverSec) {
//                    replyToDiscover = readReplyDiscover(40);
////                    System.out.println("!!!sec run = "+startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS)+", reply = "+bytesToHex(replyToDiscover));
//                    if (!isZeroAll(replyToDiscover)) {
//                        discoverReplyBuffer.add(bytesToHex(replyToDiscover));
//                        ret = true;
//                    }
//                }
//                //System.out.println("!!!sec end = "+startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS));
//            }
//        } catch (Exception e) {
//            logSm160Operations(logId, "toDiscover exception","message: "+e.getMessage(), Arrays.toString(e.getStackTrace()));
//            System.out.println("!!!toDiscover exception: "+e.getMessage());
//            e.printStackTrace();
//        }
//        finally {
//            logSm160Operations(logId, "toDiscover finally end","ret = "+ret, null);
//        }
//        return ret;
//    }

    private byte[] readReplyDiscover(int cntRead) throws IOException {
        byte[] b = new byte[cntRead];
        boolean run = true;
        int runCnt = 0;
        while (run && runCnt < 10) {
            try {
                sleep(50);
            } catch (InterruptedException ignored) {
            }
            runCnt++;
            if (in.available() > 0) {
                in.read(b);
                break;
            }
        }
        return b;
    }

    public boolean toDiscoverNew() {
        logSm160Operations(logId, "toDiscoverNew start",null,null);
        boolean reconnect = true;//false;
        boolean ret = false;
        int cntBased = 150;
        int cnt = cntBased;
        try {
            if (socket.isConnected()) {
                byte[] cmd = getCommand(7);
                out.write(cmd);
                out.flush();
                    while (cnt > 0) {
                        replyToDiscover = readReplyNew(40);
                        //if (replyToDiscover.length < 20) continue;
                        if (isZeroAll(replyToDiscover)) {
                            if (!reconnect) {
                                if (connectNew2(1)) {
                                    reconnect = true;
                                    cnt = cntBased;
                                    out.write(cmd);
                                    out.flush();
                                    //System.out.println("!!!RECONNECT");
                                    continue;
                                }
                            }
                            break;
                        }
                        //if (validReply(replyToDiscover))
                        discoverReplyBuffer.add(bytesToHex(replyToDiscover));
                        ret = true;
                        try {
                            sleep(100);
                        } catch (Exception ignored) {
                        }
                        cnt--;
                    }
                }
        } catch (Exception e) {
            logSm160Operations(logId, "toDiscoverNew exception","message: "+e.getMessage(), Arrays.toString(e.getStackTrace()));
//            System.out.println("!!!toDiscoverNew exception: "+e.getMessage());
//            e.printStackTrace();
        }
        finally {
            logSm160Operations(logId, "toDiscoverNew finally end","ret = "+ret, null);
        }
        return ret;
    }

    public boolean isZeroAll(byte[] b) {
        boolean ret = true;
        if (b.length == 0) return ret;
        for (byte bb: b){
            if (bb != (byte) 0) {
                ret = false;
                break;
            }
        }
        if (!ret && discoverReplyBuffer.size() < 5) return false;
        return ret;
    }

    public boolean checkInfoOpros(int reaskCount) {
        logSm160Operations(logId, "checkInfo start", "проверка сети", null);
        List<byte[]> resultList = new ArrayList<>();
        boolean ret = false;
        int cntRead = 100;
        boolean forceReconnect = false;
        if (socket.isClosed()) connectNew2(1);
        try
        {
            if (socket.isConnected()) {
                for (int i = 0; i < reaskCount; i++) {
                    if (sendCommand(9, forceReconnect)) {
                            disconnect(2);
                            connectNew2(1);
                    } else {
                        AbramHelper.myWait(200);
                        List<byte[]> replyBytesList = readReplyCheckInfo(cntRead, 10);
                        resultList.addAll(replyBytesList);
                    }
                }
                for (byte[] bytes: resultList) {
                    String hexStr = bytesToHex(bytes);
                    int pos = hexStr.indexOf("AA0109");
                    if (pos > -1) {
                        logSm160Operations(logId, "checkInfoAll resultList", hexStr, null);
                        String validReply = extractReply(hexStr, pos);
                        if (validReply != null) {
                            System.out.println("!!!validReply="+validReply);
                            byte[] bytesFromHex = hexStringToByteArray(validReply);
                            boolean valid = isValidCheckInfo(bytesFromHex);
                            if (valid) {
                                replyCheckInfo = bytesFromHex;
                                logSm160Operations(logId, "checkInfo validReply = "+valid, "reply = "+validReply, null);
                                break;
                            }
                        }
                    }
                }
                if (replyCheckInfo != null) {
                    ret = true;
                }
            } else {
                logSm160Operations(logId, "checkInfo socket not connected", "после многих попыток не удалось подключиться", null);
            }
        } catch (Exception e) {
            logSm160Operations(logId, "checkInfo exception", "exception: "+e.getMessage(), Arrays.toString(e.getStackTrace()));
            System.out.println("!!!checkInfo exception: "+e.getMessage());
            e.printStackTrace();
        }
        finally {
            logSm160Operations(logId, "checkInfo finally end", "ret = "+ret, null);
            System.out.println("!!!checkInfo end");
        }
        return ret;
    }

    private String extractReply(String hexStr, int pos) {
        if (hexStr.length() >= pos + 34) {
            return hexStr.substring(pos, pos + 34);
        }
        return null;
    }


//    public boolean checkInfo(int reaskCount) {
//        logSm160Operations(logId, "checkInfo start", "проверка сети", null);
//        boolean ret = false;
//        int cntRead = 100;
//        if (socket.isClosed()) connectNew2(1);
//        try
//        {
//            if (socket.isConnected()) {
//                sendCommand(9, false);
//                System.out.println("!!!checkInfo start");
//                AbramHelper.myWait(500);
//                sendCommand(9, false);
//                System.out.println("!!!checkInfo start2");
//                AbramHelper.myWait(500);
//                replyCheckInfo = readReply(cntRead);
//                logSm160Operations(logId, "checkInfo replyBeforeClear", "reply = "+bytesToHex(replyCheckInfo), null);
//                if (!isValidCheckInfo(replyCheckInfo)) {
//                    for (int i = 0; i < reaskCount; i++) {
//                        sendCommand(9, false);
//                        AbramHelper.myWait(500);
//                        replyCheckInfo = readReply(cntRead);
//                        replyCheckInfo = extractRealMessage(replyCheckInfo, 35);
//                        boolean validReply = isValidCheckInfo(replyCheckInfo);
//                        logSm160Operations(logId, "checkInfo replyBeforeClear"+i, "valid = "+validReply+", reply = "+bytesToHex(replyCheckInfo), null);
//                        if (validReply) {
//                            break;
//                        }
//                    }
//                }
//                replyCheckInfo = getBeforeTwoAA(clearBeforeAA(clearLast0(replyCheckInfo)));
//                ret = isValidCheckInfo(replyCheckInfo);
//                //ret = validReply(replyCheckInfo);
//                if (replyCheckInfo.length < 17) {
//                    ret = false;
//                    logSm160Operations(logId, "checkInfo validReply = "+ret, "size < 17, reply = "+bytesToHex(replyCheckInfo), null);
//                } else {
//                    logSm160Operations(logId, "checkInfo validReply = "+ret, "reply = "+bytesToHex(replyCheckInfo), null);
//                }
//            } else {
//                logSm160Operations(logId, "checkInfo socket not connected", "после многих попыток не удалось подключиться", null);
//            }
//        } catch (Exception e) {
//            logSm160Operations(logId, "checkInfo exception", "exception: "+e.getMessage(), Arrays.toString(e.getStackTrace()));
//            System.out.println("!!!checkInfo exception: "+e.getMessage());
//            e.printStackTrace();
//        }
//        finally {
//            logSm160Operations(logId, "checkInfo finally end", "ret = "+ret, null);
//            System.out.println("!!!checkInfo end");
//        }
//        return ret;
//    }

//    private byte[] extractRealMessage(byte[] bytes, int extractCountBytes) {
//        if (bytes != null && !isAllZero(bytes)) {
//            bytes = getBeforeTwoAA(clearBeforeAA(bytes));
//            if (bytes.length > extractCountBytes)  bytes = getByteSubArray(bytes, 0, extractCountBytes);
//            System.out.println("!!!extractRealMessage="+bytesToHex(bytes));
//        }
//        return bytes;
//    }

    private boolean isValidCheckInfo(byte[] bytes) {
        return bytes != null && !isAllZero(bytes) && validReplyWithoutClear(bytes);
    }

    private boolean sendCommand(int commandType, boolean forceReconnect) {
        boolean ret = false;
        boolean writed = false;
        byte[] cmd = getCommand(commandType);
        if (forceReconnect) {
            disconnect(2);
            connectNew2(1);
        } else {
            if (!isConnect()) {
                disconnect(2);
                connectNew2(1);
            }
        }

        try {
            out.write(cmd);
            writed = true;
        } catch(Exception e) {
            disconnect(2);
            connectNew2(1);
        }

        try {
            if (!writed) out.write(cmd);
            out.flush();
            if (forSingle) System.out.println("!!!sendCommand = "+bytesToHex(cmd));
            logSm160Operations(logId, "sendCommand",bytesToHex(cmd),null);
        } catch (IOException e) {
            ret = true;
            logSm160Operations(logId, "sendCommand exception", e.getMessage(), Arrays.toString(e.getStackTrace()));
            System.out.println("!!!sendCommand exception message = " + e.getMessage());
        }
        return ret;
    }

    public boolean getMAC() {
        logSm160Operations(logId, "getMAC start", null,null);

        boolean ret = false;
        try
        {
            if (socket.isConnected()) {
                byte[] cmd = getCommand(19);
                out.write(cmd);
                out.flush();
                replyGetMAC = readReply( 35);
                logSm160Operations(logId, "getMAC replyBeforeClear", "reply = "+bytesToHex(replyGetMAC),
                        null);
                replyGetMAC = getBeforeTwoAA(clearBeforeAA(clearLast0(replyGetMAC)));
                ret = validReply(replyGetMAC);
                logSm160Operations(logId, "getMAC validReply = "+ret,
                        "reply = "+bytesToHex(replyGetMAC),
                        null);
            }
        } catch (Exception e) {
            logSm160Operations(logId, "getMAC exception",
                    "message: "+e.getMessage(),
                    Arrays.toString(e.getStackTrace()));
        }
        finally {
            logSm160Operations(logId, "getMAC finally end","ret = "+ret,null);
        }
        return ret;
    }

    public boolean connectNew2(int conCnt) {
        AtomicBoolean res = new AtomicBoolean(false);

        if (socket != null) {
            if (socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }

        ExecutorService es = Executors.newSingleThreadExecutor();
        TimeLimiter tl = SimpleTimeLimiter.create(es);
        try {
            tl.callWithTimeout(() -> {
                boolean timeOut = false;
                int i = 0;
                while (!res.get() && i < conCnt) {
                    logSm160Operations(logId, "ConnectNew2 start", "conCnt = "+conCnt, null);
                    try {
                        socket = new Socket();
                        try {
                            socket.connect(socketAddress, connectTimeOut);
                            this.in = new DataInputStream(socket.getInputStream());
                            this.out = new DataOutputStream(socket.getOutputStream());
                            res.set(true);
                        }
                        catch (SocketTimeoutException | ConnectException | NoRouteToHostException  e1) {
                            logSm160Operations(logId, "ConnectNew2 exception1",
                                    "message: "+e1.getMessage(),
                                    Arrays.toString(e1.getStackTrace()));
                            if (e1.getMessage().contains("connect timed out")) {
                                 if (!timeOut) {
                                     timeOut = true;
                                     continue;
                                 }
                            }
                            break;
                        }
                        logSm160Operations(logId, "ConnectNew2 connected", "conCnt = "+conCnt, null);
                    } catch (Exception e) {
                        logSm160Operations(logId, "ConnectNew2 exception2",
                                "message: "+e.getMessage(),
                                Arrays.toString(e.getStackTrace()));
                        i++;
                        try {
                            sleep(500);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
                return null;
            }, 1200L, TimeUnit.SECONDS);
        } catch (TimeoutException | UncheckedIOException e){
            logSm160Operations(logId, "ConnectNew2 TimeoutException",
                    "message: "+e.getMessage(),
                    Arrays.toString(e.getStackTrace()));

            //System.out.println(index+"/"+size+" !!!TIMEOUT connectNew2");
            try {
                socket.close();
            } catch (Exception ignored) {}
        } catch (Exception e){
            logSm160Operations(logId, "ConnectNew2 Exception3",
                    "message: "+e.getMessage(),
                    Arrays.toString(e.getStackTrace()));
//            System.out.println("!!!connectNew2 Exception: "+e.getMessage());
//            e.printStackTrace();
        }
        finally {
            es.shutdown();
            logSm160Operations(logId, "ConnectNew2 finally end",
                    "connected =  "+res.get(),null);
        }

        return res.get();
    }

    private void disconnect(int repeat) {
        for (int i=0; i<repeat; i++) {
            try {
                socket.close();
            } catch (Exception ignore) {
            }
        }
    }

    private List<byte[]> readReplyCheckInfo(int cntRead, int cycleCount) throws IOException {
        List<byte[]> resultList = new ArrayList<>();
        byte[] b = new byte[cntRead];
        boolean run = true;
        int runCnt = 0;
        while (run && runCnt < cycleCount) {
            myWait(200);
            runCnt++;
            if (in.available() > 0) {
                in.read(b);
                resultList.add(b);
                //break;
            }
        }
        return resultList;
    }


    private byte[] readReplyNew(int cntRead) throws IOException {
        byte[] b = new byte[cntRead];
        boolean run = true;
        int runCnt = 0;
        while (run && runCnt < 10) {
            try {
                sleep(150);
            } catch (InterruptedException ignored) {
            }
            runCnt++;
            if (in.available() > 0) {
                in.read(b);
                break;
            }
        }
        return b;
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

    public byte[] getCommand(int type) {
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

    public byte[] setAA(byte[] b) {
        ByteBuffer buf = ByteBuffer.wrap(new byte[b.length + 1]);
        buf.position(0);
        buf.put((byte) 0xAA);
        buf.position(1);
        buf.put(b);
        byte[] ret = buf.array();
        return ret;
    }

    public byte[] setCRC(byte[] b) {
        CRC8 crc = new CRC8();
        crc.reset();
        crc.update(b);
        byte[] ret = Arrays.copyOf(b, b.length + 1);
        ret[ret.length - 1] = (byte) crc.getValue();
        return ret;
    }


}
