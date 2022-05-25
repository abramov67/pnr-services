package com.company.pnrservices.entity;

import com.haulmont.cuba.core.entity.BaseStringIdEntity;
import com.haulmont.cuba.core.global.DdlGeneration;

import javax.persistence.*;
import java.util.Date;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.CREATE_ONLY)
@Table(name = "pnrservices_last_closed_terminals")
@Entity(name = "pnrservices_LastClosedTerminals")
public class LastClosedTerminals extends BaseStringIdEntity {
    private static final long serialVersionUID = -5011652523234089342L;
    @Id
    @Column(name = "imei", nullable = false)
    private String imei;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dt")
    private Date dt;
    @Column(name = "hermes_id")
    private String hermes;
    @Lob
    @Column(name = "mess")
    private String mess;

    public String getMess() {
        return mess;
    }

    public void setMess(String mess) {
        this.mess = mess;
    }

    public String getHermes() {
        return hermes;
    }

    public void setHermes(String hermes) {
        this.hermes = hermes;
    }

    public Date getDt() {
        return dt;
    }

    public void setDt(Date dt) {
        this.dt = dt;
    }

    @Override
    public void setId(String id) {
        this.imei = id;
    }

    @Override
    public String getId() {
        return imei;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }
}