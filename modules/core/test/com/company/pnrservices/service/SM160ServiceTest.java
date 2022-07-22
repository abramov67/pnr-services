package com.company.pnrservices.service;

import com.company.pnrservices.BaseTest;
import com.company.pnrservices.PnrservicesTestContainer;
import com.company.pnrservices.core.MeterGSM;
import com.company.pnrservices.entity.RestParams;
import com.company.pnrservices.entity.SM160Log;
import com.company.pnrservices.entity.SM160LogDiscovery;
import com.company.pnrservices.entity.SM160LogOperations;
import com.haulmont.cuba.core.global.AppBeans;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;

import java.util.*;

import static com.company.pnrservices.core.AbramHelper.bytesToHex;
import static com.company.pnrservices.core.Sm160Helper.logSm160;
import static com.company.pnrservices.core.YodaRESTMethodsHelper.getFirstForSM160REST;
import static com.company.pnrservices.core.YodaRESTMethodsHelper.getNewToken;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SM160ServiceTest extends BaseTest {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(SM160ServiceTest.class);
    @RegisterExtension
    public static PnrservicesTestContainer cont = PnrservicesTestContainer.Common.INSTANCE;

    int firstListSize;
    List<JSONObject> firstList = new ArrayList<>();;
    SM160Service sm160Service;
    HashMap<UUID, List<SM160Service.MapCheckSm160Sim>> hashMap;

    static String rest_host_test = "192.1.0.92:8080";
    static boolean saveResult = true;

    static String ip = "10.81.186.244";
    static int port = 44451;
    static String num = "2210403942759_202710";

    static boolean saveToYoda = false;
    static SM160Log sm160Log = null;
    static String TOKEN;
    static MeterGSM meterGSM;

    static List<String> limitIPsList = new ArrayList<>();


    @AfterAll
    static void finish(){
        BaseTest.clearDB(RestParams.class, SM160Log.class, SM160LogOperations.class, SM160LogDiscovery.class);
    }

    @BeforeAll
    static void init(){
        BaseTest.beforeAll();
    }

    @Test
    void checkSM160SingleTest(){
        createDBSingle();
        setParamsSingle();
        TOKEN = getNewToken();
        assert TOKEN != null : "TOKEN is null";

        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка создания основного лога,  ждите...");
        assertEquals(ip, execLog());
        log.info("!!!Проверка создания основного лога,  успешно");
        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка соединения по ip = "+ip+", port = "+port+"   ждите...");
        meterGSM = execMeterGM();
        assertEquals(true, meterGSM.connectNew2(2));
        log.info("!!!Проверка соединения по ip = "+ip+", port = "+port+"   успешно");
        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка CheckInfo   ждите...");
        assertEquals(true, meterGSM.checkInfo());
        log.info("!!!Проверка CheckInfo replyCheckInfo = "+bytesToHex(meterGSM.replyCheckInfo));
        log.info("!!!Проверка CheckInfo   успешно");
        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка getMAC   ждите...");
        assertEquals(true, meterGSM.getMAC(), "Ошибка получения MAC");
        log.info("!!!Проверка getMAC replyGetMAC = "+bytesToHex(meterGSM.replyGetMAC));
        log.info("!!!Проверка getMAC   успешно");
        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка setMAC   ждите...");
        assertEquals(true, meterGSM.setMAC().length()>0);
        log.info("!!!Проверка setMAC   успешно");
        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка setVersion   ждите...");
        assertEquals(true, meterGSM.setVersion());
        log.info("!!!Проверка setVersion versionNumber = "+meterGSM.versionNumber);
        log.info("!!!Проверка setVersion boardVersion = "+meterGSM.boardVersion);
        log.info("!!!Проверка setVersion bigVersionPO = "+meterGSM.bigVersionPO);
        log.info("!!!Проверка setVersion smallVersionPO = "+meterGSM.smallVersionPO);
        log.info("!!!Проверка setVersion optionNum = "+meterGSM.optionNum);
        log.info("!!!Проверка setVersion   успешно");
        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка toDiscoverNew   ждите...");
        assertEquals(true, meterGSM.toDiscoverNew());
        log.info("!!!Проверка toDiscoverNew  discoverReplyBuffer.size = "+meterGSM.discoverReplyBuffer.size());
        log.info("!!!Проверка toDiscoverNew   успешно");
        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка generateToDiscoverMACs   ждите...");
        assertEquals(true, meterGSM.generateToDiscoverMACs());
        log.info("!!!Проверка generateToDiscoverMACs  discoverReplyBuffer.size = "+meterGSM.discoverReplyBuffer.size());
        log.info("!!!Проверка generateToDiscoverMACs   успешно");
        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка saveToYodaRest   ждите...");
        meterGSM.saveResult = saveResult;
        assertEquals(true, validJSONObject(meterGSM.saveToYodaREST()), "Ошибка в формате JSON");
        log.info("!!!Проверка saveToYodaRest   успешно");

        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка закончена, все Ок!");
    }

    boolean validJSONObject(JSONObject jsn) {
        boolean ret = true;
        //)))
        if (!jsn.has("id")) ret = false;
        else
        if (!jsn.has("ip")) ret = false;
        else
        if (!jsn.has("lastBotConnection")) ret = false;
        else
        if (!jsn.has("lastActive")) ret = false;
        else
        if (!jsn.has("panID")) ret = false;
        else
        if (!jsn.has("networkPanID")) ret = false;
        else
        if (!jsn.has("channelNum")) ret = false;
        else
        if (!jsn.has("isJoiningPermitted")) ret = false;
        else
        if (!jsn.has("versionNumber")) ret = false;
        else
        if (!jsn.has("boardVersion")) ret = false;
        else
        if (!jsn.has("bigVersionPO")) ret = false;
        else
        if (!jsn.has("smallVersionPO")) ret = false;
        else
        if (!jsn.has("optionNum")) ret = false;
        else
        if (!jsn.has("MAC")) ret = false;
        else
        if (!jsn.has("toDiscoverMAC")) ret = false;
        else
        if (!jsn.has("option")) ret = false;
        else
        if (!jsn.has("equipNumber")) ret = false;

        return ret;
    }

    @Test
    void checkSM160Test(){
        createDB();
        setParams();
        TOKEN = getNewToken();
        assert TOKEN != null : "TOKEN is null";

        log.info("!!!---------------------------------------------");
        log.info("!!!Получение первичного списка,  ждите...");
        getFirstList(TOKEN);
        assertEquals(limitIPsList.size(), firstListSize, "не полный список");
        //assertEquals(true, getFirstList(TOKEN), "получение первичного списка");
        log.info("!!!Получение первичного списка,  успешно");

        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка HashMap,  ждите...");
        sm160Service = AppBeans.get(SM160Service.class);
        hashMap = new HashMap<>();
        sm160Service.mapSet(hashMap, firstList);
        Set<String> mapItems = new HashSet<>();
        firstList.forEach(i -> mapItems.add(i.getString("id")));
        assertEquals(mapItems.size(), hashMap.size(), "Неравенство  firstList.size != hashMap.size");
        assertEquals(true, validateHashMap());
        log.info("!!!Проверка HashMap,  успешно");

        int index = 0;
        for (Map.Entry<UUID, List<SM160Service.MapCheckSm160Sim>> map : hashMap.entrySet()) {
            for (SM160Service.MapCheckSm160Sim ips : map.getValue()) {
                index++;
                ip = ips.sim_ip;
                port = ips.sm_port;
                num = ips.equip_number;

                log.info("!!!---------------------------------------------");
                log.info("!!!Проверка создания основного лога " + getParamsString() + ",  ждите...");
                assertEquals(ip, execLog(), "ошибка execLog "+index+"/"+hashMap.size()+",  "+getParamsString());
                log.info("!!!Проверка создания основного лога " + getParamsString() + ",  успешно");
                log.info("!!!---------------------------------------------");
                log.info("!!!Проверка соединения для " + getParamsString() + "   ждите...");
                meterGSM = execMeterGM();
                assertEquals(true, meterGSM.connectNew2(2), "ошибка Connect "+index+"/"+hashMap.size()+",  "+getParamsString());
                log.info("!!!Проверка соединения для " + getParamsString() + "   успешно");
                log.info("!!!---------------------------------------------");
                log.info("!!!Проверка CheckInfo " + getParamsString() + "   ждите...");
                assertEquals(true, meterGSM.checkInfo(), "ошибка CheckInfo "+index+"/"+hashMap.size()+",  "+getParamsString());
                log.info("!!!Проверка CheckInfo replyCheckInfo = " + bytesToHex(meterGSM.replyCheckInfo));
                log.info("!!!Проверка CheckInfo   успешно");
                log.info("!!!---------------------------------------------");
                log.info("!!!Проверка getMAC " + getParamsString() + "   ждите...");
                assertEquals(true, meterGSM.getMAC(), "ошибка getMAC "+index+"/"+hashMap.size()+",  "+getParamsString());
                log.info("!!!Проверка getMAC replyGetMAC = " + bytesToHex(meterGSM.replyGetMAC));
                log.info("!!!Проверка getMAC   успешно");
                log.info("!!!---------------------------------------------");
                log.info("!!!Проверка setMAC " + getParamsString() + "  ждите...");
                assertEquals(true, meterGSM.setMAC().length() > 0, "ошибка setMAC "+index+"/"+hashMap.size()+",  "+getParamsString());
                log.info("!!!Проверка setMAC   успешно");
                log.info("!!!---------------------------------------------");
                log.info("!!!Проверка setVersion " + getParamsString() + "   ждите...");
                assertEquals(true, meterGSM.setVersion(), "ошибка setVersion "+index+"/"+hashMap.size()+",  "+getParamsString());
                log.info("!!!Проверка setVersion versionNumber = " + meterGSM.versionNumber);
                log.info("!!!Проверка setVersion boardVersion = " + meterGSM.boardVersion);
                log.info("!!!Проверка setVersion bigVersionPO = " + meterGSM.bigVersionPO);
                log.info("!!!Проверка setVersion smallVersionPO = " + meterGSM.smallVersionPO);
                log.info("!!!Проверка setVersion optionNum = " + meterGSM.optionNum);
                log.info("!!!Проверка setVersion   успешно");
                log.info("!!!---------------------------------------------");
                log.info("!!!Проверка toDiscoverNew " + getParamsString() + "   ждите...");
                assertEquals(true, meterGSM.toDiscoverNew(), "ошибка toDiscover "+index+"/"+hashMap.size()+",  "+getParamsString());
                log.info("!!!Проверка toDiscoverNew  discoverReplyBuffer.size = " + meterGSM.discoverReplyBuffer.size());
                log.info("!!!Проверка toDiscoverNew   успешно");
                log.info("!!!---------------------------------------------");
                log.info("!!!Проверка generateToDiscoverMACs " + getParamsString() + "   ждите...");
                assertEquals(true, meterGSM.generateToDiscoverMACs(), "ошибка generateToDiscoverMACs "+index+"/"+hashMap.size()+",  "+getParamsString());
                log.info("!!!Проверка generateToDiscoverMACs  discoverReplyBuffer.size = " + meterGSM.discoverReplyBuffer.size());
                log.info("!!!Проверка generateToDiscoverMACs   успешно");
                log.info("!!!---------------------------------------------");
                log.info("!!!Проверка saveToYodaRest " + getParamsString() + "  ждите...");
                meterGSM.saveResult = saveResult;
                assertEquals(true, validJSONObject(meterGSM.saveToYodaREST()), "ошибка validJSONObject "+index+"/"+hashMap.size()+",  "+getParamsString());
                log.info("!!!Проверка saveToYodaRest   успешно");
            }
        }
        log.info("!!!---------------------------------------------");
        log.info("!!!Проверка закончена, все Ок!");
    }

    String getParamsString() {
        String ret = "";
        ret = "ip = "+ip+", port = "+port+", num = "+num;
        return ret;
    }

    boolean validateHashMap() {
        boolean ret = true;
        for (Map.Entry<UUID, List<SM160Service.MapCheckSm160Sim>> item: hashMap.entrySet()) {
            ret = validItemMap(item.getKey(), item.getValue());
        }
        return ret;
    }

    boolean validItemMap(UUID key, List<SM160Service.MapCheckSm160Sim> lst) {
        boolean ret = true;
        for (SM160Service.MapCheckSm160Sim item : lst) {
            ret = item.equip_number != null &&
                    item.id != null &&
                    item.ne_id != null &&
                    item.sim_ip != null &&
                    item.sm_port != null;
        }
        return ret && key != null;
    }

    boolean getFirstList(String TOKEN) {
        boolean ret = false;
        List<JSONObject> noLimitFirstList = getFirstForSM160REST("1000000", TOKEN);
        for (JSONObject o: noLimitFirstList) {
            if (limitIPsList.contains(o.getString("sim_ip"))) firstList.add(o);
        }
        System.out.println("!!!noLimitFirstList.size = "+noLimitFirstList.size());
        System.out.println("!!!firstList.size = "+firstList.size());
        firstList.forEach(System.out::println);
        firstListSize = firstList.size();
        for (JSONObject object : firstList) {
                    ret = validJSONObjectFirstList(object);

        }
        return ret;
    }

    boolean validJSONObjectFirstList(JSONObject jsn) {
        boolean ret = true;
        if (!jsn.has("networkPanId")) ret = false;
        else
        if (!jsn.has("sim_id")) ret = false;
        else
        if (!jsn.has("sim_type")) ret = false;
        else
        if (!jsn.has("sim_ip")) ret = false;
        else
        if (!jsn.has("sm_port")) ret = false;
        else
        if (!jsn.has("channelNum")) ret = false;
        else
        if (!jsn.has("id")) ret = false;
        else
        if (!jsn.has("ne_id")) ret = false;
        else
        if (!jsn.has("mac")) ret = false;
        else
        if (!jsn.has("equip_number")) ret = false;

        ret = isValid(jsn.getString("sim_ip")) &&
        isValid(jsn.getString("sm_port")) &&
        isValid(jsn.getString("id")) &&
        isValid(jsn.getString("ne_id")) &&
        isValid(jsn.getString("equip_number"));

        return ret;
    }

    boolean isValid(String value){
        boolean ret = true;
        if (value == JSONObject.NULL || value.equals("null")) ret = false;
        return ret;
    }

    String execLog() {
        sm160Log = logSm160(ip, num, port);
        return sm160Log.getIp();
    }

    MeterGSM execMeterGM() {
        return new MeterGSM(1, 1, ip, port, num, sm160Log.getId().toString(), saveToYoda, TOKEN);
    }

    static void createDBSingle() {
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

    static void setParamsSingle() {
        rest_host_test = "192.1.0.92:8080";
        saveResult = true;

//    //1
//    ip = "10.81.198.39";
//    port = 44451;
//    num = "2210403942744_202695";
//    //2
//    ip = "10.81.144.202";
//    port = 10001;
//    num = "33157";
        //3
        ip = "10.81.186.244";
        port = 44451;
        num = "2210403942759_202710";
//    //4
//    ip = "10.81.176.45";
//    port = 10001;
//    num = "33163";
//    expectedMAC = "C1679D15006F0D00";

        saveToYoda = false;
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

    static void setParams() {
        rest_host_test = "192.1.0.92:8080";
        saveResult = false;
        saveToYoda = false;
        limitIPsList.add("10.129.13.91");
        //limitIPsList.add("10.194.27.39");
        //limitIPsList.add("10.129.3.89");
    }

}
