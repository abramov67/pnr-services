package com.company.pnrservices.service;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.text.SimpleDateFormat;

import static com.company.pnrservices.core.YodaRESTMethodsHelper.clearTopologyREST;
import static com.company.pnrservices.core.YodaRESTMethodsHelper.getNewToken;


@Service(ClearTopologyService.NAME)
public class ClearTopologyServiceBean implements ClearTopologyService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ClearTopologyServiceBean.class);

    @Override
    public void clearTopology(String limitInt){
        String TOKEN = "";
        try {
            TOKEN = getNewToken();
        } catch (Exception e) {
            System.out.println("!!!getNewToken exception = " + e.getMessage());
            e.printStackTrace();
        }

        log.info(timeFormat(new Date()) + " !!!Запущена чистка топологии. limitInt = " + limitInt);
        clearTopologyREST(TOKEN);
        log.info(timeFormat(new Date()) + " !!!Чистка топологии завершена");

    }

    private String timeFormat(Date tm) {
        String pattern = "HH:mm:ss";
        return new SimpleDateFormat(pattern).format(tm);
    }
}