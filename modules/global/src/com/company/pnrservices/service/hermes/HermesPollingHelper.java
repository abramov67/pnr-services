package com.company.pnrservices.service.hermes;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("pnrservices_HermesPollingHelper")
public class HermesPollingHelper {

    public static class HermesPollingThread extends Thread {
        private static final Logger log = LoggerFactory.getLogger(HermesPollingThread.class);
        int indexPart;
        int index;
        int size;
        String ip;
        int port;
        String login;
        String pwd;
        String mac;
        String TOKEN;
        private final LocalTime startTime = LocalTime.now();


        public HermesPollingThread(int indexPart, int index, int size, String ip, int port, String login, String pwd, String mac, String token) {
            this.indexPart = indexPart;
            this.index = index;
            this.size = size;
            this.ip = ip;
            this.port = port;
            this.login = login;
            this.pwd = pwd;
            this.mac = mac;
            this.TOKEN = token;

        }

        @Override
        public void run() {
            thread();
        }

        public void thread() {
            ExecutorService es = Executors.newSingleThreadExecutor();
            TimeLimiter tl = SimpleTimeLimiter.create(es);
            try {
                tl.callWithTimeout(() -> {
                    String result = sendCommand("topology.getMeterInfo", mac);
                    System.out.println(dateTimeFormat(new Date())+"/"+indexPart+"/"+index+"/"+size+"/"+deltaTimeFormat(startTime, LocalTime.now())
                            +" !!!finally mac = "+mac+", result = "+result);
                    return null;
                }, 60L, TimeUnit.SECONDS);
            } catch (TimeoutException | UncheckedIOException e){
                System.out.println(index+"/"+size+" !!!TIMEOUT tread");
            } catch (Exception e){
                System.out.println("!!!thread Exception: "+e.getMessage());
                e.printStackTrace();
            }
            finally {
                es.shutdown();
            }
        }

        public String sendCommand(String command, String mac) {
            StringBuilder ret = new StringBuilder();
            Session session = null;
            int sessionTimeOut = 10000;
            int channelTimeOut = 5000;
            try {
                JSch jsch = new JSch();
                session = jsch.getSession(login, ip, port);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword(pwd);
                session.connect(sessionTimeOut);

                ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
                channelExec.setCommand(command + " " + mac);
                channelExec.setErrStream(System.err);

                InputStream in = channelExec.getInputStream();

                channelExec.connect(channelTimeOut);
                byte[] tmp = new byte[1024];
                while (true) {
                    while (in.available() > 0) {
                        int i = in.read(tmp, 0, 1024);
                        if (i < 0) break;
                        ret.append(new String(tmp, 0, i));
                    }
                    if (channelExec.isClosed()) {
                        if (in.available() > 0) continue;
                        break;
                    }
                }
                channelExec.disconnect();

            } catch (JSchException | IOException e) {
                log.error("Error {}", e.getMessage());
            } finally {
                if (session != null) {
                    session.disconnect();
                }
            }
            if (ret.toString().toLowerCase().contains("error")) {
                return "{\"error\":\""+ret.toString()+"\"}";
            } else {
                return createJSONResult(ret.toString());
            }
        }

