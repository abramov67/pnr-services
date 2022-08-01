package com.company.pnrservices.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

import com.company.pnrservices.core.UpdateSerialHelper.*;
import static com.company.pnrservices.core.YodaRESTMethodsHelper.*;
import static java.lang.Thread.sleep;

@Service(UpdateSerialService.NAME)
public class UpdateSerialServiceBean implements UpdateSerialService {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(UpdateSerialServiceBean.class);
    //String hermes_id = "fafc655f-8feb-49ef-d4d0-e61c82e2f86f";
    String hermes_id = "cd0acf73-3026-0684-7840-249d7f2bd758";

    @Override
    public void getSerialFromModules() {
        List<JSONObject> macList = null;
        String TOKEN = "";
        try {
            TOKEN = getNewToken();
        } catch (Exception e) {
            System.out.println("!!!getNewToken exception = " + e.getMessage());
            e.printStackTrace();
        }

        log.info("!!!Start UpdateSerial");
        macList = getMACListForUpdateSerialREST(hermes_id, "100000", TOKEN,"30");

        hermes_id = "fafc655f-8feb-49ef-d4d0-e61c82e2f86f";

        int index = 0;
        for (JSONObject item : macList) {
            String mac = item.getString("mac");
            String meter_id = item.getString("meter_id");
            String type = item.getString("type").toLowerCase();
//            if (type.contains("zb-m200")) (new UpdateSerialThread(hermes_id, index++, TOKEN, meter_id, mac, type, macList.size())).start();
            (new UpdateSerialThread(hermes_id, ++index, TOKEN, meter_id, mac, type, macList.size(), true)).start();
            try {
                sleep(50);
            } catch (Exception ignore) {}
        }

        log.info("!!!End UpdateSerial selectList = "+macList.size()+", runningCount = "+index+" threads");
    }

}