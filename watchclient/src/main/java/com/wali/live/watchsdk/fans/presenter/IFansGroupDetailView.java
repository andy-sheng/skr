package com.wali.live.watchsdk.fans.presenter;

import com.base.mvp.IRxView;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;

/**
 * Created by lan on 2017/11/9.
 */
public interface IFansGroupDetailView extends IRxView {
    void getFansGroupDetailSuccess(FansGroupDetailModel model);
}
