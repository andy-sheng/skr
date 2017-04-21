package com.mi.liveassistant.room.mvp;

/**
 * Created by lan on 17/4/13.
 */
public class BaseRxPresenter<V extends IView> {
    protected final String TAG = getTAG();

    protected V mView;

    public BaseRxPresenter(V view) {
        mView = view;
    }

    protected String getTAG() {
        return getClass().getSimpleName();
    }
}
