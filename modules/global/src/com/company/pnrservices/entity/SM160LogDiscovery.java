package com.company.pnrservices.entity;

import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.*;

@Table(name = "PNRSERVICES_SM160_LOG_DISCOVERY")
@Entity(name = "pnrservices_SM160LogDiscovery")
public class SM160LogDiscovery extends StandardEntity {
    private static final long serialVersionUID = 8112464111249840572L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOG_ID")
    private SM160Log sm160Log;

    @Column(name = "MAC", length = 16)
    private String mac;

    @Lob
    @Column(name = "MESSAGE")
    private String message;

    public SM160Log getSm160Log() {return sm160Log;}
    public void setSm160Log(SM160Log sm160Log) {this.sm160Log = sm160Log;}

    public String getMac(){return mac;}
    public void setMac(String mac){this.mac = mac;}

    public String getMessage(){return message;}
    public void setMessage(String message){this.message = message;}
}