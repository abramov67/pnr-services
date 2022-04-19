package com.company.pnrservices.service;

public interface DropModulesService {
    String NAME = "pnrservices_DropModulesService";

    void topologyUpdate();
    void openClosedTerminals();
    void dropZBModules(String withCloseTerminals);

}