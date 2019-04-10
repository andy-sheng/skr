package com.module.playways.room.prepare.presenter;

import com.common.mvp.RxLifeCyclePresenter;

public abstract class BaseMatchPresenter extends RxLifeCyclePresenter {
    public abstract void startLoopMatchTask(int playbookItemID, int gameType);

    public abstract void cancelMatch();
}
