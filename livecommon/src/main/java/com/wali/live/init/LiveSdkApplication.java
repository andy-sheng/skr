package com.wali.live.init;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.Constants;
import com.base.utils.channel.ReleaseChannelUtils;
import com.base.utils.language.LocaleUtil;
import com.base.utils.version.VersionManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.milink.sdk.base.Global;
import com.mi.milink.sdk.data.ClientAppInfo;
import com.xsj.crasheye.Crasheye;

/**
 * Created by lan on 16/7/20.
 *
 * @description 第三方应用可以继承WatchSdkApplication，或者直接使用InitManager
 */
public class LiveSdkApplication extends Application {

    private static final String TAG = "LiveSdkApplication";

    @Override
    public void onCreate() {
        GlobalData.setApplication(this);
        super.onCreate();
        Global.init(this, getClientAppInfo());
        UserAccountManager.getInstance().initAnonymous();
        initCrasheye();
        GlobalData.initialize(this);
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            MultiDex.install(this);
        } catch (Exception e) {
            MyLog.e(e);
        }
    }

    private ClientAppInfo getClientAppInfo() {
        return new ClientAppInfo.Builder(Constants.MILINK_APP_ID)
                .setAppName(Constants.APPNAME)
                .setPackageName(getPackageName())
                .setReleaseChannel(Constants.ReleaseChannel)
                .setVersionName(VersionManager.getVersionName(GlobalData.app()))
                .setVersionCode(VersionManager.getCurrentVersionCode(GlobalData.app()))
                .setLanguageCode(LocaleUtil.getLanguageCode())
                .setServiceProcessName(getPackageName() + ":remote")
                .build();
    }

    private void initCrasheye() {
        Crasheye.init(GlobalData.app(), Constants.CRASHEYE_APPID);
        Crasheye.setChannelID(ReleaseChannelUtils.getReleaseChannel());
        Crasheye.setUserIdentifier(UserAccountManager.getInstance().getUuid());
    }
}
