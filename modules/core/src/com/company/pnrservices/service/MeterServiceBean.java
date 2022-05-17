package com.company.pnrservices.service;

import com.company.pnrservices.core.Sm160Helper;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@Service(MeterService.NAME)
public class MeterServiceBean implements MeterService {

//    @Inject
//    private Persistence persistence;
//
//    @Override
//    public void check_sm160() {
//        //crc8calc();
//        StringBuilder sb = new StringBuilder().append(
//                "select neq.number_ as equip_number, ne.id as ne_id, neq.id as id, " +
//                        "s.ip as sim_ip, s.id as sim_id, coalesce(eqs.SIM_TYPE, 'master') as sim_type, " +
//                        "energ.name as res_name, ts.number_ as tp_number, neq.mac, neq.network_pan_id, neq.channel_num " +
//                        "from ENERSTROYMAIN_NETWORK_ELEMENT ne " +
//                        "   join ENERSTROYMAIN_NETWORK_EQUIPMENT neq " +
//                        "       on neq.id=ne.EQUIPMENT_ID " +
//                        "   join ENERSTROYMAIN_TRANS_STATION ts " +
//                        "       on ts.id=ne.TRANS_STATION_ID " +
//                        "   join ENERSTROYMAIN_NETWORK_ELEMENT_EQUIPPED_SIM eqs " +
//                        "       on eqs.NETWORK_EQUIPMENT_ID = neq.id " +
//                        "   join ENERSTROYMAIN_SIM s" +
//                        "       on s.id = eqs.sim_id " +
//                        "       and ip = '10.78.58.106' " +
//                        //"       on s.id = eqs.sim_id  and ip = '10.112.17.195' " +
////                        "       on s.id = eqs.sim_id  and ip = '10.78.70.193' " +
//                        "   join ENERSTROYMAIN_ENERGY_COMPANY energ " +
//                        "   on ts.res_id = energ.id " +
//                        "where ne.delete_ts is null and eqs.delete_ts is null " +
//                        "   and neq.TYPE_ID in ('3dc4f179-870c-24b6-9af4-50837bb31459','755e8e23-8b1c-9e82-8aec-5192bd3db768') " +
//                        "   and s.id is not null  and s.ip is not null and last_activity is not null " +
//                        "   and ( " +
//                        "                            (" +
//                        "                                (now()::timestamp - last_activity::timestamp)> INTERVAl '6 hours' " +
//                        "                                 and (now()::timestamp - last_bot_connection::timestamp) > INTERVAL '15 minutes' " +
//                        "                            ) " +
//                        "                                or (last_bot_connection is null or last_activity is null) " +
//                        "                          ) order by last_activity desc limit 100"
//        );
//
////        HashMap<UUID, List<Sm160Helper.MapCheckSm160Sim>> hashMap = new HashMap<>();
////        List lst;
////
////        try (Transaction tx = persistence.createTransaction("yoda")) {
////            EntityManager em = persistence.getEntityManager("yoda");
////            Query query = em.createNativeQuery(sb.toString());
////            lst = query.getResultList();
////            tx.commit();
////        }
////
////            System.out.println("!!!lst.size = " + lst.size());
////
////            mapSet(hashMap, lst);
////
////            System.out.println("!!!hashMap.size = " + hashMap.size());
////
////        hashMap.forEach((t1, t2) -> {
////            (new Sm160Helper.WorkSm160Thread(t1, t2)).start();
////        });
//
//    }
//
////    private void mapSet(HashMap<UUID, List<Sm160Helper.MapCheckSm160Sim>> hashMap, List lst) {
////        if (lst.size() > 0) {
////            for (Iterator it = lst.iterator(); it.hasNext(); ) {
////                Object[] row = (Object[]) it.next();
////
////                Sm160Helper.MapCheckSm160Sim sm = new Sm160Helper.MapCheckSm160Sim();
////                sm.equip_number = row[0].toString();
////                //sm. = UUID.fromString(row[1].toString());
////                sm.ne_id = UUID.fromString(row[2].toString());
////                sm.sim_ip = row[3].toString();
////                sm.sim_id = UUID.fromString(row[4].toString());
////                sm.sim_type = row[5].toString();
////                sm.res_name = row[6].toString();
////                sm.tp_number = Integer.decode(row[7].toString());
////                if (row[8] == null) sm.mac = null; else sm.mac = row[8].toString();
////                if (row[9] == null) sm.network_pan_id = null; else sm.network_pan_id = row[9].toString();
////                if (row[10] == null) sm.channel_num = null; else sm.channel_num = Integer.decode(row[10].toString());
////
////
////                hashMap.computeIfPresent(UUID.fromString(row[1].toString()), (k, vl) -> {
////                    return lstAdd((List) vl, sm);
////                });
////                hashMap.computeIfAbsent(UUID.fromString(row[1].toString()), k -> {
////                    List<Sm160Helper.MapCheckSm160Sim> l = new ArrayList<>();
////                    l.add(sm);
////                    return l;
////                });
////
////            }
////        }
////    }
////
////    private List lstAdd(List lst, Sm160Helper.MapCheckSm160Sim sm) {
////        lst.add(sm);
////        return lst;
////    }
//

}