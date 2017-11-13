package com.wali.live.livesdk.live.window;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.window.presenter.GameFloatIconPresenter;
import com.wali.live.livesdk.live.window.presenter.IGameFloatIcon;

/**
 * Created by yangli on 16-11-29.
 *
 * @module 悬浮窗
 */
public class GameFloatIcon extends RelativeLayout implements IGameFloatIcon {
    private static final String TAG = "GameFloatIcon";

    public final static int WINDOW_PADDING = DisplayUtils.dip2px(6.67f);
    public final static int ICON_WIDTH = DisplayUtils.dip2px(38f);
    public final static int MOVE_THRESHOLD = DisplayUtils.dip2px(2f);
    private final static int ICON_TRANSLATION = DisplayUtils.dip2px(14f);
    private final static int ICON_ROTATION = 15;
    private final static float ICON_ALPHA = 0.5f;

    public final static int MODE_NORMAL = 0; // 正常
    public final static int MODE_HALF_HIDDEN = 1; // 半隐藏
    public final static int MODE_DRAGGING = 2; // 正在拖动
    public final static int MODE_MOVING = 3; // 正在移动(拖动之后自动恢复位置)

    private GameFloatIconPresenter mPresenter;

    private final int mParentWidth;
    private final int mParentHeight;
    private final Rect mBoundRect;
    private boolean mIsLandscape;

    private final GameFloatView mGameFloatView;
    private final WindowManager mWindowManager;
    private final WindowManager.LayoutParams mFloatLayoutParams;
    private final GameFloatWindow.MyUiHandler mUiHandler;
    private final IGameFloatPresenter mGamePresenter;
    private final AnimationHelper mAnimationHelper;
    private boolean mIsWindowShow = false;
    private boolean mAlignLeft = true;

    private int mMode = MODE_NORMAL;
    private final TouchEventHelper mTouchEventHelper;

    ImageView mMainBtn;
    ImageView mStatusIcon;
    BaseImageView mGiftBiv;
    BaseImageView mGiftBiv2;//每次礼物过来，用mGiftBiv2显示。动画完成后和mGiftBiv交换，下次就可以继续用了。

    private <T> T $(int id) {
        return (T) findViewById(id);
    }

    void onMainBtnClick() {
        switch (mMode) {
            case MODE_NORMAL:
                mGameFloatView.onMainBtnClick();
                break;
            case MODE_HALF_HIDDEN:
                setMode(MODE_NORMAL);
                // 由半隐藏进入正常模式时，发送一个延迟半隐藏的事件
                mUiHandler.removeMessages(GameFloatWindow.MSG_HALF_HIDE_FLOAT_BALL);
                mUiHandler.sendEmptyMessageDelayed(GameFloatWindow.MSG_HALF_HIDE_FLOAT_BALL,
                        GameFloatWindow.TIME_HIDE_GAME_FLOAT_BALL);
                break;
            default:
                break;
        }
    }

    public GameFloatIcon(
            @NonNull Context context,
            @NonNull WindowManager windowManager,
            @NonNull GameFloatWindow.MyUiHandler uiHandler,
            @NonNull IGameFloatPresenter gamePresenter,
            @NonNull GameFloatView gameFloatView,
            int parentWidth,
            int parentHeight) {
        super(context);
        inflate(context, R.layout.game_float_icon, this);

        mMainBtn = $(R.id.main_btn);
        mStatusIcon = $(R.id.status_icon);
        mGiftBiv = $(R.id.gift_biv);
        mGiftBiv2 = $(R.id.gift_biv2);

        mParentWidth = parentWidth;
        mParentHeight = parentHeight;
        mBoundRect = new Rect(0, 0, mParentWidth, mParentHeight);

        setPadding(0, WINDOW_PADDING, 0, WINDOW_PADDING);

        mWindowManager = windowManager;
        mUiHandler = uiHandler;
        mGamePresenter = gamePresenter;
        mGameFloatView = gameFloatView;
        mFloatLayoutParams = new WindowManager.LayoutParams();
        mTouchEventHelper = new TouchEventHelper();
        mAnimationHelper = new AnimationHelper();
        // setMode(MODE_HALF_HIDDEN);
        setupLayoutParams();
        mMainBtn.setSoundEffectsEnabled(false);
        mMainBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        // TODO-YangLi Fixme 将其与小浮窗的显示与隐藏绑定
        mPresenter = new GameFloatIconPresenter();
        mPresenter.setView(this);
        mPresenter.startPresenter();
    }

