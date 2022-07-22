package com.company.pnrservices.service;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public interface SM160Service {
    String NAME = "pnrservices_SM160Service";

    public static class MapCheckSm160Sim {
        public String equip_number;
        public UUID ne_id;
        public UUID id;
        public String sim_ip;
        public UUID sim_id;
        public String sim_type;
        public Integer sm_port;
        public String mac;
        public String network_pan_id = null;
        public Integer channel_num = null;
    }

    void checkSM160(String limit);
    void checkSM160Single(String num, String ip, String port);
    void mapSet(HashMap<UUID, List<MapCheckSm160Sim>> hashMap, List<JSONObject> lst);

    }