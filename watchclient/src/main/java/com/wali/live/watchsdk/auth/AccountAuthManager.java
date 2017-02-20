package com.wali.live.watchsdk.auth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.base.dialog.MyAlertDialog;
import com.mi.live.data.account.HostChannelManager;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkBinder;

/**
 * Created by chengsimin on 2017/2/9.
 */
public class AccountAuthManager {
    static boolean showWindow = false;

    public static boolean triggerActionNeedAccount(Context activity) {
        if (UserAccountManager.getInstance().hasAccount()) {
            return true;
        } else {
            if (!showWindow) {
                MyAlertDialog mMyAlertDialog = new MyAlertDialog.Builder(activity).create();
                mMyAlertDialog.setMessage("请先登录");
                mMyAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        showWindow = false;
                    }
                });
                mMyAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        showWindow = true;
                    }
                });
                mMyAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "去登录", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MiLiveSdkBinder.getInstance().onEventWantLogin(HostChannelManager.getInstance().getmCurrentChannelId());
                        //TODO 一定记得加上
                        dialog.dismiss();
                    }
                });
                mMyAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                mMyAlertDialog.setCancelable(false);
                mMyAlertDialog.show();
            }
        }
        // 通知宿主进程，用户触发了账号操作，看宿主如何处理
        return false;
    }
}
