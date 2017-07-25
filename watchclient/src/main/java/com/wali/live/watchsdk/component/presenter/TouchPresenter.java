package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.WatchComponentController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangli on 2017/03/05.
 *
 * @module 触摸表现
 */
public class TouchPresenter extends ComponentPresenter implements View.OnTouchListener {
    private static final String TAG = "TouchPresenter";

    public static final int MOVE_UPDATE_THRESHOLD = 5;
    public static final int MOVE_THRESHOLD = 20;
    public static final int FLING_THRESHOLD_NORMAL = 300;
    public static final int FLING_THRESHOLD_LARGE = (GlobalData.screenHeight >> 1);

    @NonNull
    private List<View> mHorizontalSet;

    @NonNull
    private List<View> mVerticalSet;

    private View[] mHalfArray = new View[0];
    private View[] mPagerArray = new View[0];

    private boolean mIsHideAll = false;
    private boolean mIsGameMode = false;

    private boolean mHorizontalMoveEnabled = true;
    private boolean mVerticalMoveEnabled = true;

    // 增加竖屏滑动的开关，用于在只有一个观看列表的情况下进行控制
    private boolean mOpenVerticalMove = false;

    private View mTouchView;
    private int mViewWidth = GlobalData.screenWidth;
    private int mFlingThreshold = FLING_THRESHOLD_NORMAL;

    public void setViewSet(
            @NonNull List<View> horizontalSet) {
        mHorizontalSet = horizontalSet;
        mVerticalSet = new ArrayList<>(0);
        mIsGameMode = false;
    }

    public void setViewSet(
            @NonNull List<View> horizontalSet,
            @NonNull List<View> verticalSet,
            boolean isGameMode) {
        mHorizontalSet = horizontalSet;
        mVerticalSet = verticalSet;
        mIsGameMode = isGameMode;
    }

    public void setVerticalMoveEnabled(View[] pagerArray, View[] halfArray) {
        mOpenVerticalMove = true;
        mPagerArray = pagerArray;
        mHalfArray = halfArray;
    }

