package com.mi.liveassistant.unity;

import android.app.Activity;

import com.mi.liveassistant.account.AccountManager;
import com.mi.liveassistant.account.UserAccountManager;
import com.mi.liveassistant.account.callback.IAccountCallback;
import com.mi.liveassistant.barrage.callback.IChatMsgListener;
import com.mi.liveassistant.barrage.callback.ISysMsgListener;
import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.facade.MessageFacade;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.common.security.RSASignature;

import java.util.List;

/**
 * Created by yangli on 2017/5/10.
 */
abstract class UnitySdk<ACTIVITY extends Activity, LISTENER extends ILoginListener> {
    protected final String TAG = getTAG() + "@" + this.hashCode();

    protected ACTIVITY mActivity;
    protected LISTENER mUnityListener;

    //弹幕相关
    protected IBarrageListener mBarrageListener;
    protected long mPlayerId;
    protected String mLiveId;

    protected abstract String getTAG();

    public UnitySdk(ACTIVITY activity, LISTENER listener, IBarrageListener barrageListener) {
        mActivity = activity;
        mUnityListener = listener;
        mBarrageListener = barrageListener;
    }

    protected void pullBarrageIfNeeded(String roomId) {
        if (mBarrageListener == null) {
            return;
        }
        MessageFacade.getInstance().startPull(roomId, new IChatMsgListener() {
            @Override
            public void handleMessage(List<Message> messages) {
                if (mBarrageListener != null && messages != null) {
                    for (Message msg : messages) {
                        mBarrageListener.onChatMsg(msg);
                    }
                }
            }
        }, new ISysMsgListener() {
            @Override
            public void handleMessage(List<Message> messages) {
                if (mBarrageListener != null && messages != null) {
                    for (Message msg : messages) {
                        mBarrageListener.onSysMsg(msg);
                    }
                }
            }
        });
    }

    protected void stopBarrageIfNeeded() {
        if (mBarrageListener == null) {
            return;
        }
        MessageFacade.getInstance().stopPull();
    }

    public void sendTextBarrage(String body) {
        if (mBarrageListener == null) {
            return;
        }
        MessageFacade.getInstance().sendTextMessageAsync(body, mLiveId, mPlayerId);
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
            public void notifySuccess(long uid) {
                MyLog.d(TAG, "notifySuccess");
                if (mUnityListener != null) {
                    mUnityListener.onLoginSuccess(uid);
                }
            }
        });
    }
}
