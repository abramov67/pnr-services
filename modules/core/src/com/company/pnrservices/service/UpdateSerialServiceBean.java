package com.company.pnrservices.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

import com.company.pnrservices.core.UpdateSerialHelper.*;
import static com.company.pnrservices.core.YodaRESTMethodsHelper.*;

@Service(UpdateSerialService.NAME)
public class UpdateSerialServiceBean implements UpdateSerialService {
    String hermes_id = "fafc655f-8feb-49ef-d4d0-e61c82e2f86f";
    //String hermes_id = "cd0acf73-3026-0684-7840-249d7f2bd758";

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

        macList = getMACListForUpdateSerialREST(hermes_id, "5000", TOKEN,"300");

        int index = 0;
        for (JSONObject item : macList) {
            String mac = item.getString("mac");
            String meter_id = item.getString("meter_id");
            String type = item.getString("type").toLowerCase();
            if (type.contains("zb-m200")) (new UpdateSerialThread(hermes_id, index++, TOKEN, meter_id, mac, type, macList.size())).start();
        }
    }

}