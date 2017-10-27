package com.wali.live.watchsdk.component.presenter.panel;

import android.support.annotation.NonNull;
import android.util.Log;

import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.view.panel.MessagePanel;

/**
 * Created by yangli on 2017/10/27.
 *
 * @module 私信面板表现
 */
public class MessagePresenter extends ComponentPresenter<MessagePanel.IView>
        implements MessagePanel.IPresenter {
    private static final String TAG = "MessagePresenter";

    @Override
    protected String getTAG() {
        return TAG;
    }

    public MessagePresenter(@NonNull IEventController controller) {
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
