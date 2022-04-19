package com.company.pnrservices.core;

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
        String serial = "";
        Integer size;

        public UpdateSerialThread(String hermes_id, Integer index, String token, String meter_id, String mac, String type, Integer size) {
            hermesShell = new HermesShell(hermes_id, index, token);
            this.hermes_id = hermes_id;
            this.index = index;
            this.token = token;
            this.mac = mac;
            this.meter_id = meter_id;
            this.type = type;
            this.size = size;

        }

        @Override
        public void run() {
            String ret = "";
            super.run();
            ret = getSerial(mac);

            if (!ret.equals("")) {
                updateSerialREST(mac, serial, token);
            }
        }

        private String getSerial(String mac) {
            StringBuilder sb = new StringBuilder();
            String cmd = "meter.genopt.getSerial " + mac;
            if (type.contains("221") && type.contains("ле")) cmd = "meter.le221.getSerial " + mac;
            hermesShell.clientSSHMain.sendCommandOA(cmd).forEach(sb::append);
            if (analyse(sb.toString())) return serial;
            else return "";
        }

        private boolean analyse(String replyStr) {
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

        private String extractSerial(String serial_) {
            int ind = serial_.indexOf("}");
            if (ind > -1)
                return serial_.substring(0, ind - 5);
            else return null;
        }
    }

}
