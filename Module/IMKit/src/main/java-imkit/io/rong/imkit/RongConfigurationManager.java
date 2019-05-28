//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build.VERSION;

import java.util.Locale;

import io.rong.imkit.utilities.LangUtils;
import io.rong.imkit.utilities.LangUtils.RCLocale;
import io.rong.imlib.RongIMClient.PushLanguage;

public class RongConfigurationManager {
    private static String RONG_CONFIG = "RongKitConfiguration";
    private static String FILE_MAX_SIZE = "FileMaxSize";
    private static boolean isInit = false;

    private RongConfigurationManager() {
    }

    public static io.rong.imkit.RongConfigurationManager getInstance() {
        return io.rong.imkit.RongConfigurationManager.SingletonHolder.sInstance;
    }

    public static void init(Context context) {
        if (!isInit) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.LOCALE_CHANGED");
            context.registerReceiver(new io.rong.imkit.RongConfigurationManager.SystemConfigurationChangedReceiver(), filter);
            RCLocale locale = getInstance().getAppLocale(context);
            getInstance().switchLocale(locale, context);
            isInit = true;
        }

    }

    public void setFileMaxSize(Context context, int size) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(RONG_CONFIG, 0);
        Editor editor = sharedPreferences.edit();
        editor.putInt(FILE_MAX_SIZE, size).apply();
    }

    public int getFileMaxSize(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(RONG_CONFIG, 0);
        return sharedPreferences.getInt(FILE_MAX_SIZE, 100);
    }

    public void switchLocale(RCLocale locale, Context context) {
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.locale = locale.toLocale();
        if (VERSION.SDK_INT < 24) {
            context.getResources().updateConfiguration(config, resources.getDisplayMetrics());
        }

        LangUtils.saveLocale(context, locale);
    }

    public Context getConfigurationContext(Context context) {
        return LangUtils.getConfigurationContext(context);
    }

    public RCLocale getAppLocale(Context context) {
        return LangUtils.getAppLocale(context);
    }

    public Locale getSystemLocale() {
        return LangUtils.getSystemLocale();
    }

    public PushLanguage getPushLanguage(Context context) {
        return LangUtils.getPushLanguage(context);
    }

    public void setPushLanguage(Context context, PushLanguage pushLanguage) {
        LangUtils.setPushLanguage(context, pushLanguage);
    }

    private static class SystemConfigurationChangedReceiver extends BroadcastReceiver {
        private SystemConfigurationChangedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                LangUtils.setSystemLocale(Locale.getDefault());
                RCLocale appLocale = LangUtils.getAppLocale(context);
                Locale systemLocale = LangUtils.getSystemLocale();
                if (!appLocale.toLocale().equals(systemLocale)) {
                    io.rong.imkit.RongConfigurationManager.getInstance().switchLocale(appLocale, context);
                }
            }

        }
    }

    private static class SingletonHolder {
        static io.rong.imkit.RongConfigurationManager sInstance = new io.rong.imkit.RongConfigurationManager();

        private SingletonHolder() {
        }
    }
}
