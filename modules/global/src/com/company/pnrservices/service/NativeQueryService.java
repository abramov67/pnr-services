package com.company.pnrservices.service;

import com.company.pnrservices.entity.notpersistent.SM160LogSelectScr;

import java.util.List;

public interface NativeQueryService {
    String NAME = "pnrservices_NativeQueryService";

    List<SM160LogSelectScr> getListAsSM160LogSelectScr(String sql);

}