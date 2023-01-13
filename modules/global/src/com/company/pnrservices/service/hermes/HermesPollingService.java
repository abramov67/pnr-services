package com.company.pnrservices.service.hermes;

import java.util.List;

public interface HermesPollingService {
    String NAME = "pnrservices_HermesPollingService";

    void poll(String command, String mac, int index, int size, int indexPart);
    List<String> getListMacFromYoda();
}