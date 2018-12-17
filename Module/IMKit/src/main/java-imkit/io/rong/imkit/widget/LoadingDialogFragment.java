//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import io.rong.imkit.widget.BaseDialogFragment;

public class LoadingDialogFragment extends BaseDialogFragment {
    private static final String ARGS_TITLE = "args_title";
    private static final String ARGS_MESSAGE = "args_message";

    public LoadingDialogFragment() {
    }

    public static io.rong.imkit.widget.LoadingDialogFragment newInstance(String title, String message) {
        io.rong.imkit.widget.LoadingDialogFragment dialogFragment = new io.rong.imkit.widget.LoadingDialogFragment();
        Bundle args = new Bundle();
        args.putString("args_title", title);
        args.putString("args_message", message);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(this.getActivity());
        String title = this.getArguments().getString("args_title");
        String message = this.getArguments().getString("args_message");
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(0);
        if (!TextUtils.isEmpty(title)) {
            dialog.setTitle(title);
        }

        if (!TextUtils.isEmpty(message)) {
            dialog.setMessage(message);
        }

        return dialog;
    }

    public void show(FragmentManager manager) {
        this.show(manager, "LoadingDialogFragment");
    }
}
