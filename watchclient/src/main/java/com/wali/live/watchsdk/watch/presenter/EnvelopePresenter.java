package com.wali.live.watchsdk.watch.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.mi.live.data.event.GiftEventClass;
import com.wali.live.component.presenter.ComponentPresenter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by yangli on 2017/07/12.
 *
 * @module 抢红包表现
 */
public class EnvelopePresenter extends ComponentPresenter<RelativeLayout> {
    private static final String TAG = "EnvelopePresenter";

    public EnvelopePresenter(@NonNull IComponentController componentController) {
        super(componentController);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false, priority = 1)
    public void onEventMainThread(GiftEventClass.GiftAttrMessage.RedEnvelope event) {
        if (event != null && event.red != null) {
//            play(event.red);
        }
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
