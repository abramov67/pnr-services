package com.company.pnrservices.service;

public interface SM160Service {
    String NAME = "pnrservices_SM160Service";

    void checkSM160(String limit);
    void checkSM160Single(String num, String ip, String port);
}