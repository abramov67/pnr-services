package com.company.pnrservices.core;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.company.pnrservices.core.YodaRESTMethodsHelper.updateSerialREST;

public class UpdateSerialHelper {


    public static class UpdateSerialThread extends Thread {
        HermesShell hermesShell;
        String hermes_id;
        Integer index;
        String token;
        String mac;
        String meter_id;
        String type;
        public String serial = "";
        Integer size;
        boolean saveResult;

        public UpdateSerialThread(String hermes_id, Integer index, String token, String meter_id, String mac, String type, Integer size, boolean saveResult) {
            hermesShell = new HermesShell(hermes_id, index, token);
            this.hermes_id = hermes_id;
            this.index = index;
            this.token = token;
            this.mac = mac;
            this.meter_id = meter_id;
            this.type = type;
            this.size = size;
            this.saveResult = saveResult;

        }

        @Override
        public void run() {
            super.run();
            startSerial();
        }

        public String startSerial() {
            String ret = "";
            ret = getSerial(mac);
            if (!ret.equals("") && saveResult) {
                updateSerialREST(meter_id, mac, serial, token);
            }
            System.out.println(timeFormat(new Date())+"  "+index+"/"+size+" !!!meter_id = "+meter_id+", mac = "+mac+", serial = "+serial);
            return ret;
        }

        private String getSerial(String mac) {
            StringBuilder sb = new StringBuilder();
            String cmd = "meter.genopt.getSerial " + mac;
            if (type.contains("221") && type.contains("ле")) cmd = "meter.le221.getSerial " + mac;
            hermesShell.clientSSHMain.sendCommandOA(cmd).forEach(sb::append);
            if (analyse(sb.toString())) return serial;
            else return "";
        }

        public boolean analyse(String replyStr) {
            boolean ret = false;
            if (!replyStr.contains("[ERROR]")){
                int ind = replyStr.indexOf("serial=");
                if (ind > -1) {
                    try {
                        serial = extractSerial(replyStr.substring(ind + 13));
                        ret = true;
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("!!!analyse IndexOutOfBoundsException: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            return ret;
        }

        public String extractSerial(String serial_) {
            int ind = serial_.indexOf("}");
            if (ind > -1)
                return serial_.substring(0, ind - 5);
            else return null;
        }

        private String timeFormat(Date tm) {
            String pattern = "HH:mm:ss";
            return new SimpleDateFormat(pattern).format(tm);
        }
    }

}
