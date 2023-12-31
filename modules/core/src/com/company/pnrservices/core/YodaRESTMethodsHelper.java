package com.company.pnrservices.core;

import com.company.pnrservices.entity.RestParams;
import com.haulmont.cuba.core.global.AppBeans;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class YodaRESTMethodsHelper {

    public static List<JSONObject> jsonArrayToListJSONObjects(String jsonArray) {
        List<JSONObject> ret = new ArrayList();
        (new JSONArray(jsonArray)).forEach(t -> ret.add(new JSONObject(t.toString())));
        return ret;
    }

    public static List<String> jsonArrayToListStrings(String jsonArray) {
        List<String> ret = new ArrayList();
        (new JSONArray(jsonArray)).forEach(t -> ret.add(t.toString()));
        return ret;
    }

    private static String getStringValueFromJsonArray(String jsonArray, int i, String type) {
        if (type.equals("object")) {
            return jsonArray;
        }
        if (type.equals("array")) {
            JSONArray jsnArray = new JSONArray(jsonArray);
            if (jsnArray.length() >= i + 1)
                return (new JSONArray(jsonArray)).get(i).toString();
            else return null;
        }
        return null;
    }

    private static List<NameValuePair> createListPairParams(String paramsSplit) {
        return Arrays.stream(paramsSplit.split(";")).map(t -> {
            String[] s = t.split("=");
            return new BasicNameValuePair(s[0], s[1]);
        }).collect(Collectors.toList());
    }

    private static String baseREST(List<NameValuePair> params, String token, String methodName, String retType) {
        RESTConnectParamsBean restParams = AppBeans.get(RESTConnectParamsBean.class);
        URIBuilder builder = new URIBuilder();
        builder.setScheme(restParams.scheme)
                .setHost(restParams.host)
                .setPath(restParams.path+methodName);

        if (params != null)
            builder.setParameters(params);

        try {
            URI uri = builder.build();
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet(uri);
            getRequest.addHeader("Authorization", restParams.authorization + token);
            getRequest.addHeader("content-type", restParams.contentType);

            HttpResponse response = httpClient.execute(getRequest);
            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            return getStringValueFromJsonArray(br.readLine(), 0, retType);
        } catch (URISyntaxException e1) {
            System.out.println("!!!"+methodName+" URISyntaxException: "+e1.getMessage());
            e1.printStackTrace();
        } catch (IOException e2) {
            System.out.println("!!!"+methodName+" IOException: "+e2.getMessage());
            e2.printStackTrace();
        }
        return null;
    }

    //yes
    public static String getNewToken() {
        RESTConnectParamsBean restConnectParamsBean = AppBeans.get(RESTConnectParamsBean.class);
        restConnectParamsBean.refresh();

        NativeSQLBean nativeSQLBean = AppBeans.get(NativeSQLBean.class);

        String token;
        RestParams baseParams = nativeSQLBean.getBaseParams();
        URIBuilder builder = new URIBuilder();
        builder.setScheme(baseParams.getUrlScheme())
                .setHost(baseParams.getUrlHost())
                .setPath(baseParams.getUrlPathToken());

        try {
            URI uri = builder.build();
            org.apache.http.client.HttpClient httpClient = HttpClientBuilder.create().build();
            org.apache.http.client.methods.HttpPost getRequest = new HttpPost(uri);
            getRequest.addHeader("Authorization", baseParams.getAuthorizationToken());
            getRequest.addHeader("content-type", baseParams.getContentType());

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("grant_type", "password"));
            params.add(new BasicNameValuePair("username", baseParams.getUsrToken()));
            params.add(new BasicNameValuePair("password", baseParams.getPwdToken()));
            getRequest.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse response = httpClient.execute(getRequest);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

            JSONObject obj = new JSONObject(br.readLine());
            token = obj.getString("access_token");
            return token;
        } catch (URISyntaxException e1) {
            System.out.println("!!!getNewToken URISyntaxException: "+e1.getMessage());
            e1.printStackTrace();
        } catch (IOException e2) {
            System.out.println("!!!getNewToken IOException: "+e2.getMessage());
            e2.printStackTrace();
        }
        return null;
    }

    //yes
    public static String upsertTopologyREST(String hermes_id, String terminal_id, String meter_id, String mac, String token) {
        return baseREST(
                createListPairParams("terminal_id="+terminal_id+";mac="+mac+";meter_id="+meter_id+";hermes_id="+hermes_id),
                token, "upsertTopology", "array");
    }

    //yes
    public static List<String> getMeterIdREST(String macs, String token) {
        return jsonArrayToListStrings(baseREST(
                createListPairParams("macs="+macs),
                token, "getMeterId", "object"));
    }

    //yes
    public static JSONObject getHermesParamsREST(String hermes_id, String token) {
        return new JSONObject(baseREST(
                createListPairParams("hermes_id="+hermes_id),
                token, "getHermesParams", "object"));
    }

    //yes
    public static List<String> getListForCloseTerminalsREST(String hermes_id, String token) {
        String ret = baseREST(createListPairParams("hermes_id="+hermes_id),
                token, "getTerminalsForClosed", "object");
        return jsonArrayToListStrings(ret);
    }

    //yes
    public static List<JSONObject> getMACListForUpdateSerialREST(String hermes_id, String limit, String token, String daysInt) {
        return jsonArrayToListJSONObjects(baseREST(
                createListPairParams("hermes_id="+hermes_id+";limit="+limit+";daysInt="+daysInt),
                token, "getMACListForUpdateSerial", "object"));
    }

    //yes
    public static void updateSerialREST(String meter_id, String mac, String number, String token) {
        baseREST(createListPairParams("meter_id="+meter_id+";mac="+mac+";number="+number),
                token, "updateMeterForUpdateSerial", "object");
    }

    public static void clearTopologyREST(String savedDaysOfHistory, String token) {
        baseREST(createListPairParams("savedDaysOfHistory="+savedDaysOfHistory),
                token, "clearTopology", "object");
    }

    public static List<String> getTerminalsForUpdateTopologyREST(String token) {
        return jsonArrayToListStrings(baseREST(null,
                token, "getTerminalsForUpdateTopology", "object"));
    }

    public static void upsertTerminalForUpdateTopologyREST(String jsonObjParams, String token) {
        baseREST(createListPairParams("paramsJSONObject="+jsonObjParams),
                token, "upsertTerminalForUpdateTopology", "object");
    }

    public static List<String> getHermesIDListForUpdateTopologyREST(String token) {
        return jsonArrayToListStrings(baseREST(null,
                token, "getHermesIDListForUpdateTopology", "object"));
    }

    public static void upsertMeterForUpdateTopologyREST(String jsonObjParams, String token) {
        baseREST(createListPairParams("paramsJSONObject="+jsonObjParams),
                token, "upsertMeterForUpdateTopology", "object");
    }

    public static List<JSONObject> getFirstForSM160REST(String limit, String token) {
        return jsonArrayToListJSONObjects(baseREST(createListPairParams("limit="+limit),
                token, "getFirstForSM160", "object"));
    }

    public static void updaterForSM160REST(String jsonObjParams, String token) {
        baseREST(createListPairParams("paramsJSONObject="+jsonObjParams),
                token, "updateTopologySM160", "object");
                //token, "updaterForSM160", "object");
    }

//    public static void updaterForSM160RESTSingle(String jsonObjParams, String token) {
//        baseREST(createListPairParams("paramsJSONObject="+jsonObjParams),
//                token, "updateTopologySM160", "object");
////                token, "updaterForSM160Single", "object");
//    }

    public static void clearTopologyForTerminalREST(String terminal, String token) {
        baseREST(createListPairParams("terminal="+terminal),
                token, "clearTopologyForTerminal", "object");
    }

    public static String getMacsListForHermesREST(String token) {
        return baseREST(null,
                token, "getMacsListForHermes", "object");
    }

}
