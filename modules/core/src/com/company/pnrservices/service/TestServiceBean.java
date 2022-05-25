package com.company.pnrservices.service;

import com.company.pnrservices.core.YodaRESTMethodsHelper;
import com.haulmont.cuba.core.Persistence;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service(TestService.NAME)
public class TestServiceBean implements TestService {

    @Inject
    private Persistence persistence;
    @Inject
    private Logger log;

    @Override
    public void test() {

        System.out.println("!!!int = "+(int)'\'');
////        String hermes_id = "cd0acf73-3026-0684-7840-249d7f2bd758";
//        String hermes_id = "fafc655f-8feb-49ef-d4d0-e61c82e2f86f";
//        String token = getNewToken();
//        log.info("\n!!!token = " + token);
////---------------------------
//        log.info("\n!!!getHermesParamsREST");
//        JSONObject prm = getHermesParamsREST(hermes_id, token);
//        log.info("\n!!!getHermesParamsREST ip = " + prm.get("ip") + ", port = " + prm.get("port") + ", user = " + prm.get("user") + ", pwd = " + prm.get("pwd"));
////-------------------------------
//        log.info("\n!!!upsertTopologyREST");
//        //"fafc655f-8feb-49ef-d4d0-e61c82e2f86f"	"357666051085147"	"b0f101c9-890c-8232-9c95-d1f1c1875ce9"	"63871116006F0D00"
//        String ret = upsertTopologyREST(hermes_id,"357666051085147", "b0f101c9-890c-8232-9c95-d1f1c1875ce9", "63871116006F0D00", token);
//        log.info("\n!!!upsertTopologyREST ret = " + ret);
////--------------------
//        log.info("\n!!!getMeterIdREST");
//        List<String> retList = getMeterIdREST("63871116006F0D00", token);
//        //System.out.println("!!!getMeterIdREST ret1 = "+ret1);
//        retList.forEach(t -> log.info("\n!!!getMeterIdREST t = " + t));
////-------------------------
//        log.info("\n!!!getListForCloseTerminalsREST");
//        List<String> retList2 = getListForCloseTerminalsREST(hermes_id, token);
//        //System.out.println("!!!getMeterIdREST ret1 = "+ret1);
//        retList2.forEach(t -> log.info("!!!\ngetListForCloseTerminalsREST t = " + t));
////-------------------------
//        log.info("\n!!!getMACListForUpdateSerialREST");
//        List<JSONObject> retList3 = getMACListForUpdateSerialREST(hermes_id, "10", token, "300");
//        retList3.forEach(t -> log.info("!!!\ngetMACListForUpdateSerialREST mac = " + t.get("mac")+", meter_id = "+t.get("meter_id")+", type = "+t.get("type")));

        //getMACListForUpdateSerialREST(String hermes_id, String limit, String token, String daysInt)
//        meterService.check_sm160();
//        System.out.println("!!!Main----------------------");
//        try (Transaction tx = persistence.createTransaction()) {
//            EntityManager em = persistence.getEntityManager();
//            Query query = em.createNativeQuery(
//                    "select description from sys_scheduled_task limit 3");
//            List lst = query.getResultList();
//            lst.forEach(t -> {
//                System.out.println("!!!" + t);
//            });
//            tx.commit();
//        }
//
//        System.out.println("!!!yoda----------------------");
//
//        try (Transaction tx = persistence.createTransaction("yoda")) {
//            EntityManager em = persistence.getEntityManager("yoda");
//            Query query = em.createNativeQuery(
//                    "select description from sys_scheduled_task limit 3");
//            List lst = query.getResultList();
//            lst.forEach(t -> {
//                System.out.println("!!!" + t);
//            });
//            tx.commit();
//        }

    }


}