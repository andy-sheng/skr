package com.wali.live.watchsdk.longtext.presenter;

import com.base.mvp.IRxView;
import com.wali.live.watchsdk.longtext.model.LongTextModel;

/**
 * Created by lan on 2017/9/19.
 */
public interface ILongTextView extends IRxView {
    void getFeedInfoSuccess(LongTextModel model);

    void notifyFeedInfoDeleted();

    void notifyFeedInfoFailure();
}
