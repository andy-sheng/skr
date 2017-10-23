package com.wali.live.livesdk.live.window;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.log.MyLog;
import com.base.permission.PermissionUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.channel.HostChannelManager;
import com.wali.live.common.barrage.view.LiveCommentView;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.view.GameRepeatScrollView;
import com.wali.live.statistics.StatisticsKey;

import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * Created by yangli on 16-11-29.
 *
 * @module 悬浮窗
 */
public class GameFloatView extends RelativeLayout implements View.OnClickListener {
    private static final String TAG = "GameFloatView";

    private final static int MODE_NORMAL = 0;  // 正常模式，onMainBtnClick时，切换显示/隐藏操作按钮
    private final static int MODE_MESSAGE = 1; // 消息模式，onMainBtnClick时，切换显示操作按钮和聊天标题(或单条弹幕)

    private final int mParentWidth;
    private final int mParentHeight;
    private final Rect mBoundRect;
    private boolean mIsLandscape;

    private final WindowManager mWindowManager;
    private final WindowManager.LayoutParams mFloatLayoutParams;
    private final GameFloatWindow.MyUiHandler mUiHandler;
    private final IGameFloatPresenter mGamePresenter;
    private final AnimationHelper mAnimationHelper;
    private boolean mIsWindowShow = false;
    private boolean mAlignLeft = true;

    private final int mFullWindowHeight;
    private final int mExtraPadding = GameFloatIcon.ICON_WIDTH / 2;
    private boolean mIsExpand = true;    // 是否显示该面板
    private boolean mIsShowTitle = true; // 是否是显示聊天标题
    private boolean mIsShowList = false; // 是否显示聊天列表
    private boolean mIsFocusable = false;
    private int mMode = MODE_MESSAGE;

    LinearLayout mBtnArea;
    View mMuteBtn;
    View mFaceBtn;
    ViewGroup mCommentTitle;
    ViewGroup mListArea;
    LiveCommentView mCommentView;
    View mInputArea;
    EditText mCommentInput;
    View mSendBtn;
    GameRepeatScrollView mGameRepeatScrollView;
    View mTitleFoldLeft;
    View mTitleFoldRight;

