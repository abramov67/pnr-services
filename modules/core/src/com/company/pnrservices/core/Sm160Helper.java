package com.company.pnrservices.core;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component("pnrservices_Sm160Helper")
public class Sm160Helper {

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


    public static class WorkSm160Thread extends Thread {
        UUID id;
        List<MapCheckSm160Sim> ip_map;
        String TOKEN;
        int index;
        int size;

        public WorkSm160Thread(int index, int size, UUID id, List<MapCheckSm160Sim> p_ip_map, String token) {
            this.id = id;
            this.ip_map = p_ip_map;
            this.TOKEN = token;
            this.index = index;
            this.size = size;
        }

        @Override
        public void run() {
            super.run();
            workSM160();
        }

        public void workSM160() {
            for (MapCheckSm160Sim sim : ip_map) {
                if (sim.sm_port != null) {
                    MeterGSM m = new MeterGSM(index, size, sim.sim_ip, sim.sm_port, TOKEN, id, sim.equip_number);
                    try {
                        m.setResult();
                    } catch (IOException e) {
                        System.out.println("!!!workSM160 IOException: "+e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}
