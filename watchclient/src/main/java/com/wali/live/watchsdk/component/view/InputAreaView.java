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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;
import com.mi.live.data.preference.MLPreferenceUtils;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IOrientationListener;
import com.wali.live.common.smiley.SmileyInputFilter;
import com.wali.live.common.smiley.SmileyPicker;
import com.wali.live.common.smiley.SmileyTranslateFilter;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.presenter.InputPresenter;

import static android.widget.RelativeLayout.RIGHT_OF;
import static com.wali.live.watchsdk.component.viewmodel.BarrageState.BARRAGE_MANAGE;
import static com.wali.live.watchsdk.component.viewmodel.BarrageState.BARRAGE_NORMAL;
import static com.wali.live.watchsdk.component.viewmodel.BarrageState.BARRAGE_NOTIFY;

/**
 * Created by yangli on 17/02/20.
 *
 * @module 输入框视图
 */
public class InputAreaView extends LinearLayout implements View.OnClickListener,
        IComponentView<InputAreaView.IPresenter, InputAreaView.IView> {
    private static final String TAG = "InputAreaView";

    private static final int MINIMUM_HEIGHT_PORTRAIT = DisplayUtils.dip2px(38f + 6.67f);
    private int mMinHeightLand = DisplayUtils.dip2px(6.67f);

    @Nullable
    protected IPresenter mPresenter;
    protected int mKeyboardHeight;

    protected boolean mIsInputMode = false;
    protected boolean mIsShowSmileyPicker = false;
    protected boolean mIsLandscape = false;

    protected int mState = BARRAGE_NORMAL;

    protected InputFilter[] mNormalFilter;
    @Nullable
    protected InputFilter[] mFlyBarrageFilter;

    protected View mInputContainer;
    protected View mPlaceHolderContainer; // 用于软件键盘弹出占位和表情选择容器
    protected EditText mInputView;
    protected TextView mSendBtn;
    @Nullable
    protected View mBarrageSwitchBtn; // 飘屏弹幕开关
    @Nullable
    protected BarrageSelectView mBarrageSelectBtn;

    protected ImageView mShowSmileyBtn;
    protected SmileyPicker mSmileyPicker;
    protected boolean mIsKeyboardShowed = false;

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.send_btn) { // 发送消息
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                String body = mInputView.getText().toString().trim();
                if (!TextUtils.isEmpty(body)) {
                    if (mPresenter != null) {
                        mPresenter.sendBarrage(body, mState);
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
            mState = isSelected ? BARRAGE_NOTIFY : BARRAGE_NORMAL;
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
            mPresenter.updateInputHint(mState);
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
        mBarrageSwitchBtn = $(R.id.barrage_switch_btn);
        mBarrageSelectBtn = $(R.id.barrage_select_btn);

        mBarrageSelectBtn.setListener(new BarrageSelectView.IBtnChangeListener() {
            @Override
            public void onChange(int status) {
                mState = status;
                switch (mState) {
                    case BARRAGE_NORMAL:
                        mInputView.setFilters(mNormalFilter);
                        mPresenter.updateInputHint(mState);
                        break;
                    case BARRAGE_MANAGE:
                    case BARRAGE_NOTIFY:
                        if (mFlyBarrageFilter == null) {
                            mFlyBarrageFilter = new InputFilter[]{
                                    new SmileyTranslateFilter(mInputView.getTextSize()),
                                    new SmileyInputFilter(mInputView, 50)};
                        }
                        mInputView.setFilters(mFlyBarrageFilter);
                        mPresenter.updateInputHint(mState);
                        break;
                }
            }
        });

        $click(mBarrageSwitchBtn, this);
        $click(mSendBtn, this);
        $click(mShowSmileyBtn, this);
        $click(mBarrageSelectBtn, this);

        setMinimumHeight(MINIMUM_HEIGHT_PORTRAIT);
        mInputContainer.setOnClickListener(this); // 吃掉点击事件
        mInputContainer.setSoundEffectsEnabled(false);

        setupInputArea();
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
        } else if (!mIsKeyboardShowed) {
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
            adjustPlaceHolder(0);
            mInputContainer.setVisibility(View.GONE);
            mPlaceHolderContainer.setVisibility(View.GONE);
            if (mPresenter != null) {
                mPresenter.notifyInputViewHidden();
            }
            if (mBarrageSelectBtn.isOpen()) {
                mBarrageSelectBtn.hidePopWindow();
            }
        }
    }

    private void showSmileyPicker() {
        MyLog.w(TAG, "showSmileyPicker");
        mIsShowSmileyPicker = true;
        KeyboardUtils.hideKeyboard((Activity) getContext());
        mShowSmileyBtn.setImageResource(R.drawable.chat_bottom_enter_expression_keyboard_btn);
        mSmileyPicker.show();
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
        if (mPresenter != null) {
            mMinHeightLand = mPresenter.getMinHeightLand();
        }
        setMinimumHeight(mIsLandscape ? mMinHeightLand : MINIMUM_HEIGHT_PORTRAIT);
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
            public boolean isInputViewShowed() {
                return mIsInputMode;
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
            public void setHint(String hint) {
                mInputView.setHint(hint);
            }

            @Override
            public void enableBarrageSelectView(boolean enable) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mInputView.getLayoutParams();
                if (enable) {
                    mBarrageSwitchBtn.setVisibility(View.GONE);
                    mBarrageSelectBtn.setVisibility(View.VISIBLE);
                    lp.addRule(RIGHT_OF, mBarrageSelectBtn.getId());
                } else {
                    mBarrageSwitchBtn.setVisibility(View.VISIBLE);
                    mBarrageSelectBtn.setVisibility(View.GONE);
                    lp.addRule(RIGHT_OF, mBarrageSwitchBtn.getId());
                }
            }

            @Override
            public void onKeyboardShowed(int keyboardHeight) {
                if (getVisibility() != View.VISIBLE) {
                    return;
                }
                mIsKeyboardShowed = true;
                if (mKeyboardHeight != keyboardHeight) {
                    mKeyboardHeight = keyboardHeight;
                    MyLog.d(TAG, "onKeyboardShowed mKeyboardHeight=" + mKeyboardHeight);
                    MLPreferenceUtils.setKeyboardHeight(mKeyboardHeight, !mIsLandscape);
                    adjustPlaceHolder(mKeyboardHeight);
                }
                hideSmileyPickerAndShowInputSoft();
            }

            @Override
            public void onKeyboardHidden() {
                if (getVisibility() != View.VISIBLE) {
                    return;
                }
                mIsKeyboardShowed = false;
                if (!mIsShowSmileyPicker) {
                    hideInputViewDirectly();
                }
            }

            @Override
            public void onOrientation(boolean isLandscape) {
                InputAreaView.this.onOrientation(isLandscape);
            }

            @Override
            public EditText getInputView() {
                return mInputView;
            }
        }
        return new ComponentView();
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public interface IPresenter extends InputPresenter.IPresenter {
        /**
         * 输入框 已显示
         */
        void notifyInputViewShowed();

        /**
         * 输入框 已隐藏
         */
        void notifyInputViewHidden();

        /**
         * 得到横屏弹幕最小高度
         */
        int getMinHeightLand();

        /**
         * 更新EditText的hint
         */
        void updateInputHint(int state);
    }

    public interface IView extends InputPresenter.IView, IOrientationListener {
        /**
         * 响应返回键事件
         */
        boolean processBackPress();

        /**
         * 输入框 是否已显示
         */
        boolean isInputViewShowed();

        /**
         * 显示输入框
         */
        boolean showInputView();

        /**
         * 隐藏输入框
         */
        boolean hideInputView();

        /**
         * 设置提示
         */
        void setHint(String hint);

        /**
         * 显示管理员消息入口
         * 1. 普通发言 2. 管理员无限飘屏 3. 大喇叭消息
         */
        void enableBarrageSelectView(boolean enable);
    }
}
