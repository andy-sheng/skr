package com.base.player.component;

public abstract class ComponentPresenter<VIEW> {
    protected final String TAG = getTAG();

    protected VIEW mView;

    protected abstract String getTAG();

    public void setView(VIEW view) {
        mView = view;
    }

    public void startPresenter() {
    }

    public void stopPresenter() {
    }

    public void destroy() {
    }

}