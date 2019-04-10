package com.module.playways.room.prepare.view;

import com.module.playways.room.prepare.model.GameReadyModel;
import com.module.playways.room.prepare.model.ReadyInfoModel;

import java.util.List;

public interface IMatchSucessView {
    void ready(boolean isPrepareState);

    void allPlayerIsReady(GameReadyModel gameReadyInfo);

    boolean isReady();

    /**
     * otherEr是否是别人的原因导致无法准备
     * @param otherEr
     */
    void needReMatch(boolean otherEr);

    void readyList(List<ReadyInfoModel> readyInfos);
}
