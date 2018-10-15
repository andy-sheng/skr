package com.wali.live.watchsdk.fans.pay.presenter;

import com.base.mvp.IRxView;
import com.wali.live.watchsdk.fans.pay.model.FansPayModel;

import java.util.List;

/**
 * Created by lan on 2017/11/21.
 */
public interface IFansPayView extends IRxView {
    void setPayList(List<FansPayModel> list);

    void notifyPayResult(int errorCode, int giftId);
}
