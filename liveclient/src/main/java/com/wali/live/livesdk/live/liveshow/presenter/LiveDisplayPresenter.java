package com.wali.live.livesdk.live.liveshow.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.live.liveshow.view.LiveDisplayView;

/**
 * Created by yangli on 2017/03/08.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 直播大小窗表现
 */
public class LiveDisplayPresenter extends ComponentPresenter<LiveDisplayView.IView>
        implements LiveDisplayView.IPresenter {
    private static final String TAG = "LiveDisplayPresenter";

    public LiveDisplayPresenter(
            @NonNull IComponentController componentController) {
        super(componentController);
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                default:
                    break;
            }
            return false;
        }
    }
}
