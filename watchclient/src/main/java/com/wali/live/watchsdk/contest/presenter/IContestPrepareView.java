package com.wali.live.watchsdk.contest.presenter;

import com.base.mvp.IRxView;
import com.wali.live.watchsdk.contest.model.ContestNoticeModel;

/**
 * Created by lan on 2018/1/11.
 */
public interface IContestPrepareView extends IRxView {
    void setContestNotice(ContestNoticeModel model);
}
