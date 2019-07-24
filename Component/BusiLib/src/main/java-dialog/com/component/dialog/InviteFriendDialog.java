package com.component.dialog;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;

import com.common.clipboard.ClipboardUtils;
import com.common.utils.U;
import com.component.busilib.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

/**
 * 邀请好友
 */
public class InviteFriendDialog {

    public final static int INVITE_GRAB_GAME = 1;
    public final static int INVITE_GRAB_FRIEND = 2;
    public final static int INVITE_DOUBLE_GAME = 3;

    DialogPlus mShareDialog;

    public InviteFriendDialog(Context context, int type, int gameId,int tagId,int mediaType, String kouLingToken) {
        InviteFriendDialogView inviteFriendDialogView = new InviteFriendDialogView(context, type, gameId,tagId,mediaType, kouLingToken);

        inviteFriendDialogView.setListener(new InviteFriendDialogView.Listener() {
            @Override
            public void onClickQQShare(String text) {
                if (mShareDialog != null) {
                    mShareDialog.dismiss();
                }
                ClipboardUtils.setCopy(text);
                Intent intent = U.getActivityUtils().getLaunchIntentForPackage("com.tencent.mobileqq");
                if (intent != null && null != intent.resolveActivity(U.app().getPackageManager())) {
                    U.getActivityUtils().getTopActivity().startActivity(intent);
                    U.getToastUtil().showLong("请将口令粘贴给你的好友");
                } else {
                    U.getToastUtil().showLong("未安装QQ,请将口令粘贴给你的好友");
                }
            }

            @Override
            public void onClickWeixinShare(String text) {
                if (mShareDialog != null) {
                    mShareDialog.dismiss();
                }
                ClipboardUtils.setCopy(text);
                Intent intent = U.getActivityUtils().getLaunchIntentForPackage("com.tencent.mm");
                if (intent != null && null != intent.resolveActivity(U.app().getPackageManager())) {
                    U.getActivityUtils().getTopActivity().startActivity(intent);
                    U.getToastUtil().showLong("请将口令粘贴给你的好友");
                } else {
                    U.getToastUtil().showLong("未安装微信,请将口令粘贴给你的好友");
                }
            }
        });

        mShareDialog = DialogPlus.newDialog(context)
                .setContentHolder(new ViewHolder(inviteFriendDialogView))
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
}

