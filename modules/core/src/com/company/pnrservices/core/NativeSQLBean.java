package com.company.pnrservices.core;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.util.List;

@Component(NativeSQLBean.NAME)
public class NativeSQLBean {
    @Inject
    private Persistence persistence;

    public List getListYoda(String sql) {
        List lst;
        try (Transaction tx = persistence.createTransaction("yoda")) {
            EntityManager em = persistence.getEntityManager("yoda");
            Query query = em.createNativeQuery(sql);
            lst = query.getResultList();
            tx.commit();
        }
        if (lst.isEmpty()) return null; else return lst;
    }

    public Object getSingleYoda(String sql) {
        Object res = null;
        try (Transaction tx = persistence.createTransaction("yoda")) {
            EntityManager em = persistence.getEntityManager("yoda");
            Query query = em.createNativeQuery(sql);
            res = query.getSingleResult();
            tx.commit();
        } catch (NoResultException ignored) {
            //no result
        }
        return res;
    }

    public boolean executeYoda(String sql) {
        boolean res = false;
        try (Transaction tx = persistence.createTransaction("yoda")) {
            EntityManager em = persistence.getEntityManager("yoda");
            Query query = em.createNativeQuery(sql);
            query.executeUpdate();
            tx.commit();
            res = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public List getListMain(String sql) {
        List lst;
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createNativeQuery(sql);
            lst = query.getResultList();
            tx.commit();
        }
        if (lst.isEmpty()) return null; else return lst;
    }

    public static final String NAME = "pnrservices_NativeSQLBean";

}