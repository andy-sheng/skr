package com.wali.live.sdk.manager;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.wali.live.sdk.manager.toast.ToastUtils;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkServiceProxy;
import com.wali.live.watchsdk.watch.model.RoomInfo;
import com.wali.live.sdk.manager.global.GlobalData;
import com.wali.live.sdk.manager.uri.LiveUriUtils;
import com.wali.live.sdk.manager.version.VersionCheckManager;
import com.wali.live.sdk.manager.version.VersionCheckTask;

import java.util.HashMap;

/**
 * Created by chengsimin on 2016/12/8.
 */
public class MiLiveSdkController {
    public static final String SCHEMA_APP = "walilive";
    public static final String SCHEMA_SDK = "walilivesdk";
    public final static String TAG = MiLiveSdkController.class.getSimpleName();

    /**
     * 标识唤起方
     */
    private static int CHANNEL_ID = 0;

    static HashMap<Integer, String> map = new HashMap<Integer, String>();

    static {
        map.put(50000, "com.wali.live.sdk.manager.demo");
        map.put(50001, "com.wali.live.sdk.manager.demo");

        map.put(50010, "com.xiaomi.gamecenter");
        map.put(50011, "com.xiaomi.gamecenter.dev");
    }

    /**
     * 确保在使用 sdk插件 前 init 一下。
     * @param app
     * @param channelId
     */
    public static void init(Application app, int channelId) {
        GlobalData.setApplication(app);
        if (!GlobalData.app().getPackageName().equals(map.get(channelId))) {
            throw new RuntimeException("channelid error,unregister channelid for milivesdk,throw exception,");
        }else{
            CHANNEL_ID = channelId;
        }
    }

    private static void checkHasInit(){
        if(CHANNEL_ID ==0){
            throw new RuntimeException("CHANNEL_ID==0,check MiLiveSdkController.init(...) be called.");
        }
        MiLiveSdkServiceProxy.getInstance().tryInit();
    }

    public static int getChannelId() {
        return CHANNEL_ID;
    }

    /**
     * 只为测试使用，正常不需要
     * @param activity
     */
    public static void openRandomLive(Activity activity) {
        checkHasInit();
        Intent intent = new Intent(Intent.ACTION_VIEW);

        String packageName = VersionCheckManager.PACKAGE_NAME;
        String className = "com.wali.live.JumpTestSdkActivity";
        intent.setClassName(packageName, className);
        Bundle bundle = new Bundle();
        bundle.putInt("extra_channel_id", CHANNEL_ID);
        bundle.putString("extra_packagename", GlobalData.app().getPackageName());
        intent.putExtras(bundle);
        if (!go(activity, intent)) {
            checkSdkUpdate(activity, true, true);
        }
    }

    /**
     * 显示调用，指定跳到直播
     *
     * @param activity
     * @param roomInfo
     */
    public static void openLive(final Activity activity, RoomInfo roomInfo) {
        checkHasInit();
        roomInfo.setmChannelId(CHANNEL_ID);
        roomInfo.setPackageName(GlobalData.app().getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW);

        String packageName = VersionCheckManager.PACKAGE_NAME;
        String className = "com.wali.live.watchsdk.watch.WatchSdkActivity";
        intent.setClassName(packageName, className);
        Bundle bundle = new Bundle();
        bundle.putParcelable("extra_room_info", roomInfo);
        intent.putExtras(bundle);
        if (!go(activity, intent)) {
            checkSdkUpdate(activity, true, true);
        }
    }

    public static void openLive(RoomInfo roomInfo){
        if (hasInstallLiveSdk(GlobalData.app())) {
            MiLiveSdkServiceProxy.getInstance().openLive(roomInfo);
        } else {
            ToastUtils.showToast("未安装小米直播插件");
        }
    }

    public static void startGameLive(final Activity activity) {
        checkHasInit();
        Intent intent = new Intent(Intent.ACTION_VIEW);

        String packageName = VersionCheckManager.PACKAGE_NAME;
        String className = "com.wali.live.livesdk.live.LiveSdkActivity";
        intent.setClassName(packageName, className);
        Bundle bundle = new Bundle();
        bundle.putInt("extra_channel_id", CHANNEL_ID);
        bundle.putString("extra_package_name", GlobalData.app().getPackageName());
        intent.putExtras(bundle);
        if (!go(activity, intent)) {
            checkSdkUpdate(activity, true, true);
        }
    }

//    public static void openLive(final Activity activity, RoomInfo roomInfo) {
//        String url = String.format("walilive_app_sdk://room/join?liveid=%s&playerid=%s&videourl=%s&query_room_info=false&after_live_end=0",
//                roomInfo.getRoomId(),
//                roomInfo.getPlayerId(),
//                roomInfo.getVideoUrl());
//        goLiveBySchema(activity, url);
//    }

