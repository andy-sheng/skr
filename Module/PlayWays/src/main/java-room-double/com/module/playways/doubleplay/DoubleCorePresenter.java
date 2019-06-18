package com.module.playways.doubleplay;

import com.common.mvp.RxLifeCyclePresenter;
import com.module.playways.doubleplay.inter.IDoublePlayView;

public class DoubleCorePresenter extends RxLifeCyclePresenter {
    IDoublePlayView mIDoublePlayView;

    public DoubleCorePresenter(IDoublePlayView IDoublePlayView) {
        mIDoublePlayView = IDoublePlayView;
    }
}