    @Override
    public void onClick(View view) {
        int i = view.getId();
        String keyType = "";
        if (i == R.id.home_btn) {
            mGamePresenter.showConfirmDialog(); // 返回控制台
            keyType = StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_CONTROLDESK;
        } else if (i == R.id.screen_shot_btn) {
            mGamePresenter.takeScreenshot(); // 截屏
            keyType = StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_SCREEN;
        } else if (i == R.id.mute_btn) {
            view.setSelected(!view.isSelected()); // 禁音/取消禁音
            mGamePresenter.muteMic(view.isSelected());
            keyType = StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_SILENT;
        } else if (i == R.id.face_btn) {
            if (!view.isSelected()) {
                boolean hasCameraPermission = PermissionUtils.checkCamera(getContext());
                if (!hasCameraPermission) {
                    ToastUtils.showToast(R.string.check_camera_video_message);
                    return;
                }
            }
            view.setSelected(!view.isSelected()); // 露脸/取消露脸
            mGamePresenter.showFace(view.isSelected());
        } else if (i == R.id.comment_btn) {
            enableCommentArea(view.isSelected());  // 聊天
            view.setSelected(!view.isSelected());
        } else if (i == R.id.title_fold_left || i == R.id.title_fold_right) {
            showCommentList(!mIsShowList);
        } else if (i == R.id.send_btn) {
            onSendBtnClick();
        }
        if (!TextUtils.isEmpty(keyType)) {
            StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                    String.format(keyType, HostChannelManager.getInstance().getChannelId()),
                    TIMES, "1");
        }

    }

    private void enableCommentArea(boolean enable) {
        if (mMode == MODE_NORMAL && enable) {
            mGameRepeatScrollView.forbidReceiveComment(false);
            mMode = MODE_MESSAGE;
            showCommentTitle(true);
            StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                    String.format(StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_FREEZE, StatisticsKey.KEY_LIVESDK_FREEZE_UNFIXED,
                            HostChannelManager.getInstance().getChannelId()),
                    TIMES, "1");
        } else if (mMode == MODE_MESSAGE && !enable) {
            mGameRepeatScrollView.forbidReceiveComment(true);
            mMode = MODE_NORMAL;
            showCommentList(false);
            StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                    String.format(StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_FREEZE, StatisticsKey.KEY_LIVESDK_FREEZE_FIXED,
                            HostChannelManager.getInstance().getChannelId()),
                    TIMES, "1");
        }
    }

    private void showCommentTitle(boolean isShow) {
        if (mIsShowTitle == isShow) {
            return;
        }
        mIsShowTitle = isShow;
        mAnimationHelper.startTitleAnimation(mIsShowTitle);
    }

    private void showCommentList(boolean isShow) {
        if (mIsShowList == isShow) {
            return;
        }
        mIsShowList = isShow;
        mTitleFoldLeft.setRotation(mIsShowList ? 0 : 180);
        mTitleFoldRight.setRotation(mIsShowList ? 0 : 180);
        mAnimationHelper.startListAnimation(mIsShowList);
    }

    private void onSendBtnClick() {
        String msg = mCommentInput.getText().toString().trim();
        if (!TextUtils.isEmpty(msg)) {
            mGamePresenter.sendBarrage(msg, false);
            mCommentInput.setText("");
        }
    }

    private boolean disableFocusable() {
        if (mIsFocusable) {
            MyLog.w(TAG, "disableFocusable");
            mIsFocusable = false;
            mFloatLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mWindowManager.updateViewLayout(this, mFloatLayoutParams);
            return true;
        } else {
            return false;
        }
    }

    private boolean enableFocusable() {
        if (!mIsFocusable) {
            MyLog.w(TAG, "enableFocusable");
            mIsFocusable = true;
            mFloatLayoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mWindowManager.updateViewLayout(this, mFloatLayoutParams);
            return true;
        } else {
            return false;
        }
    }

    private <T> T $(int id) {
        return (T) findViewById(id);
    }

    private void $click(int id, OnClickListener listener) {
        findViewById(id).setOnClickListener(listener);
    }

    public GameFloatView(
            @NonNull Context context,
            @NonNull WindowManager windowManager,
            @NonNull GameFloatWindow.MyUiHandler uiHandler,
            @NonNull IGameFloatPresenter gamePresenter,
            int parentWidth,
            int parentHeight,
            String token,
            int viewerCnt) {
        super(context);
        inflate(context, R.layout.game_float_view, this);

        mBtnArea = $(R.id.btn_area);
        mMuteBtn = $(R.id.mute_btn);
        mFaceBtn = $(R.id.face_btn);
        mCommentTitle = $(R.id.comment_title);
        mListArea = $(R.id.comment_list_area);
        mCommentView = $(R.id.comment_view);
        mInputArea = $(R.id.input_area);
        mCommentInput = $(R.id.comment_input);
        mSendBtn = $(R.id.send_btn);
        mGameRepeatScrollView = $(R.id.game_repeat_sv);
        mTitleFoldLeft = $(R.id.title_fold_left);
        mTitleFoldRight = $(R.id.title_fold_right);

        $click(R.id.home_btn, this);
        $click(R.id.screen_shot_btn, this);
        $click(R.id.mute_btn, this);
        $click(R.id.comment_btn, this);
        $click(R.id.title_fold_left, this);
        $click(R.id.title_fold_right, this);
        $click(R.id.send_btn, this);
        $click(R.id.face_btn, this);

        mParentWidth = parentWidth;
        mParentHeight = parentHeight;
        mBoundRect = new Rect(0, 0, mParentWidth, mParentHeight);

        // setVisibility(View.GONE);
        setSoundEffectsEnabled(false);
        MarginLayoutParams layoutParams = (MarginLayoutParams) mListArea.getLayoutParams();
        mFullWindowHeight = GameFloatIcon.WINDOW_PADDING + layoutParams.topMargin + layoutParams.width;

        mWindowManager = windowManager;
        mUiHandler = uiHandler;
        mGamePresenter = gamePresenter;
        mFloatLayoutParams = new WindowManager.LayoutParams();
        mAnimationHelper = new AnimationHelper();
        setupLayoutParams();
        mCommentView.setToken(token);
        mGameRepeatScrollView.setToken(token);
        mGameRepeatScrollView.setViewerCnt(viewerCnt);
        mCommentView.setIsGameLive(true);
        findViewById(R.id.comment_btn).setSelected(mMode == MODE_NORMAL);
        alignBtnArea();
        alignCommentTitle();

        mCommentInput.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    enableFocusable(); // 点击输入区域时，设置为获取焦点，弹起键盘
                }
                return false;
            }
        });
        mCommentInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (null == event) {
                    return false;
                }
                return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        mCommentInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mSendBtn.setEnabled(!mCommentInput.getText().toString().equals(""));
            }
        });
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
        mMuteBtn.setSelected(mGamePresenter.isMuteMic());
        mFaceBtn.setSelected(mGamePresenter.isShowFace());
    }

    public void removeWindow() {
        if (!mIsWindowShow) {
            return;
        }
        mIsWindowShow = false;
        mWindowManager.removeViewImmediate(this);
    }

    public void register() {
        mGameRepeatScrollView.register();
        mCommentView.onActivityCreate();
    }

    public void unregister() {
        mGameRepeatScrollView.unregister();
        mCommentView.onActivityDestroy();
    }

    private void setupLayoutParams() {
        mFloatLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mFloatLayoutParams.format = PixelFormat.RGBA_8888;
        mFloatLayoutParams.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN; // 加入该属性，处理Touch事件移动浮窗时，不需再考虑状态栏
        mFloatLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mFloatLayoutParams.x = 0;
        mFloatLayoutParams.y = mBoundRect.top + mBoundRect.height() >> 1;
        mFloatLayoutParams.windowAnimations = 0;
        mFloatLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mFloatLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mFloatLayoutParams.token = getWindowToken();
        mFloatLayoutParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            MyLog.d(TAG, "onTouchEvent Test x=" + event.getX() + ", y=" + event.getY());
            int x = (int) event.getX(), y = (int) event.getY();
            if (x < 0 || x > getWidth() || y < 0 || y > getHeight()) {
                disableFocusable(); // 点击窗口之外的区域时，设置为不获取焦点
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // 不处理back键
//        if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//            MyLog.d(TAG, "onKeyDown Test keyCode=" + event.getKeyCode());
//            showCommentList(false);
//            return true;
//        }
        return super.dispatchKeyEvent(event);
    }

    public void onMainBtnClick() {
        MyLog.w(TAG, "onMainBtnClick");
        if (mMode == MODE_NORMAL) {
            mUiHandler.removeMessages(GameFloatWindow.MSG_HALF_HIDE_FLOAT_BALL);
            mIsExpand = !mIsExpand;
            if (!mIsExpand) { // 若退回只有小浮窗的模式，则启动超时半隐藏小浮窗
                mUiHandler.sendEmptyMessageDelayed(GameFloatWindow.MSG_HALF_HIDE_FLOAT_BALL,
                        GameFloatWindow.TIME_HIDE_GAME_FLOAT_BALL);
            }
            mAnimationHelper.startAnimation(mIsExpand);
        } else {
            if (!mIsExpand) {
                mIsExpand = true;
                mUiHandler.removeMessages(GameFloatWindow.MSG_HALF_HIDE_FLOAT_BALL);
            }
            showCommentTitle(!mIsShowTitle);
        }
    }

    public void onEnterMoveMode() {
        MyLog.w(TAG, "onEnterMoveMode");
        if (mIsExpand) {
            setVisibility(View.GONE);
        }
    }

    private void alignCommentTitle() {
        if (mAlignLeft) {
            mTitleFoldLeft.setVisibility(View.GONE);
            mTitleFoldRight.setVisibility(View.VISIBLE);
            mCommentTitle.setPadding(mExtraPadding, 0, 0, 0);
        } else {
            mTitleFoldLeft.setVisibility(View.VISIBLE);
            mTitleFoldRight.setVisibility(View.GONE);
            mCommentTitle.setPadding(0, 0, mExtraPadding, 0);
        }
    }

    private void alignBtnArea() {
        mBtnArea.setPadding(mExtraPadding, 0, 0, 0);
        if (mAlignLeft) {
            mBtnArea.setRotationY(0);
            setPadding(mExtraPadding, GameFloatIcon.WINDOW_PADDING, 0, GameFloatIcon.WINDOW_PADDING);
        } else {
            mBtnArea.setRotationY(180);
            setPadding(0, GameFloatIcon.WINDOW_PADDING, mExtraPadding, GameFloatIcon.WINDOW_PADDING);
        }
        int btnRotationY = mAlignLeft ? 0 : 180;
        for (int i = mBtnArea.getChildCount() - 1; i >= 0; --i) {
            View view = mBtnArea.getChildAt(i);
            view.setRotationY(btnRotationY);
        }
    }

    public void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        MyLog.w(TAG, "onOrientation isLandscape=" + isLandscape);
        mIsLandscape = isLandscape;
        if (mIsLandscape) {
            mBoundRect.set(0, 0, mParentHeight, mParentWidth);
        } else {
            mBoundRect.set(0, 0, mParentWidth, mParentHeight);
        }
    }

    public void onExitMoveMode(boolean isAlignLeft, int y) {
        MyLog.w(TAG, "onExitMoveMode");
        if (mIsShowList) {
            int maxY = Math.max(mBoundRect.top, mBoundRect.bottom - mFullWindowHeight);
            if (y > maxY) {
                y = maxY;
                mGamePresenter.adjustIconForInputShow(y);
            }
        }
        if (mAlignLeft != isAlignLeft) {
            mAlignLeft = isAlignLeft;
            alignBtnArea();
            alignCommentTitle();
            mFloatLayoutParams.y = y;
            mFloatLayoutParams.gravity = Gravity.TOP | (mAlignLeft ? Gravity.LEFT : Gravity.RIGHT);
            mWindowManager.updateViewLayout(this, mFloatLayoutParams);
        } else if (mFloatLayoutParams.y != y) {
            mFloatLayoutParams.y = y;
            mWindowManager.updateViewLayout(this, mFloatLayoutParams);
        }
        if (mIsExpand) {
            if (mIsShowTitle) {
                mAnimationHelper.startTitleAnimation(true);
            } else {
                mAnimationHelper.startAnimation(true);
            }
            if (mIsShowList) {
                mAnimationHelper.startListAnimation(true);
            }
        } else {
            mUiHandler.removeMessages(GameFloatWindow.MSG_HALF_HIDE_FLOAT_BALL);
            mUiHandler.sendEmptyMessageDelayed(GameFloatWindow.MSG_HALF_HIDE_FLOAT_BALL,
                    GameFloatWindow.TIME_HIDE_GAME_FLOAT_BALL);
        }
    }

    // 面板动画辅助类
    protected class AnimationHelper {
        private boolean isExpand = true;
        private ValueAnimator expandAnimator;

        private void setupAnimation() {
            if (expandAnimator == null) {
                expandAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
                expandAnimator.setDuration(300);
                expandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float ratio = (float) animation.getAnimatedValue();
                        if (!isExpand) {
                            ratio = 1 - ratio;
                        }
                        mBtnArea.setAlpha(ratio);
                        for (int i = mBtnArea.getChildCount() - 1; i >= 0; --i) {
                            View view = mBtnArea.getChildAt(i);
                            view.setTranslationX((ratio - 1) * view.getRight());
                        }
                        MyLog.d(TAG, "expandAnimator alpha=" + ratio + ", isExpand=" + isExpand);
                    }
                });
                expandAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        MyLog.w(TAG, "expandAnimator onAnimationStart isExpand=" + isExpand + ", alignLeft=" + mAlignLeft);
                        if (isExpand) {
                            mBtnArea.setAlpha(0.0f);
                            setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        MyLog.w(TAG, "expandAnimator onAnimationEnd isExpand=" + isExpand + ", alignLeft=" + mAlignLeft);
                        if (!isExpand) {
                            setVisibility(View.GONE);
                        }
                    }
                });
            }
        }

        public void startAnimation(boolean isExpand) {
            setupAnimation();
            this.isExpand = isExpand;
            if (!expandAnimator.isRunning()) {
                expandAnimator.start();
            }
        }

        public void stopAnimation() {
            if (expandAnimator != null && expandAnimator.isStarted()) {
                expandAnimator.cancel();
            }
        }

        private boolean isShowTitle;
        private ValueAnimator titleAnimator;

        private void setupTitleAnimation() {
            if (titleAnimator == null) {
                titleAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
                titleAnimator.setDuration(300);
                titleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float ratio = (float) animation.getAnimatedValue();
                        if (!isShowTitle) {
                            ratio = 1 - ratio;
                        }
                        mBtnArea.setAlpha(1 - ratio);
                        for (int i = mBtnArea.getChildCount() - 1; i >= 0; --i) {
                            View view = mBtnArea.getChildAt(i);
                            view.setTranslationX(-ratio * getRight());
                        }
                        mCommentTitle.setAlpha(ratio);
                        MyLog.d(TAG, "titleAnimator alpha=" + (1 - ratio) + ", isShowTitle=" + isShowTitle);
                    }
                });
                titleAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        MyLog.w(TAG, "titleAnimator onAnimationStart isShowTitle=" + isShowTitle);
                        if (isShowTitle) {
                            mCommentTitle.setAlpha(0.0f);
                            mCommentTitle.setVisibility(View.VISIBLE);
                            setVisibility(View.VISIBLE);
                        } else {
                            mBtnArea.setAlpha(0.0f);
                            mBtnArea.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        MyLog.w(TAG, "titleAnimator onAnimationEnd isShowTitle=" + isShowTitle);
                        if (isShowTitle) {
                            mBtnArea.setVisibility(View.GONE);
                        } else {
                            mCommentTitle.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        }

        public void startTitleAnimation(boolean isShowTitle) {
            setupTitleAnimation();
            titleAnimator.cancel();
            this.isShowTitle = isShowTitle;
            titleAnimator.start();
        }

        public void stopTitleAnimation() {
            if (titleAnimator != null && titleAnimator.isStarted()) {
                titleAnimator.cancel();
            }
        }

        private boolean isShowList;
        private ValueAnimator listAnimator;

        private void setupListAnimation() {
            if (listAnimator == null) {
                listAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
                listAnimator.setDuration(300);
                listAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float ratio = (float) animation.getAnimatedValue();
                        if (!isShowList) {
                            ratio = 1 - ratio;
                        }
                        mListArea.setAlpha(ratio);
                        mCommentView.setTranslationY((ratio - 1) * mListArea.getHeight());
                        mInputArea.setTranslationY((ratio - 1) * mListArea.getHeight());
                    }
                });
                listAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        MyLog.w(TAG, "listAnimator onAnimationStart isShowList=" + isShowList);
                        if (isShowList) {
                            mListArea.setAlpha(0.0f);
                            mListArea.setVisibility(View.VISIBLE);
                            int y = Math.max(mBoundRect.top, mBoundRect.bottom - mFullWindowHeight);
                            if (mFloatLayoutParams.y > y) {
                                mFloatLayoutParams.y = y;
                                mWindowManager.updateViewLayout(GameFloatView.this, mFloatLayoutParams);
                                mGamePresenter.adjustIconForInputShow(y);
                            }
                            mGameRepeatScrollView.setChatRoomMode(false);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        MyLog.w(TAG, "listAnimator onAnimationEnd isShowList=" + isShowList);
                        if (!isShowList) {
                            mGameRepeatScrollView.setChatRoomMode(true);
                            mListArea.setVisibility(View.GONE);
                            disableFocusable(); // 评论列表区域被隐藏时，设置为不获取焦点
                        }
                    }
                });
            }
        }

        public void startListAnimation(boolean isShowList) {
            setupListAnimation();
            listAnimator.cancel();
            this.isShowList = isShowList;
            listAnimator.start();
        }

        public void stopListAnimation() {
            if (listAnimator != null && listAnimator.isStarted()) {
                listAnimator.cancel();
            }
        }
    }
}