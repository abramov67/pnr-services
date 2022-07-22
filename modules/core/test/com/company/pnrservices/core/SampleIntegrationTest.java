package com.company.pnrservices.core;

import com.company.pnrservices.PnrservicesTestContainer;
import com.company.pnrservices.entity.RestParams;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.TypedQuery;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.security.entity.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

public class SampleIntegrationTest {

    @RegisterExtension
    public static PnrservicesTestContainer cont = PnrservicesTestContainer.Common.INSTANCE;

    private static Metadata metadata;
    private static Persistence persistence;
    private static DataManager dataManager;

    @BeforeAll
    public static void beforeAll() throws Exception {
        dataManager = AppBeans.get(DataManager.class);
        metadata = AppBeans.get(Metadata.class);
        persistence = AppBeans.get(Persistence.class);
    }

    @AfterAll
    public static void afterAll() throws Exception {
    }

    @Test
    public void testLoadUser() {
        createDB();

//        try (Transaction tx = persistence.createTransaction()) {
//            EntityManager em = persistence.getEntityManager();
//            TypedQuery<User> query = em.createQuery(
//                    "select u from sec$User u where u.login = :userLogin", User.class);
//            query.setParameter("userLogin", "admin");
//            List<User> users = query.getResultList();
//            tx.commit();
//            Assertions.assertEquals(1, users.size());
//            //Assertions.assertEquals(50, users.size());
//        }
    }

    void createDB() {
        RestParams restParams = metadata.create(RestParams.class);
        restParams.setUrlScheme("http");
        restParams.setUrlHost("192.1.0.221:8080");
        restParams.setUrlPath("/rest/v2/services/enerstroymain_PNRServicesService/");
        restParams.setAuthorization("Bearer ");
        restParams.setContentType("application/x-www-form-urlencoded");
        restParams.setIdType(0);
        restParams.setUrlPathToken("/rest/v2/oauth/token");
        restParams.setAuthorizationToken("Basic ZW5lcnN0cm95bWFpbi1FcUZHVUx4djpjOTg5ZGYxMGQ2MTk5NDk5MGJiMjBmODBkNDAwNTdlNmYzYzcxNWJmMGRlMGMyNDIyZGNkY2M0ZTY1N2M1ODcx");
        restParams.setUsrToken("ted");
        restParams.setPwdToken("det");
        dataManager.commit(restParams);

    }

}