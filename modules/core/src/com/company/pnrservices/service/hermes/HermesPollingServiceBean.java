package com.company.pnrservices.service.hermes;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.company.pnrservices.core.YodaRESTMethodsHelper.getMacsListForHermesREST;
import static com.company.pnrservices.core.YodaRESTMethodsHelper.getNewToken;

@Service(HermesPollingService.NAME)
public class HermesPollingServiceBean implements HermesPollingService {
    @Inject
    private HermesConfig hermesConfig;

    public void poll(String command, String mac, int index, int size, int indexPart) {
        String ip = hermesConfig.getHermesPermSSHUrl();
        int port = hermesConfig.getHermesPermSSHPort();
        String login = hermesConfig.getHermesPermUser();
        String pwd = hermesConfig.getHermesPermPassword();
        new HermesPollingHelper.HermesPollingThread(indexPart, index, size, ip, port, login, pwd, mac, "").start();
    }

    public List<String> getListMacFromYoda(){
        List<String> retList = new ArrayList<>();
        String TOKEN = null;
        try {
            TOKEN = getNewToken();
        } catch (Exception e) {
            System.out.println("!!!getNewToken exception = " + e.getMessage());
            e.printStackTrace();
        }
        if (TOKEN != null) {
            JSONArray jsonArray = new JSONArray(getMacsListForHermesREST(TOKEN));
            System.out.println("!!!Return jsonArray.length = "+jsonArray.length());
            retList = jsonArray.toList().stream().map(Object::toString)
                    .collect(Collectors.toList());
        }
        return retList;
    }

}