package com.company.pnrservices.web.sm160;

import com.company.pnrservices.entity.SM160Log;
import com.company.pnrservices.entity.SM160LogDiscoveryReply;
import com.company.pnrservices.entity.SM160LogOperations;
import com.company.pnrservices.entity.notpersistent.SM160LogSelectScr;
import com.company.pnrservices.service.NativeQueryService;
import com.company.pnrservices.service.SM160Service;
import com.haulmont.cuba.core.app.PersistenceManagerService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.CollectionLoader;
import com.haulmont.cuba.gui.model.InstanceContainer;
import com.haulmont.cuba.gui.screen.*;

import javax.inject.Inject;
import java.util.*;

@UiController("pnrservices_Sm160LogsScreen")
@UiDescriptor("SM160-logs-screen.xml")
public class Sm160LogsScreen extends Screen {
    @Inject
    private CollectionContainer<SM160LogSelectScr> idSM160LogsDc;
    @Inject
    private CollectionLoader<SM160LogOperations> idSM160LogOperationsDl;
    @Inject
    private TextField<String> idIPSearchTextField;
    @Inject
    private TextField<String> idNumSearchTextField;
    @Inject
    private GroupTable<SM160LogSelectScr> idSM160LogsTable;
    @Inject
    private Notifications notifications;
    @Inject
    private TextField<String> idIPRun;
    @Inject
    private TextField<Integer> idPortRun;
    @Inject
    private SM160Service sM160Service;
    @Inject
    private TextField<String> idNumRun;
    @Inject
    private Dialogs dialogs;
    @Inject
    private NativeQueryService nativeQueryService;
    @Inject
    private CollectionLoader<SM160LogDiscoveryReply> idSM160LogDiscoveryReplyDl;
    @Inject
    private CollectionContainer<SM160LogDiscoveryReply> idSM160LogDiscoveryReplyDc;

    @Subscribe
    public void onInit(InitEvent event) {
        idSM160LogsTable.setItemClickAction(new BaseAction("doubleClick").withHandler(e->setIpRun()));
    }

    private void setIpRun() {
        if (idSM160LogsTable.getSingleSelected().getIp() != null) {
            idNumRun.setValue(idSM160LogsTable.getSingleSelected().getNum());
            idIPRun.setValue(idSM160LogsTable.getSingleSelected().getIp());
            idPortRun.setValue(idSM160LogsTable.getSingleSelected().getPort());
        }
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
          idSM160LogsDc.setItems(getListQuery());
    }

    @Subscribe(id = "idSM160LogsDc", target = Target.DATA_CONTAINER)
    public void onIdSM160LogsDcItemChange(InstanceContainer.ItemChangeEvent<SM160LogSelectScr> event) {
        if (event.getItem() != null) {
            idSM160LogOperationsDl.setParameter("log_id", event.getItem().getId());
            idSM160LogOperationsDl.load();

            idSM160LogDiscoveryReplyDl.setParameter("log_id", event.getItem().getId());
            idSM160LogDiscoveryReplyDl.load();
        }
    }

    @Subscribe("idIPSearchTextField")
    public void onIdIPSearchTextFieldEnterPress(TextInputField.EnterPressEvent event) {
        searchTextFieldEnterPress(event);
    }

    @Subscribe("idNumSearchTextField")
    public void onIdNumSearchTextFieldEnterPress(TextInputField.EnterPressEvent event) {
        searchTextFieldEnterPress(event);
    }

    public void searchTextFieldEnterPress(TextInputField.EnterPressEvent event) {
        boolean f1 = !idIPSearchTextField.getRawValue().isEmpty();
        boolean f2 = !idNumSearchTextField.getRawValue().isEmpty();
        if (f1 && f2) {
            Optional<SM160LogSelectScr> log = idSM160LogsDc.getItems()
                    .stream()
                    .filter(it -> it.getIp().contains(idIPSearchTextField.getRawValue()) && it.getNum().contains(idNumSearchTextField.getRawValue()))
                    .findFirst();
            if (log.isPresent()) {
                idSM160LogsTable.setSelected(log.get());
                idSM160LogsTable.scrollTo(log.get());
            }
        }
        if (f1 && !f2) {
            Optional<SM160LogSelectScr> log = idSM160LogsDc.getItems()
                    .stream()
                    .filter(it -> it.getIp().contains(idIPSearchTextField.getRawValue()))
                    .findFirst();
            if (log.isPresent()) {
                idSM160LogsTable.setSelected(log.get());
                idSM160LogsTable.scrollTo(log.get());
            }
        }
        if (!f1 && f2) {
            Optional<SM160LogSelectScr> log = idSM160LogsDc.getItems()
                    .stream()
                    .filter(it -> it.getNum().contains(idNumSearchTextField.getRawValue()))
                    .findFirst();
            if (log.isPresent()) {
                idSM160LogsTable.setSelected(log.get());
                idSM160LogsTable.scrollTo(log.get());
            }
        }
    }

    public void runSingleIP() {
        if (validateRun()) {
            dialogs.createOptionDialog()
                    .withCaption("Подтверждение")
                    .withMessage("Запустить опрос для IP = "+idIPRun.getRawValue()+", port = "+idPortRun.getRawValue()+" ?")
                    .withActions(
                            new DialogAction(DialogAction.Type.OK).withHandler(e -> {
                                sM160Service.checkSM160Single(idNumRun.getRawValue(), idIPRun.getRawValue(), idPortRun.getRawValue());
                                notifications.create().withCaption("Опрос запущен").show();
                            }),
                            new DialogAction(DialogAction.Type.CANCEL)
                    )
                    .show();
        }
    }

    private boolean validateRun() {
        boolean ret = false;
        if (idIPRun.getRawValue().trim().length() > 0
                && idPortRun.getRawValue().trim().length() > 0
                && idNumRun.getRawValue().trim().length() > 0) {
            ret = true;
        } else {
            notifications.create()
                    .withDescription("Введите значения для запуска: IP, Port, Номер")
                    .withCaption("Не достаточно данных для запуска")
                    .show();
        }
        return ret;
    }

    public List<SM160LogSelectScr> getListQuery() {
        return nativeQueryService.getListAsSM160LogSelectScr("select id,version,create_ts,created_by," +
                "update_ts,updated_by,delete_ts,deleted_by,ip," +
                "num,port,end_time,start_time,macs_cnt,delta_time from selectsm160logs()");
    }

    public void refresh() {
        SM160LogSelectScr sel = idSM160LogsTable.getSingleSelected();
        idSM160LogsDc.getMutableItems().clear();
        idSM160LogsDc.setItems(getListQuery());
        if (sel != null) {
            idSM160LogsTable.setSelected(sel);
            idSM160LogsTable.setShowSelection(true);
            idSM160LogsTable.scrollTo(sel);
        }
    }

}