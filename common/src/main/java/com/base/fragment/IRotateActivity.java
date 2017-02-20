package com.base.fragment;

/**
 * Created by chengsimin on 2016/12/2.
 */

public interface IRotateActivity {
    int getScreenOrientation();

    void forceLandscape();

    void increaseLockScreenRefCount();

    void forcePortrait();

    void decreaseLockScreenRefCount();

    void forceRotate(int mCurrentScrrenRotateIsLandScape);

    boolean checkLockScreenRefCount();

    boolean isRotateOn();

    void openOrientation();

    void openOrientationButNotRotate();

}
