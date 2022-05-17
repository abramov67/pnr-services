package com.company.pnrservices.service;

import com.company.pnrservices.core.HermesShell;
import com.company.pnrservices.core.UpdateTopologyHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.company.pnrservices.core.YodaRESTMethodsHelper.getHermesIDListForUpdateTopologyREST;
import static com.company.pnrservices.core.YodaRESTMethodsHelper.getNewToken;

@Service(UpdateTopologyService.NAME)
public class UpdateTopologyServiceBean implements UpdateTopologyService {

    private static final Logger log = LoggerFactory.getLogger(UpdateTopologyServiceBean.class);

    @Override
    public void updateTopology() {
        log.info("!!!Start UpdateTopology");

        String TOKEN = null;
        try {
            TOKEN = getNewToken();
        } catch (Exception e) {
            System.out.println("!!!getNewToken exception = " + e.getMessage());
            e.printStackTrace();
        }

        if (TOKEN != null) {
            String finalTOKEN = TOKEN;
            List<String> hermesesList = getHermesIDListForUpdateTopologyREST(TOKEN);
            System.out.println("!!!hermesesList.size = "+hermesesList.size());
            hermesesList.forEach(hermes_id -> {
                HermesShell hs = new HermesShell(hermes_id, 0, finalTOKEN);
                List<JSONObject> terminalsShell = hs.getTerminalsID();

                int sizeTerminal = terminalsShell.size();
                int indexTerminal = 0;
                for (JSONObject t : terminalsShell) {
                    indexTerminal++;
                    new UpdateTopologyHelper
                            .TerminalUpdateTopologyThread(indexTerminal, sizeTerminal, finalTOKEN, t.put("hermes_id", hermes_id))
                            .upsertTerminal();
//                    new UpdateTopologyHelper
//                            .TerminalUpdateTopologyThread(indexTerminal, sizeTerminal, finalTOKEN, t.put("hermes_id", hermes_id))
//                            .start();
                }

                indexTerminal = 0;
                Integer globalCounter = 0;
                for (JSONObject t : terminalsShell) {
                    indexTerminal++;
                    String terminal = new JSONArray(t.get("terminalHwId").toString()).get(0).toString();

                    List<JSONObject> metersShell = hs.getMetersID(terminal)
                            .stream()
                            .filter(jsn -> !jsn.isNull("online"))
                            .collect(Collectors.toList());

                    int indexMeter = 0;
                    int sizeMeter = metersShell.size();
                    for (JSONObject meter : metersShell) {
                        globalCounter++;
                        indexMeter++;
                        new UpdateTopologyHelper
                                .MeterUpdateTopologyThread(globalCounter, indexTerminal, sizeTerminal, indexMeter, sizeMeter, finalTOKEN, meter)
                                .upsertMeter();
//                        new UpdateTopologyHelper
//                        .MeterUpdateTopologyThread(globalCounter, indexTerminal, sizeTerminal, indexMeter, sizeMeter, finalTOKEN, meter)
//                        .start();
//                        try { sleep(30);} catch (InterruptedException ignore) { }
                    }
                }
            });
        }
        log.info("!!!End UpdateTopology");
    }
}