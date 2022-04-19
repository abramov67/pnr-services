package com.company.pnrservices.service;

import com.company.pnrservices.core.DropModulesHelper;
import com.company.pnrservices.core.HermesShell;
import com.company.pnrservices.core.NativeSQLBean;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static com.company.pnrservices.core.DropModulesHelper.*;
import static com.company.pnrservices.core.YodaRESTMethodsHelper.*;

@Service(DropModulesService.NAME)
public class DropModulesServiceBean implements DropModulesService {
    @Inject
    private NativeSQLBean nativeSQLBean;

    String hermes_id = "fafc655f-8feb-49ef-d4d0-e61c82e2f86f";

    @Override
    public void topologyUpdate() {
        String TOKEN = "";
        try {
            TOKEN = getNewToken();
        } catch (Exception e) {
            System.out.println("!!!getNewToken exception = " + e.getMessage());
            e.printStackTrace();
        }

        List lst = nativeSQLBean.getListMain("select mac, num from get_mac_for_drop()");
        System.out.println("!!!topologyUpdate mac list size = " + lst.size());

        if (!TOKEN.equals("")) {
            Integer index = 0;
            for (Object row : lst) {
                index++;
                String mac = ((Object[]) row)[0].toString();
                List<String> meterIds = getMeterIds(mac, TOKEN);
                if (meterIds != null) {
                    for (Object row_meter : meterIds) {
                        (new DropModulesHelper.UpdaterThread(index, mac, row_meter.toString(), hermes_id, TOKEN, lst.size())).start();
                    }
                }
            }
        }
    }

    private List<String> getMeterIds(String macs, String token) {
        return getMeterIdREST(macs, token);
    }

    @Override
    public void openClosedTerminals() {
        String TOKEN = "";
        try {
            TOKEN = getNewToken();
        } catch (Exception e) {
            System.out.println("!!!getNewToken exception = " + e.getMessage());
            e.printStackTrace();
        }

        class Result {
            final String imei;
            final String hermes_id;
            public Result(String imei, String hermes_id){
                this.imei = imei;
                this.hermes_id = hermes_id;
            }
        }

        List<Result> resultList = (List<Result>) nativeSQLBean
                .getListMain("select imei, hermes_id from dev_last_closed_terminals")
                .stream()
                .map((t) -> new Result(((Object[]) t)[0].toString(), ((Object[]) t)[1].toString()))
                .collect(Collectors.toList());

        System.out.println("!!!OpenClosedTerminals list size = " + resultList.size());

        Integer index = 0;
        for (Result row : resultList) {
            index++;
            (new DropModulesHelper.OpenModuleThread(row.hermes_id, index, TOKEN, row.imei, resultList.size())).start();
        }
    }


    @Override
    public void dropZBModules(String withCloseTerminals) {
        String TOKEN = "";
        try {
            TOKEN = getNewToken();
        } catch (Exception e) {
            System.out.println("!!!getNewToken exception = " + e.getMessage());
            e.printStackTrace();
        }

        class Result {
            final String mac;
            final String num;
            public Result(String mac, String num){
                this.mac = mac;
                this.num = num;
            }
        }

        List<Result> resultList = (List<Result>) nativeSQLBean
                .getListMain("SELECT mac, num FROM dev_zbdropmodules")
                .stream()
                .map((t) -> new Result(((Object[]) t)[0].toString(), ((Object[]) t)[1].toString()))
                .collect(Collectors.toList());

        System.out.println("!!!dropZBModules list size = " + resultList.size());

        if (withCloseTerminals.equals("1")) {
            clearLastCloseTerminals();
                HermesShell hermesShell = new HermesShell(hermes_id, 0, TOKEN);
                getListForCloseTerminalsREST(hermes_id, TOKEN).forEach((t) -> {
                    hermesShell.onOffTerminal(t, "off");
                    saveClosedTerminal(t, hermes_id);
                });
        }

        Integer index = 0;
        for (int i = 0; i < 2; i++)
            for (Result row : resultList) {
                index++;
                (new DropModulesHelper.DropZBModulesThread(hermes_id, index, TOKEN, row.mac, resultList.size(), i)).start();
            }
    }

    private void saveClosedTerminal(String imei, String hermes_id) {
        nativeSQLBean.executeMain("insert into last_closed_terminals(imei, hermes_id) values('"+imei+"', '"+hermes_id+"')");
    }

}