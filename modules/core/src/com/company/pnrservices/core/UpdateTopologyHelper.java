package com.company.pnrservices.core;

import org.json.JSONObject;

import static com.company.pnrservices.core.YodaRESTMethodsHelper.upsertTerminalForUpdateTopologyREST;

public class UpdateTopologyHelper {

    public static class UpdateTopologyThread extends Thread {
        Integer index;
        Integer size;
//        String imeiShell;
//        String idYoda;
        String token;
        JSONObject params;

        public UpdateTopologyThread(Integer index, Integer size, String token, JSONObject params) {
            this.index = index;
            this.size = size;
            this.token = token;
            this.params = params;
        }

//        public UpdateTopologyThread(Integer index, Integer size, String imeiShell, String idYoda, String token) {
//            this.index = index;
//            this.size = size;
//            this.imeiShell = imeiShell;
//            this.idYoda = idYoda;
//            this.token = token;
//        }

        @Override
        public void run() {
            super.run();
            upsertTerminal();
        }

        private void upsertTerminal() {
//            JSONObject params = new JSONObject();
//            params.put("imeiShell", imeiShell);
//            params.put("idYoda", idYoda == null ? JSONObject.NULL : idYoda);

            System.out.println(index+"/"+size+" !!!params = "+params.toString());

            upsertTerminalForUpdateTopologyREST(params.toString(), token);

        }

//            if terminal in self.terminals_from_yoda:
//        sql += "UPDATE public.enerstroymain_network_equipment SET HERMES_IP='%s',ONLINE_STATATUS=%s,LAST_ACTIVITY='%s', last_bot_connection=NOW() where imei='%s' ;" % (hermeses[self.hermes_id]['hermes_ip'],terminals[terminal]['status'],terminals[terminal]['terminal_activity'],terminal,)
//                else:
//        sql += "INSERT INTO public.enerstroymain_network_equipment (id, version, CREATED_BY, CREATE_TS, UPDATED_BY, UPDATE_TS,  type_id, model, number_, mac, imei, is_used,  manufacturer_id,  last_activity, online_statatus, version_number, hermes_ip, LAST_BOT_CONNECTION) VALUES   (newid(), 1, 'patrik', now(),'patrik', now(),  '8034dd00-f1fb-8f52-eed7-13b2724854bb', 'Коммуникационный шлюз арт. ШЛ-ZB-02', '%s', '','%s', False, '7ff1a40a-4964-8bfa-12a9-ccb8a65c795c', '%s', '%s', '', '%s', '2001-01-01 00:00:00');" % (terminal,terminal,terminals[terminal]['terminal_activity'], terminals[terminal]['status'], hermeses[self.hermes_id]['hermes_ip'] )
//                if sql:
//                self.pg.execute_and_commit(sql)
//                return len(terminals)


    }

}
