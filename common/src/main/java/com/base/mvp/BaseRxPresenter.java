package com.base.mvp;

/**
 * Created by lan on 17/4/5.
 */
public class BaseRxPresenter<IV extends IRxView> {
    protected final String TAG = getTAG();

    protected IV mView;

    protected String getTAG() {
        return getClass().getSimpleName();
    }
}
