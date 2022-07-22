package com.company.pnrservices;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Metadata;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;

public abstract class BaseTest {
    @RegisterExtension
    public static PnrservicesTestContainer cont = PnrservicesTestContainer.Common.INSTANCE;

     public static Metadata metadata;
     public static Persistence persistence;
     public static DataManager dataManager;

    @BeforeAll
    public static void beforeAll() {
        dataManager = AppBeans.get(DataManager.class);
        metadata = AppBeans.get(Metadata.class);
        persistence = AppBeans.get(Persistence.class);
    }

    /**
     * Param is a varags which expected a Class of Entity which
     * should be removed. Than remove it from DB with softDelete = false.
     * The removing will occur in the order FIFO
     *
     * @param entities Class<? extends StandartEntity> varags
     */
    @SafeVarargs
    public final static void clearDB(Class<? extends BaseUuidEntity>... entities) {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            CommitContext removableContext = new CommitContext();
            for (Class<? extends BaseUuidEntity> entityToRemove : entities) {
                dataManager.load(entityToRemove).list().forEach(removableContext::addInstanceToRemove);
            }
            removableContext.getRemoveInstances().forEach(entity -> {
                em.setSoftDeletion(false);
                em.remove(entity);
            });
            tx.commit();
        }
    }
}
