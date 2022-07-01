package com.company.pnrservices.service;

import com.company.pnrservices.BaseTest;
import org.junit.jupiter.api.*;

public class SM160ServiceTest extends BaseTest {

    @AfterAll
    static void finish(){
        BaseTest.clearDB();
    }

    @BeforeAll
   static void init(){
        BaseTest.beforeAll();
    }

    @Test
    void checkSM160SingleTest(){

    }
}
