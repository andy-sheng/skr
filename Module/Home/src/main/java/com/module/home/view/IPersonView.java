package com.module.home.view;

import com.component.busilib.friends.VoiceInfoModel;
import com.component.person.model.RelationNumModel;
import com.component.person.model.ScoreDetailModel;

import java.util.List;

public interface IPersonView {
    // 展示homepage回来的结果
    void showHomePageInfo(List<RelationNumModel> relationNumModels,
                          int meiLiCntTotal, ScoreDetailModel scoreDetailModel, VoiceInfoModel voiceInfoModel);

    void loadHomePageFailed();
}
