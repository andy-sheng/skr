package com.wali.live.watchsdk.watch.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.event.KeyboardEvent;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.preference.MLPreferenceUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Star on 16/12/15.
 */
@Deprecated
public class BottomGameInputView extends RelativeLayout {
    private final static String TAG = "BottomGameInputView";
    private final static int MAX_LEN = 30;

    private int mSoftKeyboardHeight = 0;
    private IBottomGameInputView mListener;

    ImageView mBarrageBtn;

    EditText mEditText;

    View mViewPlaceholder;

    TextView mSendBtn;

    public BottomGameInputView(Context context) {
        this(context, null);
    }

    public BottomGameInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setListener(IBottomGameInputView listener) {
        this.mListener = listener;
    }

    public BottomGameInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.bottom_game_input_view, this);
        init();
    }

    BarrageBtnClickListener mBarrageBtnClickListener;

    public void setBarrageBtnClickListener(BarrageBtnClickListener l) {
        mBarrageBtnClickListener = l;
    }

    public interface BarrageBtnClickListener {
        void onSwitch(boolean open);
    }

    boolean mBarrageOpen = true;

    private void init() {
        mBarrageBtn = (ImageView) findViewById(R.id.barrage_btn);
        mEditText = (EditText) findViewById(R.id.game_input_area);
        mViewPlaceholder = findViewById(R.id.view_placeholder);
        mSendBtn = (TextView) findViewById(R.id.send_btn);

        // 用户点击了输入栏
        mViewPlaceholder.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutParams layoutParams = (LayoutParams) BottomGameInputView.this.getLayoutParams();
                layoutParams.bottomMargin = mSoftKeyboardHeight;
                BottomGameInputView.this.setLayoutParams(layoutParams);
                KeyboardUtils.showKeyboardWithDelay(getContext(), mEditText, 50);
                mViewPlaceholder.setVisibility(View.GONE);
                mEditText.requestFocus();
                if (mListener != null) {
                    mListener.onGameInputAreaClick();
                }
            }
        });
        mBarrageBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBarrageOpen = !mBarrageOpen;
                if (mBarrageBtnClickListener != null) {
                    mBarrageBtnClickListener.onSwitch(mBarrageOpen);
                }
                if (!mBarrageOpen) {
                    ToastUtils.showToast(getContext(), getContext().getString(R.string.barrage_close));
                    mBarrageBtn.setBackgroundResource(R.drawable.bg_close_barrage);
                } else {
                    ToastUtils.showToast(getContext(), getContext().getString(R.string.barrage_open));
                    mBarrageBtn.setBackgroundResource(R.drawable.bg_open_barrage);
                }

            }
        });
        mSendBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                    String msg = mEditText.getText().toString();
                    if (TextUtils.isEmpty(msg)) {
                        return;
                    }
                    mListener.onSendClick(msg);
                    mEditText.setText("");
                }
            }
        });
        mSoftKeyboardHeight = MLPreferenceUtils.getKeyboardHeight(false);
        mEditText.addTextChangedListener(new TextWatcher() {
            private String originStr;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                MyLog.d(TAG, "beforeTextChanged " + s + " " + start + " " + count + " " + after);
                originStr = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MyLog.d(TAG, "onTextChanged " + s + " " + start + " " + before + " " + count);

            }

            @Override
            public void afterTextChanged(Editable s) {
                MyLog.d(TAG, "afterTextChanged " + s);
                String result = s.toString();
                if (result.length() > MAX_LEN) {
                    mEditText.setText(result.substring(0, MAX_LEN));
                    ToastUtils.showToast(getContext(), getContext().getString(R.string.max_len_notice));
                    mEditText.setSelection(MAX_LEN);
                }
            }
        });
    }

    private void adjustPlaceHolderContainer(final int softKeyboardHeight) {
        MyLog.d(TAG, "adjustPlaceHolderContainer softKeyboardHeight=" + softKeyboardHeight);
        if (mSoftKeyboardHeight != softKeyboardHeight) {
            MyLog.d(TAG, "(mSoftKeyboardHeight != softKeyboardHeight) adjustPlaceHolderContainer softKeyboardHeight=" + softKeyboardHeight);
            final LayoutParams layoutParams = (LayoutParams) BottomGameInputView.this.getLayoutParams();
            if (softKeyboardHeight != 0) {
                //如果键盘向上顶activity，则把顶出去那部分减回来
                final BaseActivity mBaseActivity = (BaseActivity) getContext();
                View view = mBaseActivity.getWindow().getDecorView();
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                layoutParams.bottomMargin = softKeyboardHeight + location[1];
                mSoftKeyboardHeight = softKeyboardHeight;
                MLPreferenceUtils.setKeyboardHeight(softKeyboardHeight);

                mEditText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        View view = mBaseActivity.getWindow().getDecorView();
                        int[] location = new int[2];
                        view.getLocationOnScreen(location);
                        layoutParams.bottomMargin = softKeyboardHeight + location[1];
                        BottomGameInputView.this.setLayoutParams(layoutParams);
                    }
                }, 100);
            } else {
                layoutParams.bottomMargin = softKeyboardHeight;
            }
            BottomGameInputView.this.setLayoutParams(layoutParams);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMain(KeyboardEvent event) {
        MyLog.w(TAG, "KeyboardEvent eventType=" + event.eventType);
        if (getVisibility() != View.VISIBLE) {
            return;
        }
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE:
                int keyboardHeight;
                try {
                    keyboardHeight = Integer.parseInt(String.valueOf(event.obj1));
                } catch (NumberFormatException e) {
                    MyLog.e(TAG, e);
                    return;
                }
//                if (keyboardHeight > Math.min(DisplayUtils.getScreenWidth(), DisplayUtils.getScreenHeight())) {
//                    return;
//                }

                MyLog.d(TAG, "onEventMainThread mSoftKeyboardHeight=" + keyboardHeight);
                adjustPlaceHolderContainer(keyboardHeight);
                break;
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN:
                adjustPlaceHolderContainer(0);
                mViewPlaceholder.setVisibility(View.VISIBLE);
                mEditText.clearFocus();
                break;
            default:
                break;
        }
    }

    public interface IBottomGameInputView {
        void onSendClick(String msg);

        void onGameInputAreaClick();
    }
}
