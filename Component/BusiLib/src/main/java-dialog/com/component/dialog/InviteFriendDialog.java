package com.component.dialog;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;

import com.common.clipboard.ClipboardUtils;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.component.busilib.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import io.reactivex.Observable;

/**
 * 邀请好友
 */
public class InviteFriendDialog {

    DialogPlus mShareDialog;

    public InviteFriendDialog(Context context, String kouLingToken, IInviteDialogCallBack inviteCallBack) {
        InviteFriendDialogView inviteFriendDialogView = new InviteFriendDialogView(context, kouLingToken, inviteCallBack);

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
                .setMargin(U.getDisplayUtils().dip2px(16), -1, U.getDisplayUtils().dip2px(16), U.getDisplayUtils().dip2px(16))
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

    public interface IInviteDialogCallBack {
        //获得口令接口
        Observable<ApiResult> getKouLingTokenObservable();

        //在分享弹窗以文字的形式在qq,微信好友分享，就是聊天形式
        String getInviteDialogText(String kouling);
    }
}

