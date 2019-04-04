package com.common.floatwindow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.common.log.MyLog;
import com.common.utils.ActivityUtils;
import com.common.utils.U;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by yhao on 2017/12/22.
 * https://github.com/yhaolpz
 */

public class IFloatWindowImpl extends IFloatWindow {
    public final static String TAG = "IFloatWindowImpl";
    private FloatWindow.B mB;
    private FloatView mFloatView;
    private FloatLifecycle mFloatLifecycle;
    private boolean isShow;
    private boolean mFirst = true;
    private ValueAnimator mAnimator;
    private TimeInterpolator mDecelerateInterpolator;
    private float downX;
    private float downY;
    private float upX;
    private float upY;
    private boolean mClick = false;
    private int mSlop;

    private IFloatWindowImpl() {

    }

    IFloatWindowImpl(FloatWindow.B b) {
        mB = b;
        if (mB.mMoveType == MoveType.fixed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                mFloatView = new FloatPhone(mB);
            } else {
                mFloatView = new FloatToast(mB);
            }
        } else {
            mFloatView = new FloatPhone(mB);
            initTouchEvent();
        }
        mFloatLifecycle = new FloatLifecycle(mB.mApplicationContext, mB.mShow, mB.mActivities, new LifecycleListener() {
            @Override
            public void onShow(String from) {
                // Activity 的 onResume 中响应
                show(from);
            }

            @Override
            public void onHide(String from) {
                hide(from);
            }

        });
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Subscribe
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        if (!event.foreground) {
            if (!mB.mDesktopShow) {
                hide("回到桌面");
            }
            if (mB.mViewStateListener != null) {
                mB.mViewStateListener.onBackToDesktop();
            }
        } else {
            if (mFloatLifecycle.needShow(U.getActivityUtils().getTopActivity())) {
                show("从桌面返回");
            }
        }
    }

    @Override
    public void show(String from) {
        MyLog.d(TAG, "show mFirst=" + mFirst + " isShow=" + isShow + " from=" + from);
        if (mFirst) {
            mFloatView.init();
            mFirst = false;
            isShow = true;
        } else {
            if (isShow) {
                return;
            }
            getView().setVisibility(View.VISIBLE);
            isShow = true;
        }
        if (mB.mViewStateListener != null) {
            mB.mViewStateListener.onShow();
        }
    }

    @Override
    public void hide(String from) {
        MyLog.d(TAG, "hide once=" + mFirst + " isShow=" + isShow + " from=" + from);
        if (mFirst || !isShow) {
            return;
        }
        getView().setVisibility(View.INVISIBLE);
        isShow = false;
        if (mB.mViewStateListener != null) {
            mB.mViewStateListener.onHide();
        }
    }

    @Override
    public boolean isShowing() {
        return isShow;
    }

    @Override
    void dismiss() {
        MyLog.d(TAG, "dismiss");
        mFloatView.dismiss();
        isShow = false;
        if (mB.mViewStateListener != null) {
            mB.mViewStateListener.onDismiss();
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void updateX(int x) {
        checkMoveType();
        mB.xOffset = x;
        mFloatView.updateX(x);
    }

    @Override
    public void updateY(int y) {
        checkMoveType();
        mB.yOffset = y;
        mFloatView.updateY(y);
    }

    @Override
    public void updateX(int screenType, float ratio) {
        checkMoveType();
        mB.xOffset = (int) ((screenType == Screen.width ?
                U.getDisplayUtils().getScreenWidth() :
                U.getDisplayUtils().getScreenHeight()) * ratio);
        mFloatView.updateX(mB.xOffset);

    }

    @Override
    public void updateY(int screenType, float ratio) {
        checkMoveType();
        mB.yOffset = (int) ((screenType == Screen.width ?
                U.getDisplayUtils().getScreenWidth() :
                U.getDisplayUtils().getScreenHeight()) * ratio);
        mFloatView.updateY(mB.yOffset);

    }

    @Override
    public int getX() {
        return mFloatView.getX();
    }

    @Override
    public int getY() {
        return mFloatView.getY();
    }


    @Override
    public View getView() {
        mSlop = ViewConfiguration.get(mB.mApplicationContext).getScaledTouchSlop();
        return mB.mView;
    }


    private void checkMoveType() {
        if (mB.mMoveType == MoveType.fixed) {
            throw new IllegalArgumentException("FloatWindow of this tag is not allowed to move!");
        }
    }


    private void initTouchEvent() {
        switch (mB.mMoveType) {
            case MoveType.inactive:
                break;
            default:
                getView().setOnTouchListener(new View.OnTouchListener() {
                    float lastX, lastY;
                    int originVX, originVY;

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                downX = event.getRawX();
                                downY = event.getRawY();
                                lastX = event.getRawX();
                                lastY = event.getRawY();
                                originVX = mFloatView.getX();
                                originVY = mFloatView.getY();
                                cancelAnimator();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                float rx = event.getRawX();
                                float ry = event.getRawY();
                                MyLog.d(TAG, "onTouch rx=" + rx + " ry=" + ry);

                                float changeX = rx - lastX;
                                float changeY = ry - lastY;
                                if (mB.mMoveType == MoveType.canRemove) {
                                    mFloatView.updateX((int) (mFloatView.getX()+changeX));
                                } else {
                                    mFloatView.updateXY((int) (mFloatView.getX()+changeX), (int) (mFloatView.getY()+changeY));
                                }
                                if (mB.mViewStateListener != null) {
                                    mB.mViewStateListener.onPositionUpdate(mFloatView.getX(), mFloatView.getY());
                                }
                                lastX = rx;
                                lastY = ry;
                                break;
                            case MotionEvent.ACTION_UP:
                                upX = event.getRawX();
                                upY = event.getRawY();
                                mClick = (Math.abs(upX - downX) > mSlop) || (Math.abs(upY - downY) > mSlop);
                                switch (mB.mMoveType) {
                                    case MoveType.slide:
                                        int startX = mFloatView.getX();
                                        int endX = (startX * 2 + v.getWidth() > U.getDisplayUtils().getScreenWidth()) ?
                                                U.getDisplayUtils().getScreenWidth() - v.getWidth() - mB.mSlideRightMargin :
                                                mB.mSlideLeftMargin;
                                        mAnimator = ObjectAnimator.ofInt(startX, endX);
                                        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator animation) {
                                                int x = (int) animation.getAnimatedValue();
                                                mFloatView.updateX(x);
                                                if (mB.mViewStateListener != null) {
                                                    mB.mViewStateListener.onPositionUpdate(x, (int) upY);
                                                }
                                            }
                                        });
                                        startAnimator();
                                        break;
                                    case MoveType.back: {
                                        PropertyValuesHolder pvhX = PropertyValuesHolder.ofInt("x", mFloatView.getX(), originVX);
                                        PropertyValuesHolder pvhY = PropertyValuesHolder.ofInt("y", mFloatView.getY(), originVY);
                                        mAnimator = ObjectAnimator.ofPropertyValuesHolder(pvhX, pvhY);
                                        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator animation) {
                                                int x = (int) animation.getAnimatedValue("x");
                                                int y = (int) animation.getAnimatedValue("y");
                                                mFloatView.updateXY(x, y);
                                                if (mB.mViewStateListener != null) {
                                                    mB.mViewStateListener.onPositionUpdate(x, y);
                                                }
                                            }
                                        });
                                        startAnimator();
                                    }
                                    break;
                                    case MoveType.canRemove: {
                                        if (upX - downX > U.getDisplayUtils().getScreenWidth() * 0.2) {
                                            MyLog.d(TAG, "onTouchUp 划走消失");
                                            PropertyValuesHolder pvhX = PropertyValuesHolder.ofInt("x", mFloatView.getX(), U.getDisplayUtils().getScreenWidth());
                                            mAnimator = ObjectAnimator.ofPropertyValuesHolder(pvhX);
                                            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                                @Override
                                                public void onAnimationUpdate(ValueAnimator animation) {
                                                    int x = (int) animation.getAnimatedValue("x");
                                                    mFloatView.updateX(x);
                                                    if (mB.mViewStateListener != null) {
                                                        mB.mViewStateListener.onPositionUpdate(mFloatView.getX(), mFloatView.getY());
                                                    }
                                                }
                                            });
                                            mAnimator.setInterpolator(new AccelerateInterpolator());
                                            mAnimator.addListener(new AnimatorListenerAdapter() {
                                                @Override
                                                public void onAnimationCancel(Animator animation) {
                                                    super.onAnimationCancel(animation);
                                                }

                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                    super.onAnimationEnd(animation);
                                                    FloatWindow.destroy(mB.mTag);
                                                    //dismiss();
                                                }
                                            });
                                            startAnimator();
                                        } else {
                                            MyLog.d(TAG, "onTouchUp back");
                                            PropertyValuesHolder pvhX = PropertyValuesHolder.ofInt("x", mFloatView.getX(), originVX);
                                            mAnimator = ObjectAnimator.ofPropertyValuesHolder(pvhX);
                                            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                                @Override
                                                public void onAnimationUpdate(ValueAnimator animation) {
                                                    int x = (int) animation.getAnimatedValue("x");
                                                    mFloatView.updateX(x);
                                                    if (mB.mViewStateListener != null) {
                                                        mB.mViewStateListener.onPositionUpdate(mFloatView.getX(), mFloatView.getY());
                                                    }
                                                }
                                            });
                                            startAnimator();
                                        }
                                        break;
                                    }
                                    default:
                                        break;
                                }
                                break;
                            default:
                                break;
                        }
                        return mClick;
                    }
                });
        }
    }

    private void startAnimator() {
        if (mB.mInterpolator == null) {
            if (mDecelerateInterpolator == null) {
                mDecelerateInterpolator = new DecelerateInterpolator();
            }
            mB.mInterpolator = mDecelerateInterpolator;
        }
        mAnimator.setInterpolator(mB.mInterpolator);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimator.removeAllUpdateListeners();
                mAnimator.removeAllListeners();
                mAnimator = null;
                if (mB.mViewStateListener != null) {
                    mB.mViewStateListener.onMoveAnimEnd();
                }
            }
        });
        mAnimator.setDuration(mB.mDuration).start();
        if (mB.mViewStateListener != null) {
            mB.mViewStateListener.onMoveAnimStart();
        }
    }

    private void cancelAnimator() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }

}
