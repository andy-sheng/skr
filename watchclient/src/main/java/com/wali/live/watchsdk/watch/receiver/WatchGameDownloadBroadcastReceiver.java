package com.wali.live.watchsdk.watch.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.milink.sdk.base.Global;
import com.wali.live.watchsdk.watch.download.CustomDownloadManager;
import com.wali.live.watchsdk.watch.download.GameDownloadOptControl;
import com.wali.live.watchsdk.watch.model.OperationSession;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by zhujianning on 18-8-31.
 * 暂时用于监听宿主app下载进度事件
 */

public class WatchGameDownloadBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "WatchGameDownloadBroadcastReceiver";

    private static final String ACTION_GAME_DOWNLOAD_STATUS_CHANGE = "game_download_status_change";

    private static final String NAME_GAME_ID = "name_game_id";
    private static final String NAME_PACKAGENAME = "name_package";
    private static final String NAME_PERCENTAGE = "name_percentage";
    private static final String DOWNLOAD_TYPE = "download_type";

    private HashSet<Long> mGameIdSet = new HashSet<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        if(null == intent){
            return;
        }

        long gameId = intent.getLongExtra(NAME_GAME_ID, 0);
        String packageName = intent.getStringExtra(NAME_PACKAGENAME);
        int progress = intent.getIntExtra(NAME_PERCENTAGE, 0);
        int type = intent.getIntExtra(DOWNLOAD_TYPE, 0);

        if(gameId == 0
                || type == 0) {
            return;
        }

        MyLog.d(TAG,"WatchGameDownloadBroadcastReceiver"
                + " packageName=" + packageName
                + " gameId=" + gameId
                + ",type" + type
                + ",progress: " + progress);

        CustomDownloadManager.ApkStatusEvent apkStatusEvent = new CustomDownloadManager.ApkStatusEvent(type);
        apkStatusEvent.progress = progress;
        apkStatusEvent.gameId = gameId;
        apkStatusEvent.isByGame = true;
        EventBus.getDefault().post(apkStatusEvent);
    }

    public void add(long gameId) {
        if(!mGameIdSet.contains(gameId)) {
            unRegister();
            mGameIdSet.add(gameId);
            initRegister();
        }
    }

    public void initRegister() {
        IntentFilter iif = new IntentFilter();
        if(!mGameIdSet.isEmpty()) {
            Iterator<Long> iterator = mGameIdSet.iterator();
            while (iterator.hasNext()) {
                Long gameId = iterator.next();
                if(gameId != 0) {
                    iif.addAction(ACTION_GAME_DOWNLOAD_STATUS_CHANGE + gameId);
                }
            }
            iif.addAction(ACTION_GAME_DOWNLOAD_STATUS_CHANGE);
        }
        try{
            GlobalData.app().registerReceiver(this, iif);
        }catch(Exception e){
            MyLog.d("", e);
        }
    }

    public void unRegister() {
        try{
            GlobalData.app().unregisterReceiver(this);
        }catch(Exception e){
            MyLog.d("", e);
        }
    }
}
