package com.wali.live.watchsdk.videothird.data;

import android.support.annotation.NonNull;

import com.base.global.GlobalData;
import com.wali.live.ipselect.FeedsIpSelectionHelper;
import com.wali.live.watchsdk.videodetail.data.PullStreamerPresenter;

/**
 * Created by yangli on 2017/08/31.
 *
 * @module 第三方拉流器 IP优选和重连辅助类
 */
public class ThirdStreamerPresenter extends PullStreamerPresenter {
    private static final String TAG = "ThirdStreamerPresenter";

    private boolean mIsLocalVideo = false;

    @Override
    protected final String getTAG() {
        return TAG;
    }

    @Override
    public final boolean isLocalVideo() {
        return mIsLocalVideo;
    }

    public ThirdStreamerPresenter(@NonNull PlayerCallbackWrapper callbackWrapper) {
        mOuterCallback = callbackWrapper;
        mUIHandler = new MyUIHandler(this);
        mReconnectHelper = new ReconnectHelper();
        mIpSelectionHelper = new FeedsIpSelectionHelper(GlobalData.app(), mReconnectHelper);
    }

    public void setOriginalStreamUrl(String originalStreamUrl) {
        mIpSelectionHelper.setOriginalStreamUrl(originalStreamUrl);
        mIsLocalVideo = originalStreamUrl == null ||
                originalStreamUrl.startsWith("file://") ||
                originalStreamUrl.startsWith("content://") ||
                originalStreamUrl.startsWith("/");
    }
}
