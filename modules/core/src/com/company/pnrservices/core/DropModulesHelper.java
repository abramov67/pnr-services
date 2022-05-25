package com.company.pnrservices.core;

import com.haulmont.cuba.core.global.AppBeans;
import java.util.List;

import static com.company.pnrservices.core.YodaRESTMethodsHelper.upsertTopologyREST;

public class DropModulesHelper {

    public static void clearLastCloseTerminals() {
        NativeSQLBean nativeSQLBean = AppBeans.get(NativeSQLBean.class);
        nativeSQLBean.clearLastClosedTerminals();
    }


    public static class UpdaterThread extends Thread {
        Integer index;
        String mac;
        String meter_id;
        String hermes_id;
        String terminal_id;
        HermesShell hermesShell;
        NativeSQLBean nativeSQLBean;
        String TOKEN;
        Integer size;

        public UpdaterThread(Integer index, String mac, String meter_id, String hermes_id, String token, Integer size) {
            this.index = index;
            this.mac = mac;
            this.meter_id = meter_id;
            this.hermes_id = hermes_id;
            this.hermesShell = new HermesShell(hermes_id, index, token);
            this.nativeSQLBean = AppBeans.get(NativeSQLBean.class);
            this.TOKEN = token;
            this.size = size;
        }

        @Override
        public void run() {
            super.run();
            getTerminalFromShell();
            System.out.println(index+"/"+size+" !!topologyUpdate id = "+meter_id+", mac = "+mac);
        }

        private void getTerminalFromShell() {
            String replyStr = hermesShell.getTerminalID(mac);
            String s = extractTerminal(replyStr);
            if (s != null) {
                terminal_id = s;
                    upsertTopologyREST(hermes_id, terminal_id, meter_id, mac, TOKEN);
            }
        }

        private String extractTerminal(String replyStr) {
            if (replyStr.contains("[ERROR]"))  return null;
            int ind = replyStr.indexOf("THW[");
            if (ind > -1) {
                return replyStr.substring(ind + 4, ind + 4 + 15);
            } else return null;
        }

    }

    public static class OpenModuleThread extends Thread {
        HermesShell hermesShell;
        String hermes_id;
        Integer index;
        String token;
        String terminal_imei;
        NativeSQLBean nativeSQLBean;
        Integer size;

        public OpenModuleThread(String hermes_id, Integer index, String token, String terminal_imei, Integer size) {
            hermesShell = new HermesShell(hermes_id, index, token);
            this.hermes_id =  hermes_id;
            this.index = index;
            this.token =  token;
            this.terminal_imei = terminal_imei;
            this.nativeSQLBean = AppBeans.get(NativeSQLBean.class);
            this.size = size;
        }

        @Override
        public void run() {
            super.run();
            int repeat = 3;
            while (repeat > 0) {
                repeat--;
                if (hermesShell.onOffTerminal(terminal_imei, "on").contains("PermitNetworkJoinDevUartResponse")) {
                    nativeSQLBean.deleteFormLastClosedTerminal(terminal_imei);
                    break;
                }
                try { sleep(500); } catch (InterruptedException ignored){}
            }
            System.out.println(index+"/"+size+" !!OpenModuleThread imei = "+terminal_imei);

        }

    }


    public static class DropZBModulesThread extends Thread {
        HermesShell hermesShell;
        String hermes_id;
        Integer index;
        String token;
        String mac;
        NativeSQLBean nativeSQLBean;
        Integer size;
        int iteration;

        public  DropZBModulesThread(String hermes_id, Integer index, String token, String mac, Integer size, int iteration) {
            hermesShell = new HermesShell(hermes_id, index, token);
            this.hermes_id =  hermes_id;
            this.index = index;
            this.token =  token;
            this.mac = mac;
            this.nativeSQLBean = AppBeans.get(NativeSQLBean.class);
            this.size = size;
            this.iteration = iteration;
        }

        @Override
        public void run() {
            super.run();
            int repeat = 3;
            while (repeat > 0) {
                repeat--;
                if (dropModule()) break;
                try { sleep(500); } catch (InterruptedException ignored){}
            }
            System.out.println(index+"/"+size+" iteration="+iteration+" !!DropZBModulesThread mac = "+mac);
        }

        private boolean dropModule() {
            boolean ret = false;
            List<String> retList = hermesShell.clientSSHMain.sendCommandOA("zb.fervid.leaveNetwork " + mac);
            for (String s : retList) {
                if (s.contains("LeaveNetworkDevUartResponse")) {
                    ret = true;
                    break;
                }
            }
            return ret;
        }
    }

}
