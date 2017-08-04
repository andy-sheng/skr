package com.wali.live.livesdk.live.liveshow.presenter;

import android.support.annotation.NonNull;

import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.live.liveshow.view.LiveDisplayView;

/**
 * Created by yangli on 2017/03/08.
 *
 * @module 直播大小窗表现
 */
public class LiveDisplayPresenter extends ComponentPresenter<LiveDisplayView.IView>
        implements LiveDisplayView.IPresenter {
    private static final String TAG = "LiveDisplayPresenter";

    @Override
    protected String getTAG() {
        return TAG;
    }

    public LiveDisplayPresenter(@NonNull IEventController controller) {
        super(controller);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        return false;
    }
}
