package com.company.pnrservices.core;

import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.company.pnrservices.core.YodaRESTMethodsHelper.getHermesParamsREST;

public class HermesShell {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(HermesShell.class);
    String hermes_ip, login, password;
    int port;
    ClientSSHMain clientSSHMain;
    int index;
    String hermes_id;

    public HermesShell(String hermes_id, int index, String token) {
        JSONObject obj = getHermesParamsREST(hermes_id, token);
        this.hermes_ip = obj.get("ip").toString();
        this.port = Integer.parseInt(obj.get("port").toString());
        this.login = obj.get("user").toString();
        this.password = obj.get("pwd").toString();
        this.index = index;
        this.hermes_id = hermes_id;
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

    public List<JSONObject> getTerminalsID() {
        return clientSSHMain.sendCommandOA("topology.listTerminals")
                .stream()
                .filter(t -> t.contains("GWKERNEL_ZIGBEE"))
                .map(t -> {
                            String s = removeEscapeChars(
                                    t.replaceAll("=", ":")
                                            .replaceAll("'", "\"")
                                            .replaceAll("THW\\[", "[")
                                            .replace("TerminalInfo", "")
                                            .replace("TerminalType", ""));
                            return new JSONObject(s);
                        }
                )
                .collect(Collectors.toList());
    }

    private String removeEscapeChars(String inStr) {
        String seq1 = String.valueOf(new char[] {27, '[', '3', '2', 'm'});
        String seq2 = String.valueOf(new char[] {27, '[', '3', '3', 'm'});
        String seq3 = String.valueOf(new char[] {27, '[', '0', 'm'});
        return inStr.replaceAll(Pattern.quote(seq1), "")
                .replaceAll(Pattern.quote(seq2), "")
                .replaceAll(Pattern.quote(seq3), "");
    }

    public List<JSONObject> getMetersID(String terminalImei) {
        AtomicReference<Boolean> online = new AtomicReference<>(false);
        return clientSSHMain.sendCommandOA("topology.listMeters "+terminalImei)
                .stream()
                .map(t -> {
                    if (t.contains("THW[" + terminalImei + "]")) {
                        if (t.contains("ONLINE")) online.set(true);
                        else online.set(false);
                        return new JSONObject();
                    } else {
                        String s = removeEscapeChars(
                                t.replaceAll("=", ":")
                                        .replace(">>> ", ""))
                                .replace("MeterInfo", "")
                                .replaceAll("'", "\"")
                                .replaceAll("MHW\\[", "[")
                                .replace("MeterVersionInfo", "")
                                .replace("OptionList", "");
                        JSONObject jsn = new JSONObject();
                        String mac = extractMeterMAC(s);
                        String dataActivity = extractDataActivityTs(s);
                        return jsn.put("online", online.get().booleanValue())
                                .put("mac", mac)
                                .put("updateTime", dataActivity)
                                .put("terminalImei", terminalImei)
                                .put("hermes_id", hermes_id);
                    }
                }
                )
                .collect(Collectors.toList());
    }

    private String extractMeterMAC(String replyStr) {
        if (replyStr.contains("[ERROR]"))  return null;
        int ind = replyStr.indexOf("meterHwId:[");
        if (ind > -1) {
            return replyStr.substring(ind + 11, ind + 11 + 16);
        } else return null;
    }

    private String extractDataActivityTs(String replyStr) {
        if (replyStr.contains("dataActivityTs:NONE"))  return null;
        int ind = replyStr.indexOf("dataActivityTs:");
        if (ind > -1) {
            String date = replyStr.substring(ind + 15, ind + 15 + 13);
            if (date.contains("NONE")) return null;
            String year = "20"+date.substring(0, 2);
            String month = date.substring(2, 4);
            String day = date.substring(4, 6);
            String hour = date.substring(7, 9);
            String minute = date.substring(9, 11);
            String sec = date.substring(11, 13);
            return year+"-"+month+"-"+day+" "+hour+":"+minute+":"+sec;
        } else return null;
    }

}
