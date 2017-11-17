package com.wali.live.watchsdk.fans.dialog;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.base.keyboard.KeyboardUtils;
import com.base.mvp.specific.RxDialog;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.dialog.listener.OnConfirmClickListener;

/**
 * Created by zhaomin on 17-6-16.
 */
public class CreateGroupDialog extends RxDialog implements View.OnClickListener {
    private EditText mEditText;

    private OnConfirmClickListener mOnConfirmClickListener;

    public CreateGroupDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void init() {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_group, null);
        setContentView(view);

        mEditText = (EditText) view.findViewById(R.id.editText);
        View confirm = view.findViewById(R.id.button2);
        View cancel = view.findViewById(R.id.button1);
        cancel.setOnClickListener(this);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnConfirmClickListener != null) {
                    mOnConfirmClickListener.confirm();
                }
            }
        });

        setWindow();
    }

    public void setOnConfirmClickListener(OnConfirmClickListener onConfirmClickListener) {
        mOnConfirmClickListener = onConfirmClickListener;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button1) {
            KeyboardUtils.hideKeyboard((Activity) mContext);
            dismiss();
        }
    }

    public String getEditMessage() {
        return mEditText.getText().toString().trim();
    }
}
