package com.company.pnrservices.service;

import com.company.pnrservices.BaseTest;
import com.company.pnrservices.PnrservicesTestContainer;
import com.company.pnrservices.core.UpdateSerialHelper;
import com.company.pnrservices.entity.RestParams;
import com.haulmont.cuba.testsupport.TestContainer;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.company.pnrservices.core.YodaRESTMethodsHelper.*;
import static org.junit.jupiter.api.Assertions.*;
// See https://doc.cuba-platform.com/manual-7.2/integration_tests_mw.html

class UpdateSerialServiceTest extends BaseTest {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(UpdateSerialServiceTest.class);
    static String rest_host_test = "192.1.0.92:8080";
    String TOKEN;
    //String hermes_id = "cd0acf73-3026-0684-7840-249d7f2bd758";
    String hermes_id = "fafc655f-8feb-49ef-d4d0-e61c82e2f86f";
    boolean isListMACsFromREST = false;
    boolean saveResult = true;

    @RegisterExtension
    static TestContainer cont = PnrservicesTestContainer.Common.INSTANCE;

    @BeforeAll
    static void init() {
        BaseTest.beforeAll();
    }

    @AfterAll
    static void finish(){
        BaseTest.clearDB(RestParams.class);
    }

    @Test
    void getSerialFromModulesTest() {
        createDB();
        TOKEN = getNewToken();
        assert TOKEN != null : "TOKEN is null";
        List<JSONObject> macList;
        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка REST метода, получение списка MACов   ждите...");
        if (isListMACsFromREST) {
            macList = getMACListForUpdateSerialREST(hermes_id, "2", TOKEN, "1");
        } else {
            macList = fillMACList();
        }
        assertEquals(true, checkJSONStruct(macList.get(0)));
        getSerial(macList);
        log.info("!!!Проверка REST метода   macList.size = "+macList.size());
        log.info("!!!Проверка REST метода   успешно");
    }

    void getSerial(List<JSONObject> macList) {
        int index = 0;
        for (JSONObject jsn : macList) {
            UpdateSerialHelper.UpdateSerialThread ut = new UpdateSerialHelper
                    .UpdateSerialThread(hermes_id, ++index, TOKEN, jsn.getString("meter_id"),
                    jsn.getString("mac"), jsn.getString("type"), macList.size(), saveResult);
            assertEquals(true, functionsTest(jsn, ut));
            assertEquals(true,ut.startSerial().length() > 0);
        }
    }

    boolean functionsTest(JSONObject jsn, UpdateSerialHelper.UpdateSerialThread ut) {
        assertEquals(true, ut.analyse(jsn.getString("replyStr")), "в вызоове функции ut.analyse проблемы");
        assertEquals(jsn.getString("serial"), ut.serial, "в функции ut.analyse проблемы, не верно определен serial");
        assertEquals(jsn.getString("serial"), ut.extractSerial(jsn.getString("extractSerial")), "в функции ut.extractSerial проблемы, не верно извлечен serial");
        assertEquals(false, ut.analyse("zGSfdkhf [ERROR]ogckhnokfp6  "), "в функции ut.analyse проблемы, не определена ошибка");
        return true;
    }

    boolean checkJSONStruct(JSONObject jsn) {
        assertEquals(true, jsn.has("type"));
        assertEquals(true, jsn.has("meter_id"));
        assertEquals(true, jsn.has("mac"));
        assertEquals(true, jsn.getString("mac") != JSONObject.NULL);
        assertEquals(true, jsn.getString("meter_id") != JSONObject.NULL);
        assertEquals(true, jsn.getString("type") != JSONObject.NULL);
        return true;
    }

    List<JSONObject> fillMACList() {
        String checkReplyStr1 = "Посылаем на MHW[C7726D13006F0D00]: genopt.SimpleSequenceBasedGenoptRequest{header=MHeader{meterHwId=MHW[C7726D13006F0D00], optionCommand=GENERAL/010B-GET_SERIAL_NUMBER/18, status=REQUEST/00, attributes=[]}, sequence=0x84}Таймаут ответа: 120 сек.Ответ от THW[352555109201407]: genopt.GetSerialNumberResponse{terminalHwId=THW[352555109201407], header=MHeader{meterHwId=MHW[C7726D13006F0D00], optionCommand=GENERAL/010B-GET_SERIAL_NUMBER/18, status=DONE_REPORT/01, attributes=[]}, status=DONE_REPORT/01, sequence=84, option=M200/0124, serial=11111'44419907'1111}Серийный номер: 44419907";
        String checkReplyStr2 = "Посылаем на MHW[DAD16613006F0D00]: genopt.SimpleSequenceBasedGenoptRequest{header=MHeader{meterHwId=MHW[DAD16613006F0D00], optionCommand=GENERAL/010B-GET_SERIAL_NUMBER/18, status=REQUEST/00, attributes=[]}, sequence=0x85}Таймаут ответа: 120 сек.Ответ от THW[352555109201407]: genopt.GetSerialNumberResponse{terminalHwId=THW[352555109201407], header=MHeader{meterHwId=MHW[DAD16613006F0D00], optionCommand=GENERAL/010B-GET_SERIAL_NUMBER/18, status=DONE_REPORT/01, attributes=[]}, status=DONE_REPORT/01, sequence=85, option=M200/0124, serial=00000'44796491'0000}Серийный номер: 44796491";
        List<JSONObject> ret = new ArrayList<>();
        JSONObject jsn = new JSONObject();
        jsn.put("type", "Модуль ZigBee ZB-M200");
        jsn.put("mac", "C7726D13006F0D00");
        jsn.put("meter_id", "b5a01846-8b46-49c5-a9ee-ca9df43b66aa");
        jsn.put("replyStr", checkReplyStr1);
        jsn.put("serial", "44419907");
        jsn.put("extractSerial", "44419907'0000}gfchgjhv");
        ret.add(jsn);
        jsn = new JSONObject();
        jsn.put("type", "Модуль ZigBee ZB-M200");
        jsn.put("mac", "DAD16613006F0D00");
        jsn.put("meter_id", "5c93103b-8422-44a9-8e48-b0a21277a486");
        jsn.put("replyStr", checkReplyStr2);
        jsn.put("serial", "44796491");
        jsn.put("extractSerial", "44796491'3333}kjghkjhjklh");
        ret.add(jsn);
        return ret;
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