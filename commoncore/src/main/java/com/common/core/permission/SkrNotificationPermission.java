package com.common.core.permission;

import android.app.Activity;

import com.common.utils.U;

public class SkrNotificationPermission extends SkrBasePermission {

    public SkrNotificationPermission() {
        super("NOTIFICATION_PERMISSION", "开启消息通知，限定专场、好友消息，游戏邀请，一个不落！", true);
    }

    @Override
    public void ensurePermission(Activity activity, Runnable ifAgreeAction, boolean goSettingIfRefuse) {
        if (activity == null) {
            return;
        }

        if (U.getPermissionUtils().checkNotification(activity)) {
            if (ifAgreeAction != null) {
                ifAgreeAction.run();
            }
        } else {
            onRequestPermissionFailureWithAskNeverAgain1(activity,goSettingIfRefuse);
        }
    }

    protected void goSettingPage(Activity activity) {
        U.getPermissionUtils().goNotificationSettingPage();
        if (mTipsDialogView != null) {
            mTipsDialogView.dismiss();
        }
        mHasGoPermission = false;
    }
}
