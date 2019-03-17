package com.common.core.global;

import android.text.TextUtils;

import com.common.clipboard.ClipboardUtils;
import com.common.core.account.UserAccountManager;
import com.common.core.kouling.SkrKouLingUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.share.SharePanel;
import com.common.core.share.SharePlatform;
import com.common.core.share.ShareType;
import com.common.log.MyLog;
import com.common.utils.ActivityUtils;
import com.common.utils.LogUploadUtils;
import com.common.utils.U;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import io.agora.rtc.RtcEngine;

public class GlobalEventReceiver {
    public final static String TAG = "GlobalEventReceiver";

    private static class GlobalEventReceiverHolder {
        private static final GlobalEventReceiver INSTANCE = new GlobalEventReceiver();
    }

    private GlobalEventReceiver() {

    }

    public static final GlobalEventReceiver getInstance() {
        return GlobalEventReceiverHolder.INSTANCE;
    }

    public void register() {
        EventBus.getDefault().register(this);
    }


    @Subscribe
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        if (event.foreground) {
            // 检查剪贴板
            String str = ClipboardUtils.getPaste();
            if (!TextUtils.isEmpty(str)) {
                if (SkrKouLingUtils.tryParseScheme(str)) {
                    ClipboardUtils.clear();
                }
            }
        }
    }

    @Subscribe
    public void onEvent(LogUploadUtils.RequestOthersUploadLogSuccess event) {
        SharePanel sharePanel = new SharePanel(U.getActivityUtils().getTopActivity());
        String title = String.format("日志 id=%s,name=%s,date=%s", event.uploaderId, event.uploaderName, event.date);
        sharePanel.setShareContent(event.uploaderAvatar, title, event.extra, event.mLogUrl);
        sharePanel.share(SharePlatform.WEIXIN, ShareType.URL);
        MyLog.w(TAG, title + " url:" + event.mLogUrl);
        U.getToastUtil().showLong(title + "拉取成功，请将其分享给研发同学");
    }
}
