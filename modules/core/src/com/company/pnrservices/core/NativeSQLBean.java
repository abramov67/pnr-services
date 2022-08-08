package com.company.pnrservices.core;

import com.company.pnrservices.entity.LastClosedTerminals;
import com.company.pnrservices.entity.RestParams;
import com.company.pnrservices.entity.Zbdropmodules;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.security.app.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Component(NativeSQLBean.NAME)
public class NativeSQLBean {
    @Inject
    private DataManager dataManager;
    @Inject
    private Authentication authentication;

    public RestParams getBaseParams() {
        return dataManager.load(RestParams.class)
                .query("select r from pnrservices_RestParams r where r.idType = 0")
                .one();
    }

    public List<Zbdropmodules> getMACForDropModules() {
        return dataManager.load(Zbdropmodules.class).list();
    }

    public List<LastClosedTerminals> getLastClosedTerminals() {
        return dataManager.load(LastClosedTerminals.class).query("select l from pnrservices_LastClosedTerminals l order by l.imei").list();
    }

    public void insertLastClosedTerminal(String imei, String hermesId) {
        LastClosedTerminals newClosedTerminal = dataManager.create(LastClosedTerminals.class);
        newClosedTerminal.setImei(imei);
        newClosedTerminal.setHermes(hermesId);
        dataManager.commit(newClosedTerminal);
    }

    public void clearLastClosedTerminals() {
        dataManager.load(LastClosedTerminals.class)
                .softDeletion(false).list()
                .forEach((t) -> dataManager.remove(dataManager.getReference(LastClosedTerminals.class, t.getId())));
   }

    public void deleteFormLastClosedTerminal(String imei) {
        authentication.begin();
        try {
            dataManager.load(LastClosedTerminals.class)
                    .query("select l from pnrservices_LastClosedTerminals l where l.imei = :imei")
                    .parameter("imei", imei)
                    .softDeletion(false).list()
                    .forEach((t) -> dataManager.remove(dataManager.getReference(LastClosedTerminals.class, t.getId())));
        } catch(Exception e) {
            System.out.println("И здесь ошибка: "+e.getMessage());
        }
    }

//    public Object getSingleMain(String sql) {
//        Object res = null;
//        try (Transaction tx = persistence.createTransaction()) {
//            EntityManager em = persistence.getEntityManager();
//            Query query = em.createNativeQuery(sql);
//            res = query.getSingleResult();
//            tx.commit();
//        } catch (NoResultException e1) {
//            System.out.println(timeFormat(new Date())+" !!! Ошибка исполнения NoResultException NativeSQL.getSingleMain sql = '"+sql+"'");
//            e1.printStackTrace();
//        } catch (Exception e2) {
//            System.out.println(timeFormat(new Date())+" !!! Ошибка исполнения NativeSQL.getSingleMain:"+e2.getMessage()+", sql = '"+sql+"'");
//            e2.printStackTrace();
//        }
//        return res;
//    }
//
//
//    public boolean executeMain(String sql) {
//        boolean res = false;
//        try (Transaction tx = persistence.createTransaction()) {
//            EntityManager em = persistence.getEntityManager();
//            Query query = em.createNativeQuery(sql);
//            query.executeUpdate();
//            tx.commit();
//            res = true;
//        } catch (Exception e) {
//            System.out.println(timeFormat(new Date())+" !!! Ошибка исполнения NativeSQL.executeMain:"+e.getMessage()+", sql = '"+sql+"'");
//            e.printStackTrace();
//        }
//        return res;
//    }
//
//    public List getListMain(String sql) {
//        List lst = null;
//        try (Transaction tx = persistence.createTransaction()) {
//            EntityManager em = persistence.getEntityManager();
//            Query query = em.createNativeQuery(sql);
//            lst = query.getResultList();
//            tx.commit();
//        } catch (Exception e) {
//            System.out.println(timeFormat(new Date())+" !!!Ошибка исполнения NativeSQL.getListMain:"+e.getMessage()+", sql = '"+sql+"'");
//            e.printStackTrace();
//        }
//        return lst;
//    }

//    private String timeFormat(Date tm) {
//        String pattern = "HH:mm:ss";
//        return new SimpleDateFormat(pattern).format(tm);
//    }


    public static final String NAME = "pnrservices_NativeSQLBean";

}