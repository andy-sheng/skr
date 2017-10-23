package com.wali.live.livesdk.live.window;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.base.event.SdkEventClass;
import com.base.log.MyLog;
import com.base.permission.PermissionUtils;
import com.base.utils.display.DisplayUtils;
import com.wali.live.livesdk.live.presenter.GameLivePresenter;
import com.wali.live.livesdk.live.view.camera.GameCameraView;
import com.wali.live.livesdk.live.window.dialog.GameConfirmDialog;

import java.lang.ref.WeakReference;

/**
 * Created by yangli on 16-11-29.
 *
 * @module 悬浮窗
 */
public class GameFloatWindow implements IGameFloatPresenter {
    private static final String TAG = "GameFloatWindow";

    public static final int TIME_TAKE_SCREEN_SHOT_DONE = 2000;
    public static final int TIME_HIDE_GAME_FLOAT_BALL = 3000;

    public static final int MSG_TAKE_SCREEN_SHOT_DONE = 1000;
    public static final int MSG_HALF_HIDE_FLOAT_BALL = 1001;

    public static final int DIALOG_PADDING = DisplayUtils.dip2px(6.67f);

    private final Context mContext;
    private final GameLivePresenter mGameLivePresenter;

    private final WindowManager mWindowManager;
    private final MyUiHandler mUiHandler;

    private boolean mIsStuttering = false;

    // 悬浮窗
    private final int mParentWidth;
    private final int mParentHeight;
    private GameFloatIcon mGameMainIcon;
    private GameFloatView mGameFloatView;

    private GameCameraView mGameCameraView;

    // 弹窗
    private WeakReference<GameConfirmDialog> mGameConfirmDialogRef;

    private static <T> T deRef(WeakReference<T> reference) {
        return reference != null ? reference.get() : null;
    }

