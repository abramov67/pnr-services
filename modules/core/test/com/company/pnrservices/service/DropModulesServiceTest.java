package com.company.pnrservices.service;

import com.company.pnrservices.BaseTest;
import com.company.pnrservices.PnrservicesTestContainer;
import com.company.pnrservices.core.DropModulesHelper;
import com.company.pnrservices.core.NativeSQLBean;
import com.company.pnrservices.entity.LastClosedTerminals;
import com.company.pnrservices.entity.RestParams;
import com.company.pnrservices.entity.Zbdropmodules;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.testsupport.TestContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;

import java.util.Date;
import java.util.List;

import static com.company.pnrservices.core.DropModulesHelper.clearLastCloseTerminals;
import static com.company.pnrservices.core.YodaRESTMethodsHelper.*;
import static org.junit.jupiter.api.Assertions.*;
// See https://doc.cuba-platform.com/manual-7.2/integration_tests_mw.html

class DropModulesServiceTest extends BaseTest {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(UpdateSerialServiceTest.class);
    static String rest_host_test = "192.1.0.92:8080";
    String TOKEN;
    boolean saveResult = false;
    static RestParams restParamsExpected;
    static NativeSQLBean nativeSQLBean;
    List<Zbdropmodules> zbMACList;
    DropModulesHelper.UpdaterThread updateThread;
    String hermes_id = "fafc655f-8feb-49ef-d4d0-e61c82e2f86f";

    DropModulesHelper.OpenModuleThread openModuleThread;
    static Date dateTest;

    DropModulesService dropModulesService;



    @RegisterExtension
    static TestContainer cont = PnrservicesTestContainer.Common.INSTANCE;

    @BeforeAll
    static void init() {
        BaseTest.beforeAll();
        restParamsExpected = metadata.create(RestParams.class);
        nativeSQLBean = AppBeans.get(NativeSQLBean.class);
    }

    @AfterAll
    static void finish(){
        BaseTest.clearDB(RestParams.class);
        BaseTest.clearDBBaseStringIdEntity(Zbdropmodules.class, LastClosedTerminals.class);
    }

    @Test
    void dropZBModulesTest() {
        createDB_dropZBModules();
        dropModulesService = AppBeans.get(DropModulesService.class);
        TOKEN = getNewToken();
        assert TOKEN != null : "TOKEN is null";

        List<Zbdropmodules> macList = nativeSQLBean.getMACForDropModules();
        int sizeMACList = macList.size();
        assertEquals(
                dataManager.load(Zbdropmodules.class)
                        .list().size(),
                sizeMACList
        );

        int sizeLastCloses = dataManager.load(LastClosedTerminals.class).list().size();
        System.out.println("!!!sizeLastCloses = "+sizeLastCloses);
        clearLastCloseTerminals();
        assertEquals(0, dataManager.load(LastClosedTerminals.class).list().size());

        String terminalTmp = "11111111111";
        dropModulesService.saveClosedTerminal(terminalTmp, hermes_id);

        dataManager.load(LastClosedTerminals.class)
                .query("select l from pnrservices_LastClosedTerminals l " +
                        "where l.imei = :terminal and l.hermes = :hermes_id")
                .parameter("terminal", terminalTmp)
                .parameter("hermes_id", hermes_id)
                .one();
    }


    @Test
    void openClosedTerminalsTest() {
        createDB_OpenClosedTerminals();
        TOKEN = getNewToken();
        assert TOKEN != null : "TOKEN is null";

        List<LastClosedTerminals> lastClosedTerminalsList = nativeSQLBean.getLastClosedTerminals();
        int size = lastClosedTerminalsList.size();
        assertEquals(
                dataManager.load(LastClosedTerminals.class)
                        .query("select l from pnrservices_LastClosedTerminals l order by l.imei")
                        .list().size(),
                lastClosedTerminalsList.size()
        );

        int index = 1;
        openModuleThread = new DropModulesHelper.OpenModuleThread(lastClosedTerminalsList.get(0).getHermes(), index, TOKEN, lastClosedTerminalsList.get(0).getImei(), lastClosedTerminalsList.size());

        assertEquals("356612022988812", nativeSQLBean.getLastClosedTerminals().get(0).getImei());
        assertEquals("fafc655f-8feb-49ef-d4d0-e61c82e2f86f", nativeSQLBean.getLastClosedTerminals().get(0).getHermes());
        assertEquals(dateTest, nativeSQLBean.getLastClosedTerminals().get(0).getDt());
        assertEquals("654", nativeSQLBean.getLastClosedTerminals().get(0).getMess());

        if  (size > 0) {
            nativeSQLBean.deleteFormLastClosedTerminal(nativeSQLBean.getLastClosedTerminals().get(0).getImei());
            assertEquals(
                    dataManager.load(LastClosedTerminals.class)
                            .query("select l from pnrservices_LastClosedTerminals l order by l.imei")
                            .list().size() ,
                    size - 1
            );
        }

    }

