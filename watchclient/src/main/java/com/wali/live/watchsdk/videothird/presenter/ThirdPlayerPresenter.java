package com.wali.live.watchsdk.videothird.presenter;

import android.support.annotation.NonNull;

import com.thornbirds.component.IEventController;
import com.wali.live.watchsdk.videodetail.presenter.DetailPlayerPresenter;
import com.wali.live.watchsdk.videothird.data.ThirdStreamerPresenter;

/**
 * Created by yangli on 2017/08/31.
 *
 * @module 第三方播放器视图
 */
public class ThirdPlayerPresenter extends DetailPlayerPresenter {
    private static final String TAG = "ThirdPlayerPresenter";

    @Override
    protected String getTAG() {
        return TAG;
    }

    public ThirdPlayerPresenter(
            @NonNull IEventController controller,
            @NonNull ThirdStreamerPresenter streamerPresenter) {
        super(controller, streamerPresenter);
    }
}
