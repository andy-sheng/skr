package com.zq.dialog;

import android.content.Context;
import android.view.Gravity;

import com.component.busilib.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

public class ShareWorksDialog {

    DialogPlus mShareDialog;

    public ShareWorksDialog(Context context, String songName, final ShareListener listener) {
        ShareWorksDialogView shareWorksDialogView = new ShareWorksDialogView(context, songName, new ShareWorksDialogView.Listener() {
            @Override
            public void onClickQQShare() {
                if (mShareDialog != null) {
                    mShareDialog.dismiss();
                }
                if (listener != null) {
                    listener.onClickQQShare();
                }
            }

            @Override
            public void onClickQZoneShare() {
                if (mShareDialog != null) {
                    mShareDialog.dismiss();
                }
                if (listener != null) {
                    listener.onClickQZoneShare();
                }
            }

            @Override
            public void onClickWeixinShare() {
                if (mShareDialog != null) {
                    mShareDialog.dismiss();
                }
                if (listener != null) {
                    listener.onClickWeixinShare();
                }
            }

            @Override
            public void onClickQuanShare() {
                if (mShareDialog != null) {
                    mShareDialog.dismiss();
                }
                if (listener != null) {
                    listener.onClickQuanShare();
                }
            }
        });

        mShareDialog = DialogPlus.newDialog(context)
                .setContentHolder(new ViewHolder(shareWorksDialogView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false)
                .setGravity(Gravity.BOTTOM)
                .create();
    }

    public void dismiss() {
        if (mShareDialog != null) {
            mShareDialog.dismiss();
        }
    }

    public void dismiss(boolean useAnimation) {
        if (mShareDialog != null) {
            mShareDialog.dismiss(useAnimation);
        }
    }

    public void show() {
        if (mShareDialog != null) {
            mShareDialog.show();
        }
    }

    public interface ShareListener {
        void onClickQQShare();

        void onClickQZoneShare();

        void onClickWeixinShare();

        void onClickQuanShare();
    }
}
