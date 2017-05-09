package com.mi.liveassistant.unity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.login.ILoginCallback;
import com.mi.liveassistant.login.LoginManager;
import com.mi.liveassistant.milink.MiLinkClientAdapter;
import com.mi.liveassistant.room.manager.live.GameLiveManager;
import com.mi.liveassistant.room.manager.live.callback.ILiveCallback;
import com.mi.liveassistant.room.manager.live.callback.ILiveListener;
import com.mi.liveassistant.utils.RSASignature;

/**
 * Created by yangli on 2017/5/4.
 *
 * @module Unity游戏直播辅助类
 */
public class LiveForUnity {
    private static final String TAG = "LiveForUnity";

    protected MiLiveActivity mActivity;

    private GameLiveManager mLiveManager;
    private boolean mIsBegin;

    public LiveForUnity(Activity activity) {
        MyLog.w(TAG, "LiveForUnity");
        mActivity = (MiLiveActivity) activity;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLiveManager = new GameLiveManager(new ILiveListener() {
                    @Override
                    public void onEndUnexpected() {
                        MyLog.w(TAG, "onEndUnexpected");
                    }
                });
                mActivity.setLiveForUnity(LiveForUnity.this);
            }
        });
    }

    protected void startLive(@NonNull Intent intent) {
        MyLog.w(TAG, "startLive mIsBegin=" + mIsBegin);
        if (mIsBegin) {
            return;
        }
        mLiveManager.setCaptureIntent(intent);
        mLiveManager.beginLive(null, "TEST", null, new ILiveCallback() {
            @Override
            public void notifyFail(int errCode) {
                MyLog.w(TAG, "startLive failed");
            }

            @Override
            public void notifySuccess(long playerId, String liveId) {
                mIsBegin = true;
                MyLog.w(TAG, "startLive success");
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startLive() {
        if (!checkLogin()) {
            return;
        }
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "startLive");
                mActivity.queryScreenRecordIntent();
            }
        });
    }

    public void stopLive() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "stopLive mIsBegin=" + mIsBegin);
                if (!mIsBegin) {
                    return;
                }
                mLiveManager.endLive(new ILiveCallback() {
                    @Override
                    public void notifyFail(int errCode) {
                        MyLog.w(TAG, "stopLive failed");
                    }

                    @Override
                    public void notifySuccess(long playerId, String liveId) {
                        mIsBegin = false;
                        MyLog.w(TAG, "stopLive success");
                    }
                });
            }
        });
    }

    public void resume() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLiveManager.resume();
            }
        });
    }

    public void pause() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLiveManager.pause();
            }
        });
    }

    public void destroy() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLiveManager.destroy();
                mLiveManager = null;
            }
        });
    }

    private static final String RSA_PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMC8ISWECSak6Z1X" +
            "tgTy9jrq85dZ7Z95CndJ6Sz0ty5fiVqiJ4WrRf7d+78hlEOvlE0fwLQraHZ28gkD" +
            "kdNX1ycFDV+SBDTn+rFnRJQZjA8t3cQGiJmpyFIpaSzpz9PMTScxDmmxygUzsTXe" +
            "sCcFV8p9thCyJj5kGsUFxzkfwR7dAgMBAAECgYAqUCMmzVoE9eej94GqjHyqarKX" +
            "49JbVIOLtNpQWFlvAOJy12691eBEGBAQ4hpe0clJNVNlOrJwb6SrffEh6QL+2Aht" +
            "oocO7ST4kGpYTk53ofkK9AOwdZkhhzn226qRlDFN+OyAedLsv5sZ3166KTfxaCkO" +
            "5/KeXuD9BucT4eHTMQJBAPUGugXFJBVUZimsqwi5PKBGtmqQEJAi5M0vZvGz4vtq" +
            "H8pXQVYHOwAQA2Kmx7LSWqUa5EZCfKQIHE88dhmcru8CQQDJXd825pM0FW6ENr/9" +
            "IZLZBMgOlFG06WkVa442trbViGP0TPJMeEzBHoCDtlxDUxKcbFworXvVk+f8SYUo" +
            "6g7zAkAJSIb1vwFd+YOhYpRcUUBVxjgVE349J8VJbNlWoP0hj2TC8slb7Aw1NWYb" +
            "b7wzLzsV9E3fx5cXU+NWsTC8Sa5rAkEAw8DL4/UWmQVUoJcQ4KUoumwZh4LMQ1C8" +
            "5SPf5nSNHNwwPygmTAyOoRZj3KcE3jX9267DkI/F2ISmeu2F05Zl3QJAX8qggola" +
            "wpkdbvZn81X80lFuye6b0KjSWqlrrQLtjSR9/ov/avbuEDI+Ni4rDZn5a0rkGuaN" +
            "DzBZBemtWvPkjg==";

    protected boolean checkLogin() {
        MyLog.w(TAG, "checkLogin isTouristMode=" + MiLinkClientAdapter.getsInstance().isTouristMode());
        if (!MiLinkClientAdapter.getsInstance().isTouristMode()) {
            return true;
        }
        String uid = "100067";
        String name = "游不动的鱼";
        String headUrl = "";
        int sex = 1;
        int channelId = 50001;
        String signStr = "channelId=" + channelId + "&headUrl=" + headUrl + "&nickname=" + name + "&sex=" + sex + "&xuid=" + uid;
        String sign = RSASignature.sign(signStr, RSA_PRIVATE_KEY, "UTF-8");

        LoginManager.thirdPartLogin(channelId, uid, name, headUrl, sex, sign, new ILoginCallback() {
            @Override
            public void notifyFail(int errCode) {
                MyLog.d(TAG, "notifyFail");
            }

            @Override
            public void notifySuccess() {
                MyLog.d(TAG, "notifySuccess");
            }
        });
        return false;
    }
}
