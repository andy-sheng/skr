package com.mi.live.engine.streamer;

import android.os.Environment;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.presenter.RxLifeCyclePresenter;
import com.base.utils.CommonUtils;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class FixedStreamerDebugPresenter extends RxLifeCyclePresenter {
    private static final String TAG = FixedStreamerDebugPresenter.class.getSimpleName();
    private static final String FILE_PATH = Environment.getExternalStorageDirectory() + "/Xiaomi/WALI_LIVE/streamer_debug.txt";
    private static final String[] KEY_DEBUG_CONFIGS = new String[]{"live_url", "live_ip", "watch_url", "watch_ip"};
    private static final int LIVE_URL = 0;
    private static final int LIVE_IP = 1;
    private static final int WATCH_URL = 2;
    private static final int WATCH_IP = 3;

    private String[] mDebugConfigs = new String[]{"", "", "", ""};
    private boolean mStreamerDebug = false;
    private static FixedStreamerDebugPresenter sInstance;

    private FixedStreamerDebugPresenter() {
    }

    public synchronized static FixedStreamerDebugPresenter getsInstance() {
        if (sInstance == null) {
            sInstance = new FixedStreamerDebugPresenter();
        }
        return sInstance;
    }

    public void readStreamerDebugConfig() {
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    boolean isStreamerDebug = PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceUtils.KEY_DEBUG_FIX_STREAMER, false);
                    setStreamerDebug(isStreamerDebug);

                    byte[] streamerConfigBytes = CommonUtils.readFromFile(FILE_PATH, 0, -1);
                    if (streamerConfigBytes != null) {
                        String fileConfig = new String(streamerConfigBytes, "UTF-8");
                        if (!TextUtils.isEmpty(fileConfig)) {  //分割參數
                            String[] fileConfigs = fileConfig.trim().split(";");
                            if (fileConfigs != null && fileConfigs.length == mDebugConfigs.length) {
                                for (int i = 0; i < fileConfigs.length; ++i) {
                                    if (!TextUtils.isEmpty(fileConfigs[i]) && fileConfigs[i].contains(KEY_DEBUG_CONFIGS[i])) {
                                        String[] ss = fileConfigs[i].split("#");
                                        if (ss != null && ss.length >= 2) {
                                            mDebugConfigs[i] = ss[1].trim();
                                            MyLog.w(TAG, "mDebugConfigs " + i + " =" + mDebugConfigs[i]);
                                        } else {
                                            mDebugConfigs[i] = "";
                                            MyLog.w(TAG, "mDebugConfigs " + i + " = \"\"");
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        MyLog.w(TAG, "streamerConfigBytes == null");
                    }
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                } catch (Exception e) {

                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public void writeStreamerDebugToSp(final boolean isChecked) {
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                PreferenceUtils.setSettingBoolean(GlobalData.app(), PreferenceUtils.KEY_DEBUG_FIX_STREAMER, isChecked);
                subscriber.onNext(isChecked);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        mStreamerDebug = (Boolean) o;
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public String getFixedWatchUrl() {
        return mDebugConfigs[WATCH_URL];
    }

    public String getFixedWatchIp() {
        return mDebugConfigs[WATCH_IP];
    }

    public String getFixedLiveUrl() {
        return mDebugConfigs[LIVE_URL];
    }

    public String getFixedLiveIp() {
        return mDebugConfigs[LIVE_IP];
    }

    public boolean isStreamerDebug() {
        return mStreamerDebug;
    }

    public void setStreamerDebug(boolean streamerDebug) {
        mStreamerDebug = streamerDebug;
        writeStreamerDebugToSp(mStreamerDebug);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
