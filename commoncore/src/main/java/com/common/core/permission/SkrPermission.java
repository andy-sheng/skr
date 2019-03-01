package com.common.core.permission;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;

import com.common.core.R;
import com.common.log.MyLog;
import com.common.utils.PermissionUtils;
import com.common.utils.U;
import com.dialog.view.TipsDialogView;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.List;

public class SkrPermission {
    public final static String TAG = "SkrPermission";
    DialogPlus mPerTipsDialogPlus;
    TipsDialogView mTipsDialogView;

    long mLastCheckTs = 0;

    public void checkPermiss(Activity activity) {
        MyLog.d(TAG, "checkPermiss");
        long now = System.currentTimeMillis();
        if (now - mLastCheckTs > 2000) {
            /**
             * 这里  U.getPermissionUtils().requestExternalStorage 会启动
             * RxPermissionFragment ，RxPermissionFragment结束后出发 Activity的生命周期
             * 所以这里加个判断，防止onResume 被触发导致不断回调
             */
            mLastCheckTs = now;
//            check1(activity);
        } else {
            MyLog.d(TAG, "checkPermiss too many times，return");
        }
    }

    public void ensureSdcardPermission(final Runnable ifAgreeAction, final boolean goSettingIfRefuse) {
        final Activity activity = U.getActivityUtils().getTopActivity();
        if (!U.getPermissionUtils().checkExternalStorage(activity)) {
            U.getPermissionUtils().requestExternalStorage(new PermissionUtils.RequestPermission() {
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
                    if (goSettingIfRefuse) {
                        onReject("请开启存储权限，保证应用正常使用");
                    }
                }

                @Override
                public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                    MyLog.d(TAG, "onRequestPermissionFailureWithAskNeverAgain" + " permissions=" + permissions);
                    if (goSettingIfRefuse) {
                        onReject("请开启存储权限，保证应用正常使用");
                    }
                }
            }, activity);
        } else {
            if (ifAgreeAction != null) {
                ifAgreeAction.run();
            }
        }
    }

    public void onReject(String text) {

        if (mPerTipsDialogPlus == null) {
            final Activity activity = U.getActivityUtils().getTopActivity();
            mTipsDialogView = new TipsDialogView.Builder(activity)
                    .setMessageTip(text)
                    .setOkBtnTip("去设置")
                    .setOkBtnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            U.getPermissionUtils().goToPermissionManager(activity);
                        }
                    })
                    .build();

            mPerTipsDialogPlus = DialogPlus.newDialog(activity)
                    .setContentHolder(new ViewHolder(mTipsDialogView))
                    .setGravity(Gravity.BOTTOM)
//                    .setCancelable(false)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .create();
        }
        mTipsDialogView.mMessageTv.setText(text);
        mPerTipsDialogPlus.show();
    }

    public void onAgree() {
        if (mPerTipsDialogPlus != null) {
            mPerTipsDialogPlus.dismiss();
        }
    }
}
