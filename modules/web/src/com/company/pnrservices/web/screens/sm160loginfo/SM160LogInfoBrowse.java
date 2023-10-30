package com.company.pnrservices.web.screens.sm160loginfo;

import com.haulmont.cuba.gui.screen.*;
import com.company.pnrservices.entity.SM160LogInfo;

@UiController("pnrservices_SM160LogInfo.browse")
@UiDescriptor("sm160-log-info-browse.xml")
@LookupComponent("sM160LogInfoesTable")
@LoadDataBeforeShow
public class SM160LogInfoBrowse extends StandardLookup<SM160LogInfo> {
}