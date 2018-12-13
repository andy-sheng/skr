package com.module.home.persenter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;

import com.common.log.MyLog;
import com.common.utils.PermissionUtils;
import com.common.utils.U;
import com.module.home.R;
import com.module.home.view.PermissionTipsView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.List;

public class HomePresenter {

    public final static String TAG = "HomePresenter";

    DialogPlus mPerTipsDialogPlus;
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
            check1(activity);
        }
    }

    void check1(Activity activity) {
        if (!U.getPermissionUtils().checkExternalStorage(activity)) {
            U.getPermissionUtils().requestExternalStorage(new PermissionUtils.RequestPermission() {
                @Override
                public void onRequestPermissionSuccess() {
                    MyLog.d(TAG, "onRequestPermissionSuccess");
                    check2(activity);
                }

                @Override
                public void onRequestPermissionFailure(List<String> permissions) {
                    MyLog.d(TAG, "onRequestPermissionFailure" + " permissions=" + permissions);
                    onReject();
                }

                @Override
                public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                    MyLog.d(TAG, "onRequestPermissionFailureWithAskNeverAgain" + " permissions=" + permissions);
                    onReject();
                }
            }, activity);
        } else {
            check2(activity);
        }
    }

    void check2(Activity activity) {
        if (!U.getPermissionUtils().checkRecordAudio(activity)) {
            U.getPermissionUtils().requestRecordAudio(new PermissionUtils.RequestPermission() {
                @Override
                public void onRequestPermissionSuccess() {
                    MyLog.d(TAG, "onRequestPermissionSuccess");
                    onAgree();
                }

                @Override
                public void onRequestPermissionFailure(List<String> permissions) {
                    MyLog.d(TAG, "onRequestPermissionFailure" + " permissions=" + permissions);
                    onReject();
                }

                @Override
                public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                    MyLog.d(TAG, "onRequestPermissionFailureWithAskNeverAgain" + " permissions=" + permissions);
                    onReject();
                }
            }, activity);
        } else {
            onAgree();
        }
    }

    public void onReject() {
        if (mPerTipsDialogPlus == null) {
            Activity activity = U.getActivityUtils().getTopActivity();
            mPerTipsDialogPlus = DialogPlus.newDialog(activity)
                    .setCancelable(false)
                    .setGravity(Gravity.CENTER)
                    .setContentHolder(new ViewHolder(new PermissionTipsView(activity)))
                    .create();
        }
        mPerTipsDialogPlus.show();
    }

    public void onAgree() {
        if (mPerTipsDialogPlus != null) {
            mPerTipsDialogPlus.dismiss();
        }
    }
}
