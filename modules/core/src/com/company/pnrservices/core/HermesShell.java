package com.company.pnrservices.core;

import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.company.pnrservices.core.YodaRESTMethodsHelper.getHermesParamsREST;

public class HermesShell {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(HermesShell.class);
    String hermes_ip, login, password;
    int port;
    ClientSSHMain clientSSHMain;
    int index;

    public HermesShell(String hermes_id, int index, String token) {
        JSONObject obj = getHermesParamsREST(hermes_id, token);
        this.hermes_ip = obj.get("ip").toString();
        this.port = Integer.parseInt(obj.get("port").toString());
        this.login = obj.get("user").toString();
        this.password = obj.get("pwd").toString();
        this.index = index;
        this.clientSSHMain = new ClientSSHMain(hermes_ip, port, login, password);
    }

    public String getTerminalID(String mac) {
        StringBuilder sb = new StringBuilder();
        List<String> reply = clientSSHMain.sendCommandOA("topology.getMeterInfo " + mac);
        if (reply != null) {
            for (String s : reply) {
                sb.append(s);
            }
        }
        return sb.toString();
    }

    public String onOffTerminal(String terminal_imei, String onOff) {
        StringBuilder sb = new StringBuilder();
        List<String> reply = clientSSHMain.sendCommandOA("zb.fervid.permitJoin " + terminal_imei + " " + onOff);
        if (reply != null) {
            for (String s : reply) {
                sb.append(s);
            }
        }
        return sb.toString();
    }

//{terminalHwId=[356612029461797], hwIdVerified=true, gdopId=3319, login='t356612029461797', online=true, dataSentTs=NONE, dataReceivedTs=220415 122645.830, presenceTs=220415 082149.632, terminalType=TerminalType{[33mname=[32mCOMMODA_WIRE_TRANSPARENT_OLD[0m, [33mprotocolTypes=[32m[TEXT[0m, LEO_FRAME_LEO_OPTION, TRANSPARENT_FRAME_TRANSPARENT_CMD]}}[0m

    public List<JSONObject> getTerminalsID() {
        return clientSSHMain.sendCommandOA("topology.listTerminals")
                .stream()
                .limit(50)
                .map(t -> {
                            String s = removeEscapeChars(
                                    t.replaceAll("=", ":")
                                            .replaceAll("'", "\"")
                                            .replaceAll("THW\\[", "[")
                                            .replace("TerminalInfo", "")
                                            .replace("TerminalType", ""));
                            log.info("!!!---" + s);
                            return new JSONObject(s);
                        }
                )
                .collect(Collectors.toList());
    }

    private String removeEscapeChars(String inStr) {
        String seq1 = String.valueOf(new char[] {27, '[', '3', '2', 'm'});
        String seq2 = String.valueOf(new char[] {27, '[', '3', '3', 'm'});
        String seq3 = String.valueOf(new char[] {27, '[', '0', 'm'});
//        char[] c = {27, '[','\\', 'd', '\\', '?', 'm'};
//        String seq1 = String.valueOf(c);
        return inStr.replaceAll(Pattern.quote(seq1), "")
                .replaceAll(Pattern.quote(seq2), "")
                .replaceAll(Pattern.quote(seq3), "");
    }

//    private String removeEscapeChars(String remainingValue) {
//        Matcher matcher = Pattern.compile("\\&([^;]{6})", Pattern.CASE_INSENSITIVE).matcher(remainingValue);
//        while (matcher.find()) {
//            String before = remainingValue.substring(0, matcher.start());
//            String after = remainingValue.substring(matcher.start() + 1);
//            remainingValue = (before + after);
//        }
//        return remainingValue;
//    }

    private String extractTerminalID(String replyStr) {
        if (replyStr.contains("[ERROR]"))  return null;
        int ind = replyStr.indexOf("THW[");
        if (ind > -1) {
            return replyStr.substring(ind + 4, ind + 4 + 15);
        } else return null;
    }

    private String extractDataReceived(String replyStr) {
        if (replyStr.contains("dataReceivedTs=NONE"))  return null;
        int ind = replyStr.indexOf("dataReceivedTs=");
        if (ind > -1) {
            String date = replyStr.substring(ind + 20, ind + 20 + 13);
            if (date.contains("NONE")) return null;
            String year = "20"+date.substring(0, 2);
            String month = date.substring(2, 4);
            String day = date.substring(4, 6);
            String hour = date.substring(7, 9);
            String minute = date.substring(9, 11);
            String sec = date.substring(11, 13);
            //System.out.println("!!!date = "+date);
            //System.out.println("!!!replyStr = "+replyStr);

            return year+"-"+month+"-"+day+" "+hour+":"+minute+":"+sec;
        } else return null;
    }

    private Boolean extractOnlineStatus(String replyStr) {
        int ind = replyStr.indexOf("online=");
        if (ind > -1) {
            String date = replyStr.substring(ind + 7 + 5, ind + 7 + 5 + 3);

            System.out.println("!!!online = ["+date+"]");
            System.out.println("!!!replyStr = ["+replyStr+"]");
            return date.equals("tru");
        } else return null;
    }

}
