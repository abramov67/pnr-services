package com.company.pnrservices.service;

import com.company.pnrservices.core.DropModulesHelper;
import com.company.pnrservices.core.HermesShell;
import com.company.pnrservices.core.NativeSQLBean;
import com.company.pnrservices.entity.LastClosedTerminals;
import com.company.pnrservices.entity.Zbdropmodules;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

import static com.company.pnrservices.core.DropModulesHelper.*;
import static com.company.pnrservices.core.YodaRESTMethodsHelper.*;

@Service(DropModulesService.NAME)
public class DropModulesServiceBean implements DropModulesService {
    @Inject
    private NativeSQLBean nativeSQLBean;

    String hermes_id = "fafc655f-8feb-49ef-d4d0-e61c82e2f86f";
    @Inject
    private Logger log;

    @Override
    public void topologyUpdate() {
        String TOKEN = "";
        try {
            TOKEN = getNewToken();
        } catch (Exception e) {
            System.out.println("!!!getNewToken exception = " + e.getMessage());
            e.printStackTrace();
        }

        log.info("!!!DropModules TopologyUpdate Start");
        List <Zbdropmodules> zbMACList = nativeSQLBean.getMACForDropModules();

        System.out.println("!!!DropModules topologyUpdate macList.size = " + zbMACList.size());

        if (!TOKEN.equals("")) {
            Integer index = 0;
            for (Zbdropmodules row : zbMACList) {
                index++;
                String mac = row.getMac();
                List<String> meterIds = getMeterIds(mac, TOKEN);
                for (Object row_meter : meterIds) {
                    (new UpdaterThread(index, mac, row_meter.toString(), hermes_id, TOKEN, zbMACList.size())).start();
                }
            }
            log.info("!!!DropModules TopologyUpdate End threads run = "+index);
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

        log.info("!!!DropModules OpenClosedTerminal Start");

        List<LastClosedTerminals> lastClosedTerminalsList = nativeSQLBean.getLastClosedTerminals();

        System.out.println("!!!DropModules OpenClosedTerminals list size = " + lastClosedTerminalsList.size());

        Integer index = 0;
        for (LastClosedTerminals row : lastClosedTerminalsList) {
            index++;
            (new OpenModuleThread(row.getHermes(), index, TOKEN, row.getImei(), lastClosedTerminalsList.size())).start();
        }
        log.info("!!!DropModules OpenClosedTerminal end, threads running = "+index);
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

        log.info("!!!DropModules DropZBModules Start");
        List<Zbdropmodules> macList = nativeSQLBean.getMACForDropModules();

        System.out.println("!!!dropZBModules list size = " + macList.size());

        int terminalIndex = 0;
        if (withCloseTerminals.equals("1")) {
            clearLastCloseTerminals();
                HermesShell hermesShell = new HermesShell(hermes_id, 0, TOKEN);
                List<String> terminals = getListForCloseTerminalsREST(hermes_id, TOKEN);
                for (String t : terminals) {
                    terminalIndex++;
                    hermesShell.onOffTerminal(t, "off");
                    saveClosedTerminal(t, hermes_id);
                }
            log.info("!!!DropModules DropZBModules with closed terminals "+terminalIndex+" from "+terminals.size());
        }

        Integer index = 0;
        for (int i = 0; i < 2; i++) {
            for (Zbdropmodules row : macList) {
                index++;
                (new DropZBModulesThread(hermes_id, index, TOKEN, row.getMac(), macList.size(), i)).start();
            }
        }
        log.info("!!!DropModules DropZBModules end, threads runnings = "+index);

    }

    private void saveClosedTerminal(String imei, String hermes_id) {
        nativeSQLBean.insertLastClosedTerminal(imei, hermes_id);
    }

}