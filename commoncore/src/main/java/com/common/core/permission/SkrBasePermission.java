package com.common.core.permission;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;

import com.common.core.R;
import com.common.log.MyLog;
import com.common.permission.PermissionUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.dialog.view.TipsDialogView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.List;

public abstract class SkrBasePermission {
    public final static String TAG = "SkrPermission";
    DialogPlus mPerTipsDialogPlus;
    TipsDialogView mTipsDialogView;
    boolean mHasGoPermission = false;
    String mPermissionStr;
    String mGoPermissionManagerTips;
    boolean mCannable = true;


    public SkrBasePermission(String permissionStr, String goPermissionManagerTips, boolean cannable) {
        mPermissionStr = permissionStr;
        mGoPermissionManagerTips = goPermissionManagerTips;
        mCannable = cannable;
    }

    public void ensurePermission(final Runnable ifAgreeAction, final boolean goSettingIfRefuse) {
        ensurePermission(U.getActivityUtils().getTopActivity(), ifAgreeAction, goSettingIfRefuse);
    }

    public void ensurePermission(final Activity activity, final Runnable ifAgreeAction, final boolean goSettingIfRefuse) {
        if (!U.getPermissionUtils().checkPermission(activity, mPermissionStr)) {
            MyLog.d(TAG, "ensurePhoneStatePermission false");
            // 这里会起个 Activity 判断权限，会回调 activity 的 onResume 方法
            U.getPermissionUtils().requestPermission(new PermissionUtils.RequestPermission() {
                @Override
                public void onRequestPermissionSuccess() {
                    MyLog.d(TAG, "onRequestPermissionSuccess");
                    if (ifAgreeAction != null) {
                        ifAgreeAction.run();
                    }
                }

                @Override
                public void onRequestPermissionFailure(List<String> permissions) {
                    MyLog.d(TAG, "onRequestPermissionFailure" + " permissions=" + permissions);
                    onRequestPermissionFailure1(activity,goSettingIfRefuse);
                }

                @Override
                public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                    MyLog.d(TAG, "onRequestPermissionFailureWithAskNeverAgain" + " permissions=" + permissions);
                    onRequestPermissionFailureWithAskNeverAgain1(activity,goSettingIfRefuse);

                }
            }, activity, mPermissionStr);
        } else {
            if (ifAgreeAction != null) {
                ifAgreeAction.run();
            }
        }
    }

    public void onRequestPermissionFailure1(Activity activity,boolean goSettingIfRefuse) {

    }

    public void onRequestPermissionFailureWithAskNeverAgain1(Activity activity,boolean goSettingIfRefuse) {
        if (goSettingIfRefuse) {
            onReject(activity,mGoPermissionManagerTips);
        }
    }

    public void onReject(final Activity activity, String text) {
        if (mPerTipsDialogPlus == null) {
            mTipsDialogView = new TipsDialogView.Builder(activity)
                    .setMessageTip(text)
                    .setOkBtnTip("去设置")
                    .setOkBtnClickListener(new AnimateClickListener() {
                        @Override
                        public void click(View view) {
                            mHasGoPermission = true;
                            goSettingPage(activity);
                        }
                    })
                    .build();

            mPerTipsDialogPlus = DialogPlus.newDialog(activity)
                    .setContentHolder(new ViewHolder(mTipsDialogView))
                    .setGravity(Gravity.BOTTOM)
                    .setCancelable(mCannable)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .create();
        }
        mTipsDialogView.mMessageTv.setText(text);
        mPerTipsDialogPlus.show();
    }

    protected void goSettingPage(Activity activity){
        U.getPermissionUtils().goToPermissionManager(activity);
    }

    private void onAgree() {
        if (mPerTipsDialogPlus != null) {
            mPerTipsDialogPlus.dismiss();
        }
    }

    public boolean onBackFromPermisionManagerMaybe(Activity activity) {
        if (mHasGoPermission) {
            mHasGoPermission = false;
            if (U.getPermissionUtils().checkPermission(activity, mPermissionStr)) {
                MyLog.d(TAG, "onBack true");
                onAgree();
                return true;
            } else {
                MyLog.d(TAG, "onBack false");

            }
        }
        return false;
    }

}
