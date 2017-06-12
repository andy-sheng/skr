package com.wali.live.watchsdk.callback;

import com.base.log.MyLog;
import com.mi.live.data.account.HostChannelManager;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;

/**
 * 权限检查回调，用于登录接口
 * <p>
 * Created by wuxiaoshan on 17-2-23.
 */
public abstract class SecureLoginCallback implements ISecureCallBack {
    private static final String TAG = SecureLoginCallback.class.getSimpleName();

    @Override
    public void process(Object... objects) {
        int channelId = (int) objects[0];
        String packageName = (String) objects[1];
        MyLog.w(TAG, "old channelId=" + channelId + "; new channelId=" + HostChannelManager.getInstance().getChannelId());
        if (channelId == HostChannelManager.getInstance().getChannelId() || !BaseComponentSdkActivity.isActive()) {
            UserAccountManager.getInstance().logoffWithoutClearAccount(HostChannelManager.getInstance().getChannelId());
            HostChannelManager.getInstance().setChannelData(channelId, packageName);
            postSuccess();
        } else {
            postActive();
        }
    }

    public abstract void postSuccess();

    public abstract void postActive();
}
