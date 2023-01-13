package com.company.pnrservices.service;

import com.company.pnrservices.core.Sm160Helper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.company.pnrservices.core.YodaRESTMethodsHelper.*;
import static java.lang.Thread.sleep;

@Service(SM160Service.NAME)
public class SM160ServiceBean implements SM160Service {

    private static final Logger log = LoggerFactory.getLogger(SM160ServiceBean.class);

    @Override
    public void checkSM160Single(String num, String ip, String port) {
        log.info("!!!Start SM160 single");

        String TOKEN = null;
        try {
            TOKEN = getNewToken();
        } catch (Exception e) {
            System.out.println("!!!getNewToken exception = " + e.getMessage());
            e.printStackTrace();
        }

        int intPort = Integer.parseInt(port);

        if (TOKEN != null) (new Sm160Helper.WorkSm160Thread(num, ip, intPort, TOKEN)).start();

        log.info("!!!End SM160 single");
    }

    @Override
    public void checkSM160(String limit) {
        log.info("!!!Start SM160");
        Integer intLimit;
        try {
            intLimit = Integer.parseInt(limit);
        } catch (Exception e) {
            log.info("!!!Exception in limit param = "+limit+", set limit = 10 000 000, message: "+e.getMessage());
            e.printStackTrace();
            intLimit = 10000000;
        }

        String TOKEN = null;
        try {
            TOKEN = getNewToken();
        } catch (Exception e) {
            System.out.println("!!!getNewToken exception = " + e.getMessage());
            e.printStackTrace();
        }

        if (TOKEN != null) {
            try {
                List<JSONObject> firstList = getFirstForSM160REST(intLimit.toString(), TOKEN);
                log.info("!!!firstList.size = " + firstList.size());
                //System.out.println("!!!firstList.size = "+firstList.size());
                HashMap<UUID, List<MapCheckSm160Sim>> hashMap = new HashMap<>();
                mapSet(hashMap, firstList);
                int index = 0;
                for (Map.Entry<UUID, List<MapCheckSm160Sim>> map : hashMap.entrySet()) {
                    index++;
                    (new Sm160Helper.WorkSm160Thread(index, hashMap.size(), map.getKey(), map.getValue(), TOKEN)).start();
                    try {
                        sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                }
            } catch (Exception e) {
                log.info("!!!"+Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
                return;
            }
        }
        log.info("!!!End SM160");
    }

    @Override
    public void mapSet(HashMap<UUID, List<MapCheckSm160Sim>> hashMap, List<JSONObject> lst) {
        if (lst.size() > 0) {
            lst.forEach(t -> {
                MapCheckSm160Sim sm = new MapCheckSm160Sim();
                UUID id = UUID.fromString(t.getString("id"));
                sm.equip_number = t.getString("equip_number").equals("null") ? null : t.getString("equip_number");
                sm.ne_id = null;//t.getString("ne_id").equals("null") ? null : UUID.fromString(t.getString("ne_id"));
                sm.id = id;
                sm.sim_ip = t.getString("sim_ip").equals("null") ? null : t.getString("sim_ip");
                sm.sim_id = t.getString("sim_id").equals("null") ? null : UUID.fromString(t.getString("sim_id"));
                sm.sim_type = t.getString("sim_type").equals("null") ? null : t.getString("sim_type");
                sm.sm_port = t.getString("sm_port").equals("null") ? null : Integer.parseInt(t.getString("sm_port"));
                sm.mac = t.getString("mac").equals("null") ? null : t.getString("mac");
                sm.network_pan_id = t.getString("networkPanId").equals("null") ? null : t.getString("networkPanId");
                sm.channel_num = t.getString("channelNum").equals("null") ? null : Integer.parseInt(t.getString("channelNum"));

                hashMap.computeIfPresent(id, (k, vl) -> lstAdd((List) vl, sm));
                hashMap.computeIfAbsent(id, k -> {
                    List<MapCheckSm160Sim> l = new ArrayList<>();
                    l.add(sm);
                    return l;
                });
            });
        }
    }

    private List lstAdd(List lst, MapCheckSm160Sim sm) {
        lst.add(sm);
        return lst;
    }




}