package com.company.pnrservices.entity;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.global.DdlGeneration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@DdlGeneration(value = DdlGeneration.DbScriptGenerationMode.CREATE_ONLY)
@Table(name = "pnrservices_rest_params")
@Entity(name = "pnrservices_RestParams")
public class RestParams extends BaseUuidEntity {
    private static final long serialVersionUID = -397789230026054328L;
    @Lob
    @Column(name = "add_params")
    private String addParams;
    @Column(name = "authorization_", length = 1000)
    private String authorization;
    @Column(name = "authorization_token", length = 1000)
    private String authorizationToken;
    @Column(name = "content_type")
    private String contentType;
    @Column(name = "id_type", nullable = false)
    private Integer idType;
    @Column(name = "pwd_token")
    private String pwdToken;
    @Column(name = "url_host", length = 1000)
    private String urlHost;
    @Column(name = "url_path", length = 1000)
    private String urlPath;
    @Column(name = "url_path_token", length = 1000)
    private String urlPathToken;
    @Column(name = "url_scheme")
    private String urlScheme;
    @Column(name = "usr_token")
    private String usrToken;

    public String getUsrToken() {
        return usrToken;
    }

    public void setUsrToken(String usrToken) {
        this.usrToken = usrToken;
    }

    public String getUrlScheme() {
        return urlScheme;
    }

    public void setUrlScheme(String urlScheme) {
        this.urlScheme = urlScheme;
    }

    public String getUrlPathToken() {
        return urlPathToken;
    }

    public void setUrlPathToken(String urlPathToken) {
        this.urlPathToken = urlPathToken;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public String getUrlHost() {
        return urlHost;
    }

    public void setUrlHost(String urlHost) {
        this.urlHost = urlHost;
    }

    public String getPwdToken() {
        return pwdToken;
    }

    public void setPwdToken(String pwdToken) {
        this.pwdToken = pwdToken;
    }

    public Integer getIdType() {
        return idType;
    }

    public void setIdType(Integer idType) {
        this.idType = idType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public void setAuthorizationToken(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getAddParams() {
        return addParams;
    }

    public void setAddParams(String addParams) {
        this.addParams = addParams;
    }
}