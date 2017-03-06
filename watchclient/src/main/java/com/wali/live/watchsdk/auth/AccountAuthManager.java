package com.wali.live.watchsdk.auth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import com.wali.live.watchsdk.R;
import com.base.dialog.MyAlertDialog;
import com.mi.live.data.account.HostChannelManager;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkBinder;

/**
 * Created by chengsimin on 2017/2/9.
 */
public class AccountAuthManager {
    public static boolean sShowWindow = false;

    public static boolean triggerActionNeedAccount(Context activity) {
        if (UserAccountManager.getInstance().hasAccount()) {
            return true;
        } else {
            if (!sShowWindow) {
                MyAlertDialog alertDialog = new MyAlertDialog.Builder(activity).create();
                alertDialog.setMessage(activity.getString(R.string.please_login));
                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        sShowWindow = false;
                    }
                });
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        sShowWindow = true;
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, activity.getString(R.string.go_to_login), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MiLiveSdkBinder.getInstance().onEventWantLogin(HostChannelManager.getInstance().getChannelId());
                        dialog.dismiss();
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        }
        // 通知宿主进程，用户触发了账号操作，看宿主如何处理
        return false;
    }
}
