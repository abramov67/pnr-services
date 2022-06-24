package com.company.pnrservices.core;

import ch.ethz.ssh2.StreamGobbler;
import com.company.pnrservices.entity.SM160Log;
import com.company.pnrservices.entity.SM160LogDiscovery;
import com.company.pnrservices.entity.SM160LogOperations;
import com.company.pnrservices.service.UpdateTopologyServiceBean;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.security.app.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Component("pnrservices_Sm160Helper")
public class Sm160Helper {

    public static SM160LogDiscovery logSm160Discovery(String logId, String mac, String message) {
        Authentication authentication = AppBeans.get(Authentication.class);
        DataManager dataManager = AppBeans.get(DataManager.class);
        authentication.begin();
        SM160LogDiscovery sm160LogDiscovery;
        sm160LogDiscovery = dataManager.create(SM160LogDiscovery.class);
        sm160LogDiscovery.setSm160Log(dataManager.load(SM160Log.class).id(UUID.fromString(logId)).one());
        sm160LogDiscovery.setMac(mac);
        sm160LogDiscovery.setMessage(message);
        dataManager.commit(sm160LogDiscovery);
        return sm160LogDiscovery;
    }

    public static SM160LogOperations logSm160Operations(String logId, String type, String message, String stackTrace) {
        Authentication authentication = AppBeans.get(Authentication.class);
        DataManager dataManager = AppBeans.get(DataManager.class);
        authentication.begin();
        SM160LogOperations sm160LogOperations;
        sm160LogOperations = dataManager.create(SM160LogOperations.class);
        sm160LogOperations.setSm160Log(dataManager.load(SM160Log.class).id(UUID.fromString(logId)).one());
        sm160LogOperations.setType(type);
        sm160LogOperations.setMessage(message);
        sm160LogOperations.setStackTrace(stackTrace);
        dataManager.commit(sm160LogOperations);
        return sm160LogOperations;
    }

    public static SM160Log logSm160(String ip, String num, Integer port) {
        Authentication authentication = AppBeans.get(Authentication.class);
        DataManager dataManager = AppBeans.get(DataManager.class);
        authentication.begin();
        SM160Log sm160Log;
        List<SM160Log> sm160LogList = dataManager.load(SM160Log.class)
                .query("select s from pnrservices_SM160Log s where s.ip = :ip")
                .parameter("ip", ip)
                .list();
        if (sm160LogList.size() > 0) {
            sm160Log = sm160LogList.get(0);
            logRemoveOldSm160(sm160Log);
            sm160Log.setStartTime(new Date());
            sm160Log.setEndTime(null);
        } else {
            sm160Log = dataManager.create(SM160Log.class);
            sm160Log.setStartTime(new Date());
        }
        sm160Log.setNum(num);
        sm160Log.setIp(ip);
        sm160Log.setPort(port);
        dataManager.commit(sm160Log);
        return sm160Log;
    }

    private static void logRemoveOldSm160(SM160Log sm160Log){
        Persistence persistence = AppBeans.get(Persistence.class);
        String query = "select o from pnrservices_SM160LogOperations o " +
                " where  o.sm160Log = :sm";
        String query2 = "select o from pnrservices_SM160LogDiscovery o " +
                " where  o.sm160Log = :sm";
        try(Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            List<SM160LogOperations> result = em.createQuery(query, SM160LogOperations.class)
                    .setParameter("sm", sm160Log)
                    .getResultList();
            em.setSoftDeletion(false);
            result.forEach(em::remove);

            List<SM160LogDiscovery> result2 = em.createQuery(query2, SM160LogDiscovery.class)
                    .setParameter("sm", sm160Log)
                    .getResultList();
            em.setSoftDeletion(false);
            result2.forEach(em::remove);

            tx.commit();
        }
    }

    public static void logSm160SetEnd(SM160Log sm160Log) {
        Authentication authentication = AppBeans.get(Authentication.class);
        DataManager dataManager = AppBeans.get(DataManager.class);
        authentication.begin();
        sm160Log = dataManager.load(SM160Log.class).query("select l from pnrservices_SM160Log l where l.id = :id")
                .parameter("id", sm160Log.getId()).one()
        ;
        sm160Log.setEndTime(new Date());
        dataManager.commit(sm160Log);
    }

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

        private static final Logger log = LoggerFactory.getLogger(UpdateTopologyServiceBean.class);

        boolean saveToYoda = true;
        UUID id;
        List<MapCheckSm160Sim> ip_map;
        String TOKEN;
        int index;
        int size;
        List<SM160Log> sm160LogList;
        //Single
        String num;
        String ip;
        int port;

