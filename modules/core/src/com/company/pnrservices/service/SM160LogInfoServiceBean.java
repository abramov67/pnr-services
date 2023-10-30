package com.company.pnrservices.service;

import com.company.pnrservices.entity.SM160LogInfo;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.View;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service(SM160LogInfoService.NAME)
public class SM160LogInfoServiceBean implements SM160LogInfoService {

    @Inject
    private Persistence persistence;
    @Inject
    private DataManager dataManager;

    @Override
    public void upsertStoredProc(String p_login, String p_pan_id, Integer p_channel, Boolean p_is_join_permitted) {
        String sql = "select * from public.pnrservices_upsert_sm160_log_info(?1, ?2, ?3, ?4)";
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, p_login);
            query.setParameter(2, p_pan_id);
            query.setParameter(3, p_channel);
            query.setParameter(4, p_is_join_permitted);
            System.out.println("!!!query="+query.getQueryString());
            query.getFirstResult();// executeUpdate();
            tx.commit();
        }
    }

    @Override
    public List<SM160LogInfo> find(View view) {
        return dataManager.load(SM160LogInfo.class).view(view).list();
    }

}