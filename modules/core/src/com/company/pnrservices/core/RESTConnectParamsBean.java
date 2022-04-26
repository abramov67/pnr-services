package com.company.pnrservices.core;

import com.haulmont.cuba.core.global.AppBeans;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component(RESTConnectParamsBean.NAME)
public class RESTConnectParamsBean {
    public static final String NAME = "pnrservices_RESTConnectParamsBean";

    private NativeSQLBean nativeSQLBean;

    public String scheme;
    public String host;
    public String path;
    public String authorization;
    public String contentType;


    public void refresh() {
        nativeSQLBean = AppBeans.get(NativeSQLBean.class);
        List<String> baseParams  = Arrays.stream((Object[]) nativeSQLBean
                .getSingleMain("select url_scheme, url_host, url_path, authorization_, content_type " +
                        "from dev_rest_params where id_type = 0")).map(Object::toString).collect(Collectors.toList());
        scheme = baseParams.get(0);
        host = baseParams.get(1);
        path = baseParams.get(2);
        authorization = baseParams.get(3);
        contentType = baseParams.get(4);
    }

}