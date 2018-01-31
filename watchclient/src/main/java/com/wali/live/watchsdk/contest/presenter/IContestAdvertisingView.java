package com.wali.live.watchsdk.contest.presenter;

import com.base.mvp.IRxView;

/**
 * Created by wanglinzhang on 2018/1/30.
 */

public interface IContestAdvertisingView extends IRxView {

    void processChanged(int percent);

    void statusChanged(ContestAdvertisingPresenter.State status);
}
