package com.module.rankingmode.prepare.sence.controller;

import android.os.Bundle;

public interface MatchSenceController {
    void toNextSence(Bundle bundle);

    void popSence();

    void toAssignSence(MatchSenceContainer.MatchSenceState matchSenceState, Bundle bundle);

    boolean interceptBackPressed();

    int getSenceSize();
}