    public GameFloatWindow(
            @NonNull Context context,
            @NonNull GameLivePresenter gameLivePresenter) {
        mContext = context.getApplicationContext();
        mGameLivePresenter = gameLivePresenter;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mUiHandler = new MyUiHandler(this);
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        mParentWidth = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        mParentHeight = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    public void updateStutterStatus(boolean isStuttering) {
        if (mIsStuttering != isStuttering) {
            MyLog.w(TAG, "updateStutterStatus " + isStuttering);
            mIsStuttering = isStuttering;
            if (mGameMainIcon != null) {
                mGameMainIcon.updateStutterStatus(isStuttering);
            }
        }
    }

    public boolean isWindowShow() {
        return (mGameFloatView != null && mGameFloatView.isIsWindowShow()) ||
                (mGameMainIcon != null && mGameMainIcon.isIsWindowShow());
    }

    public void showWindow(String token, int viewerCnt) {
        if (mGameFloatView == null) {
            mGameFloatView = new GameFloatView(
                    mContext,
                    mWindowManager,
                    mUiHandler,
                    this,
                    mParentWidth,
                    mParentHeight,
                    token, viewerCnt);
            mGameFloatView.register();
        }
        if (mGameMainIcon == null) {
            mGameMainIcon = new GameFloatIcon(
                    mContext,
                    mWindowManager,
                    mUiHandler,
                    this,
                    mGameFloatView,
                    mParentWidth,
                    mParentHeight);
        }

        boolean hasCameraPermission = PermissionUtils.checkCamera(mContext);
        if (!hasCameraPermission) {
            mGameCameraView = null;
        }
        if (hasCameraPermission && mGameCameraView == null) {
            mGameCameraView = new GameCameraView(
                    mContext,
                    mWindowManager,
                    mParentWidth,
                    mParentHeight);
        }

        mGameFloatView.showWindow();
        mGameMainIcon.showWindow();

        if (mGameCameraView != null && isShowFace()) {
            mGameCameraView.showWindow();
        }
    }

    public void removeWindow() {
        if (mGameFloatView != null) {
            mGameFloatView.removeWindow();
        }
        if (mGameMainIcon != null) {
            mGameMainIcon.removeWindow();
        }
        if (mGameCameraView != null) {
            mGameCameraView.removeWindow();
        }
    }

    public void removeWindowUnexpected() {
        removeWindow();
        backToApp();
    }

    public void destroyWindow() {
        removeWindow();
        mUiHandler.removeCallbacksAndMessages(null);
        if (mGameFloatView != null) {
            mGameFloatView.unregister();
        }
        if (mGameMainIcon != null) {
            mGameMainIcon.destroy();
        }
        if (mGameCameraView != null) {
            mGameCameraView.destroy();
        }
    }

    @Override
    public void adjustIconForInputShow(int y) {
        mGameMainIcon.adjustForInputShow(y);
    }

    @Override
    public void takeScreenshot() {
        mGameLivePresenter.screenshot();
        mGameFloatView.setVisibility(View.GONE);
        mGameMainIcon.setVisibility(View.GONE);
        if (mGameCameraView != null) {
            mGameCameraView.setVisibility(View.GONE);
        }
        mUiHandler.removeMessages(MSG_TAKE_SCREEN_SHOT_DONE);
        mUiHandler.sendEmptyMessageDelayed(MSG_TAKE_SCREEN_SHOT_DONE, TIME_TAKE_SCREEN_SHOT_DONE);
    }

    @Override
    public void muteMic(boolean isMute) {
        mGameLivePresenter.muteMic(isMute);
    }

    @Override
    public void showFace(boolean isShow) {
        MyLog.d(TAG, "face show=" + isShow);
        // 将值进行保存
        mGameLivePresenter.showFace(isShow);

        boolean hasCameraPermission = PermissionUtils.checkCamera(mContext);
        if (!hasCameraPermission) {
            mGameCameraView = null;
        }
        if (hasCameraPermission && mGameCameraView == null) {
            mGameCameraView = new GameCameraView(
                    mContext,
                    mWindowManager,
                    mParentWidth,
                    mParentHeight);
        }
        if (mGameCameraView != null) {
            if (!isShow) {
                mGameCameraView.removeWindow();
            } else {
                mGameCameraView.showWindow();
            }
        }
    }

    @Override
    public boolean isMuteMic() {
        return mGameLivePresenter.isMuteMic();
    }

    @Override
    public boolean isShowFace() {
        return mGameLivePresenter.isShowFace();
    }

    @Override
    public void backToApp() {
        // 返回控制台
//        Intent intent = new Intent(GlobalData.app(), LiveSdkActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        GlobalData.app().startActivity(intent);

        SdkEventClass.postBringFront();
        mUiHandler.removeMessages(MSG_HALF_HIDE_FLOAT_BALL);
    }

    @Override
    public void showConfirmDialog() {
        GameConfirmDialog gameConfirmDialog = deRef(mGameConfirmDialogRef);
        if (gameConfirmDialog == null) {
            gameConfirmDialog = new GameConfirmDialog(
                    mContext,
                    mWindowManager,
                    this);
            mGameConfirmDialogRef = new WeakReference<>(gameConfirmDialog);
        }
        gameConfirmDialog.showDialog();
    }

    @Override
    public void sendBarrage(String msg, boolean isFlyBarrage) {
        mGameLivePresenter.sendBarrage(msg, isFlyBarrage);
    }

    @Override
    public void onOrientation(boolean isLandscape) {
        mGameLivePresenter.onOrientation(isLandscape);
    }

    // UI Handler：处理延迟任务
    public static class MyUiHandler extends Handler {

        private WeakReference<GameFloatWindow> mGameFloatWindowRef;

        public MyUiHandler(GameFloatWindow gameFloatWindow) {
            super(Looper.getMainLooper());
            mGameFloatWindowRef = new WeakReference<>(gameFloatWindow);
        }

        @Override
        public void handleMessage(Message msg) {
            GameFloatWindow gameFloatWindow = mGameFloatWindowRef.get();
            if (gameFloatWindow == null) {
                return;
            }
            switch (msg.what) {
                case MSG_TAKE_SCREEN_SHOT_DONE:
                    gameFloatWindow.mGameFloatView.setVisibility(View.VISIBLE);
                    gameFloatWindow.mGameMainIcon.setVisibility(View.VISIBLE);
                    if (gameFloatWindow.mGameCameraView != null) {
                        gameFloatWindow.mGameCameraView.setVisibility(View.VISIBLE);
                    }
                    break;
                case MSG_HALF_HIDE_FLOAT_BALL:
                    gameFloatWindow.mGameMainIcon.setMode(GameFloatIcon.MODE_HALF_HIDDEN);
                    break;
                default:
                    break;

            }
        }
    }
}
