//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import io.rong.imkit.widget.BaseDialogFragment;

public class AlterDialogFragment extends BaseDialogFragment {
    private static final String ARGS_TITLE = "args_title";
    private static final String ARGS_MESSAGE = "args_message";
    private static final String ARGS_CANCEL_BTN_TXT = "args_cancel_button_text";
    private static final String ARGS_OK_BTN_TXT = "args_ok_button_text";
    private io.rong.imkit.widget.AlterDialogFragment.AlterDialogBtnListener mAlterDialogBtnListener;

    public AlterDialogFragment() {
    }

    public static io.rong.imkit.widget.AlterDialogFragment newInstance(String title, String message, String cancelBtnText, String okBtnText) {
        io.rong.imkit.widget.AlterDialogFragment dialogFragment = new io.rong.imkit.widget.AlterDialogFragment();
        Bundle args = new Bundle();
        args.putString("args_title", title);
        args.putString("args_message", message);
        args.putString("args_cancel_button_text", cancelBtnText);
        args.putString("args_ok_button_text", okBtnText);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = this.getArguments().getString("args_title");
        String message = this.getArguments().getString("args_message");
        String cancelBtnText = this.getArguments().getString("args_cancel_button_text");
        String okBtnText = this.getArguments().getString("args_ok_button_text");
        Builder builder = new Builder(this.getActivity());
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message);
        }

        if (!TextUtils.isEmpty(okBtnText)) {
            builder.setPositiveButton(okBtnText, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (io.rong.imkit.widget.AlterDialogFragment.this.mAlterDialogBtnListener != null) {
                        io.rong.imkit.widget.AlterDialogFragment.this.mAlterDialogBtnListener.onDialogPositiveClick(io.rong.imkit.widget.AlterDialogFragment.this);
                    }

                }
            });
        }

        if (!TextUtils.isEmpty(cancelBtnText)) {
            builder.setNegativeButton(cancelBtnText, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (io.rong.imkit.widget.AlterDialogFragment.this.mAlterDialogBtnListener != null) {
                        io.rong.imkit.widget.AlterDialogFragment.this.mAlterDialogBtnListener.onDialogNegativeClick(io.rong.imkit.widget.AlterDialogFragment.this);
                    }

                }
            });
        }

        return builder.create();
    }

    public void show(FragmentManager manager) {
        this.show(manager, "AlterDialogFragment");
    }

    public void setOnAlterDialogBtnListener(io.rong.imkit.widget.AlterDialogFragment.AlterDialogBtnListener alterDialogListener) {
        this.mAlterDialogBtnListener = alterDialogListener;
    }

    public interface AlterDialogBtnListener {
        void onDialogPositiveClick(io.rong.imkit.widget.AlterDialogFragment var1);

        void onDialogNegativeClick(io.rong.imkit.widget.AlterDialogFragment var1);
    }
}
