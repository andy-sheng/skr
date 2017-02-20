package com.wali.live.watchsdk.ipc.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.base.log.MyLog;


/**
 * Created by chengsimin on 2016/12/26.
 */
public class MiLiveSdkService extends Service {
    private static final String TAG = "MiLinkService";

    @Override
    public IBinder onBind(Intent intent) {
        MyLog.w(TAG, "MiLink Service Binded");
        return MiLiveSdkBinder.getInstance();
    }

    @Override
    public void onRebind(Intent intent) {
        onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // 如果这样，要求业务退出时如有需要，立即登出
        // MnsBinder.Instance.stop();
        MyLog.w(TAG, "MiLink Service UnBinded");
        return true;
    }

    @Override
    public int onStartCommand(Intent intent1, int flags, int startId) {
        MyLog.w(TAG, "MiLink Service Started ,and onStartCommandReturn=" + 1);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.v(TAG, "MiLink Service end");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        MyLog.v(TAG, "onTaskRemoved");
//        if (Build.VERSION.SDK_INT > 14) {
//            Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
//            restartServiceIntent.setPackage(getPackageName());
//
//            PendingIntent restartServicePendingIntent = PendingIntent.getService(
//                    getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
//            AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(
//                    Context.ALARM_SERVICE);
//            alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000,
//                    restartServicePendingIntent);
//
//        }
        super.onTaskRemoved(rootIntent);
    }
}
