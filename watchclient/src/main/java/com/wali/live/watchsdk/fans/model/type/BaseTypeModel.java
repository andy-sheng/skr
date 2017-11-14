package com.wali.live.watchsdk.fans.model.type;

import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;

/**
 * Created by lan on 2017/8/13.
 */
public abstract class BaseTypeModel extends BaseViewModel {
    protected int mViewType = defaultType();

    protected abstract int defaultType();

    public int getViewType() {
        return mViewType;
    }
}
