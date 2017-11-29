package com.wali.live.watchsdk.fans.rank.presenter;

import com.base.mvp.IRxView;
import com.wali.live.watchsdk.fans.rank.model.RankListModel;

/**
 * Created by zhaomin on 17-6-12.
 */
public interface IFansRankView extends IRxView {
    void notifyGetRankListSuccess(RankListModel rankListModel);

    void notifyGetRankListFailure();
}
