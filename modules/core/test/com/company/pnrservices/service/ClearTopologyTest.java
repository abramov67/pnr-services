package com.company.pnrservices.service;

import com.company.pnrservices.BaseTest;
import com.company.pnrservices.PnrservicesTestContainer;
import com.company.pnrservices.entity.RestParams;
import com.company.pnrservices.entity.SM160Log;
import com.company.pnrservices.entity.SM160LogDiscovery;
import com.company.pnrservices.entity.SM160LogOperations;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;

import static com.company.pnrservices.core.YodaRESTMethodsHelper.clearTopologyREST;
import static com.company.pnrservices.core.YodaRESTMethodsHelper.getNewToken;
import static org.junit.jupiter.api.Assertions.*;
// See https://doc.cuba-platform.com/manual-7.2/integration_tests_mw.html

class ClearTopologyTest extends BaseTest {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ClearTopologyTest.class);
    static String rest_host_test = "192.1.0.92:8080";
    String TOKEN;

    @RegisterExtension
    public static PnrservicesTestContainer cont = PnrservicesTestContainer.Common.INSTANCE;

    @AfterAll
    static void finish(){
        BaseTest.clearDB(RestParams.class);
    }

    @BeforeAll
    static void init(){
        BaseTest.beforeAll();
    }

    @Test
    void clearTopologyTest() {
        createDB();
        TOKEN = getNewToken();
        assert TOKEN != null : "TOKEN is null";

        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка REST метода   ждите...");
        clearTopologyREST("3", TOKEN);
        log.info("!!!Проверка REST метода   успешно");
        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка закончена, все Ок!");

    }

    static void createDB() {
        RestParams restParams = metadata.create(RestParams.class);
        restParams.setUrlScheme("http");
        restParams.setUrlHost(rest_host_test);
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