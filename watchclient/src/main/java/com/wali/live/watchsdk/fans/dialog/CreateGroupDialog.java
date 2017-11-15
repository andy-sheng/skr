package com.wali.live.watchsdk.fans.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.base.keyboard.KeyboardUtils;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;

/**
 * Created by zhaomin on 17-6-16.
 *
 * @module 底部带有输入框的dialog
 */
public class CreateGroupDialog extends Dialog implements View.OnClickListener {
    private EditText mEditText;
    private Context mContext;

    private OnConfirmClickListener mOnConfirmClickListener;

    public CreateGroupDialog(Context context) {
        super(context, R.style.MyAlertDialog);
        mContext = context;
    }

    public CreateGroupDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
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
                    mOnConfirmClickListener.doConfirm();
                }
            }
        });

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.windowAnimations = R.style.MyDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.getDecorView().setPadding(DisplayUtils.dip2px(8), 0, DisplayUtils.dip2px(8), DisplayUtils.dip2px(8));
        dialogWindow.setAttributes(lp);
    }

    public void setOnConfirmClickListener(OnConfirmClickListener onConfirmClickListener) {
        mOnConfirmClickListener = onConfirmClickListener;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button1) {
            onBackPressed();
            KeyboardUtils.hideKeyboard((Activity) mContext);
        }
    }

    public String getEditMessage() {
        return mEditText.getText().toString().trim();
    }

    public interface OnConfirmClickListener {
        void doConfirm();
    }
}
