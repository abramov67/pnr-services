package com.company.pnrservices.service;

import com.company.pnrservices.entity.notpersistent.SM160LogSelectScr;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.Metadata;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

@Service(NativeQueryService.NAME)
public class NativeQueryServiceBean implements NativeQueryService {

    @Inject
    private Persistence persistence;
    @Inject
    private Metadata metadata;

    public List<SM160LogSelectScr> getListAsSM160LogSelectScr(String sql) {
        List<SM160LogSelectScr> result = new ArrayList<>();
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createNativeQuery(sql);
            List tmpList = query.getResultList();
            for (Iterator it = tmpList.iterator(); it.hasNext(); ) {
                Object[] row = (Object[]) it.next();
                SM160LogSelectScr item = metadata.create(SM160LogSelectScr.class);
                item.setUuid((UUID) row[0]);
                item.setVersion((Integer) row[1]);
                item.setCreateTs((Date) row[2]);
                item.setCreatedBy((String) row[3]);
                item.setUpdateTs((Date) row[4]);
                item.setUpdatedBy((String) row[5]);
                item.setDeleteTs((Date) row[6]);
                item.setDeletedBy((String) row[7]);
                item.setIp((String) row[8]);
                item.setNum((String) row[9]);
                item.setPort((Integer) row[10]);
                item.setEndTime((Date) row[11]);
                item.setStartTime((Date) row[12]);
                item.setMacsCnt((Long) row[13]);
                item.setDeltaTime((Date) row[14]);
                result.add(item);
            }
            tx.commit();
        } catch (Exception e) {
            System.out.println(timeFormat(new Date())+" !!!Ошибка исполнения NativeSQL.getListMain:"+e.getMessage()+", sql = '"+sql+"'");
            //e.printStackTrace();
        }
        return result;
    }

    private String timeFormat(Date tm) {
        String pattern = "HH:mm:ss";
        return new SimpleDateFormat(pattern).format(tm);
    }

}