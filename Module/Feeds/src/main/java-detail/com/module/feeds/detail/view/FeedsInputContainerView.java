package com.module.feeds.detail.view;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.emoji.EmotionKeyboard;
import com.common.emoji.EmotionLayout;
import com.common.emoji.LQREmotionKit;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.NoLeakEditText;
import com.module.feeds.R;

public class FeedsInputContainerView extends RelativeLayout implements EmotionKeyboard.BoardStatusListener {
    EmotionKeyboard mEmotionKeyboard;
    ConstraintLayout mInputContainer;
    protected NoLeakEditText mEtContent;
    ViewGroup mPlaceHolderView;
    protected View mSendMsgBtn;
    EmotionLayout mElEmotion;

    protected boolean mHasPretend = false;

    protected Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                mHasPretend = true;
            }
        }
    };

    public FeedsInputContainerView(Context context) {
        super(context);
        init();
    }

    public FeedsInputContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {
        inflate(getContext(), R.layout.feeds_input_container_view_layout, this);
        initInputView();
    }

    /**
     * 输入面板相关view的初始化
     */
    protected void initInputView() {

        LQREmotionKit.tryInit(U.app());
        mInputContainer = this.findViewById(R.id.et_container);
        mEtContent = (NoLeakEditText) this.findViewById(R.id.etContent);
        mPlaceHolderView = this.findViewById(R.id.place_holder_view);
        mElEmotion = (EmotionLayout) this.findViewById(R.id.elEmotion);
        mSendMsgBtn = this.findViewById(R.id.send_msg_btn);

        initEmotionKeyboard();

        mSendMsgBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mHasPretend = false;

            }
        });
    }

    private void initEmotionKeyboard() {
        mEmotionKeyboard = EmotionKeyboard.with((Activity) getContext());
        mEmotionKeyboard.bindToPlaceHodlerView(mPlaceHolderView);
        mEmotionKeyboard.bindToEditText(mEtContent);
        mEmotionKeyboard.setEmotionLayout(mElEmotion);
        mEmotionKeyboard.setBoardStatusListener(this);
    }

    @Override
    public void onBoradShow() {
//        EventBus.getDefault().post(new InputBoardEvent(true));
        mInputContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBoradHide() {
//        EventBus.getDefault().post(new InputBoardEvent(false));
        mInputContainer.setVisibility(View.GONE);
    }

    public void showSoftInput() {
        mEmotionKeyboard.showSoftInput();
    }

    public void hideSoftInput() {
        mEmotionKeyboard.hideSoftInput();
    }

    public boolean onBackPressed() {
        if (mEmotionKeyboard.isEmotionShown()) {
            mEmotionKeyboard.hideEmotionLayout(false);
            return true;
        }
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mEmotionKeyboard.destroy();
        mUiHandler.removeCallbacksAndMessages(null);
    }
}
