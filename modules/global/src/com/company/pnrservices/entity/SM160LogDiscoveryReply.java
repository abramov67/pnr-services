package com.company.pnrservices.entity;

import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.*;

@Table(name = "PNRSERVICES_SM160_LOG_DISCOVERY_REPLY")
@Entity(name = "pnrservices_SM160LogDiscoveryReply")
public class SM160LogDiscoveryReply extends StandardEntity {
    private static final long serialVersionUID = -2170974145498428492L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOG_ID")
    private SM160Log sm160Log;

    @Lob
    @Column(name = "REPLY")
    private String reply;

    @Lob
    @Column(name = "MESSAGE")
    private String message;

    public SM160Log getSm160Log() {return sm160Log;}
    public void setSm160Log(SM160Log sm160Log) {this.sm160Log = sm160Log;}

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}