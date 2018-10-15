package com.mi.liveassistant.milink.callback;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.milink.sdk.data.Const;

/**
 * 此类负责监听mlink的状态
 * Created by MK on 15-3-30.
 */
public class MiLinkChannelStatusObserver extends MiLinkStatusObserver {
    public MiLinkChannelStatusObserver() {
        super();
    }

    protected String getTAG() {
        return "MiLinkChannelStatusObserver";
    }

    @Override
    public void onLoginStateUpdate(int i) {
        MyLog.w(TAG + " onLoginStateUpdate ,i=" + i);
        mLoginState = i;
        if (mLoginState == Const.LoginState.NotLogin) {
        } else if (mLoginState == Const.LoginState.Logined) {
        }
    }
}
