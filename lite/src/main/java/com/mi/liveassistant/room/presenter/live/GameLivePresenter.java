package com.mi.liveassistant.room.presenter.live;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;

import com.mi.liveassistant.common.callback.ICommonCallBack;
import com.mi.liveassistant.common.filesystem.SDCardUtils;
import com.mi.liveassistant.common.image.ImageUtils;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.engine.streamer.IStreamer;
import com.mi.liveassistant.room.constant.LiveRoomType;
import com.mi.liveassistant.room.view.ILiveView;
import com.mi.liveassistant.screenrecord.ScreenRecordManager;

import java.io.File;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by chenyong on 2016/11/22.
 */
public class GameLivePresenter extends BaseLivePresenter {
    private ScreenRecordManager mScreenRecordManager;
    private IStreamer mStreamer;

    private boolean mIsStarted = false;

    private boolean mIsLandscape;
    private boolean mIsMuteMic;

    public GameLivePresenter(ILiveView view) {
        super(view);
        mLiveRoomType = LiveRoomType.TYPE_LIVE_GAME;
    }

    public void initStreamer(IStreamer streamer, int width, int height, Intent intent) {
        mStreamer = streamer;
        mScreenRecordManager = new ScreenRecordManager(streamer, width, height, intent);
    }

    public void startLive() {
        if (!mIsStarted) {
            MyLog.d(TAG, "startLive");
            mScreenRecordManager.startScreenRecord();
            mIsStarted = true;
        }
    }

    public void stopLive() {
        if (mIsStarted) {
            MyLog.d(TAG, "stopLive");
            mScreenRecordManager.stopScreenRecord();
            mIsStarted = false;
        }
    }

    public void muteMic(boolean isMute) {
        if (mIsMuteMic != isMute && mStreamer != null) {
            MyLog.d(TAG, "muteMic isMute=" + isMute);
            mIsMuteMic = isMute;
            if (mIsMuteMic) {
                mStreamer.muteMic();
            } else {
                mStreamer.unMuteMic();
            }
        }
    }

    public boolean isMuteMic() {
        return mIsMuteMic;
    }

    public void screenshot() {
        screenshot(null);
    }

    public void screenshot(final ICommonCallBack callback) {
        if (mScreenRecordManager != null) {
            String screenshotPath = Environment.getExternalStorageDirectory().getPath() + SDCardUtils.IMAGE_DIR_PATH;
            File filePath = new File(screenshotPath);
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            final String fileName = screenshotPath + "/screenshot_" + System.currentTimeMillis() + ".jpg";
            mScreenRecordManager.getScreenshot(new ScreenRecordManager.OnScreenshotReadyListener() {
                @Override
                public void onScreenshotReady(Bitmap bitmap) {
                    postScreenshot(bitmap, fileName, callback);
                }
            });
        }
    }

    private void postScreenshot(Bitmap bitmap, final String screenshotPath, final ICommonCallBack callback) {
        Observable.just(bitmap)
                .map(new Func1<Bitmap, Boolean>() {
                    @Override
                    public Boolean call(Bitmap bitmap1) {
                        boolean isSaveSuccess = ImageUtils.saveToFile(bitmap1, screenshotPath);
                        ImageUtils.recycleBitmap(bitmap1);
                        return isSaveSuccess;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean b) {
                        if (callback != null) {
                            MyLog.w(TAG, "postScreenshot callback saveToFile isSuccess=" + b + " screenshotPath=" + screenshotPath);
                            if (b) {
                                callback.process(screenshotPath);
                            }
                        } else {
                            MyLog.w(TAG, "postScreenshot toast saveToFile isSuccess=" + b + " screenshotPath=" + screenshotPath);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public void resumeStream() {
        mScreenRecordManager.resume();
    }

    public void pauseStream() {
        mScreenRecordManager.pause();
    }

    public void destroy() {
        mScreenRecordManager.destroy();
    }

    public void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        mIsLandscape = isLandscape;
        if (mScreenRecordManager != null) {
            mScreenRecordManager.setOrientation(isLandscape);
        }
    }
}
