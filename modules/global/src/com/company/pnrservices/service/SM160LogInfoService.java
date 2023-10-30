package com.company.pnrservices.service;

import com.company.pnrservices.entity.SM160LogInfo;
import com.haulmont.cuba.core.global.View;

import java.util.List;

public interface SM160LogInfoService {
    String NAME = "pnrservices_SM160LogInfoService";


    void upsertStoredProc(String p_login, String p_pan_id, Integer p_channel, Boolean p_is_join_permitted);

    List<SM160LogInfo> find(View view);
}