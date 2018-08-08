package com.base.activity;

import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.OrientationEventListener;
import android.view.WindowManager;

import com.base.event.SdkEventClass;
import com.base.fragment.IRotateActivity;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;

/**
 * Created by lan on 16/7/4.
 *
 * @module sdk activity
 */
public abstract class BaseRotateSdkActivity extends BaseSdkActivity implements IRotateActivity {
    // 旋转角度
    public static final int ORIENTATION_DEFAULT = -1;
    public static final int ORIENTATION_PORTRAIT_NORMAL = 0;
    public static final int ORIENTATION_LANDSCAPE_REVERSED = 90;
    public static final int ORIENTATION_PORTRAIT_REVERSED = 180;
    public static final int ORIENTATION_LANDSCAPE_NORMAL = 270;

    // 角度间的距离，防止过度灵敏
    public static final int ORIENTATION_GAP = 10;

    public static final int[] ORIENTATION_ARRAY = {
            0,
            45 - ORIENTATION_GAP,
            45 + ORIENTATION_GAP,
            135 - ORIENTATION_GAP,
            135 + ORIENTATION_GAP,
            225 - ORIENTATION_GAP,
            225 + ORIENTATION_GAP,
            315 - ORIENTATION_GAP,
            315 + ORIENTATION_GAP,
            360
    };

    protected OrientationEventListener mOrientationEventListener;

    // 传感器指示的方向
    protected int mScreenOrientation = ORIENTATION_PORTRAIT_NORMAL;
    protected int mLastScreenOrientation = mScreenOrientation;

    // 当前屏幕显示的方向
    protected int mScreenDisplayOrientation = mScreenOrientation;

    protected boolean mOpenOrientation = false;

    // 当前的横竖屏
    protected boolean mLandscape = false;

