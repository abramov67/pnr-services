package com.company.pnrservices.entity;

import com.haulmont.cuba.core.entity.BaseStringIdEntity;
import com.haulmont.cuba.core.global.DdlGeneration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.CREATE_ONLY)
@Table(name = "pnrservices_zbdropmodules")
@Entity(name = "pnrservices_Zbdropmodules")
public class Zbdropmodules extends BaseStringIdEntity {
    private static final long serialVersionUID = -8976419662555871910L;
    @Id
    @Column(name = "mac", nullable = false)
    private String mac;
    @Column(name = "num")
    private String num;

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    @Override
    public void setId(String id) {
        this.mac = id;
    }

    @Override
    public String getId() {
        return mac;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}