package com.module.rankingmode.prepare.sence.controller;

import android.os.Bundle;

import com.common.view.titlebar.CommonTitleBar;

public interface MatchSenceController {
    void toNextSence(Bundle bundle);

    void popSence();

    void toAssignSence(MatchSenceContainer.MatchSenceState matchSenceState, Bundle bundle);

    boolean interceptBackPressed();

    int getSenceSize();

    CommonTitleBar getCommonTitleBar();

    void setCommonTitleBar(CommonTitleBar commonTitleBar);
}
