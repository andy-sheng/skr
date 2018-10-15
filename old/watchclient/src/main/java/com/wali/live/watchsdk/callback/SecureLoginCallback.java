package com.wali.live.watchsdk.callback;

import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.channel.HostChannelManager;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;

/**
 * 权限检查回调，用于登录接口
 * <p>
 * Created by wuxiaoshan on 17-2-23.
 */
public abstract class SecureLoginCallback implements ISecureCallBack {
    private static final String TAG = SecureLoginCallback.class.getSimpleName();

    long mMiId = 0;

    public SecureLoginCallback() {
    }

    public SecureLoginCallback(long miid) {
        mMiId = miid;
    }

    @Override
    public void process(Object... objects) {
        int channelId = (int) objects[0];
        String packageName = (String) objects[1];
        MyLog.w(TAG, "old channelId=" + channelId + "; new channelId=" + HostChannelManager.getInstance().getChannelId());
        //sso 登录 同一账号拦截
        if (channelId == HostChannelManager.getInstance().getChannelId()
                && (mMiId != 0 && mMiId == UserAccountManager.getInstance().getMiId())) {
            MyLog.w(TAG, "the same account. channelId is equal channelId =" + channelId
                    + " and miId is equal mid =" + mMiId);
            postSame();
            return;
        }
        if (channelId == HostChannelManager.getInstance().getChannelId()
                || !BaseComponentSdkActivity.isActive()) {
            UserAccountManager.getInstance().logoffWithoutClearAccount(HostChannelManager.getInstance().getChannelId());
            HostChannelManager.getInstance().setChannelData(channelId, packageName);
            postSuccess();
        } else {
            postActive();
        }
    }

    public abstract void postSame();

    public abstract void postSuccess();

    public abstract void postActive();
}
