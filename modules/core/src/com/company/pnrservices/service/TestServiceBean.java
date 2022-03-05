package com.company.pnrservices.service;

import com.haulmont.cuba.core.Persistence;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service(TestService.NAME)
public class TestServiceBean implements TestService {

    @Inject
    private Persistence persistence;
    @Inject
    private MeterService meterService;

    @Override
    public void test() {

        meterService.check_sm160();

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