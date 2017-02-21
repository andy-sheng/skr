package com.wali.live.sdk.manager.demo.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.wali.live.sdk.manager.demo.R;
import com.wali.live.sdk.manager.global.GlobalData;

/**
 * Created by milive on 16/12/6.
 */
public class NotificationManger {
    public static final int UPDATE_DOWNLOADING = 100001;

    private static NotificationManger sInstance = new NotificationManger();
    private NotificationManager mNotificationManager;

    private NotificationManger() {
        mNotificationManager = (NotificationManager) GlobalData.app().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static NotificationManger getInstance() {
        return sInstance;
    }

    public void showDownloadNotification(String msg) {
        Notification.Builder nb = new Notification.Builder(GlobalData.app());
        nb.setContentText(msg);
        nb.setContentTitle(GlobalData.app().getString(R.string.update_downloading));
        nb.setSmallIcon(R.drawable.milive_ic);
        nb.setAutoCancel(true);
        int defaults = 0;
        nb.setDefaults(defaults);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= 16) {
            notification = nb.build();
        } else {
            notification = nb.getNotification();
        }
        mNotificationManager.notify(UPDATE_DOWNLOADING, notification);
    }

    public void removeNotification(int notificationId) {
        mNotificationManager.cancel(notificationId);
    }
}
