package com.wali.live.livesdk.live.presenter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.Nullable;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.presenter.Presenter;
import com.base.utils.callback.ICommonCallBack;
import com.base.utils.sdcard.SDCardUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.engine.base.GalileoConstants;
import com.mi.live.engine.streamer.IStreamer;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.manager.ScreenRecordManager;
import com.wali.live.livesdk.live.service.GameLiveService;
import com.wali.live.livesdk.live.utils.ImageUtils;
import com.wali.live.livesdk.live.window.GameFloatWindow;
import com.wali.live.watchsdk.active.KeepActiveProcessor;

import java.io.File;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by chenyong on 2016/11/22.
 */
public class GameLivePresenter implements Presenter {
    private static final String TAG = "GameLivePresenter";

    private ScreenRecordManager mScreenRecordManager;
    private IStreamer mStreamer;
    private LiveRoomChatMsgManager mRoomChatMsgManager;
    private RoomBaseDataModel mMyRoomData;
    private boolean mIsStarted = false;
    private long mDefaultPicStreamId;
    private boolean mIsForeground;
    private boolean mIsLandscape;
    private String mToken;
    private boolean mIsMuteMic;

    private final GameFloatWindow mGameFloatWindow;
    private Intent mGameLiveServiceIntent;

    public GameLivePresenter(
            @Nullable IStreamer streamer,
            @Nullable LiveRoomChatMsgManager roomChatMsgManager,
            @Nullable RoomBaseDataModel myRoomData,
            int width,
            int height,
            @Nullable Intent intent,
            String token) {
        mStreamer = streamer;
        mRoomChatMsgManager = roomChatMsgManager;
        mMyRoomData = myRoomData;
        mScreenRecordManager = new ScreenRecordManager(streamer, width, height, intent);
        mGameFloatWindow = new GameFloatWindow(GlobalData.app(), this);
        mToken = token;
        mGameLiveServiceIntent = new Intent(GlobalData.app(), GameLiveService.class);
    }

    public void startGameLive() {
        if (!mIsStarted) {
            KeepActiveProcessor.keepActive();
            mScreenRecordManager.startScreenRecord();
            startAddExtra();
            mIsStarted = true;
        }
    }

    public void stopGameLive() {
        if (mIsStarted) {
            stopAddExtra();
            mScreenRecordManager.stopScreenRecord();
            KeepActiveProcessor.stopActive();
            mIsStarted = false;
            if (mGameFloatWindow.isWindowShow()) {
                ToastUtils.showToast(R.string.game_live_unexpected_end_toast);
                mGameFloatWindow.removeWindow();
            }
        }
    }

    public void muteMic(boolean isMute) {
        if (mIsMuteMic != isMute && mStreamer != null) {
            mIsMuteMic = isMute;
            if (mIsMuteMic) {
                ToastUtils.showToast(R.string.game_mute_microphone);
                mStreamer.muteMic();
            } else {
                ToastUtils.showToast(R.string.game_unmute_microphone);
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
        Observable.just(bitmap).map(new Func1<Bitmap, Boolean>() {
            @Override
            public Boolean call(Bitmap bitmap1) {
                boolean isSaveSuccess = ImageUtils.saveToFile(bitmap1, screenshotPath);
                ImageUtils.recycleBitmap(bitmap1);
                return isSaveSuccess;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean b) {
                if (callback != null) {
                    MyLog.w(TAG, "postScreenshot callback saveToFile isSuccess=" + b + " screenshotPath=" + screenshotPath);
                    if (b) {
                        callback.process(screenshotPath);
                    }
                } else {
                    MyLog.w(TAG, "postScreenshot toast saveToFile isSuccess=" + b + " screenshotPath=" + screenshotPath);
                    if (b) {
                        ToastUtils.showToast(GlobalData.app().getString(R.string.game_save_picture_success, screenshotPath));
                    } else {
                        ToastUtils.showToast(R.string.game_save_picture_failed);
                    }
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                MyLog.e(TAG, throwable);
            }
        });
    }

    public void sendBarrage(String msg, boolean isFlyBarrage) {
        if (mRoomChatMsgManager != null && mMyRoomData != null) {
            MyLog.d(TAG, "sendBarrage BaseInputArea msg=" + msg + ", isFlyBarrage=" + isFlyBarrage);
            if (isFlyBarrage) { // 飘屏弹幕
                mRoomChatMsgManager.sendFlyBarrageMessageAsync(msg, mMyRoomData.getRoomId(), mMyRoomData.getUid(), null);
            } else {
                mRoomChatMsgManager.sendTextBarrageMessageAsync(msg, mMyRoomData.getRoomId(), mMyRoomData.getUid(), null);
            }
        } else {
            MyLog.w(TAG, "sendBarrage failed, mRoomChatMsgManager or mMyRoomData is null");
        }
    }

    private void startAddExtra() {
        stopAddExtra();
        mDefaultPicStreamId = System.currentTimeMillis();
        mStreamer.startAddExtra(mDefaultPicStreamId, 0, 0, 1, 1, 1, 1, 2);
        Bitmap defaultPicBmp;
        if (mIsLandscape) {
            defaultPicBmp = ImageUtils.readArgbtmap(GlobalData.app(), R.drawable.game_artboard_landscape);
        } else {
            defaultPicBmp = ImageUtils.readArgbtmap(GlobalData.app(), R.drawable.game_artboard);
        }
        mStreamer.putExtraDetailInfo(defaultPicBmp.getWidth(), defaultPicBmp.getHeight(), ImageUtils.bitmap2Byte(defaultPicBmp), defaultPicBmp.getWidth(),
                GalileoConstants.TYPE_BGR, GalileoConstants.PICTURE_FRAME, mDefaultPicStreamId);
        ImageUtils.recycleBitmap(defaultPicBmp);
    }

    private void stopAddExtra() {
        if (mDefaultPicStreamId != 0) {
            mStreamer.stopAddExtra(mDefaultPicStreamId);
            mDefaultPicStreamId = 0;
        }
    }

    public void resumeStream() {
        mScreenRecordManager.resume();
    }

    public void pauseStream() {
        mScreenRecordManager.pause();
    }

    @Override
    public void start() {

    }

    @Override
    public void resume() {
        mGameFloatWindow.removeWindow();
        mIsForeground = true;
        if (mIsStarted) {
            startAddExtra();
            GlobalData.app().stopService(mGameLiveServiceIntent);
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {
        mIsForeground = false;
        if (mIsStarted) {
            onOrientation(false);
            stopAddExtra();
            mGameFloatWindow.showWindow(mToken, mMyRoomData.getViewerCnt());
            GlobalData.app().startService(mGameLiveServiceIntent);
        }
    }

    @Override
    public void destroy() {
        mGameFloatWindow.destroyWindow();
        mScreenRecordManager.destroy();
        GlobalData.app().stopService(mGameLiveServiceIntent);
        KeepActiveProcessor.stopActive();
    }

    public void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        mIsLandscape = isLandscape;
        if (mScreenRecordManager != null) {
            mScreenRecordManager.setOrientation(isLandscape);
            if (mIsForeground) {
                startAddExtra();
            }
        }
    }

    public void updateStutterStatus(boolean isStuttering) {
        mGameFloatWindow.updateStutterStatus(isStuttering);
    }
}