    @Test
    void topologyUpdateTest() {
        createDB_topologyUpdate();
        TOKEN = getNewToken();
        assert TOKEN != null : "TOKEN is null";
        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка getBaseParamsCheck   ждите...");
        assertTrue(getBaseParamsCheck(), "в NativeSQLBean.getBaseParams проблемы");
        log.info("!!!Проверка getBaseParamsCheck   успешно...");
        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка getMACForDropModules   ждите...");
        assertTrue(getMACListCheck(), "в NativeSQLBean.getMACForDropModules проблемы");
        log.info("!!!Проверка getMACForDropModules   успешно...");
        Integer index = 0;
        for (Zbdropmodules zbdropmodules: zbMACList) {
            log.info("!!!---------------------------------------------");
            log.info("!!!Проверка getMeterIdREST   ждите...");
            List<String> metersIDsList = getMeterIdREST(zbdropmodules.getMac(), TOKEN);
            assertEquals(2, metersIDsList.size());
            log.info("!!!Проверка getMeterIdREST   успешно...");

            for (String meter_id : metersIDsList) {
                index++;
                updateThread = (new DropModulesHelper.UpdaterThread(index, zbdropmodules.getMac(), meter_id, hermes_id, TOKEN, zbMACList.size(), saveResult));

                updateThread.getTerminalFromShell();
                assertEquals("352555106639997", updateThread.terminal_id);
            }
        }

        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка функций functionsTopologyUpdateTest   ждите...");
        assertTrue(functionsTopologyUpdateTest());
        log.info("!!!Проверка функций functionsTopologyUpdateTest   успешно");
    }

    boolean getBaseParamsCheck() {
        RestParams restParams = nativeSQLBean.getBaseParams();
        assertEquals(restParamsExpected, restParams);
        return true;
    }

    boolean getMACListCheck() {
        zbMACList = nativeSQLBean.getMACForDropModules();
        assertTrue(zbMACList.size() > 0);
        zbDropModulesCheck(zbMACList.get(0));
        return true;
    }

    void zbDropModulesCheck(Zbdropmodules it) {
        assertEquals("9591BC14006F0D00", it.getMac());
        assertEquals("9210140049291", it.getNum());
    }

    boolean functionsTopologyUpdateTest() {
        String reply = "Счётчик находится за терминалом 0000THW[352555106639997] (статус: ONLINE)Информация о счётчике: MeterInfo{meterHwId=MHW[9591BC14006F0D00], optionList=[GENERAL/010B, OTA_UPDATE/011A, SYSTEM/0107, KASKAD_1MT/0116, SYNCTIME/010C], dataActivityTs=220227 185009.640, version=MeterVersionInfo{hwName='6477.049-04[8]', hwVersion='-1.-1', swVersion='1.69'}}";
        String reply2 = "SHdghz[ERROR]xdvnlxkndfls";
        assertEquals("352555106639997", updateThread.extractTerminal(reply));
        assertEquals(null, updateThread.extractTerminal(reply2));
        return true;
    }

