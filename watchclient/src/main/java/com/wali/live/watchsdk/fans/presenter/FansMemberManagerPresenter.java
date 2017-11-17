package com.wali.live.watchsdk.fans.presenter;

import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.fans.view.FansMemberManagerView;

/**
 * Created by yangli on 2017/11/16.
 *
 * @module 粉丝团成员管理页表现
 */
public class FansMemberManagerPresenter extends ComponentPresenter<FansMemberManagerView.IView>
        implements FansMemberManagerView.IPresenter {
    private static final String TAG = "FansMemberManagerPresenter";

    @Override
    protected final String getTAG() {
        return TAG;
    }

    public FansMemberManagerPresenter(@NonNull IEventController controller) {
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
