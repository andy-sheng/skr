package com.wali.live.watchsdk.videothird.presenter;

import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.videothird.view.ThirdVideoPlayerView;

/**
 * Created by yangli on 2017/08/31.
 * <p>
 * Generated using create_view_with_presenter.py
 *
 * @module 第三方播放器视图
 */
public class ThirdVideoPlayerPresenter extends ComponentPresenter<ThirdVideoPlayerView.IView>
        implements ThirdVideoPlayerView.IPresenter {
    private static final String TAG = "ThirdVideoPlayerPresenter";

    @Override
    protected String getTAG() {
        return TAG;
    }

    public ThirdVideoPlayerPresenter(@NonNull IEventController controller) {
        super(controller);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            default:
                break;
        }
        return false;
    }
}