    static void createDB_topologyUpdate() {
        restParamsExpected.setUrlScheme("http");
        restParamsExpected.setUrlHost(rest_host_test);
        restParamsExpected.setUrlPath("/rest/v2/services/enerstroymain_PNRServicesService/");
        restParamsExpected.setAuthorization("Bearer ");
        restParamsExpected.setContentType("application/x-www-form-urlencoded");
        restParamsExpected.setIdType(0);
        restParamsExpected.setUrlPathToken("/rest/v2/oauth/token");
        restParamsExpected.setAuthorizationToken("Basic ZW5lcnN0cm95bWFpbi1FcUZHVUx4djpjOTg5ZGYxMGQ2MTk5NDk5MGJiMjBmODBkNDAwNTdlNmYzYzcxNWJmMGRlMGMyNDIyZGNkY2M0ZTY1N2M1ODcx");
        restParamsExpected.setUsrToken("ted");
        restParamsExpected.setPwdToken("det");
        dataManager.commit(restParamsExpected);

        Zbdropmodules zbdropmodules = dataManager.create(Zbdropmodules.class);
        zbdropmodules.setMac("9591BC14006F0D00");
        zbdropmodules.setNum("9210140049291");
        dataManager.commit(zbdropmodules);
//        zbdropmodules = dataManager.create(Zbdropmodules.class);
//        zbdropmodules.setMac("D6315C16006F0D00");
//        zbdropmodules.setNum("2210144313387");
//        dataManager.commit(zbdropmodules);
    }

    static void createDB_OpenClosedTerminals() {
        restParamsExpected.setUrlScheme("http");
        restParamsExpected.setUrlHost(rest_host_test);
        restParamsExpected.setUrlPath("/rest/v2/services/enerstroymain_PNRServicesService/");
        restParamsExpected.setAuthorization("Bearer ");
        restParamsExpected.setContentType("application/x-www-form-urlencoded");
        restParamsExpected.setIdType(0);
        restParamsExpected.setUrlPathToken("/rest/v2/oauth/token");
        restParamsExpected.setAuthorizationToken("Basic ZW5lcnN0cm95bWFpbi1FcUZHVUx4djpjOTg5ZGYxMGQ2MTk5NDk5MGJiMjBmODBkNDAwNTdlNmYzYzcxNWJmMGRlMGMyNDIyZGNkY2M0ZTY1N2M1ODcx");
        restParamsExpected.setUsrToken("ted");
        restParamsExpected.setPwdToken("det");
        dataManager.commit(restParamsExpected);

        LastClosedTerminals lastClosedTerminals = dataManager.create(LastClosedTerminals.class);
        lastClosedTerminals.setImei("356612022988812");
        lastClosedTerminals.setHermes("fafc655f-8feb-49ef-d4d0-e61c82e2f86f");
        dateTest = new Date();
        lastClosedTerminals.setDt(dateTest);
        lastClosedTerminals.setMess("654");
        dataManager.commit(lastClosedTerminals);

    }

    static void createDB_dropZBModules() {
        restParamsExpected.setUrlScheme("http");
        restParamsExpected.setUrlHost(rest_host_test);
        restParamsExpected.setUrlPath("/rest/v2/services/enerstroymain_PNRServicesService/");
        restParamsExpected.setAuthorization("Bearer ");
        restParamsExpected.setContentType("application/x-www-form-urlencoded");
        restParamsExpected.setIdType(0);
        restParamsExpected.setUrlPathToken("/rest/v2/oauth/token");
        restParamsExpected.setAuthorizationToken("Basic ZW5lcnN0cm95bWFpbi1FcUZHVUx4djpjOTg5ZGYxMGQ2MTk5NDk5MGJiMjBmODBkNDAwNTdlNmYzYzcxNWJmMGRlMGMyNDIyZGNkY2M0ZTY1N2M1ODcx");
        restParamsExpected.setUsrToken("ted");
        restParamsExpected.setPwdToken("det");
        dataManager.commit(restParamsExpected);

        LastClosedTerminals lastClosedTerminals = dataManager.create(LastClosedTerminals.class);
        lastClosedTerminals.setImei("356612022988812");
        lastClosedTerminals.setHermes("fafc655f-8feb-49ef-d4d0-e61c82e2f86f");
        dateTest = new Date();
        lastClosedTerminals.setDt(dateTest);
        lastClosedTerminals.setMess("654");
        dataManager.commit(lastClosedTerminals);

        Zbdropmodules zbdropmodules = dataManager.create(Zbdropmodules.class);
        zbdropmodules.setMac("9591BC14006F0D00");
        zbdropmodules.setNum("9210140049291");
        dataManager.commit(zbdropmodules);


    }

}