    /**
     * 显示调用，指定跳到回放
     *
     * @param activity
     * @param roomInfo
     */
    public static void openPlayback(final Activity activity, RoomInfo roomInfo) {
        checkHasInit();
        roomInfo.setmChannelId(CHANNEL_ID);
        roomInfo.setPackageName(GlobalData.app().getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW);

        String packageName = VersionCheckManager.PACKAGE_NAME;
        String className = "com.wali.live.watchsdk.watch.ReplaySdkActivity";
        intent.setClassName(packageName, className);
        Bundle bundle = new Bundle();
        bundle.putParcelable("extra_room_info", roomInfo);
        intent.putExtras(bundle);
        if (!go(activity, intent)) {
            checkSdkUpdate(activity, true, true);
        }
    }

    /**
     * @param activity
     * @param schema   walilive 只跳直播，walilivesdk只跳sdk，walilive_app_sdk优先跳app，walilive_sdk_app,优先跳sdk
     */
    public static void goLiveBySchema(final Activity activity, String schema) {
        final Intent intent = new Intent();
        Uri uri = Uri.parse(schema);
        String head = uri.getScheme();
        if (head.equals(SCHEMA_APP)) {
            // 只跳app，没啥好说的
            intent.setData(uri);
            go(activity, intent);
            return;
        }
        if (head.equals(SCHEMA_SDK)) {
            // 只跳sdk，
            intent.setData(uri);
            if (!go(activity, intent)) {
                checkSdkUpdate(activity, true, true);
            }
            return;
        }
        if (head.equals("walilive_app_sdk")) {
            intent.setData(LiveUriUtils.turnAppUri(uri));
            // 判断这个schema 有没有直播app响应。
            if (!go(activity, intent)) {
                Log.d(TAG, "componentName is null");
                intent.setData(LiveUriUtils.turnSdkUri(uri));
                if (!go(activity, intent)) {
                    checkSdkUpdate(activity, true, true);
                }
            }
            return;
        }
        if (head.equals("walilive_sdk_app")) {
            intent.setData(LiveUriUtils.turnSdkUri(uri));
            // 判断这个schema 有没有直播app响应。
            if (!go(activity, intent)) {
                Log.d(TAG, "componentName is null");
                intent.setData(LiveUriUtils.turnAppUri(uri));
                if (!go(activity, intent)) {
                    checkSdkUpdate(activity, true, true);
                }
            }
            return;
        }
    }

    // 跳转
    private static boolean go(Context activity, Intent intent) {
        Log.d(TAG, "uri:" + intent.getDataString());
        if (intent.resolveActivity(GlobalData.app().getPackageManager()) != null) {
            try {
                activity.startActivity(intent);
            } catch (Exception e) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    // 强制检查更新
    public static void checkSdkUpdate(Activity context, boolean isManualCheck, boolean isNeedDialog) {
        new VersionCheckTask(context, isManualCheck, isNeedDialog).execute();
    }

    /**
     * 使用eventbus 接收 MiLiveSdkEvent.LoginResult 来监听登录事件的回调
     *
     * @param authcode
     */
    public static void loginByMiAccount(String authcode) {
        if (hasInstallLiveSdk(GlobalData.app())) {
            MiLiveSdkServiceProxy.getInstance().loginByMiAccount(authcode);
        } else {
            ToastUtils.showToast("未安装小米直播插件");
        }
    }
    public static void loginByMiAccountSso(long miid, String authCode) {
        if (hasInstallLiveSdk(GlobalData.app())) {
            MiLiveSdkServiceProxy.getInstance().loginByMiAccountSso(miid,authCode);
        } else {
            ToastUtils.showToast("未安装小米直播插件");
        }
    }

    /**
     * 清空账号
     */
    public static void clearAccount() {
        if (hasInstallLiveSdk(GlobalData.app())) {
            MiLiveSdkServiceProxy.getInstance().clearAccount();
        } else {
            ToastUtils.showToast("未安装小米直播插件");
        }
    }

    /**
     * 判断该手机中是否安装的直播助手
     */
    private static boolean hasInstallLiveSdk(Context context) {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(
                    VersionCheckManager.PACKAGE_NAME, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return pInfo != null;
    }
}
