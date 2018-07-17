package view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.base.event.KeyboardEvent;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.mvp.specific.RxRelativeLayout;
import com.wali.live.watchsdk.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by lan on 2018/1/11.
 */
public class ContestRevivalInputView extends RxRelativeLayout implements View.OnClickListener {
    private View mBgView;
    private View mReviveInputArea;
    private View mPlaceholderView;

    private EditText mInputEt;
    private TextView mOkBtn;

    private boolean mIsShown = false;

    private int mKeyboardHeight;

    private RevivalInputListener mInputListener;

    public ContestRevivalInputView(Context context) {
        super(context);
        init(context);
    }

    public ContestRevivalInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ContestRevivalInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.contest_revival_input_view, this);
        registerEventBus();

        mBgView = $(R.id.background_view);
        mBgView.setOnClickListener(this);

        mReviveInputArea = $(R.id.revive_input_area);
        mReviveInputArea.setOnClickListener(this);

        mPlaceholderView = $(R.id.placeholder_view);

        mInputEt = $(R.id.input_et);
        mInputEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String content = mInputEt.getText().toString().trim();
                mOkBtn.setEnabled(!TextUtils.isEmpty(content));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mOkBtn = $(R.id.ok_btn);
        mOkBtn.setOnClickListener(this);
    }

    public void setInputListener(RevivalInputListener listener) {
        mInputListener = listener;
    }

    public boolean isShown() {
        return mIsShown;
    }

    public void hide() {
        if (!mIsShown) {
            return;
        }
        mIsShown = false;
        KeyboardUtils.hideKeyboard((Activity) getContext());
        setVisibility(View.GONE);
    }

    public void show() {
        if (mIsShown) {
            return;
        }
        mIsShown = true;
        setVisibility(View.VISIBLE);

        Animation alpha = AnimationUtils.loadAnimation(getContext(), R.anim.alpha_in_time300_fromp4);
        mBgView.startAnimation(alpha);

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_bottom_in_time400);
        mReviveInputArea.startAnimation(animation);
    }

    public void reset() {
        mInputEt.setText("");
    }

    private void setRevivalCode() {
        String content = mInputEt.getText().toString().trim();
        if (!TextUtils.isEmpty(content)) {
            if (mInputListener != null) {
                mInputListener.setRevivalCode(content);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.background_view) {
            hide();
        } else if (id == R.id.ok_btn) {
            setRevivalCode();
        }
    }

    private void registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        unregisterEventBus();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(KeyboardEvent event) {
        MyLog.w(TAG, "KeyboardEvent eventType=" + event.eventType);
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN:
                if (mPlaceholderView != null) {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mPlaceholderView.getLayoutParams();
                    layoutParams.height = 0;
                    mPlaceholderView.setLayoutParams(layoutParams);
                }
                break;
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE:
                if (mPlaceholderView != null) {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mPlaceholderView.getLayoutParams();
                    layoutParams.height = mKeyboardHeight = (int) event.obj1;
                    mPlaceholderView.setLayoutParams(layoutParams);
                }
                break;
        }
    }

    public interface RevivalInputListener {
        void setRevivalCode(String revivalCode);
    }
}
