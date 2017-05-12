package com.mi.liveassistant.unity;

import android.app.Activity;

import com.mi.liveassistant.account.UserAccountManager;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.account.AccountManager;
import com.mi.liveassistant.account.callback.IAccountCallback;
import com.mi.liveassistant.utils.RSASignature;

/**
 * Created by yangli on 2017/5/10.
 */
abstract class UnitySdk<ACTIVITY extends Activity, LISTENER extends ILoginListener> {
    private final String TAG = getTAG();

    protected ACTIVITY mActivity;
    protected LISTENER mUnityListener;

    protected abstract String getTAG();

    public UnitySdk(ACTIVITY activity, LISTENER listener) {
        mActivity = activity;
        mUnityListener = listener;
    }

    public boolean isLogin() {
        return UserAccountManager.getInstance().hasAccount();
    }

    public void login(String rsaKey, String uid, String name, int sex, int channelId, String avatarUrl) {
        String signStr = "channelId=" + channelId + "&headUrl=" + avatarUrl + "&nickname=" + name + "&sex=" + sex + "&xuid=" + uid;
        String sign = RSASignature.sign(signStr, rsaKey, "UTF-8");
        AccountManager.getInstance().thirdPartLogin(channelId, uid, name, avatarUrl, sex, sign, new IAccountCallback() {
            @Override
            public void notifyFail(int errCode) {
                MyLog.d(TAG, "notifyFail");
                if (mUnityListener != null) {
                    mUnityListener.onLoginFailed(errCode, "thirdPartLogin failed");
                }
            }

            @Override
            public void notifySuccess(String uid) {
                MyLog.d(TAG, "notifySuccess");
                if (mUnityListener != null) {
                    mUnityListener.onLoginSuccess(uid);
                }
            }
        });
    }
}
