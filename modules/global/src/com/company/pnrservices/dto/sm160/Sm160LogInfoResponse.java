package com.company.pnrservices.dto.sm160;

import com.company.pnrservices.mapper.sm160.Response;

import java.io.Serializable;
import java.util.Date;

public class Sm160LogInfoResponse implements Serializable, Response {

    Date createTs;
    String createdBy;
    Date updateTs;
    String login;
    String panId;
    Integer channel;
    Boolean isJoinPermitted;
    Integer data_receive_id;

    public Date getCreateTs() {
        return createTs;
    }

    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(Date updateTs) {
        this.updateTs = updateTs;
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

    public Boolean getJoinPermitted() {
        return isJoinPermitted;
    }

    public void setJoinPermitted(Boolean joinPermitted) {
        isJoinPermitted = joinPermitted;
    }

    public Integer getData_receive_id() {
        return data_receive_id;
    }

    public void setData_receive_id(Integer data_receive_id) {
        this.data_receive_id = data_receive_id;
    }
}
