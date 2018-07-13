package com.wali.live.watchsdk.component.presenter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.thornbirds.component.view.IOrientationListener;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.wali.live.component.BaseSdkController.MSG_BACKGROUND_CLICK;
import static com.wali.live.component.BaseSdkController.MSG_CLEAR_SCREEN_CANCEL;
import static com.wali.live.component.BaseSdkController.MSG_CLEAR_SCREEN_VIEW_GONE;
import static com.wali.live.component.BaseSdkController.MSG_CLEAR_SCREEN_VIEW_VISIABLE;
import static com.wali.live.component.BaseSdkController.MSG_DISABLE_MOVE_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_ENABLE_MOVE_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_PAGE_DOWN;
import static com.wali.live.component.BaseSdkController.MSG_PAGE_UP;

/**
 * Created by yangli on 2017/03/05.
 *
 * @module 触摸表现
 */
public class TouchPresenter extends ComponentPresenter implements View.OnTouchListener,
        IOrientationListener {
    private static final String TAG = "TouchPresenter";

    private static final int MODE_IDLE = 0;
    private static final int MODE_HORIZONTAL = 1;
    private static final int MODE_VERTICAL = 2;

    public static final int MOVE_UPDATE_THRESHOLD = 5;
    public static final int MOVE_THRESHOLD = 20;
    public static final int FLING_THRESHOLD_NORMAL = 300;
    public static final int FLING_THRESHOLD_LARGE = (GlobalData.screenHeight / 3);

    public static final int FLING_VELOCITY_THRESHOLD = 5000;

    public static final float SLOW_SPEED = 0.6f;

    @NonNull
    private List<View> mHorizontalSet;
    @NonNull
    private List<View> mVerticalSet;

    private View[] mSlowArray = new View[0];
    private View[] mPagerArray = new View[0];

    private boolean mIsHideAll = false;
    private boolean mIsGameMode = false;
    private boolean mIsLandscape = false;

    private boolean mHorizontalMoveEnabled = true;
    private boolean mVerticalMoveEnabled = true;

    // 增加竖屏滑动的开关，用于在只有一个观看列表的情况下进行控制
    private boolean mOpenVerticalMove = false;

    private View mTouchView;
    private int mViewWidth = GlobalData.screenWidth;
    private int mFlingThreshold = FLING_THRESHOLD_NORMAL;

    private final AnimationHelper mAnimationHelper = new AnimationHelper();

    private VelocityTracker mVelocityTracker;
    private int mMaxVelocity;

    protected RoomBaseDataModel mRoomBaseDataModel;

    public void setViewSet(@NonNull List<View> horizontalSet) {
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
        mSlowArray = halfArray;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    public TouchPresenter(@NonNull IEventController controller, RoomBaseDataModel roomBaseDataModel, @NonNull View touchView) {
        super(controller);
        mRoomBaseDataModel = roomBaseDataModel;
        mTouchView = touchView;
        mTouchView.setSoundEffectsEnabled(false);
        mTouchView.setOnTouchListener(this);
        mTouchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackgroundClick();
            }
        });

        mMaxVelocity = ViewConfiguration.get(GlobalData.app()).getScaledMaximumFlingVelocity();
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_ENABLE_MOVE_VIEW);
        registerAction(MSG_DISABLE_MOVE_VIEW);
        registerAction(MSG_CLEAR_SCREEN_CANCEL);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        mAnimationHelper.clearAnimation();
    }

    private float mCurrX = -1, mCurrY = -1, mDownX = -1, mDownY = -1;
    private float mTranslation;
    private int mMode = MODE_IDLE;
    private boolean mTouchCanceled = true;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mAnimationHelper.isInAnimation()) {
            return false;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mTouchCanceled = false;
                mMode = MODE_IDLE;
                mCurrX = mDownX = event.getX();
                mCurrY = mDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchCanceled) {
                    break;
                }
                if (mDownX == -1 && mDownY == -1) {
                    mCurrX = mDownX = event.getX();
                    mCurrY = mDownY = event.getY();
                }
                if (mMode == MODE_IDLE) {
                    calcDirection(event.getX(), event.getY());
                } else if (mMode == MODE_VERTICAL) {
                    calcMoveVertical(event.getY());
                } else if (mMode == MODE_HORIZONTAL) {
                    calcMoveHorizontal(event.getX());
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchCanceled) {
                    break;
                }
                if (mMode == MODE_VERTICAL) {
                    calcMoveVertical(event.getY());
                    if (mTranslation <= -FLING_THRESHOLD_LARGE) {
                        onFlingUp();
                    } else if (mTranslation >= FLING_THRESHOLD_LARGE) {
                        onFlingDown();
                    } else {
                        mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                        float velocityY = mVelocityTracker.getYVelocity();

                        if (velocityY <= -FLING_VELOCITY_THRESHOLD) {
                            onFlingUp();
                        } else if (velocityY >= FLING_VELOCITY_THRESHOLD) {
                            onFlingDown();
                        } else {
                            onCancelMoveVertical();
                        }
                    }
                } else if (mMode == MODE_HORIZONTAL) {
                    calcMoveHorizontal(event.getX());
                    if (mTranslation <= -mFlingThreshold) {
                        onFlingLeft();
                    } else if (mTranslation >= mFlingThreshold) {
                        onFlingRight();
                    } else {
                        onCancelMoveHorizontal();
                    }
                }
                mCurrX = mCurrY = mDownY = mDownX = -1;

                releaseVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                releaseVelocityTracker();
                break;
            default:
                break;
        }
        return false;
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void calcDirection(float currX, float currY) {
        if (Math.abs(currX - mCurrX) < MOVE_UPDATE_THRESHOLD &&
                Math.abs(currY - mCurrY) < MOVE_UPDATE_THRESHOLD) { // 过滤一下，防止滑动事件通知过于频繁
            return;
        }
        mCurrX = currX;
        mCurrY = currY;
        float deltaX = mCurrX - mDownX, deltaY = mCurrY - mDownY;
        float distX = Math.abs(deltaX), distY = Math.abs(deltaY);
        if (checkVerticalMovable() && distY >= MOVE_THRESHOLD) {
            mMode = MODE_VERTICAL;
            onMoveVertical(mTranslation, deltaY);
            mTranslation = deltaY;
        } else if (mHorizontalMoveEnabled && distX >= MOVE_THRESHOLD) {
            if ((mIsHideAll && deltaX <= -MOVE_THRESHOLD) || (!mIsHideAll && deltaX >= MOVE_THRESHOLD)) {
                mMode = MODE_HORIZONTAL;
                onMoveHorizontal(mTranslation, deltaX);
                mTranslation = deltaX;
            }
        }
    }

    private void calcMoveHorizontal(float currX) {
        if (Math.abs(currX - mCurrX) < MOVE_UPDATE_THRESHOLD) { // 过滤一下，防止滑动事件通知过于频繁
            return;
        }
        mCurrX = currX;
        float translateX, deltaX = mCurrX - mDownX;
        if (mIsHideAll) {
            translateX = deltaX <= -MOVE_THRESHOLD ? deltaX : 0;
        } else {
            translateX = deltaX >= MOVE_THRESHOLD ? deltaX : 0;
        }
        if (mTranslation != translateX) {
            onMoveHorizontal(mTranslation, translateX);
            mTranslation = translateX;
        }
    }

    private void calcMoveVertical(float currY) {
        if (Math.abs(currY - mCurrY) < MOVE_UPDATE_THRESHOLD) { // 过滤一下，防止滑动事件通知过于频繁
            return;
        }
        mCurrY = currY;
        float deltaY = mCurrY - mDownY, translateY = Math.abs(deltaY) >= MOVE_THRESHOLD ? deltaY : 0;
        if (mTranslation != translateY) {
            onMoveVertical(mTranslation, translateY);
            mTranslation = translateY;
        }
    }

    private boolean checkVerticalMovable() {
        return mOpenVerticalMove && mVerticalMoveEnabled && !mIsLandscape;
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
        for (View view : mSlowArray) {
            if (view != null) {
                view.setTranslationY(newTranslateY * SLOW_SPEED);
            }
        }
    }

    private void onMoveHorizontal(float oldTranslateX, float newTranslateX) {
        for (View view : mHorizontalSet) {
            if (view != null) {
                if (isNotNeedToCare(view)) {
                    continue;
                }
                if (mIsHideAll) {
                    view.setTranslationX(mViewWidth + newTranslateX);
                    if (newTranslateX == 0) {
                        MyLog.d(TAG, "onMoveHorizontal setVisibility INVISIBLE");
                        view.setVisibility(View.INVISIBLE);
                        mController.postEvent(MSG_CLEAR_SCREEN_VIEW_GONE);
                    } else if (oldTranslateX == 0) {
                        MyLog.d(TAG, "onMoveHorizontal setVisibility VISIBLE");
                        int visiableValue = getPreViewVisiable(view,View.VISIBLE);
                        view.setVisibility(visiableValue);
                        mController.postEvent(MSG_CLEAR_SCREEN_VIEW_VISIABLE);
                    }
                } else {
                    view.setTranslationX(newTranslateX);
                }
            }
        }
    }

    private void onFlingLeft() {
        setHideAll(false);
        mTranslation = 0;
        for (View view : mHorizontalSet) {
            if (view != null) {
                view.setTranslationX(0);
            }
        }
        MyLog.d(TAG, "onFlingLeft setTranslationX 0");
    }

    private HashMap<Integer, Integer> mPreViewVisiableMap = new HashMap<>();

    private void onFlingRight() {
        setHideAll(true);
        mTranslation = 0;
        for (View view : mHorizontalSet) {
            if (view != null) {
                view.setTranslationX(0);
                mPreViewVisiableMap.put(view.getId(),  view.getVisibility());
                view.setVisibility(View.GONE);
            }
        }
        mController.postEvent(MSG_CLEAR_SCREEN_VIEW_GONE);
        MyLog.d(TAG, "onFlingRight setTranslationX 0");
    }

    private void setHideAll(boolean hideAll) {
        mIsHideAll = hideAll;
        if (mIsHideAll) {

        } else {
            mPreViewVisiableMap.clear();
        }
    }

    int getPreViewVisiable(View view, int defaultValue) {
        Integer visiable = mPreViewVisiableMap.get(view.getId());
        if (visiable == null) {
            return defaultValue;
        }
        return visiable.intValue();
    }

    private void onCancelMoveHorizontal() {
        mTranslation = 0;
        int visibility = mIsHideAll ? View.GONE : View.VISIBLE;
        for (View view : mHorizontalSet) {
            if (view != null) {
                if (isNotNeedToCare(view)) {
                    continue;
                }
                view.setTranslationX(0);
                view.setVisibility(visibility);
            }
        }
        if (mIsHideAll) {
            mController.postEvent(MSG_CLEAR_SCREEN_VIEW_GONE);
        } else {
            mController.postEvent(MSG_CLEAR_SCREEN_VIEW_VISIABLE);
        }
        MyLog.d(TAG, "onFlingRight setTranslationX 0");
    }

    private boolean isNotNeedToCare(View view) {
        // 判断
        if (!mRoomBaseDataModel.isVideoLandscape() && !mRoomBaseDataModel.isLandscape()) {
            // 隐藏旋转按钮
            if (view.getId() == R.id.rotate_btn) {
                return true;
            }
        }
        return false;
    }

    private void onFlingUp() {
        MyLog.d(TAG, "onFlingUp");
        mAnimationHelper.startSwitchAnimator(mTranslation, -GlobalData.screenHeight, MSG_PAGE_UP);
    }

    private void onFlingDown() {
        MyLog.d(TAG, "onFlingDown");
        mAnimationHelper.startSwitchAnimator(mTranslation, GlobalData.screenHeight, MSG_PAGE_DOWN);
    }

    private void onCancelMoveVertical() {
        mAnimationHelper.startCancelAnimator(mTranslation, 0);
    }

    public void onBackgroundClick() {
        postEvent(MSG_BACKGROUND_CLICK);
    }

    @Override
    public void onOrientation(boolean isLandscape) {
        if (mTouchCanceled || mMode == MODE_IDLE) {
            return;
        }
        mTouchCanceled = true;
        if (mMode == MODE_HORIZONTAL) {
            onMoveHorizontal(mTranslation, 0);
        } else if (mMode == MODE_VERTICAL) {
            mAnimationHelper.clearAnimation();
            onMoveVertical(mTranslation, 0);
        }
        mTranslation = 0;
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                mIsLandscape = false;
                mViewWidth = GlobalData.screenWidth;
                mFlingThreshold = FLING_THRESHOLD_NORMAL;
                onOrientation(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mIsLandscape = true;
                mViewWidth = GlobalData.screenHeight;
                mFlingThreshold = FLING_THRESHOLD_LARGE;
                if (mIsGameMode && mIsHideAll) { // 竖屏转横屏，恢复被隐藏的View，横屏转竖屏的逻辑在WatchSdkView中处理
                    setHideAll(false);
                    for (View view : mHorizontalSet) {
                        if (view != null && view.getVisibility() != View.VISIBLE) {
                            view.setVisibility(View.VISIBLE);
                        }
                    }
                }
                onOrientation(true);
                return true;
            case MSG_ENABLE_MOVE_VIEW:
                mHorizontalMoveEnabled = true;
                mVerticalMoveEnabled = true;
                return true;
            case MSG_DISABLE_MOVE_VIEW:
                mHorizontalMoveEnabled = false;
                mVerticalMoveEnabled = false;
                return true;
            case MSG_CLEAR_SCREEN_CANCEL:
                onMoveHorizontal(0, 1);
                onFlingLeft();
                break;
            default:
                break;
        }
        return false;
    }

    private class AnimationHelper {
        private static final int ANIMATION_TIME = 250;

        private ValueAnimator mMoveAnimator;
        private ValueAnimator mShowAnimator;
        private int mSource;
        private boolean mIsWithShow;
        private boolean mIsInAnimation;

        public boolean isInAnimation() {
            return mIsInAnimation;
        }

        private void onChangeAlphaVertical(float alpha) {
            for (View view : mVerticalSet) {
                if (view != null) {
                    view.setAlpha(alpha);
                }
            }
        }

        protected void setupAnimator() {
            if (mMoveAnimator == null) {
                mMoveAnimator = new ValueAnimator();
                mMoveAnimator.setInterpolator(new DecelerateInterpolator());
                mMoveAnimator.setDuration(ANIMATION_TIME);
                mMoveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float value = (float) valueAnimator.getAnimatedValue();
                        onMoveVertical(mTranslation, value);
                        mTranslation = value;
                    }
                });
                mMoveAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onMoveVertical(mTranslation, 0);
                        mTranslation = 0;
                        if (!mIsWithShow) {
                            mIsInAnimation = false;
                        } else {
                            onChangeAlphaVertical(0);
                            mShowAnimator.start();
                        }
                        if (mSource > 0) {
                            postEvent(mSource);
                        }
                    }
                });
            }
            if (mShowAnimator == null) {
                mShowAnimator = ValueAnimator.ofFloat(0, 1);
                mShowAnimator.setInterpolator(new LinearInterpolator());
                mShowAnimator.setDuration(ANIMATION_TIME);
                mShowAnimator.setStartDelay(ANIMATION_TIME);
                mShowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        onChangeAlphaVertical((float) valueAnimator.getAnimatedValue());
                    }
                });
                mShowAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mIsInAnimation = false;
                    }
                });
            }
        }

        protected void startCancelAnimator(float fromPos, float toPos) {
            setupAnimator();
            mIsWithShow = false;
            mSource = 0;
            mIsInAnimation = true;
            mMoveAnimator.setFloatValues(fromPos, toPos);
            mMoveAnimator.setDuration(ANIMATION_TIME);
            mMoveAnimator.start();
        }

        protected void startSwitchAnimator(float fromPos, float toPos, int msgType) {
            setupAnimator();
            mIsWithShow = true;
            mSource = msgType;
            mMoveAnimator.setFloatValues(fromPos, toPos);
            mMoveAnimator.setDuration(ANIMATION_TIME);
            mIsInAnimation = true;
            mMoveAnimator.start();
        }

        protected void clearAnimation() {
            mSource = 0;
            mIsWithShow = false;
            if (mIsInAnimation) {
                if (mMoveAnimator.isRunning()) {
                    mMoveAnimator.cancel();
                }
                if (mShowAnimator.isRunning()) {
                    mShowAnimator.end();
                }
            }
        }
    }
}
