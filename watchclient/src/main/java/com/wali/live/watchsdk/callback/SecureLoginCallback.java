package com.wali.live.watchsdk.callback;

import com.mi.live.data.account.HostChannelManager;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;

/**
 * 权限检查回调，用于登录接口
 *
 * Created by wuxiaoshan on 17-2-23.
 */
public abstract class SecureLoginCallback implements ISecureCallBack {
    @Override
    public void process(Object... objects) {
        int channelId = (int)objects[0];
        String packageName = (String)objects[1];
        if(channelId == HostChannelManager.getInstance().getChannelId() || !BaseComponentSdkActivity.isActive()) {
            UserAccountManager.getInstance().logoffWithoutClearAccount(HostChannelManager.getInstance().getChannelId());
            HostChannelManager.getInstance().setChannelData(channelId, packageName);
            postProcess();
        }else {
            onActive();
        }
    }

    public abstract void postProcess();

    public abstract void onActive();
}
