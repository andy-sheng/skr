package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.log.MyLog;
import com.mi.live.data.preference.MLPreferenceUtils;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IOrientationListener;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;

/**
 * Created by yangli on 2017/02/28.
 *
 * @module 游戏直播输入框视图, 观看
 */
public class GameInputView extends RelativeLayout implements View.OnClickListener,
        IComponentView<GameInputView.IPresenter, GameInputView.IView> {
    private static final String TAG = "GameInputView";

    private final static int MAX_LEN = 30;

    @Nullable
    protected IPresenter mPresenter;

    protected int mKeyboardHeight;
    protected boolean mIsInputMode = false;

    protected EditText mInputView;
    protected View mSendBtn;
    protected View mBarrageBtn;

    // Auto-generated to easy use findViewById
    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    // Auto-generated to easy use setOnClickListener
    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.send_btn) { // 发送消息
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                String body = mInputView.getText().toString().trim();
                if (!TextUtils.isEmpty(body)) {
                    if (mPresenter != null) {
                        mPresenter.sendBarrage(body, false);
                    }
                    mInputView.setText("");
                }
            }
        } else if (id == R.id.barrage_btn) { // 是否显示弹幕
            boolean isSelected = !view.isSelected();
            view.setSelected(isSelected);
            if (mPresenter != null) {
                mPresenter.showGameBarrage(!isSelected);
            }
        }
    }

    public GameInputView(Context context) {
        this(context, null);
    }

    public GameInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.game_input_view, this);

        mInputView = $(R.id.input_et);
        mSendBtn = $(R.id.send_btn);
        mBarrageBtn = $(R.id.barrage_btn);
        $click(mSendBtn, this);
        $click(mBarrageBtn, this);

        setupInputView();
    }

    private void setupInputView() {
        mKeyboardHeight = MLPreferenceUtils.getKeyboardHeight(false);
        if (mKeyboardHeight <= 0) {
            mKeyboardHeight = Integer.MAX_VALUE;
        }

        mInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mInputView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (null == event) {
                    return false;
                }
                return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Nullable
            @Override
            public <T extends View> T getRealView() {
                return (T) GameInputView.this;
            }

            @Override
            public boolean processBackPress() {
                return false;
            }

            @Override
            public void onKeyboardShowed(int keyboardHeight) {
                MyLog.d(TAG, "onKeyboardShowed keyboardHeight=" + keyboardHeight);
                if (!mIsInputMode) {
                    mIsInputMode = true;
                }
                if (mKeyboardHeight != keyboardHeight) {
                    mKeyboardHeight = keyboardHeight;
                    mKeyboardHeight = MLPreferenceUtils.getKeyboardHeight(false);
                }
                setTranslationY(-mKeyboardHeight);
                MyLog.d(TAG, "setTranslationY " + -mKeyboardHeight);
            }

            @Override
            public void onKeyboardHidden() {
                MyLog.d(TAG, "onKeyboardHidden");
                if (mIsInputMode) {
                    mIsInputMode = false;
                }
                setTranslationY(0);
            }

            @Override
            public void onOrientation(boolean isLandscape) {
                if (isLandscape) {
                    GameInputView.this.setVisibility(View.VISIBLE);
                    if (mPresenter != null && !mBarrageBtn.isSelected()) {
                        mPresenter.showGameBarrage(true);
                    }
                } else {
                    GameInputView.this.setVisibility(View.GONE);
                    if (mPresenter != null && !mBarrageBtn.isSelected()) {
                        mPresenter.showGameBarrage(false);
                    }
                }
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 发送消息
         */
        void sendBarrage(String msg, boolean isFlyBarrage);

        /**
         * 显示游戏直播样式弹幕
         */
        void showGameBarrage(boolean isShow);
    }

    public interface IView extends IViewProxy, IOrientationListener {
        /**
         * 响应返回键事件
         */
        boolean processBackPress();

        /**
         * 键盘弹起
         */
        void onKeyboardShowed(int keyboardHeight);

        /**
         * 键盘隐藏
         */
        void onKeyboardHidden();
    }
}
