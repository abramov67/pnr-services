package com.company.pnrservices.service;

import com.company.pnrservices.core.HermesShell;
import com.company.pnrservices.core.NativeSQLBean;
import com.haulmont.cuba.core.global.AppBeans;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service(DropModulesService.NAME)
public class DropModulesServiceBean implements DropModulesService {
    @Inject
    private NativeSQLBean nativeSQLBean;

    String hermes_id = "fafc655f-8feb-49ef-d4d0-e61c82e2f86f";

    @Override
    public void topologyUpdate() {
        List lst = nativeSQLBean.getListMain("select mac, num from get_mac_for_drop()");
        System.out.println("!!!Main lst.size = " + lst.size());
        if (lst != null) {
            Integer index = 0;
            for(Object row :  lst) {
                index++;
                String mac = ((Object[]) row)[0].toString();
                List meterIds = nativeSQLBean.getListYoda("select id from enerstroymain_meter where delete_ts is null and mac = '" + mac + "'");
                if (meterIds != null) {
                    for (Object row_meter : meterIds) {
                        UpdaterThread ut = new UpdaterThread(index, mac, row_meter.toString(), hermes_id);
                        ut.start();
                    }
                }
            }
        }
    }

    static class UpdaterThread extends Thread {
        Integer index;
        String mac;
        String meter_id;
        String hermes_id;
        String terminal_id;
        HermesShell hermesShell;
        NativeSQLBean nativeSQLBean;

        public UpdaterThread(Integer index, String mac, String meter_id, String hermes_id) {
            this.index = index;
            this.mac = mac;
            this.meter_id = meter_id;
            this.hermes_id = hermes_id;
            this.hermesShell = new HermesShell(hermes_id, index);
            this.nativeSQLBean = AppBeans.get(NativeSQLBean.class);
        }

        @Override
        public void run() {
            super.run();
            getTerminalFromShell();
        }

        private void getTerminalFromShell() {
            String replyStr = hermesShell.getTerminalID(mac);
            String s = extractTerminal(replyStr);
            if (s != null) {
                terminal_id = s;
                updateTopology();
            }
        }

        private String extractTerminal(String replyStr) {
            if (replyStr.contains("[ERROR]"))  return null;
            int ind = replyStr.indexOf("THW[");
            if (ind > -1) {
                return replyStr.substring(ind + 4, ind + 4 + 15);
            } else return null;
        }

        private void updateTopology() {
            if (terminal_id != null && meter_id != null && mac != null) {
                Object res = inTopology();
                String sql;
                if (res != null) {
                    sql = " update enerstroymain_topology set \n" +
                            " UPDATE_TIME=now(), TERMINAL='"+terminal_id+"', eui='"+mac+"', version=version+1, " +
                            " UPDATE_TS=now(), UPDATED_BY='patrik', options='updater' \n" +
                            " where id = '"+res.toString()+"'";
                } else {
                    sql = " insert into ENERSTROYMAIN_TOPOLOGY (id, version, CREATED_BY, CREATE_TS, CREATE_TIME, \n" +
                        " UPDATE_TIME, TERMINAL, EUI, HERMES_ID, METER_ID, options) values ( \n" +
                        " newid(), 1, 'patrik', now(), '2001-01-01 00:00:00', \n" +
                        " now(), '"+terminal_id+"', '"+mac+"', '"+hermes_id+"', '"+meter_id+"', 'updater')";
                }
                nativeSQLBean.executeYoda(sql);
            }
        }

        private Object inTopology() {
            Object ret;
            String sql = " select id from ENERSTROYMAIN_TOPOLOGY where hermes_id='"+hermes_id+"' and meter_id = '"+meter_id+"'";
            ret = nativeSQLBean.getSingleYoda(sql);
            return ret;
        }

    }
}