package com.wali.live.watchsdk.videothird.data;

import com.base.global.GlobalData;
import com.wali.live.ipselect.FeedsIpSelectionHelper;

/**
 * Created by yangli on 2017/08/31.
 *
 * @module 第三方拉流器 IP优选和重连辅助类
 */
public class ThirdStreamerPresenter extends PullStreamerPresenter {
    private static final String TAG = "ThirdStreamerPresenter";

    private boolean mPaused = true;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public ThirdStreamerPresenter() {
        mUIHandler = new MyUIHandler(this);
        mReconnectHelper = new ReconnectHelper();
        mIpSelectionHelper = new FeedsIpSelectionHelper(GlobalData.app(), mReconnectHelper);
    }

    public void setOriginalStreamUrl(String originalStreamUrl) {
        mIpSelectionHelper.setOriginalStreamUrl(originalStreamUrl);
    }

    // 恢复播放
    public void resumeWatch() {
        if (mStreamer == null || !mStarted || mPaused) {
            return;
        }
        mPaused = false;
        mStreamer.start();
    }

    // 结束播放
    public void pauseWatch() {
        if (mStreamer == null || !mStarted || !mPaused) {
            return;
        }
        mPaused = true;
        mStreamer.pause();
    }
}
