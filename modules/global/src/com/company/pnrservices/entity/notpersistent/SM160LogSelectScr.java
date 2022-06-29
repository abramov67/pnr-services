package com.company.pnrservices.entity.notpersistent;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;

import java.time.Duration;
import java.util.Date;

@MetaClass(name = "pnrservices_SM160LogSelectScr")
@NamePattern("%s|id")
public class SM160LogSelectScr extends StandardEntity {
    private static final long serialVersionUID = 4723932066318183538L;

    @MetaProperty
    private String ip;

    @MetaProperty
    private String num;

    @MetaProperty
    private Integer port;

    @MetaProperty
    private Date startTime;

    @MetaProperty
    private Date endTime;

    @MetaProperty
    private Long macsCnt;

    @MetaProperty
    private Date deltaTime;

    public String getIp() {
        return ip;
    }
    public void setIp(String ip){this.ip = ip;}

    public String getNum() {return num; }
    public void setNum(String num) {this.num = num; }

    public Integer getPort() {
        return port;
    }
    public void setPort(Integer port) {this.port = port;}

    public Date getStartTime() {
        return startTime;
    }
    public void setStartTime(Date startTime) {this.startTime = startTime;}

    public Date getEndTime() {
        return endTime;
    }
    public void setEndTime(Date endTime) {this.endTime = endTime;}

    public Long getMacsCnt() {
        return macsCnt;
    }
    public void setMacsCnt(Long macsCnt) {this.macsCnt = macsCnt;}

    public Date getDeltaTime() {
        return deltaTime;
    }
    public void setDeltaTime(Date deltaTime) {
        this.deltaTime = deltaTime;
    }

}