    public void updateStutterStatus(boolean isStuttering) {
        if (isStuttering) {
            mStatusIcon.setVisibility(View.VISIBLE);
            mStatusIcon.setImageResource(R.drawable.game_live_status_reconnecting);
        } else {
            mStatusIcon.setVisibility(View.GONE);
            mStatusIcon.setImageResource(0);
        }
    }

    public boolean isIsWindowShow() {
        return mIsWindowShow;
    }

    public void showWindow() {
        if (mIsWindowShow) {
            return;
        }
        mIsWindowShow = true;
        mWindowManager.addView(this, mFloatLayoutParams);
        onOrientation(false);
    }

    public void removeWindow() {
        if (!mIsWindowShow) {
            return;
        }
        mIsWindowShow = false;
        mWindowManager.removeViewImmediate(this);
    }

    public void setMode(int mode) {
        if (mMode == mode) {
            return;
        }
        mMode = mode;
        if (mMode == MODE_HALF_HIDDEN) {
            int sign = mAlignLeft ? -1 : 1;
            mMainBtn.setTranslationX(sign * ICON_TRANSLATION);
            mMainBtn.setRotation(-sign * ICON_ROTATION);
            mMainBtn.setAlpha(ICON_ALPHA);
        } else {
            mMainBtn.setTranslationX(0);
            mMainBtn.setRotation(0);
            mMainBtn.setAlpha(1.0f);
        }
    }