        public WorkSm160Thread(int index, int size, UUID id, List<MapCheckSm160Sim> p_ip_map, String token) {
            this.id = id;
            this.ip_map = p_ip_map;
            this.TOKEN = token;
            this.index = index;
            this.size = size;
            this.sm160LogList = new ArrayList<>();
        }

        public WorkSm160Thread(String num, String ip, int port) {
            this.ip = ip;
            this.num = num;
            this.port = port;
            saveToYoda =  false;
            this.sm160LogList = new ArrayList<>();
        }

        @Override
        public void run() {
            if (saveToYoda) {
                workSM160();
            } else {
                workSM160Single();
            }
//            super.run();
//            ExecutorService es = Executors.newSingleThreadExecutor();
//            TimeLimiter tl = SimpleTimeLimiter.create(es);
//            try {
//                tl.callWithTimeout(() -> {
//                    for (MapCheckSm160Sim sim : ip_map) {
//                        SM160Log sm160Log = logSm160(sim.sim_ip, sim.equip_number, sim.sm_port);
//                        sm160LogList.add(sm160Log);
//                        if (sim.sm_port != null) {
//                            MeterGSM m = new MeterGSM(index, size, sim.sim_ip, sim.sm_port,
//                                    TOKEN, id, sim.equip_number,
//                                    sm160Log.getId().toString());
//                            try {
//                                m.setResult();
//                            } catch (IOException e) {
//                                System.out.println("!!!workSM160 IOException: "+e.getMessage());
//                                //e.printStackTrace();
//                            }
//                        }
//                    }
//                    return null;
//                }, 3000L, TimeUnit.SECONDS);
//            } catch (TimeoutException | UncheckedIOException e){
//                System.out.println(index+"/"+size+" !!!TIMEOUT workSm160");
//            } catch (Exception e){
//                System.out.println("!!!workSM160 Exception: "+e.getMessage());
//                e.printStackTrace();
//            }
//            finally {
//                sm160LogList.forEach(Sm160Helper::logSm160SetEnd);
//                es.shutdown();
//            }
        }


        public void workSM160() {
            ExecutorService es = Executors.newSingleThreadExecutor();
            TimeLimiter tl = SimpleTimeLimiter.create(es);
            try {
                tl.callWithTimeout(() -> {
                    for (MapCheckSm160Sim sim : ip_map) {
                        SM160Log sm160Log = logSm160(sim.sim_ip, sim.equip_number, sim.sm_port);
                        sm160LogList.add(sm160Log);
                        if (sim.sm_port != null) {
                            MeterGSM m = new MeterGSM(index, size, sim.sim_ip, sim.sm_port,
                                    TOKEN, id, sim.equip_number,
                                    sm160Log.getId().toString(), true);
                            try {
                                m.setResult();
                            } catch (IOException e) {
                                System.out.println("!!!workSM160 IOException: "+e.getMessage());
                                //e.printStackTrace();
                            }
                        }
                    }
                    return null;
                }, 3000L, TimeUnit.SECONDS);
            } catch (TimeoutException | UncheckedIOException e){
                System.out.println(index+"/"+size+" !!!TIMEOUT workSm160");
            } catch (Exception e){
                System.out.println("!!!workSM160 Exception: "+e.getMessage());
                e.printStackTrace();
            }
            finally {
                sm160LogList.forEach(Sm160Helper::logSm160SetEnd);
                es.shutdown();
            }
        }

        public void workSM160Single() {
            ExecutorService es = Executors.newSingleThreadExecutor();
            TimeLimiter tl = SimpleTimeLimiter.create(es);
            try {
                tl.callWithTimeout(() -> {
//                    for (MapCheckSm160Sim sim : ip_map) {
                        SM160Log sm160Log = logSm160(ip, num, port);
                        sm160LogList.add(sm160Log);
                        //if (sim.sm_port != null) {
                            MeterGSM m = new MeterGSM(1, 1, ip, port, num,
                                    sm160Log.getId().toString(), saveToYoda);
                            try {
                                m.setResult();
                            } catch (IOException e) {
                                System.out.println("!!!workSM160 IOException: "+e.getMessage());
                                //e.printStackTrace();
                            }
                        //}
                    //}
                    return null;
                }, 3000L, TimeUnit.SECONDS);
            } catch (TimeoutException | UncheckedIOException e){
                System.out.println(index+"/"+size+" !!!TIMEOUT workSm160");
            } catch (Exception e){
                System.out.println("!!!workSM160 Exception: "+e.getMessage());
                e.printStackTrace();
            }
            finally {
                sm160LogList.forEach(Sm160Helper::logSm160SetEnd);
                es.shutdown();
            }
        }

    }


}
