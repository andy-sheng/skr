package com.wali.live.livesdk.live.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.receiver.GameLiveNotificationReceiver;

/**
 * Created by chenyong on 2016/12/26.
 */

public class GameLiveService extends Service {

    private static final String TAG = "GameLiveService";

    private static final int GAME_LIVE_NOTIFICATION = 10000;

    @Override
    public void onCreate() {
        MyLog.w(TAG, "onCreate");
        super.onCreate();
        Notification.Builder nb = new Notification.Builder(this);
        nb.setContentTitle("游戏直播中");
        nb.setSmallIcon(R.drawable.ic_launcher);
        nb.setAutoCancel(true);
        nb.setDefaults(Notification.DEFAULT_LIGHTS);
        Intent notificationIntent = new Intent(this, GameLiveNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        nb.setContentIntent(pendingIntent);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= 16) {
            notification = nb.build();
        } else {
            notification = nb.getNotification();
        }
        startForeground(GAME_LIVE_NOTIFICATION, notification);
    }

    @Override
    public void onDestroy() {
        MyLog.w(TAG, "onDestroy");
        super.onDestroy();
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