    public TouchPresenter(
            @NonNull IComponentController componentController,
            @NonNull View touchView) {
        super(componentController);
        mTouchView = touchView;
        mTouchView.setSoundEffectsEnabled(false);
        mTouchView.setOnTouchListener(this);
        mTouchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackgroundClick();
            }
        });
        registerAction(WatchComponentController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(WatchComponentController.MSG_ON_ORIENT_LANDSCAPE);
        registerAction(WatchComponentController.MSG_ENABLE_MOVE_VIEW);
        registerAction(WatchComponentController.MSG_DISABLE_MOVE_VIEW);
    }

    private float mCurrX, mCurrY, mDownX, mDownY;
    private float mTranslateX, mTranslateY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mCurrX = mDownX = event.getX();
                mCurrY = mDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                calcTranslation(event.getX(), event.getY(), false);
                break;
            case MotionEvent.ACTION_UP:
                calcTranslation(event.getX(), event.getY(), false);
                if (mTranslateY >= FLING_THRESHOLD_LARGE) {
                    onFlingUp();
                } else if (mTranslateY <= -FLING_THRESHOLD_LARGE) {
                    onFlingDown();
                } else if (mTranslateX >= mFlingThreshold) {
                    onFlingRight();
                } else if (mTranslateX <= -mFlingThreshold) {
                    onFlingLeft();
                } else {
                    calcTranslation(mDownX, mDownY, true);
                }
                break;
            default:
                break;
        }
        return false;
    }

    private void calcTranslation(float currX, float currY, boolean isForce) {
        if (!isForce && Math.abs(currX - mCurrX) < MOVE_UPDATE_THRESHOLD &&
                Math.abs(currY - mCurrY) < MOVE_UPDATE_THRESHOLD) {
            // 过滤一下，防止滑动事件通知过于频繁
            return;
        }
        mCurrX = currX;
        mCurrY = currY;
        float deltaX = mCurrX - mDownX, deltaY = mCurrY - mDownY;
        if (checkVerticalMovable() && mTranslateX == 0) {
            float translateY = Math.abs(deltaY) >= MOVE_THRESHOLD ? deltaY : 0;
            if (mTranslateY != translateY) {
                onMoveVertical(mTranslateY, translateY);
                mTranslateY = translateY;
                if (mTranslateY == 0) {
                    mDownX = mCurrX; // 重置Down，防止动画跳动
                    mDownY = mCurrY;
                    deltaX = 0;
                }
            }
        }
        if (mHorizontalMoveEnabled && mTranslateY == 0) {
            float translateX;
            if (mIsHideAll) {
                translateX = deltaX <= -MOVE_THRESHOLD ? deltaX : 0;
            } else {
                translateX = deltaX >= MOVE_THRESHOLD ? deltaX : 0;
            }
            if (mTranslateX != translateX) {
                onMoveHorizontal(mTranslateX, translateX);
                mTranslateX = translateX;
                if (mTranslateX == 0) {
                    mDownX = mCurrX; // 重置Down，防止动画跳动
                    mDownY = mCurrY;
                }
            }
        }
    }

    private void onMoveVertical(float oldTranslateY, float newTranslateY) {
        for (View view : mVerticalSet) {
            if (view != null) {
                view.setTranslationY(newTranslateY);
            }
        }
        for (View view : mPagerArray) {
            if (view != null) {
                view.setTranslationY(newTranslateY);
            }
        }
        for (View view : mHalfArray) {
            if (view != null) {
                view.setTranslationY(newTranslateY / 2);
            }
        }
    }

    private void onMoveHorizontal(float oldTranslateX, float newTranslateX) {
        for (View view : mHorizontalSet) {
            if (view != null) {
                if (mIsHideAll) {
                    view.setTranslationX(mViewWidth + newTranslateX);
                    if (newTranslateX == 0) {
                        MyLog.d(TAG, "onMoveHorizontal setVisibility INVISIBLE");
                        view.setVisibility(View.INVISIBLE);
                    } else if (oldTranslateX == 0) {
                        MyLog.d(TAG, "onMoveHorizontal setVisibility VISIBLE");
                        view.setVisibility(View.VISIBLE);
                    }
                } else {
                    view.setTranslationX(newTranslateX);
                }
            }
        }
        if (mIsHideAll) {
            MyLog.d(TAG, "onMoveHorizontal setTranslationX allHided old=" + oldTranslateX + ", new=" + newTranslateX);
        } else {
            MyLog.d(TAG, "onMoveHorizontal setTranslationX allShowed old=" + oldTranslateX + ", new=" + newTranslateX);
        }
    }

    private void onFlingLeft() {
        mIsHideAll = false;
        mTranslateX = 0;
        for (View view : mHorizontalSet) {
            if (view != null) {
                view.setTranslationX(0);
            }
        }
        MyLog.d(TAG, "onFlingLeft setTranslationX 0");
    }

    private void onFlingRight() {
        mIsHideAll = true;
        mTranslateX = 0;
        for (View view : mHorizontalSet) {
            if (view != null) {
                view.setTranslationX(0);
                view.setVisibility(View.GONE);
            }
        }
        MyLog.d(TAG, "onFlingRight setTranslationX 0");
    }

    private void onFlingUp() {
        MyLog.d(TAG, "onFlingUp");
        mTranslateY = 0;
        for (View view : mVerticalSet) {
            if (view != null) {
                view.setTranslationY(0);
            }
        }
        for (View view : mHalfArray) {
            if (view != null) {
                view.setTranslationY(0);
            }
        }
        mComponentController.onEvent(ComponentController.MSG_PAGE_UP);
    }

    private void onFlingDown() {
        MyLog.d(TAG, "onFlingDown");
        mTranslateY = 0;
        for (View view : mVerticalSet) {
            if (view != null) {
                view.setTranslationY(0);
            }
        }
        for (View view : mHalfArray) {
            if (view != null) {
                view.setTranslationY(0);
            }
        }
        mComponentController.onEvent(ComponentController.MSG_PAGE_DOWN);
    }

    public void onBackgroundClick() {
        mComponentController.onEvent(ComponentController.MSG_BACKGROUND_CLICK);
    }

    private boolean checkVerticalMovable() {
        return mOpenVerticalMove && mVerticalMoveEnabled;
    }

    @Nullable
    @Override
    protected ComponentPresenter.IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            switch (source) {
                case WatchComponentController.MSG_ON_ORIENT_PORTRAIT:
                    mViewWidth = GlobalData.screenWidth;
                    mFlingThreshold = FLING_THRESHOLD_NORMAL;
                    return true;
                case WatchComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    mViewWidth = GlobalData.screenHeight;
                    mFlingThreshold = FLING_THRESHOLD_LARGE;
                    if (mIsGameMode && mIsHideAll) { // 竖屏转横屏，恢复被隐藏的View，横屏转竖屏的逻辑在WatchSdkView中处理
                        mIsHideAll = false;
                        for (View view : mHorizontalSet) {
                            if (view != null && view.getVisibility() != View.VISIBLE) {
                                view.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    return true;
                case WatchComponentController.MSG_ENABLE_MOVE_VIEW:
                    mHorizontalMoveEnabled = true;
                    mVerticalMoveEnabled = true;
                    return true;
                case WatchComponentController.MSG_DISABLE_MOVE_VIEW:
                    mHorizontalMoveEnabled = false;
                    mVerticalMoveEnabled = false;
                    return true;
                default:
                    break;
            }
            return false;
        }
    }
}
