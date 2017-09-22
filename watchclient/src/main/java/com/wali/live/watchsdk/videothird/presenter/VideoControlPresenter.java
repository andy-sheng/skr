package com.wali.live.watchsdk.videothird.presenter;

import android.support.annotation.NonNull;
import android.util.Log;

import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.videothird.view.VideoControlView;

/**
 * Created by yangli on 2017/09/22.
 *
 * @module 播放控制表现
 */
public class VideoControlPresenter extends ComponentPresenter<VideoControlView.IView>
        implements VideoControlView.IPresenter {
    private static final String TAG = "VideoControlPresenter";

    @Override
    protected String getTAG() {
        return TAG;
    }

    public VideoControlPresenter(@NonNull IEventController controller) {
        super(controller);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            Log.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            default:
                break;
        }
        return false;
    }
}
