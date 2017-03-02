package com.wali.live.watchsdk.component.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;
import com.mi.live.data.preference.MLPreferenceUtils;
import com.wali.live.common.smiley.SmileyInputFilter;
import com.wali.live.common.smiley.SmileyPicker;
import com.wali.live.common.smiley.SmileyTranslateFilter;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IOrientationListener;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.watchsdk.auth.AccountAuthManager;

/**
 * Created by yangli on 17/02/20.
 *
 * @module 输入框视图
 */
public class InputAreaView extends LinearLayout implements View.OnClickListener,
        IComponentView<InputAreaView.IPresenter, InputAreaView.IView> {
    private static final String TAG = "InputAreaView";

    private static final int MINIMUM_HEIGHT_PORTRAIT = DisplayUtils.dip2px(38f + 6.67f);
    private static final int MINIMUM_HEIGHT_LANDSCAPE = DisplayUtils.dip2px(6.67f);

    @Nullable
    protected IPresenter mPresenter;
    protected int mKeyboardHeight;

    protected boolean mIsInputMode = false;
    protected boolean mIsShowSmileyPicker = false;
    protected boolean mIsLandscape = false;

    protected InputFilter[] mNormalFilter;
    protected
    @Nullable
    InputFilter[] mFlyBarrageFilter;

    protected View mInputContainer;
    protected View mPlaceHolderContainer; // 用于软件键盘弹出占位和表情选择容器
    protected EditText mInputView;
    protected TextView mSendBtn;
    protected
    @Nullable
    View mBarrageSwitchBtn; // 飘屏弹幕开关

    protected ImageView mShowSmileyBtn;
    protected SmileyPicker mSmileyPicker;

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.send_btn) { // 发送消息
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                String body = mInputView.getText().toString().trim();
                if (!TextUtils.isEmpty(body)) {
                    if (mPresenter != null) {
                        mPresenter.sendBarrage(body, mBarrageSwitchBtn != null ?
                                mBarrageSwitchBtn.isSelected() : false);
                    }
                    mInputView.setText("");
                }
            }
        } else if (id == R.id.show_smiley_btn) { // 显示表情
            if (mIsInputMode) {
                if (mSmileyPicker.isPickerShowed()) {
                    hideSmileyPickerAndShowInputSoft();
                } else {
                    showSmileyPicker();
                }
            }
        } else if (id == R.id.barrage_switch_btn) { // 开启飘屏弹幕
            boolean isSelected = !view.isSelected();
            view.setSelected(isSelected);
            if (isSelected && mFlyBarrageFilter == null) {
                mFlyBarrageFilter = new InputFilter[]{
                        new SmileyTranslateFilter(mInputView.getTextSize()),
                        new SmileyInputFilter(mInputView, 50)
                };
            }
            if (mBarrageSwitchBtn instanceof ImageView) {
                ((ImageView) mBarrageSwitchBtn).setScaleType(isSelected ?
                        ImageView.ScaleType.FIT_END : ImageView.ScaleType.FIT_START);
            }
            mInputView.setFilters(isSelected ? mFlyBarrageFilter : mNormalFilter);
        }
    }

    @Nullable
    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(@Nullable View view, OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    public InputAreaView(Context context) {
        this(context, null);
    }

    public InputAreaView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InputAreaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.input_area_view, this);

        mInputContainer = $(R.id.input_container);
        mPlaceHolderContainer = $(R.id.place_holder_view_container);
        mInputView = $(R.id.input_et);
        mSendBtn = $(R.id.send_btn);

        mShowSmileyBtn = $(R.id.show_smiley_btn);
        mSmileyPicker = $(R.id.smiley_picker);

        $click(mSendBtn, this);
        $click(mShowSmileyBtn, this);

        setMinimumHeight(MINIMUM_HEIGHT_PORTRAIT);
        mInputContainer.setOnClickListener(this); // 吃掉点击事件
        mInputContainer.setSoundEffectsEnabled(false);

        setupInputArea();
    }

    private void enableFlyBarrage(boolean isEnable) {
        if (isEnable) {
            if (mBarrageSwitchBtn == null) {
                mBarrageSwitchBtn = $(R.id.barrage_switch_btn);
                $click(mBarrageSwitchBtn, this);
            }
            if (mBarrageSwitchBtn != null) {
                mBarrageSwitchBtn.setVisibility(View.VISIBLE);
            }
        } else if (mBarrageSwitchBtn != null) {
            mBarrageSwitchBtn.setSelected(false);
            mBarrageSwitchBtn.setVisibility(View.GONE);
        }
    }

    private void setupInputArea() {
        mKeyboardHeight = MLPreferenceUtils.getKeyboardHeight(!mIsLandscape);

        mNormalFilter = new InputFilter[]{
                new SmileyTranslateFilter(mInputView.getTextSize()),
                new SmileyInputFilter(mInputView, 120)
        };
        mInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSendBtn.setEnabled(!TextUtils.isEmpty(mInputView.getText().toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mInputView.setFilters(mNormalFilter);
        mInputView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (null == event) {
                    return false;
                }
                return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        mSmileyPicker.setEditText(mInputView);
    }

    private void adjustPlaceHolder(final int keyboardHeight) {
        MyLog.d(TAG, "adjustPlaceHolder softKeyboardHeight=" + keyboardHeight);
        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();
            View view = activity.getWindow().getDecorView();
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            ViewGroup.LayoutParams layoutParams = mPlaceHolderContainer.getLayoutParams();
            layoutParams.height = keyboardHeight + location[1];
            mPlaceHolderContainer.setLayoutParams(layoutParams);

            postDelayed(new Runnable() {
                @Override
                public void run() {
                    Activity activity = (Activity) getContext();
                    if (activity != null && !activity.isFinishing()) {
                        View view = activity.getWindow().getDecorView();
                        int[] location = new int[2];
                        view.getLocationOnScreen(location);
                        if (location[1] < 0) {
                            ViewGroup.LayoutParams layoutParams = mPlaceHolderContainer.getLayoutParams();
                            layoutParams.height = keyboardHeight + location[1];
                            mPlaceHolderContainer.setLayoutParams(layoutParams);
                        }
                    }
                }
            }, 100);
        } else {
            ViewGroup.LayoutParams layoutParams = mPlaceHolderContainer.getLayoutParams();
            layoutParams.height = keyboardHeight;
            mPlaceHolderContainer.setLayoutParams(layoutParams);
        }
    }

    public boolean showInputView(int keyboardHeight) {
        if (mIsInputMode) {
            MyLog.w(TAG, "showInputView, but mIsInputMode is true");
            return false;
        }
        MyLog.w(TAG, "showInputView softKeyboardHeight=" + keyboardHeight);
        mIsInputMode = true;
        setVisibility(View.VISIBLE);
        mInputContainer.setVisibility(View.VISIBLE);
        mPlaceHolderContainer.setVisibility(View.VISIBLE);
        adjustPlaceHolder(keyboardHeight);
        mShowSmileyBtn.setImageResource(R.drawable.chat_bottom_enter_expression_btn_2);
        mInputView.requestFocus();
        if (mIsLandscape) {
            KeyboardUtils.showKeyboardWithDelay(getContext(), mInputView, 50);
        } else {
            KeyboardUtils.showKeyboard(getContext());
        }
        if (mPresenter != null) {
            mPresenter.notifyInputViewShowed();
        }
        return true;
    }

    public boolean hideInputView() {
        if (!mIsInputMode) {
            MyLog.w(TAG, "hideInputView, but mIsInputMode is false");
            return false;
        }
        MyLog.w(TAG, "hideInputView");
        if (mIsShowSmileyPicker) {
            hideSmileyPicker();
            hideInputViewDirectly();
        } else {
            KeyboardUtils.hideKeyboard((Activity) getContext());
        }
        return true;
    }

    private void hideInputViewDirectly() {
        if (mIsInputMode) {
            MyLog.w(TAG, "hideInputViewDirectly");
            mIsInputMode = false;
            KeyboardUtils.hideKeyboardThenReturnResult((Activity) getContext());
            setVisibility(View.INVISIBLE);
            mInputContainer.setVisibility(View.GONE);
            mPlaceHolderContainer.setVisibility(View.GONE);
            if (mPresenter != null) {
                mPresenter.notifyInputViewHidden();
            }
        }
    }

    private void showSmileyPicker() {
        MyLog.w(TAG, "showSmileyPicker");
        mIsShowSmileyPicker = true;
        KeyboardUtils.hideKeyboard((Activity) getContext());
        mShowSmileyBtn.setImageResource(R.drawable.chat_bottom_enter_expression_keyboard_btn);
        mSmileyPicker.show((Activity) getContext(), -1, 0, null);
    }

    private void hideSmileyPicker() {
        MyLog.w(TAG, "hideSmileyPicker");
        mIsShowSmileyPicker = false;
        mSmileyPicker.hide(null);
    }

    private void hideSmileyPickerAndShowInputSoft() {
        if (!mIsShowSmileyPicker) {
            return;
        }
        MyLog.w(TAG, "hideSmileyPickerAndShowInputSoft");
        mIsShowSmileyPicker = false;
        mShowSmileyBtn.setImageResource(R.drawable.chat_bottom_enter_expression_btn_2);
        KeyboardUtils.showKeyboard(getContext());
        mSmileyPicker.hide(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mIsInputMode && !mIsShowSmileyPicker) {
                    KeyboardUtils.showKeyboard(getContext());
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        MyLog.w(TAG, "onOrientation isLandscape=" + isLandscape);
        mIsLandscape = isLandscape;
        mKeyboardHeight = MLPreferenceUtils.getKeyboardHeight(!isLandscape);
        hideInputView();
        setMinimumHeight(mIsLandscape ? MINIMUM_HEIGHT_LANDSCAPE : MINIMUM_HEIGHT_PORTRAIT);
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
                return (T) InputAreaView.this;
            }

            @Override
            public boolean processBackPress() {
                return InputAreaView.this.hideInputView();
            }

            @Override
            public boolean showInputView() {
                return InputAreaView.this.showInputView(mKeyboardHeight);
            }

            @Override
            public boolean hideInputView() {
                return InputAreaView.this.hideInputView();
            }

            @Override
            public void onKeyboardShowed(int keyboardHeight) {
                if (getVisibility() != View.VISIBLE) {
                    return;
                }
                if (mKeyboardHeight != keyboardHeight) {
                    mKeyboardHeight = keyboardHeight;
                    MyLog.d(TAG, "onKeyboardShowed mKeyboardHeight=" + mKeyboardHeight);
                    MLPreferenceUtils.setKeyboardHeight(mKeyboardHeight, !mIsLandscape);
                    adjustPlaceHolder(mKeyboardHeight);
                }
                hideSmileyPickerAndShowInputSoft();
            }

            @Override
            public void onKeyboardHided() {
                if (getVisibility() != View.VISIBLE) {
                    return;
                }
                if (!mIsShowSmileyPicker) {
                    hideInputViewDirectly();
                }
            }

            @Override
            public void enableFlyBarrage(boolean isEnable) {
                InputAreaView.this.enableFlyBarrage(isEnable);
            }

            @Override
            public void onOrientation(boolean isLandscape) {
                InputAreaView.this.onOrientation(isLandscape);
            }
        }
        return new ComponentView();
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public interface IPresenter {
        /**
         * 发送消息
         */
        void sendBarrage(String msg, boolean isFlyBarrage);

        /**
         * 输入框 已显示
         */
        void notifyInputViewShowed();

        /**
         * 输入框 已隐藏
         */
        void notifyInputViewHidden();
    }

    public interface IView extends IViewProxy, IOrientationListener {
        /**
         * 响应返回键事件
         */
        boolean processBackPress();

        /**
         * 显示输入框
         */
        boolean showInputView();

        /**
         * 隐藏输入框
         */
        boolean hideInputView();

        /**
         * 键盘弹起
         */
        void onKeyboardShowed(int keyboardHeight);

        /**
         * 键盘隐藏
         */
        void onKeyboardHided();

        /**
         * 设置是否显示飘屏弹幕开关按钮
         */
        void enableFlyBarrage(boolean isEnable);
    }
}
