package com.wali.live.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.base.log.MyLog;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.milink.sdk.base.os.LevelPromote;

public class PacketProcessService extends Service {
    public static void startPacketProcessService(Context context) {
        Intent intent = new Intent(context, PacketProcessService.class);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initResource();
        LevelPromote.promoteApplicationLevelInMIUI();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLog.v("PacketProcessService onStartCommand");
        MiLinkClientAdapter.getsInstance().initCallBack();
        return START_STICKY;
    }

    public void initResource() {
        MyLog.w("PacketProcessService initResource");
        MiLinkClientAdapter.getsInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}