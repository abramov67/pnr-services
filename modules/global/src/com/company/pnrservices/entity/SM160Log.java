package com.company.pnrservices.entity;

import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Date;

@Table(name = "PNRSERVICES_SM160_LOG", indexes = {
        @Index(name = "IDX_PNRSERVICES_SM160_LOG_UNIQ_IP", columnList = "ip", unique = true)
})
@Entity(name = "pnrservices_SM160Log")
public class SM160Log extends StandardEntity {
    private static final long serialVersionUID = 4751575822546323949L;

    @Column(name = "IP", length = 15)
    private String ip;

    @Column(name = "NUM", length = 255)
    private String num;

    @Column(name = "PORT")
    private Integer port;

    @Column(name = "START_TIME")
    private Date startTime;

    @Column(name = "END_TIME")
    private Date endTime;

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

}