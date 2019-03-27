package com.zq.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.component.busilib.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.common.core.global.event.ShowDialogInHomeEvent;

import org.greenrobot.eventbus.EventBus;

public class ConfirmDialog {

    public static final int TYPE_KICK_CONFIRM = 1;
    public static final int TYPE_KICK_REQUEST = 2;
    public static final int TYPE_INVITE_CONFIRM = 3;

    DialogPlus mDialogPlus;
    UserInfoModel mUserInfoModel;

    Listener mListener;

    public ConfirmDialog(Context context, final UserInfoModel userInfoModel, int type) {
        this(context, userInfoModel, type, 0);
    }

    public ConfirmDialog(Context context, final UserInfoModel userInfoModel, int type, int num) {
        this.mUserInfoModel = userInfoModel;
        ConfirmDialogView dialogView = new ConfirmDialogView(context, userInfoModel, type, num);
        dialogView.setListener(new ConfirmDialogView.Listener() {
            @Override
            public void onTimeOut() {
                if (mDialogPlus != null && mDialogPlus.isShowing()) {
                    mDialogPlus.dismiss();
                }
            }
        });
        if (mDialogPlus == null) {
            mDialogPlus = DialogPlus.newDialog(context)
                    .setContentHolder(new ViewHolder(dialogView))
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
                                if (mListener != null) {
                                    mListener.onClickConfirm(userInfoModel);
                                }
                            }
                        }
                    })
                    .setGravity(Gravity.BOTTOM)
                    .create();
        }
    }

    public void setListener(Listener grabClickListener) {
        this.mListener = grabClickListener;
    }

    public void show() {
        if (mDialogPlus != null) {
            if(U.getActivityUtils().isHomeActivity(U.getActivityUtils().getTopActivity())){
                EventBus.getDefault().post(new ShowDialogInHomeEvent(mDialogPlus,20));
            }else{
                mDialogPlus.show();
            }
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
        mListener = null;
    }

    public interface Listener {
        void onClickConfirm(UserInfoModel userInfoModel);
    }
}
