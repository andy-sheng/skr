package com.dialog.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.base.R;
import com.common.view.ex.ExTextView;

public class TipsDialogView extends RelativeLayout {

    public ExTextView mMessageTv;
    public ExTextView mOkBtn;
    public ExTextView mConfirmTv;
    public ExTextView mCancelTv;

    private TipsDialogView(Context context) {
        super(context);
        init();
    }

    private TipsDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private TipsDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.template_tips1_dialog, this);

        mMessageTv = (ExTextView)this.findViewById(R.id.message_tv);
        mOkBtn = (ExTextView)this.findViewById(R.id.ok_btn);
        mConfirmTv = (ExTextView)this.findViewById(R.id.confirm_tv);
        mCancelTv = (ExTextView) this.findViewById(R.id.cancel_tv);
    }

    public static final class Builder {
        TipsDialogView tipsDialogView;
        private ExTextView mMessageTv;
        private ExTextView mOkBtn;

        public Builder(Context context) {
            tipsDialogView = new TipsDialogView(context);
        }

        public Builder setMessageTip(CharSequence text) {
            tipsDialogView.mMessageTv.setText(text);
            tipsDialogView.mMessageTv.setVisibility(VISIBLE);
            return this;
        }

        public Builder setConfirmTip(String text) {
            tipsDialogView.mConfirmTv.setText(text);
            tipsDialogView.mConfirmTv.setVisibility(VISIBLE);
            return this;
        }

        public Builder setCancelTip(String text){
            tipsDialogView.mCancelTv.setText(text);
            tipsDialogView.mCancelTv.setVisibility(VISIBLE);
            return this;
        }

        public Builder setOkBtnTip(String text){
            tipsDialogView.mOkBtn.setText(text);
            tipsDialogView.mOkBtn.setVisibility(VISIBLE);
            return this;
        }

        public Builder setOkBtnClickListener(OnClickListener l){
            tipsDialogView.mOkBtn.setOnClickListener(l);
            return this;
        }

        public Builder setConfirmBtnClickListener(OnClickListener l){
            tipsDialogView.mConfirmTv.setOnClickListener(l);
            return this;
        }

        public Builder setCancelBtnClickListener(OnClickListener l){
            tipsDialogView.mCancelTv.setOnClickListener(l);
            return this;
        }
        public TipsDialogView build() {
            return tipsDialogView;
        }
    }
}