    private void setupLayoutParams() {
        mFloatLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mFloatLayoutParams.format = PixelFormat.RGBA_8888;
        mFloatLayoutParams.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN // 加入该属性，处理Touch事件移动浮窗时，不需再考虑状态栏
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        mFloatLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mFloatLayoutParams.x = 0;
        mFloatLayoutParams.y = mBoundRect.top + mBoundRect.height() >> 1;
        mFloatLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mFloatLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mFloatLayoutParams.token = getWindowToken();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mTouchEventHelper.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onOrientation(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    public void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        MyLog.w(TAG, "onOrientation isLandscape=" + isLandscape);
        mIsLandscape = isLandscape;
        mGamePresenter.onOrientation(isLandscape);
        if (mIsLandscape) {
            mBoundRect.set(0, 0, mParentHeight, mParentWidth);
            mFloatLayoutParams.y = mBoundRect.top + mBoundRect.height() >> 3;
        } else {
            mBoundRect.set(0, 0, mParentWidth, mParentHeight);
            mFloatLayoutParams.y = mBoundRect.top + mBoundRect.height() >> 1;
        }
        mFloatLayoutParams.x = mAlignLeft ? 0 : (mBoundRect.right - getWidth());
        mWindowManager.updateViewLayout(GameFloatIcon.this, mFloatLayoutParams);
        mGameFloatView.onOrientation(mIsLandscape);
        mGameFloatView.onExitMoveMode(mAlignLeft, mFloatLayoutParams.y);
    }

    public void onEnterDragMode() {
        if (mMode != MODE_NORMAL) {
            return;
        }
        MyLog.w(TAG, "onEnterDragMode");
        mMode = MODE_DRAGGING;
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        // just use this to vibrate
        mUiHandler.removeMessages(GameFloatWindow.MSG_HALF_HIDE_FLOAT_BALL);
        mGameFloatView.onEnterMoveMode();
    }

    public void onEnterMoveMode(boolean isAlignLeft) {
        if (mMode != MODE_DRAGGING) {
            return;
        }
        MyLog.w(TAG, "onEnterMoveMode isAlignLeft=" + isAlignLeft);
        mMode = MODE_MOVING;
        mAlignLeft = isAlignLeft;
        mAnimationHelper.startMoveAnimation();
    }

    public void onExitMoveMode() {
        if (mMode != MODE_MOVING) {
            return;
        }
        MyLog.w(TAG, "onExitMoveMode");
        mMode = MODE_NORMAL;
        mGameFloatView.onExitMoveMode(mAlignLeft, mFloatLayoutParams.y);
    }

    public void adjustForInputShow(int y) {
        if (mFloatLayoutParams.y != y) {
            mFloatLayoutParams.y = y;
            mWindowManager.updateViewLayout(this, mFloatLayoutParams);
        }
    }

    @Override
    public void startGiftAnimator(boolean mainShow, BaseImage image) {
        if (image != null) {
            FrescoWorker.loadImage(mGiftBiv2, image);
        }
        mAnimationHelper.mMainBtnShow = mainShow;
        if (image != null || mainShow) {
            mAnimationHelper.startGiftAnimator();
        }
    }

    public void destroy() {
        if (mPresenter != null) {
            mPresenter.stopPresenter();
        }
        mAnimationHelper.stopAnimation();
    }

    // 面板动画辅助类
    protected class AnimationHelper {
        private int fromX;
        private int toX;
        private ValueAnimator moveAnimator;
        private ValueAnimator showGiftAnimator;
        private ValueAnimator hideGiftAnimator;
        private AnimatorSet giftAnimatorSet;
        private boolean mMainBtnShow = false;

        private boolean animIsRunning() {
            return (giftAnimatorSet != null && giftAnimatorSet.isRunning()) ||
                    (moveAnimator != null && moveAnimator.isRunning());
        }

        private void startGiftAnimator() {
            setupGiftAnimator();
            if (giftAnimatorSet != null && giftAnimatorSet.isRunning()) {
                return;
            }
            if (!mMainBtnShow) {
                showGiftAnimator.setInterpolator(new OvershootInterpolator(1.5f));
            } else {
                showGiftAnimator.setInterpolator(new LinearInterpolator());
            }
            giftAnimatorSet.start();
        }

        private void setupGiftAnimator() {
            if (showGiftAnimator == null) {
                showGiftAnimator = ValueAnimator.ofFloat(0f, 1f);
                showGiftAnimator.setDuration(300);
                showGiftAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float tempScale = (float) animation.getAnimatedValue();
                        if (!mMainBtnShow) {
                            mGiftBiv2.setScaleX(tempScale);
                            mGiftBiv2.setScaleY(tempScale);
                        } else {
                            mMainBtn.setScaleX(tempScale);
                            mMainBtn.setScaleY(tempScale);
                        }

                    }
                });
                showGiftAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        mGiftBiv.setVisibility(View.GONE);
                        mGiftBiv2.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (!mMainBtnShow) {
                            BaseImageView giftView = mGiftBiv;
                            mGiftBiv = mGiftBiv2;
                            mGiftBiv2 = giftView;
                            mGiftBiv.setVisibility(View.VISIBLE);
                            mPresenter.giftShowTimer(4500);
                        } else {
                            mMainBtnShow = false;
                        }
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        if (!mMainBtnShow) {
                            mGiftBiv.setVisibility(View.INVISIBLE);
                            mGiftBiv2.setVisibility(View.VISIBLE);
                            mGiftBiv2.bringToFront();
                        } else {
                            mMainBtn.setVisibility(View.VISIBLE);
                            mMainBtn.bringToFront();
                        }
                    }
                });
            }
            if (hideGiftAnimator == null) {
                hideGiftAnimator = ValueAnimator.ofFloat(1f, 0f);
                hideGiftAnimator.setDuration(100);
                hideGiftAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float tempScale = (float) animation.getAnimatedValue();
                        if (mMainBtn.getVisibility() == View.VISIBLE) {
                            mMainBtn.setScaleX(tempScale);
                            mMainBtn.setScaleY(tempScale);
                        } else {
                            mGiftBiv.setScaleX(tempScale);
                            mGiftBiv.setScaleY(tempScale);
                        }
                    }
                });
                hideGiftAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        mGiftBiv.setVisibility(View.GONE);
                        mGiftBiv2.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (mMainBtn.getVisibility() == View.VISIBLE) {
                            mMainBtn.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
            if (giftAnimatorSet == null) {
                giftAnimatorSet = new AnimatorSet();
                giftAnimatorSet.playSequentially(hideGiftAnimator, showGiftAnimator);
            }
        }

        private void calcMoveParam() {
            fromX = mFloatLayoutParams.x;
            toX = mAlignLeft ? mBoundRect.left : (mBoundRect.right - getWidth());
            MyLog.w(TAG, "calcMoveParam fromX=" + fromX + ", toX=" + toX);
        }

        private void setupMoveAnimation() {
            if (moveAnimator == null) {
                moveAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
                moveAnimator.setDuration(300);
                moveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float ratio = (float) animation.getAnimatedValue();
                        mFloatLayoutParams.x = (int) (fromX + ratio * (toX - fromX));
                        MyLog.d(TAG, "moveAnimator x=" + mFloatLayoutParams.x);
                        mWindowManager.updateViewLayout(GameFloatIcon.this, mFloatLayoutParams);
                    }
                });
                moveAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        MyLog.w(TAG, "moveAnimator onAnimationEnd isAlignLeft=" + mAlignLeft);
                        onExitMoveMode();
                    }
                });
            }
        }

        private void startMoveAnimation() {
            setupMoveAnimation();
            if (!moveAnimator.isRunning()) {
                calcMoveParam();
                moveAnimator.start();
            }
        }

        private void stopAnimation() {
            if (moveAnimator != null && moveAnimator.isStarted()) {
                moveAnimator.cancel();
            }
            if (giftAnimatorSet != null && giftAnimatorSet.isStarted()) {
                giftAnimatorSet.cancel();
            }
            resetViewState();
        }

        private void resetViewState() {
            mGiftBiv.setVisibility(View.GONE);
            mGiftBiv2.setVisibility(View.GONE);
            mMainBtn.setVisibility(View.VISIBLE);
            mMainBtn.setScaleX(1f);
            mMainBtn.setScaleY(1f);
        }
    }

    // 触摸移动辅助类
    protected class TouchEventHelper {
        private float xInView;
        private float yInView;
        private float xDownInScreen;
        private float yDownInScreen;
        private float xInScreen;
        private float yInScreen;

        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    MyLog.i(TAG, "onTouchEvent ACTION_DOWN");
                    xInView = event.getX();
                    yInView = event.getY();
                    xInScreen = xDownInScreen = event.getRawX();
                    yInScreen = yDownInScreen = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    MyLog.i(TAG, "onTouchEvent ACTION_MOVE");
                    if (mMode == MODE_DRAGGING) {
                        xInScreen = event.getRawX();
                        yInScreen = event.getRawY();
                        // 手指移动时更新小悬浮窗的位置
                        updateViewPosition((int) (xInScreen - xInView), (int) (yInScreen - yInView));
                    } else if (mMode == MODE_NORMAL) {
                        xInScreen = event.getRawX();
                        yInScreen = event.getRawY();
                        if (Math.abs(xDownInScreen - xInScreen) > MOVE_THRESHOLD ||
                                Math.abs(yDownInScreen - yInScreen) > MOVE_THRESHOLD) {
                            onEnterDragMode();
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    MyLog.i(TAG, "onTouchEvent ACTION_UP");
                    if (mMode == MODE_DRAGGING) {
                        onEnterMoveMode(xInScreen < mBoundRect.centerX());
                    } else if (mMode != MODE_MOVING) {
                        xInScreen = event.getRawX();
                        yInScreen = event.getRawY();
                        if (Math.abs(xDownInScreen - xInScreen) <= MOVE_THRESHOLD &&
                                Math.abs(yDownInScreen - yInScreen) <= MOVE_THRESHOLD) {
                            // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
                            onMainBtnClick();
                        }
                    }
                    break;
                default:
                    break;
            }
            return true;
        }

        private void updateViewPosition(int x, int y) {
            if (x < mBoundRect.left) {
                x = mBoundRect.left;
            } else if (x > mBoundRect.right - getWidth()) {
                x = mBoundRect.right - getWidth();
            }
            if (y < mBoundRect.top) {
                y = mBoundRect.top;
            } else if (y > mBoundRect.bottom - getHeight()) {
                y = mBoundRect.bottom - getHeight();
            }
            MyLog.d(TAG, "updateViewPosition x=" + x + ", y=" + y);
            mFloatLayoutParams.x = x;
            mFloatLayoutParams.y = y;
            mWindowManager.updateViewLayout(GameFloatIcon.this, mFloatLayoutParams);
        }
    }
}