package com.wali.live.watchsdk.fans.dialog;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.base.keyboard.KeyboardUtils;
import com.base.mvp.specific.RxDialog;
import com.wali.live.common.smiley.SmileyInputFilter;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.dialog.listener.OnConfirmClickListener;
import com.wali.live.watchsdk.fans.presenter.specific.ApplyJoinGroupPresenter;
import com.wali.live.watchsdk.fans.presenter.specific.IApplyJoinGroupView;

/**
 * Created by zhaomin on 17-6-16.
 */
public class ApplyJoinDialog extends RxDialog implements View.OnClickListener, IApplyJoinGroupView {
    private EditText mEditText;

    private OnConfirmClickListener mOnConfirmClickListener;

    ApplyJoinGroupPresenter mApplyJoinPresenter;

    public ApplyJoinDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_apply_join, null);
        setContentView(view);

        mEditText = (EditText) view.findViewById(R.id.edit_content);
        mEditText.setFilters(new InputFilter[]{new SmileyInputFilter(mEditText, 60)});

        View confirm = view.findViewById(R.id.sure);
        View cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String applyMsg = getEditMessage();
                mApplyJoinPresenter.applyJoinGroup(applyMsg);
            }
        });

        setWindow();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.cancel) {
            KeyboardUtils.hideKeyboard((Activity) mContext);
            dismiss();
        }
    }

    public String getEditMessage() {
        return mEditText.getText().toString().trim();
    }

    public void show(long groupId, String roomId, OnConfirmClickListener onConfirmClickListener) {
        super.show();
        mApplyJoinPresenter = new ApplyJoinGroupPresenter(this, groupId, roomId);
        mOnConfirmClickListener = onConfirmClickListener;
        KeyboardUtils.showKeyboard(getContext());
    }

    @Override
    public void setApplyJoinResult(boolean result) {
        if (result) {
            if (mOnConfirmClickListener != null) {
                mOnConfirmClickListener.confirm();
            }
            KeyboardUtils.hideKeyboard((Activity) mContext);
            dismiss();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mEditText.clearFocus();
        mEditText.setFilters(new InputFilter[]{});
        mEditText.setOnClickListener(null);
        mEditText.setOnTouchListener(null);
    }
}
