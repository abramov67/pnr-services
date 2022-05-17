package com.company.pnrservices.core;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import com.company.pnrservices.core.Sm160Helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.apache.poi.util.HexDump.byteToHex;

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
