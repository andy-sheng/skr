package com.wali.live.watchsdk.fans.presenter;

import com.base.mvp.IRxView;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.model.member.FansMemberModel;

import java.util.List;

/**
 * Created by lan on 2017/11/9.
 */
public interface IFansGroupDetailView extends IRxView {
    void setFansGroupDetail(FansGroupDetailModel model);

    void setTopThreeMember(List<FansMemberModel> modelList);
}
