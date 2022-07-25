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

import static org.junit.jupiter.api.Assertions.*;
// See https://doc.cuba-platform.com/manual-7.2/integration_tests_mw.html

class ClearTopologyTest extends BaseTest {

    @RegisterExtension
    public static PnrservicesTestContainer cont = PnrservicesTestContainer.Common.INSTANCE;

    @AfterAll
    static void finish(){
        BaseTest.clearDB(RestParams.class, SM160Log.class, SM160LogOperations.class, SM160LogDiscovery.class);
    }

    @BeforeAll
    static void init(){
        BaseTest.beforeAll();
    }

    @Test
    void clearTopologyTest() {

    }
}