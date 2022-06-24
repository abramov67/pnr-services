package com.company.pnrservices.entity;

import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.*;

@Table(name = "PNRSERVICES_SM160_LOG_OPERATIONS")
@Entity(name = "pnrservices_SM160LogOperations")
public class SM160LogOperations extends StandardEntity {
    private static final long serialVersionUID = 1388341500363770911L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOG_ID")
    private SM160Log sm160Log;

    @Column(name = "TYPE", length = 255)
    private String type;

    @Lob
    @Column(name = "MESSAGE")
    private String message;

    @Lob
    @Column(name = "STACK_TRACE")
    private String stackTrace;

    public SM160Log getSm160Log() {return sm160Log;}
    public void setSm160Log(SM160Log sm160Log) {this.sm160Log = sm160Log;}

    public String getType(){return type;}
    public void setType(String type){this.type = type;}

    public String getMessage(){return message;}
    public void setMessage(String message){this.message = message;}

    public String getStackTrace(){return stackTrace;}
    public void setStackTrace(String stackTrace){this.stackTrace = stackTrace;}

}