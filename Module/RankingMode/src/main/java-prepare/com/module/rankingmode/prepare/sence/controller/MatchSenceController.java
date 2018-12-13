package com.module.rankingmode.prepare.sence.controller;

import android.os.Bundle;

import com.common.view.titlebar.CommonTitleBar;
import com.module.rankingmode.prepare.model.PrepareData;

public interface MatchSenceController {
    void toNextSence(PrepareData data);

    void popSence();

    void toAssignSence(MatchSenceContainer.MatchSenceState matchSenceState, PrepareData prepareData);

    boolean interceptBackPressed();

    int getSenceSize();

    CommonTitleBar getCommonTitleBar();

    void setCommonTitleBar(CommonTitleBar commonTitleBar);
}
