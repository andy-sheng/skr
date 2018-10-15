package com.wali.live.watchsdk.fans.pay.model;

import com.wali.live.dao.Gift;
import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;

/**
 * Created by lan on 2017/11/21.
 */
public class FansPayModel extends BaseViewModel {
    private Gift mGift;

    public FansPayModel(Gift gift) {
        mGift = gift;
    }

    public Gift getGift() {
        return mGift;
    }
}
