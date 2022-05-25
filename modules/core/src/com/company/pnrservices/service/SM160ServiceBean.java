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

    private static final Logger log = LoggerFactory.getLogger(UpdateTopologyServiceBean.class);

    @Override
    public void checkSM160() {
        log.info("!!!Start SM160");

        String TOKEN = null;
        try {
            TOKEN = getNewToken();
        } catch (Exception e) {
            System.out.println("!!!getNewToken exception = " + e.getMessage());
            e.printStackTrace();
        }

        if (TOKEN != null) {
            List<JSONObject> firstList = getFirstForSM160REST(TOKEN);
            System.out.println("!!!firstList.size = "+firstList.size());
            HashMap<UUID, List<Sm160Helper.MapCheckSm160Sim>> hashMap = new HashMap<>();
            mapSet(hashMap, firstList);
            int  index = 0;
            for (Map.Entry<UUID, List<Sm160Helper.MapCheckSm160Sim>> map : hashMap.entrySet()) {
                index++;
                (new Sm160Helper.WorkSm160Thread(index, hashMap.size(), map.getKey(), map.getValue(), TOKEN)).start();
                try {
                    sleep((long) (Math.random() * 300));
                } catch (InterruptedException ignored) {      }
            }
        }
        log.info("!!!End SM160");
    }

    private void mapSet(HashMap<UUID, List<Sm160Helper.MapCheckSm160Sim>> hashMap, List<JSONObject> lst) {
        if (lst.size() > 0) {
            lst.forEach(t -> {
                Sm160Helper.MapCheckSm160Sim sm = new Sm160Helper.MapCheckSm160Sim();
                UUID id = UUID.fromString(t.getString("id"));
                sm.equip_number = t.getString("equip_number").equals("null") ? null : t.getString("equip_number");
                sm.ne_id = t.getString("ne_id").equals("null") ? null : UUID.fromString(t.getString("ne_id"));
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
                    List<Sm160Helper.MapCheckSm160Sim> l = new ArrayList<>();
                    l.add(sm);
                    return l;
                });

            });
        }
    }

    private List lstAdd(List lst, Sm160Helper.MapCheckSm160Sim sm) {
        lst.add(sm);
        return lst;
    }




}