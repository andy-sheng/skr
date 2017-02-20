package com.wali.live.livesdk.live.window.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.base.log.MyLog;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.window.GameFloatWindow;
import com.wali.live.livesdk.live.window.IGameFloatPresenter;

/**
 * Created by yangli on 2016/12/13.
 *
 * @author YangLi
 * @mail yanglijd@gmail.com
 */
public class GameConfirmDialog extends LinearLayout implements View.OnClickListener {
    private static final String TAG = "GameConfirmDialog";

    private final WindowManager mWindowManager;
    private final WindowManager.LayoutParams mFloatLayoutParams;
    private final IGameFloatPresenter mGamePresenter;

    private final AnimationHelper mAnimationHelper;
    private boolean mIsShow = false;

    View mRootContainer;

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.root_container) {
            return;
        } else if (i == R.id.ok_btn) {
            mGamePresenter.backToApp();
        }
        dismissDialog();
    }

    public WindowManager.LayoutParams getWindowLayoutParams() {
        return mFloatLayoutParams;
    }

    public GameConfirmDialog(
            @NonNull Context context,
            @NonNull WindowManager windowManager,
            @NonNull IGameFloatPresenter gamePresenter) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.game_confirm_dialog, this, true);

        mRootContainer = findViewById(R.id.root_container);
        mRootContainer.setOnClickListener(this);
        findViewById(R.id.cancel_btn).setOnClickListener(this);
        findViewById(R.id.ok_btn).setOnClickListener(this);

        setBackgroundColor(getResources().getColor(R.color.color_black_trans_60));
        setPadding(GameFloatWindow.DIALOG_PADDING, GameFloatWindow.DIALOG_PADDING,
                GameFloatWindow.DIALOG_PADDING, GameFloatWindow.DIALOG_PADDING);
        setGravity(Gravity.BOTTOM);
        setOnClickListener(this);

        mWindowManager = windowManager;
        mGamePresenter = gamePresenter;
        mAnimationHelper =  new AnimationHelper();
        mFloatLayoutParams = new WindowManager.LayoutParams();
        setupLayoutParams();
    }

    private void setupLayoutParams() {
        mFloatLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mFloatLayoutParams.format = PixelFormat.RGBA_8888;
        mFloatLayoutParams.flags =  WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN // 加入该属性，处理Touch事件移动浮窗时，不需再考虑状态栏
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        mFloatLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mFloatLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mFloatLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mFloatLayoutParams.token = getWindowToken();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            dismissDialog();
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    public void showDialog() {
        if (mIsShow) {
            return;
        }
        mIsShow = true;
        mAnimationHelper.startAnimation(true);
    }

    public void dismissDialog() {
        if (!mIsShow) {
            return;
        }
        mIsShow = false;
        mAnimationHelper.startAnimation(false);
    }

    // 面板动画辅助类
    protected class AnimationHelper {
        private ValueAnimator moveAnimator;
        private boolean isShow = false;

        private void setupAnimation() {
            if (moveAnimator == null) {
                moveAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
                moveAnimator.setDuration(300);
                moveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float ratio = (float) animation.getAnimatedValue();
                        if (!isShow) {
                            ratio = 1 - ratio;
                        }
                        setAlpha(ratio);
                        mRootContainer.setAlpha(ratio);
                        mRootContainer.setTranslationY((1 - ratio) * mRootContainer.getHeight());
                    }
                });
                moveAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        MyLog.w(TAG, "moveAnimator onAnimationStart");
                        if (isShow) {
                            setAlpha(0.0f);
                            mRootContainer.setAlpha(0.0f);
                            mWindowManager.addView(GameConfirmDialog.this, mFloatLayoutParams);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        MyLog.w(TAG, "moveAnimator onAnimationEnd");
                        if (!isShow) {
                            mWindowManager.removeViewImmediate(GameConfirmDialog.this);
                        }
                    }
                });
            }
        }

        public void startAnimation(boolean isShow) {
            setupAnimation();
            this.isShow = isShow;
            if (!moveAnimator.isRunning()) {
                moveAnimator.start();
            }
        }

        public void stopAnimation() {
            if (moveAnimator != null && moveAnimator.isStarted()) {
                moveAnimator.cancel();
            }
        }
    }
}
