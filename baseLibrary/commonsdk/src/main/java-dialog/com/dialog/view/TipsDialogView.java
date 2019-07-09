package com.dialog.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.RelativeLayout;

import com.common.base.R;
import com.common.view.ex.ExTextView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

public class TipsDialogView extends RelativeLayout {

    public ExTextView mTitleTv;
    public ExTextView mMessageTv;
    public ExTextView mOkBtn;
    public ExTextView mCancelTv;
    public ExTextView mConfirmTv;

    DialogPlus mDialogPlus;

    private TipsDialogView(Context context) {
        super(context);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.template_tips1_dialog, this);

        mTitleTv = this.findViewById(R.id.title_tv);
        mMessageTv = this.findViewById(R.id.message_tv);
        mOkBtn = this.findViewById(R.id.ok_btn);
        mConfirmTv = this.findViewById(R.id.confirm_tv);
        mCancelTv = this.findViewById(R.id.cancel_tv);
    }


    /**
     * 以后tips dialog 不要在外部单独写 dialog 了。
     * 可以不
     */
    public void showByDialog() {
        showByDialog(true);
    }

    public void showByDialog(boolean canCancel) {
        if (mDialogPlus != null) {
            mDialogPlus.dismiss(false);
        }
        mDialogPlus = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(this))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .setCancelable(canCancel)
                .create();
        mDialogPlus.show();
    }

    public void dismiss() {
        if (mDialogPlus != null) {
            mDialogPlus.dismiss();
        }
    }

    public void dismiss(boolean isAnimation){
        if (mDialogPlus != null) {
            mDialogPlus.dismiss(isAnimation);
        }
    }

    public static final class Builder {
        TipsDialogView tipsDialogView;

        public Builder(Context context) {
            tipsDialogView = new TipsDialogView(context);
        }

        public Builder setTitleTip(CharSequence text) {
            tipsDialogView.mTitleTv.setText(text);
            tipsDialogView.mTitleTv.setVisibility(VISIBLE);
            return this;
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

        public Builder setCancelTip(String text) {
            tipsDialogView.mCancelTv.setText(text);
            tipsDialogView.mCancelTv.setVisibility(VISIBLE);
            return this;
        }

        public Builder setOkBtnTip(String text) {
            tipsDialogView.mOkBtn.setText(text);
            tipsDialogView.mOkBtn.setVisibility(VISIBLE);
            return this;
        }

        public Builder setOkBtnClickListener(OnClickListener l) {
            tipsDialogView.mOkBtn.setOnClickListener(l);
            return this;
        }

        public Builder setConfirmBtnClickListener(OnClickListener l) {
            tipsDialogView.mConfirmTv.setOnClickListener(l);
            return this;
        }

        public Builder setCancelBtnClickListener(OnClickListener l) {
            tipsDialogView.mCancelTv.setOnClickListener(l);
            return this;
        }

        public TipsDialogView build() {
            return tipsDialogView;
        }
    }
}
