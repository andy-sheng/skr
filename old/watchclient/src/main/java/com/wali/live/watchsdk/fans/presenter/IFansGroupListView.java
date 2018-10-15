package com.wali.live.watchsdk.fans.presenter;

import com.base.mvp.IRxView;
import com.wali.live.watchsdk.fans.model.FansGroupListModel;

/**
 * Created by lan on 2017/11/7.
 */
public interface IFansGroupListView extends IRxView {
    void setFansGroupList(FansGroupListModel model);

    void notifyCreateGroupResult(boolean isSuccess);
}
