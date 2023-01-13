package com.company.pnrservices.web.hermespolling;

import com.company.pnrservices.service.hermes.HermesPollingHelper;
import com.company.pnrservices.service.hermes.HermesPollingService;
import com.company.pnrservices.service.hermes.HermesPollingService.*;
import com.google.common.collect.Lists;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.defaults.Default;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.components.TextArea;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;


@UiController("pnrservices_HermesPollingScreen")
@UiDescriptor("hermes-polling-screen.xml")
public class HermesPollingScreen extends Screen {
    @Inject
    private TextField<String> idMACText;
    @Inject
    private TextField<String> idCommandText;
    @Inject
    private HermesPollingService hermesPollingService;
    @Inject
    private TextArea<String> idResultArea;

    public void runCommand() throws InterruptedException {

        List<String> macList = hermesPollingService.getListMacFromYoda();
        int size = macList.size();
        int index = 1;
        int indexPart = 1;
        System.out.println(dateTimeFormat(new Date())+" !!!Start HermesPolling macList.size = "+macList.size());
        ListIterator<List<String>> listIterator = Lists.partition(macList, 1000).listIterator();
                while (listIterator.hasNext()) {
                    List<String> macsListPart = listIterator.next();
                    indexPart++;
                    for (String mac : macsListPart) {
                        hermesPollingService.poll("topology.getMeterInfo", mac, index, size, indexPart);
                        index++;
                        sleep(70);
                    }
                    if (index > 3200) break;
                    System.out.println("!!!im slipping tsss");
                    sleep(30000);
                }

        System.out.println(dateTimeFormat(new Date())+" !!!End HermesPolling");
    }

    public static String dateTimeFormat(Date tm) {
        if (tm == null) return "null";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return new SimpleDateFormat(pattern).format(tm);
    }

//    public static String deltaTimeFormat(LocalTime startTime, LocalTime endTime) {
//        return formatDuration(Duration.between(endTime, startTime));
//    }
//
//    public static String formatDuration(Duration duration) {
//        return String.format("%02d sec",
//                Math.abs(duration.getSeconds()));
//    }

}