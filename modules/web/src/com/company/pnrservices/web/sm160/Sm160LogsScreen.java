package com.company.pnrservices.web.sm160;

import com.company.pnrservices.entity.SM160Log;
import com.company.pnrservices.entity.SM160LogOperations;
import com.company.pnrservices.service.SM160Service;
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
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

@UiController("pnrservices_Sm160LogsScreen")
@UiDescriptor("SM160-logs-screen.xml")
public class Sm160LogsScreen extends Screen {
    @Inject
    private CollectionContainer<SM160Log> idSM160LogsDc;
    @Inject
    private CollectionLoader<SM160Log> idSM160LogsDl;
    @Inject
    private CollectionLoader<SM160LogOperations> idSM160LogOperationsDl;
    @Inject
    private CollectionContainer<SM160LogOperations> idSM160LogOperationsDc;
    @Inject
    private TextField<String> idIPSearchTextField;
    @Inject
    private TextField<String> idNumSearchTextField;
    @Inject
    private GroupTable<SM160Log> idSM160LogsTable;
    @Inject
    private Notifications notifications;
    @Inject
    private UiComponents uiComponents;
    @Inject
    private DataManager dataManager;
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
        idSM160LogsDl.load();
    }

    @Subscribe(id = "idSM160LogsDc", target = Target.DATA_CONTAINER)
    public void onIdSM160LogsDcItemChange(InstanceContainer.ItemChangeEvent<SM160Log> event) {
        idSM160LogOperationsDl.setParameter("log", event.getItem());
        idSM160LogOperationsDl.load();
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
            Optional<SM160Log> log = idSM160LogsDc.getItems()
                    .stream()
                    .filter(it -> it.getIp().contains(idIPSearchTextField.getRawValue()) && it.getNum().contains(idNumSearchTextField.getRawValue()))
                    .findFirst();
            if (log.isPresent()) {
                idSM160LogsTable.setSelected(log.get());
                idSM160LogsTable.scrollTo(log.get());
            }
        }
        if (f1 && !f2) {
            Optional<SM160Log> log = idSM160LogsDc.getItems()
                    .stream()
                    .filter(it -> it.getIp().contains(idIPSearchTextField.getRawValue()))
                    .findFirst();
            if (log.isPresent()) {
                idSM160LogsTable.setSelected(log.get());
                idSM160LogsTable.scrollTo(log.get());
            }
        }
        if (!f1 && f2) {
            Optional<SM160Log> log = idSM160LogsDc.getItems()
                    .stream()
                    .filter(it -> it.getNum().contains(idNumSearchTextField.getRawValue()))
                    .findFirst();
            if (log.isPresent()) {
                idSM160LogsTable.setSelected(log.get());
                idSM160LogsTable.scrollTo(log.get());
            }
        }
    }

    public Component deltaGen(SM160Log entity) {
        if(entity instanceof SM160Log){
            if (entity.getStartTime() != null && entity.getEndTime() != null) {
                Label label = uiComponents.create(Label.class);
                label.setValue(deltaSec(entity.getStartTime(), entity.getEndTime()));
                return label;
            } else return null;
        }else
            return null;
    }

    private String deltaSec(Date date1, Date date2) {
        Duration d = Duration.between(date2.toInstant(), date1.toInstant());
        Long m = Math.abs(d.toMinutes());
        return m.toString();
    }

    public Component getMACsGen(SM160Log entity) {
        Integer cnt = dataManager.
                loadValue("select count(d) cnt " +
                        "from pnrservices_SM160LogDiscovery d " +
                        "where d.sm160Log = :log", Integer.class)
                .parameter("log", entity)
                .one();
        Label label = uiComponents.create(Label.class);
        label.setValue(cnt.toString());
        return label;
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
        if (idIPRun.getRawValue().length() > 0 && idPortRun.getRawValue().length() > 0 && idNumRun.getRawValue().length() > 0) {
            ret = true;
        } else {
            notifications.create()
                    .withDescription("Введите значения для запуска: IP, Port, Номер")
                    .withCaption("Не достаточно данных для запуска")
                    .show();
        }
        return ret;
    }


    //
//    private String deltaDate(Date date1, Date date2) {
//        Duration d = Duration.between(date2.toInstant(), date1.toInstant());
//        DecimalFormat df = new DecimalFormat("00");
//        String hour = df.format(Math.abs(d.toHoursPart()));
//        String minute = df.format(Math.abs(d.toMinutesPart()));
//        String second = df.format(Math.abs(d.toSecondsPart()));
//        String millis = df.format(Math.abs(d.toMillisPart()));
//        return hour+":"+minute+":"+second+"."+millis;
//    }

}