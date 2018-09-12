package com.wali.live.watchsdk.watch.view.watchgameview;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.thornbirds.component.view.IComponentView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.presenter.InputPresenter;

import static com.wali.live.watchsdk.component.viewmodel.BarrageState.BARRAGE_NORMAL;

/**
 * Created by zhujianning on 18-8-13.
 */

public class GameNewLandscapeInputView extends LinearLayout implements IComponentView<GameNewLandscapeInputView.IPresenter, GameNewLandscapeInputView.IView> {
    private static final String TAG = "GameNewLandscapeInputView";
    private final static int BARRAGE_MAX_LEN = 30; // 弹幕最多一次输入三十个字

    @Nullable
    private IPresenter mPresenter;
    private EditText mLandscapeBarrageEt;
    private TextView mLandscapeBarrageSendBtn;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String result = s.toString();
            if (result.length() > 0 && UserAccountManager.getInstance().hasAccount()) {
                mLandscapeBarrageSendBtn.setSelected(true);
            } else {
                mLandscapeBarrageSendBtn.setSelected(false);
            }
            if (result.length() > BARRAGE_MAX_LEN) {
                mLandscapeBarrageEt.setText(result.substring(0, BARRAGE_MAX_LEN));
                ToastUtils.showToast(getContext(), getContext().getString(R.string.max_len_notice));
                mLandscapeBarrageEt.setSelection(BARRAGE_MAX_LEN);
            }
        }
    };

    public GameNewLandscapeInputView(Context context) {
        this(context, null);
    }

    public GameNewLandscapeInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameNewLandscapeInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View root = inflate(context, R.layout.game_new_landscape_input_view, this);
        mLandscapeBarrageEt = (EditText) root.findViewById(R.id.landscape_barrage_edit);
        mLandscapeBarrageSendBtn = (TextView) root.findViewById(R.id.landscape_barrage_send_btn);

        initListener();
    }

    private void initListener() {
        mLandscapeBarrageEt.addTextChangedListener(mTextWatcher);
        mLandscapeBarrageEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (null == event) {
                    return false;
                }
                return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });

        mLandscapeBarrageSendBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                    String body = mLandscapeBarrageEt.getText().toString().trim();
                    if (!TextUtils.isEmpty(body)) {
                        if (mPresenter != null) {
                            mPresenter.sendBarrage(body, BARRAGE_NORMAL);
                        }
                        mLandscapeBarrageEt.setText("");
                    }
                }else{
                    KeyboardUtils.hideKeyboard((Activity) getContext());
                }
            }
        });
    }

    /**
     * 暂时可以认为销毁
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MyLog.d(TAG, "onDetachedFromWindow");
        mTextWatcher = null;
    }

    @Override
    public GameNewLandscapeInputView.IView getViewProxy() {
        class ComponentView implements IView {

            @Override
            public EditText getInputView() {
                return mLandscapeBarrageEt;
            }

            @Override
            public void onKeyboardShowed(int keyboardHeight) {

            }

            @Override
            public void onKeyboardHidden() {

            }

            @Override
            public <T extends View> T getRealView() {
                return (T) GameNewLandscapeInputView.this;
            }
        }
        return new ComponentView();
    }

    @Override
    public void setPresenter(IPresenter iPresenter) {
        this.mPresenter = iPresenter;
    }

    public interface IPresenter extends InputPresenter.IPresenter {

    }

    public interface IView extends InputPresenter.IView {

    }
}
