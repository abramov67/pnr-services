package com.company.pnrservices.service;

import com.company.pnrservices.core.HermesShell;
import com.company.pnrservices.core.UpdateTopologyHelper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.company.pnrservices.core.YodaRESTMethodsHelper.getNewToken;

@Service(UpdateTopologyService.NAME)
public class UpdateTopologyServiceBean implements UpdateTopologyService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(UpdateTopologyServiceBean.class);
    String hermes_id = "fafc655f-8feb-49ef-d4d0-e61c82e2f86f";

    @Override
    public void updateTopology() {
        String TOKEN = null;
        try {
            TOKEN = getNewToken();
        } catch (Exception e) {
            System.out.println("!!!getNewToken exception = " + e.getMessage());
            e.printStackTrace();
        }

        if (TOKEN != null) {
            HermesShell hs = new HermesShell(hermes_id, 0, TOKEN);
            System.out.println("!!!UpdateTopology start token = " + TOKEN);
            List<JSONObject> terminalsShell = hs.getTerminalsID();
            log.info("!!!terminals.size = " + terminalsShell.size());
            int index = 0;
            int size = terminalsShell.size();
            String finalTOKEN = TOKEN;
            terminalsShell.forEach(t -> new UpdateTopologyHelper
                    .UpdateTopologyThread(index, size, finalTOKEN, t.put("hermes_id", hermes_id))
                    .start());
        }
    }


    //        class Terminal {
//            final String imeiShell;
//            final String idYoda;
//            final String number
//
//            public Terminal(String imeiShell, String idYoda, String number) {
//                this.imeiShell = imeiShell;
//                this.idYoda = idYoda;
//                this.number = number;
//            }
//        }

//            List<String> terminalsYoda = getTerminalsForUpdateTopologyREST(TOKEN);
            //List<Terminal> resultTerminals = new ArrayList<>();
            //terminalsShell.forEach(log::info);

//             terminalsShell.forEach(jsnShell -> {
//                 log.info("!!!jsnShell="+jsnShell.toString());
//                 JSONArray ja = new JSONArray(jsnShell.get("terminalHwId").toString());
//                 String imeiShell = ja.get(0).toString();
//                 String fnd = find(imeiShell, terminalsYoda);
//                 String idYoda = null;
//                 if (!fnd.equals("")) {
//                     JSONArray jsn = new JSONArray(fnd);
//                     idYoda = jsn.getString(1);
//                 }
//                 resultTerminals.add(new Terminal(imeiShell, idYoda));
//             });
//
//             int index = 0;
//             int size = resultTerminals.size();
//             for (Terminal terminal : resultTerminals) {
//                 index++;
//
//                 (new UpdateTopologyHelper
//                         .UpdateTopologyThread(index, size, terminal.imeiShell, terminal.idYoda, TOKEN))
//                         .start();
//             }




//            for (Terminal terminal : resultTerminals) {
//                index++;
//
//                (new UpdateTopologyHelper
//                        .UpdateTopologyThread(index, size, terminal.imeiShell, terminal.idYoda, TOKEN))
//                        .start();
//            }

//        }
//    }

//    private String find(String str, List<String> list) {
//        String ret = "";
//        try {
//            ret = Arrays.stream(list.stream().filter(s -> s.contains(str)).toArray()).findFirst().get().toString();
//        } catch (NoSuchElementException ignored) {  }
//        return ret;
//    }

}