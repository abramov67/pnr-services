package com.company.pnrservices.core;

import org.json.JSONObject;

import static com.company.pnrservices.core.YodaRESTMethodsHelper.upsertMeterForUpdateTopologyREST;
import static com.company.pnrservices.core.YodaRESTMethodsHelper.upsertTerminalForUpdateTopologyREST;

public class UpdateTopologyHelper {

    public static class TerminalUpdateTopologyThread extends Thread {
        Integer index;
        Integer size;
        String token;
        JSONObject params;

        public TerminalUpdateTopologyThread(Integer index, Integer size, String token, JSONObject params) {
            this.index = index;
            this.size = size;
            this.token = token;
            this.params = params;
        }

        @Override
        public void run() {
            super.run();
            upsertTerminal();
        }

        public void upsertTerminal() {
            upsertTerminalForUpdateTopologyREST(params.toString(), token);
        }
    }

    public static class MeterUpdateTopologyThread extends Thread {
        Integer indexTerminal;
        Integer sizeTerminal;
        Integer indexMeter;
        Integer sizeMeter;
        String token;
        JSONObject params;
        Integer globalCounter;

        public MeterUpdateTopologyThread(Integer globalCounter, Integer indexTerminal, Integer sizeTerminal, Integer indexMeter, Integer sizeMeter, String token, JSONObject params) {
            this.indexTerminal = indexTerminal;
            this.sizeTerminal = sizeTerminal;
            this.indexMeter = indexMeter;
            this.sizeMeter = sizeMeter;
            this.token = token;
            this.params = params;
            this.globalCounter = globalCounter;
        }

        @Override
        public void run() {
            super.run();
            upsertMeter();
        }

        public void upsertMeter() {
            upsertMeterForUpdateTopologyREST(params.toString(), token);
        }
    }


}
