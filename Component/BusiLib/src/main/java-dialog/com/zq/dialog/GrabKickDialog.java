package com.zq.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;

import com.common.core.userinfo.model.UserInfoModel;
import com.component.busilib.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;

public class GrabKickDialog {

    public static final int KICK_TYPE_CONFIRM = 1;
    public static final int KICK_TYPE_REQUEST = 2;

    DialogPlus mDialogPlus;
    UserInfoModel mUserInfoModel;

    Listener mGrabClickListener;

    public GrabKickDialog(Context context, final UserInfoModel userInfoModel, int type, int num) {
        this.mUserInfoModel = userInfoModel;
        GrabKickDialogView grabKickDialogView = new GrabKickDialogView(context, userInfoModel, type, num);
        grabKickDialogView.setListener(new GrabKickDialogView.Listener() {
            @Override
            public void onTimeOut() {
                if (mDialogPlus != null && mDialogPlus.isShowing()) {
                    mDialogPlus.dismiss();
                }
            }
        });

        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(new ViewHolder(grabKickDialogView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                        if (view.getId() == R.id.cancle_tv) {
                            dialog.dismiss();
                        } else if (view.getId() == R.id.confirm_tv) {
                            dialog.dismiss();
                            if (mGrabClickListener != null) {
                                mGrabClickListener.onClickConfirm(userInfoModel);
                            }
                        }
                    }
                })
                .setGravity(Gravity.CENTER)
                .create();
    }

    public void setListener(Listener grabClickListener) {
        this.mGrabClickListener = grabClickListener;
    }

    public void show() {
        if (mDialogPlus != null) {
            mDialogPlus.show();
        }
    }

    public boolean isShowing() {
        if (mDialogPlus != null) {
            return mDialogPlus.isShowing();
        }
        return false;
    }

    public void dismiss() {
        if (mDialogPlus != null) {
            mDialogPlus.dismiss();
        }
        mGrabClickListener = null;
    }

    public interface Listener {
        void onClickConfirm(UserInfoModel userInfoModel);
    }
}
