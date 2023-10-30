package com.company.pnrservices.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.Creatable;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "PNRSERVICES_SM160_LOG_INFO")
@Entity(name = "pnrservices_SM160LogInfo")
@NamePattern("%s|id")
public class SM160LogInfo extends BaseUuidEntity implements Creatable {
    private static final long serialVersionUID = -8281820783296437090L;

    @Column(name = "CREATE_TS")
    protected Date createTs;

    @Column(name = "CREATED_BY", length = 50)
    protected String createdBy;

    @Column(name = "UPDATE_TS")
    protected Date updateTs;

    @Column(name = "login", nullable = false, length = 100)
    private String login;

    @Column(name = "pan_id", nullable = false, length = 50)
    private String panId;

    @Column(name = "channel", nullable = false)
    private Integer channel;

    @Column(name = "is_join_permitted", nullable = false)
    private Boolean isJoinPermitted;

    @Column(name = "data_receive_id", nullable = false)
    private Integer data_receive_id;


    public Date getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(Date updateTs) {
        this.updateTs = updateTs;
    }

    @Override
    public Date getCreateTs() {
        return createTs;
    }

    @Override
    public void setCreateTs(Date date) {
        this.createTs = date;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPanId() {
        return panId;
    }

    public void setPanId(String panId) {
        this.panId = panId;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public Integer getData_receive_id() {
        return data_receive_id;
    }

    public void setData_receive_id(Integer data_receive_id) {
        this.data_receive_id = data_receive_id;
    }

    public Boolean getIsJoinPermitted() {
        return isJoinPermitted;
    }

    public void setIsJoinPermitted(Boolean joinPermitted) {
        isJoinPermitted = joinPermitted;
    }
}