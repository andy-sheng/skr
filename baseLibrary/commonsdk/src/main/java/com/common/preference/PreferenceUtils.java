package com.common.preference;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.common.log.MyLog;
import com.common.utils.U;

import java.util.Iterator;
import java.util.Map;

public class PreferenceUtils {
    private static final String TAG = "PreferenceUtils";

    public static void setSettingString(String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(U.app()).edit().putString(key, value);
    }

    public static String getSettingString(String key, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(U.app()).getString(key, defaultValue);
    }

    public static void setSettingBoolean(String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(U.app()).edit().putBoolean(key, value).apply();
    }

    public static boolean getSettingBoolean(String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(U.app()).getBoolean(key, defaultValue);
    }

    public static void setSettingInt(String key, int value) {
        PreferenceManager.getDefaultSharedPreferences(U.app()).edit().putInt(key, value).apply();
    }

    public static int getSettingInt(String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(U.app()).getInt(key, defaultValue);
    }

    public static void setSettingFloat(String key, float value) {
        PreferenceManager.getDefaultSharedPreferences(U.app()).edit().putFloat(key, value).apply();
    }

    public static float getSettingFloat(String key, float defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(U.app()).getFloat(key, defaultValue);
    }

    public static void setSettingLong(String key, long value) {
        PreferenceManager.getDefaultSharedPreferences(U.app()).edit().putLong(key, value).apply();
    }

    public static long getSettingLong(String key, long defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(U.app()).getLong(key, defaultValue);
    }

    public static boolean hasKey(String key) {
        return PreferenceManager.getDefaultSharedPreferences(U.app()).contains(key);
    }

    public static void removePreference(String key) {
        PreferenceManager.getDefaultSharedPreferences(U.app()).edit().remove(key).apply();
    }

    /**
     * 支持自定义 SharedPreferences
     * name
     * mode 取值包括
     * Context.MODE_PRIVATE 应用内访问
     * Context.MODE_APPEND 文件存在,在文件中写入,不存在则创建
     * Context.MODE_WORLD_READABLE 当前文件可以被其他应用读取
     * Context.MODE_WORLD_WRITEABLE 当前文件可以被其他应用写入
     */
    public static void setSettingString(SharedPreferences sp, String key, String value) {
        sp.edit().putString(key, value);
    }

    public static String getSettingString(SharedPreferences sp, String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public static void setSettingBoolean(SharedPreferences sp, String key, boolean value) {
        sp.edit().putBoolean(key, value);
    }

    public static boolean getSettingBoolean(SharedPreferences sp, String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    public static void setSettingInt(SharedPreferences sp, String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    public static int getSettingInt(SharedPreferences sp, String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    public static void setSettingFloat(SharedPreferences sp, String key, float value) {
        sp.edit().putFloat(key, value).apply();
    }

    public static float getSettingFloat(SharedPreferences sp, String key, float defaultValue) {
        return sp.getFloat(key, defaultValue);
    }

    public static void setSettingLong(SharedPreferences sp, String key, long value) {
        sp.edit().putLong(key, value).apply();
    }

    public static long getSettingLong(SharedPreferences sp, String key, long defaultValue) {
        return sp.getLong(key, defaultValue);
    }

    public static void clearPreference(SharedPreferences sp) {
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    public static void dumpDefaultPreference() {
        dumpPreference(PreferenceManager.getDefaultSharedPreferences(U.app()), "default preference");
    }

    public static void dumpDefaultPreference(SharedPreferences sp, String name) {
        dumpPreference(sp, name);
    }

    private static void dumpPreference(SharedPreferences sp, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("\n");
        Map<String, ?> preferences = sp.getAll();
        Iterator<String> iterator = preferences.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            sb.append(key);
            sb.append(":");
            sb.append(preferences.get(key));
            sb.append("\n");
        }
        MyLog.w(sb.toString());
    }
}
