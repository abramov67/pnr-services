package com.company.pnrservices.core;

import com.haulmont.cuba.core.global.AppBeans;

import java.util.List;

public class HermesShell {
    NativeSQLBean nativeSQLBean;
    String hermes_ip, login, password;
    int port;
    ClientSSHMain clientSSHMain;
    int index;

    public HermesShell(String hermes_id, int index) {
        nativeSQLBean = AppBeans.get(NativeSQLBean.class);
        Object[] row = (Object[]) nativeSQLBean.getSingleYoda("select hermes_ip, user_, secret, port_ssh from enerstroymain_hermes where id = '" + hermes_id + "'");
        if (row != null) {
            this.hermes_ip = (String) row[0];
            this.login = (String) row[1];
            this.password = (String) row[2];
            this.port = Integer.parseInt((String) row[3]);
            this.index = index;
            //System.out.println("!!!hermes_ip = " + hermes_ip + ", user_ = " + user + ", secret = " + secret + ", port_ssh = " + port_ssh);
        }
    }

    public String getTerminalID(String mac) {
        StringBuilder sb = new StringBuilder();
        if (clientSSHMain == null) clientSSHMain = new ClientSSHMain(hermes_ip, port, login, password, mac);
        List<String> reply = clientSSHMain.sendCommandOA("topology.getMeterInfo " + mac);
        if (reply != null) {
            for (String s : reply) {
                sb.append(s);
            }
            //System.out.println(index + " !!!mac = " + mac + ", reply = " + sb.toString());
        } else System.out.println(index + " !!!mac = " + mac + ", reply = null");
        return sb.toString();
    }

//    public List<String> getTerminalList(List<String> macs) {
//        StringBuilder sb = new StringBuilder();
//        if (clientSSHMain == null) clientSSHMain = new ClientSSHMain(hermes_ip, port, login, password, "");
//        return clientSSHMain.sendCommandMACList(macs);
////        List<String> replyList = clientSSHMain.sendCommandMACList(macs);
////        if (replyList != null) {
////            for (String t : replyList) {
////                String[] arr = t.split(";");
////
////                System.out.println(" !!!mac = "+arr[0]+", reply = "+arr[1]);
////            }
////        } else System.out.println(" !!!reply = null");
////        return sb.toString();
//    }

}
