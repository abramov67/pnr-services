package com.company.pnrservices.core;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component(NativeSQLBean.NAME)
public class NativeSQLBean {
    @Inject
    private Persistence persistence;

    public Object getSingleMain(String sql) {
        Object res = null;
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createNativeQuery(sql);
            res = query.getSingleResult();
            tx.commit();
        } catch (NoResultException e1) {
            System.out.println(timeFormat(new Date())+" !!! Ошибка исполнения NoResultException NativeSQL.getSingleMain sql = '"+sql+"'");
            e1.printStackTrace();
        } catch (Exception e2) {
            System.out.println(timeFormat(new Date())+" !!! Ошибка исполнения NativeSQL.getSingleMain:"+e2.getMessage()+", sql = '"+sql+"'");
            e2.printStackTrace();
        }
        return res;
    }


    public boolean executeMain(String sql) {
        boolean res = false;
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createNativeQuery(sql);
            query.executeUpdate();
            tx.commit();
            res = true;
        } catch (Exception e) {
            System.out.println(timeFormat(new Date())+" !!! Ошибка исполнения NativeSQL.executeMain:"+e.getMessage()+", sql = '"+sql+"'");
            e.printStackTrace();
        }
        return res;
    }

    public List getListMain(String sql) {
        List lst = null;
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createNativeQuery(sql);
            lst = query.getResultList();
            tx.commit();
        } catch (Exception e) {
            System.out.println(timeFormat(new Date())+" !!!Ошибка исполнения NativeSQL.getListMain:"+e.getMessage()+", sql = '"+sql+"'");
            e.printStackTrace();
        }
        return lst;
    }

    private String timeFormat(Date tm) {
        String pattern = "HH:mm:ss";
        return new SimpleDateFormat(pattern).format(tm);
    }


    public static final String NAME = "pnrservices_NativeSQLBean";

}