package com.company.pnrservices.service.hermes;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;

@Source(type = SourceType.DATABASE)
public interface HermesConfig extends Config {
    @Property("hermesPerm.sshUrl")
    @Default("172.20.30.77")
    String getHermesPermSSHUrl();

    @Property("hermesPerm.port")
    @Default("5222")
    Integer getHermesPermSSHPort();

    @Property("hermesPerm.user")
    @Default("user")
    String getHermesPermUser();

    @Property("hermesPerm.password")
    @Default("pass")
    String getHermesPermPassword();

    @Property("sm160.discoverPoolingSec")
    @Default("240")
    String getSm160DiscoverPoolingSec();
}
