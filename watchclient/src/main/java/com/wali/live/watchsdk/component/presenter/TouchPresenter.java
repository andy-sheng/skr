package com.wali.live.watchsdk.component.presenter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.WatchComponentController;

import java.lang.ref.WeakReference;
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
    public static final int FLING_THRESHOLD_LARGE = 450;

    @NonNull
    private final MoveHelper mMoveHelper = new MoveHelper();
    @Nullable
    private GameHideHelper mGameHideHelper;

    private final List<View> mHorizontalSet = new ArrayList<>();
    private final List<View> mVerticalSet = new ArrayList<>(0);
    private final List<View> mGameHideSet = new ArrayList<>(0);

    private View mTouchView;
    private int mViewWidth = GlobalData.screenWidth;
    private int mFlingThreshold = FLING_THRESHOLD_NORMAL;

    public void addHorizontalView(View... viewList) {
        if (viewList != null && viewList.length > 0) {
            for (View view : viewList) {
                mHorizontalSet.add(view);
                mVerticalSet.add(view);
            }
        }
    }

    public void addVerticalView(View... viewList) {
        if (viewList != null && viewList.length > 0) {
            for (View view : viewList) {
                mVerticalSet.add(view);
            }
        }
    }

    public void addGameHideView(View... viewList) {
        if (viewList != null && viewList.length > 0) {
            for (View view : viewList) {
                mGameHideSet.add(view);
            }
        }
        if (!mGameHideSet.isEmpty()) {
            mGameHideHelper = new GameHideHelper();
        }
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
                mMoveHelper.calcTranslation(event.getX(), event.getY(), false);
                break;
            case MotionEvent.ACTION_UP:
                mMoveHelper.calcTranslation(event.getX(), event.getY(), false);
                if (mTranslateY >= FLING_THRESHOLD_LARGE) {
                    mMoveHelper.onFlingUp();
                } else if (mTranslateY <= -FLING_THRESHOLD_LARGE) {
                    mMoveHelper.onFlingDown();
                } else if (mTranslateX >= mFlingThreshold) {
                    mMoveHelper.onFlingRight();
                } else if (mTranslateX <= -mFlingThreshold) {
                    mMoveHelper.onFlingLeft();
                } else {
                    mMoveHelper.calcTranslation(mDownX, mDownY, true);
                }
                break;
            default:
                break;
        }
        return false;
    }

    public void onBackgroundClick() {
        mComponentController.onEvent(ComponentController.MSG_BACKGROUND_CLICK);
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
                    if (mGameHideHelper != null) {
                        mGameHideHelper.onOrientation(false);
                    }
                    return true;
                case WatchComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    mViewWidth = GlobalData.screenHeight;
                    mFlingThreshold = FLING_THRESHOLD_LARGE;
                    if (mGameHideHelper != null) {
                        mGameHideHelper.onOrientation(false);
                    }
                    return true;
                case WatchComponentController.MSG_ENABLE_MOVE_VIEW:
                    mMoveHelper.mHorizontalMoveEnabled = true;
                    return true;
                case WatchComponentController.MSG_DISABLE_MOVE_VIEW:
                    mMoveHelper.mHorizontalMoveEnabled = false;
                    return true;
                default:
                    break;
            }
            return false;
        }
    }

    private class GameHideHelper {

        private WeakReference<ValueAnimator> mGameAnimatorRef; // 游戏直播竖屏时，隐藏显示动画
        private boolean mGameShow = true;

        private <T> T deRef(WeakReference<?> reference) {
            return reference != null ? (T) reference.get() : null;
        }

        private void setVisibility(List<View> viewSet, int visibility) {
            if (viewSet == null) {
                return;
            }
            for (View view : viewSet) {
                if (view != null) {
                    view.setVisibility(visibility);
                }
            }
        }

        public void onOrientation(boolean isLandscape) {
            stopAnimator();
            if (isLandscape) {
                setVisibility(mHorizontalSet, View.GONE);
                setVisibility(mGameHideSet, View.VISIBLE);
            } else {
                setVisibility(mHorizontalSet, View.VISIBLE);
                setVisibility(mGameHideSet, View.GONE);
            }
        }

        /**
         * 观看游戏直播横屏时，点击隐藏显示View
         */
        private void startGameAnimator() {
            mGameShow = !mGameShow;
            ValueAnimator valueAnimator = deRef(mGameAnimatorRef);
            if (valueAnimator != null) {
                if (!valueAnimator.isStarted() && !valueAnimator.isRunning()) {
                    valueAnimator.start();
                }
                return;
            }
            valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            valueAnimator.setDuration(300);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    if (!mGameShow) {
                        value = 1.0f - value;
                    }
                    for (View view : mGameHideSet) {
                        if (view != null) {
                            view.setAlpha(value);
                        }
                    }
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mGameShow) {
                        for (View view : mGameHideSet) {
                            if (view != null) {
                                view.setAlpha(0.0f);
                                view.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!mGameShow) {
                        for (View view : mGameHideSet) {
                            if (view != null) {
                                view.setAlpha(1.0f);
                                view.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });
            valueAnimator.start();
            mGameAnimatorRef = new WeakReference<>(valueAnimator);
        }

        private void stopAnimator() {
            ValueAnimator valueAnimator = deRef(mGameAnimatorRef);
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
        }

        public void clearAnimation() {
            stopAnimator();
            mGameAnimatorRef = null;
        }
    }

    /**
     * 左右上下滑动辅助类
     */
    private class MoveHelper {
        private boolean mIsHideAll = false;

        private boolean mHorizontalMoveEnabled = true;
        private boolean mVerticalMoveEnabled = true;

        private void calcTranslation(float currX, float currY, boolean isForce) {
            if (!isForce && Math.abs(currX - mCurrX) < MOVE_UPDATE_THRESHOLD &&
                    Math.abs(currY - mCurrY) < MOVE_UPDATE_THRESHOLD) {
                // 过滤一下，防止滑动事件通知过于频繁
                return;
            }
            mCurrX = currX;
            mCurrY = currY;
            float deltaX = mCurrX - mDownX, deltaY = mCurrY - mDownY;
            if (mVerticalMoveEnabled && mTranslateX == 0) {
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
            mTranslateY = 0;
            for (View view : mVerticalSet) {
                if (view != null) {
                    view.setTranslationY(0);
                }
            }
        }

        private void onFlingDown() {
            mTranslateY = 0;
            for (View view : mVerticalSet) {
                if (view != null) {
                    view.setTranslationY(0);
                }
            }
        }
    }
}