        private String createJSONResult(String str) {
            JSONObject ret = new JSONObject();
            String thw = getTHW(str);
            ret.put("THW", thw == null ? JSONObject.NULL : thw);
            ret.put("online", getStatus(str));
            String seq1 = String.valueOf(new char[]{27, '[', '3', '2', 'm'});
            String seq2 = String.valueOf(new char[]{27, '[', '3', '3', 'm'});
            String seq3 = String.valueOf(new char[]{27, '[', '0', 'm'});
            String resultStr = str.replaceAll(Pattern.quote(seq1), "")
                    .replaceAll(Pattern.quote(seq2), "")
                    .replaceAll(Pattern.quote(seq3), "")
                    .replaceAll("\r", "")
                    .replaceAll("\n", "");
            String mhw = getMHW(resultStr);
            ret.put("MHW", mhw == null ? JSONObject.NULL : mhw);
            String optionList = getOptionList(resultStr);
            if (optionList != null) {
                try {
                    JSONArray jsnOptList = new JSONArray(optionList);
                    ret.put("optionList", jsnOptList);
                } catch (Exception e) {
                    System.out.println("!!!Exception in jsnOptList message = " + e.getMessage());
                    e.printStackTrace();
                    ret.put("optionList", optionList);
                    ret.put("optionListErr", e.getMessage());
                }
            } else {
                ret.put("optionList", JSONObject.NULL);
            }
            String lastActivity = getDataActivityTs(resultStr);
            ret.put("lastActivity", lastActivity == null ? JSONObject.NULL : lastActivity);
            String hwName = getHwNAME(resultStr);
            ret.put("hwName", hwName == null ? JSONObject.NULL : hwName);
            String hwVersion = getHwVersion(resultStr);
            ret.put("hwVersion", hwVersion == null ? JSONObject.NULL : hwVersion);
            String swVersion = getSwVersion(resultStr);
            ret.put("swVersion", swVersion == null ? JSONObject.NULL : swVersion);
            //ret.put("result", resultStr.substring(resultStr.indexOf("MeterInfo") + 9));
            return ret.toString();
        }

        private String getTHW(String val) {
            int ind = val.indexOf("THW[");
            if (ind != -1) {
                return val.substring(ind + 4, ind + 4 + 15);
            }
            return null;
        }

        private boolean getStatus(String val) {
            int ind = val.indexOf("ONLINE");
            if (ind > -1) {
                return true;
            }
            return false;
        }

        private String getMHW(String val) {
            int ind = val.indexOf("MHW[");
            if (ind != -1) {
                return val.substring(ind + 4, ind + 4 + 16);
            }
            return null;
        }

        private String getOptionList(String val) {
            int ind = val.indexOf("optionList=");
            if (ind != -1) {
                String strTemp = val.substring(ind + 11);
                strTemp = strTemp.substring(0, strTemp.indexOf("]") + 1)
                        .replace("[", "[\"")
                        .replace("]", "\"]")
                        .replace(", ", "\",\"");
                return strTemp;
            }
            return null;
        }

        private String getDataActivityTs(String val) {
            Pattern patternLastActivity = Pattern.compile("dataActivityTs=\\d{6} (\\d{6})[.](\\d{3})");
            Matcher matcherLastActivity = patternLastActivity.matcher(val);
            if (matcherLastActivity.find()) {
                String lastActivity = val.substring(matcherLastActivity.start(), matcherLastActivity.end());
                DateFormat df = new SimpleDateFormat("yyMMdd hhmmss.SSS");
                DateFormat dfResult = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                Date dateActivity;
                try {
                    dateActivity = df.parse(lastActivity.replaceAll("dataActivityTs=", ""));
                    return dfResult.format(dateActivity);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        private String getHwNAME(String val) {
            int ind = val.indexOf("hwName='");
            if (ind != -1) {
                try {
                    String strTemp = val.substring(ind + 8);
                    strTemp = strTemp.substring(0, strTemp.indexOf("',"));
                    return strTemp.replaceAll("'", "\"");
                } catch (Exception e) {
                    System.out.println("!!!Exception in hwName, message: " + e.getMessage()+", value = " + val);
                    e.printStackTrace();
                }
            }
            return null;
        }

        private String getHwVersion(String val) {
            int ind = val.indexOf("hwVersion='");
            if (ind != -1) {
                try {
                    String strTemp = val.substring(ind + 11);
                    strTemp = strTemp.substring(0, strTemp.indexOf("',"));
                    return strTemp.replaceAll("'", "\"");
                } catch (Exception e) {
                    System.out.println("!!!Exception in hwVersion, message: " + e.getMessage()+", value = " + val);
                    e.printStackTrace();
                }
            }
            return null;
        }

        private String getSwVersion(String val) {
            int ind = val.indexOf("swVersion='");
            if (ind != -1) {
                try {
                    String strTemp = val.substring(ind + 11);
                    strTemp = strTemp.substring(0, strTemp.indexOf("'}"));
                    return strTemp.replaceAll("'", "\"");
                } catch (Exception e) {
                    System.out.println("!!!Exception in swVersion, message: " + e.getMessage()+", value = " + val);
                    e.printStackTrace();
                }
            }
            return null;
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

    }

}