    private int mLockScreenRefCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initOrientationEventListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableOrientationEventListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableOrientationEventListener();
    }

    private void initOrientationEventListener() {
        if (mOrientationEventListener == null) {
            mOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
                @Override
                public void onOrientationChanged(int orientation) {
                    MyLog.d(TAG, "onOrientationChanged");
                    int currScreenOrientation = convertOrientation(orientation);
                    if (currScreenOrientation == ORIENTATION_DEFAULT) {
                        return;
                    }
                    mLastScreenOrientation = mScreenOrientation;
                    mScreenOrientation = currScreenOrientation;
                    if (!mOpenOrientation || !isRotateOn()) {
                        return;
                    }
                    if (mScreenOrientation != mLastScreenOrientation) {
                        rotateOrientationIfNeed();
                    }
                }
            };
        }
    }

    private int convertOrientation(int orientation) {
        int screenOrientation = ORIENTATION_DEFAULT;
        if ((orientation >= ORIENTATION_ARRAY[0] && orientation < ORIENTATION_ARRAY[1])
                || (orientation >= ORIENTATION_ARRAY[8] && orientation < ORIENTATION_ARRAY[9])) {
            screenOrientation = ORIENTATION_PORTRAIT_NORMAL;
        } else if (orientation >= ORIENTATION_ARRAY[2] && orientation < ORIENTATION_ARRAY[3]) {
            screenOrientation = ORIENTATION_LANDSCAPE_REVERSED;
        } else if (orientation >= ORIENTATION_ARRAY[4] && orientation < ORIENTATION_ARRAY[5]) {
            screenOrientation = CommonUtils.isNotchPhone() ? mScreenOrientation : ORIENTATION_PORTRAIT_REVERSED;
        } else if (orientation >= ORIENTATION_ARRAY[6] && orientation < ORIENTATION_ARRAY[7]) {
            screenOrientation = ORIENTATION_LANDSCAPE_NORMAL;
        }
        return screenOrientation;
    }

    private void rotateOrientationIfNeed() {
        MyLog.d(TAG, "rotateOrientationIfNeed");
        if (mScreenOrientation != mScreenDisplayOrientation) {
            if (isLandscape(mScreenOrientation)) {
                rotateOrientation();
                notifyOrientation(mScreenOrientation);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else if (isPortrait(mScreenOrientation)) {
                rotateOrientation();
                notifyOrientation(mScreenOrientation);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }

    private void notifyOrientation(int orientation) {
        MyLog.d(TAG, "notifyOrientation");
        SdkEventClass.postOrient(orientation);
    }

    private void rotateOrientation() {
        KeyboardUtils.hideKeyboard(this);
        switch (mScreenOrientation) {
            case ORIENTATION_PORTRAIT_NORMAL:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mLandscape = false;
                break;
            case ORIENTATION_PORTRAIT_REVERSED:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                mLandscape = false;
                break;
            case ORIENTATION_LANDSCAPE_NORMAL:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mLandscape = true;
                break;
            case ORIENTATION_LANDSCAPE_REVERSED:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                mLandscape = true;
                break;
        }
        mScreenDisplayOrientation = mScreenOrientation;
    }

    private void enableOrientationEventListener() {
        if (mOrientationEventListener != null && mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
    }

    private void disableOrientationEventListener() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }
    }

    @Override
    public void openOrientation() {
        MyLog.d(TAG, "openOrientation isRorateOn:" + isRotateOn());
        mOpenOrientation = true;
        if (isRotateOn()) {
            rotateOrientationIfNeed();
        }
    }

    public void closeOrientation() {
        mOpenOrientation = false;
    }

    // 以下两个方法提供给子类调用 物理传感器方向
    protected final boolean isLandscape() {
        return isLandscape(mScreenOrientation);
    }

    protected final boolean isPortrait() {
        return isPortrait(mScreenOrientation);
    }

    // 以下两个方法提供给子类调用 屏幕实际显示方向
    public final boolean isDisplayLandscape() {
        return isLandscape(mScreenDisplayOrientation);
    }

    public final boolean isDisplayPortrait() {
        return isPortrait(mScreenDisplayOrientation);
    }

    public boolean isLandscape(int orientation) {
        return orientation == ORIENTATION_LANDSCAPE_NORMAL || orientation == ORIENTATION_LANDSCAPE_REVERSED;
    }

    public boolean isPortrait(int orientation) {
        return orientation == ORIENTATION_PORTRAIT_NORMAL || orientation == ORIENTATION_PORTRAIT_REVERSED;
    }

    @Override
    public int getScreenOrientation() {
        return mScreenOrientation;
    }

    @Override
    public void forceLandscape() {
        if (!isDisplayLandscape()) {
            mScreenOrientation = ORIENTATION_LANDSCAPE_NORMAL;
            rotateOrientationIfNeed();
        }
        mOpenOrientation = false;
    }

    @Override
    public void forcePortrait() {
        if (!isDisplayPortrait()) {
            mScreenOrientation = ORIENTATION_PORTRAIT_NORMAL;
            rotateOrientationIfNeed();
        }
        mOpenOrientation = false;
    }

    @Override
    public void forceRotate(int screenOrientation) {
        if (mScreenOrientation != screenOrientation) {
            mScreenOrientation = screenOrientation;
            rotateOrientationIfNeed();
        }
        mOpenOrientation = false;
    }

    @Override
    public void tempForceLandscape() {
        int tempOrientation = mScreenOrientation;
        mScreenOrientation = ORIENTATION_LANDSCAPE_NORMAL;
        rotateOrientationIfNeed();
        mScreenOrientation = tempOrientation;
    }

    @Override
    public void tempForcePortrait() {
        int tempOrientation = mScreenOrientation;
        mScreenOrientation = ORIENTATION_PORTRAIT_NORMAL;
        rotateOrientationIfNeed();
        mScreenOrientation = tempOrientation;
    }

    @Override
    public void openOrientationButNotRotate() {
        mOpenOrientation = true;
    }

    @Override
    public boolean isRotateOn() {
        return (Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
    }

    @Override
    public boolean checkLockScreenRefCount() {
        return mLockScreenRefCount == 0;
    }

    @Override
    public void increaseLockScreenRefCount() {
        ++mLockScreenRefCount;
    }

    @Override
    public void decreaseLockScreenRefCount() {
        if (mLockScreenRefCount > 0) {
            --mLockScreenRefCount;
        }
    }
}
