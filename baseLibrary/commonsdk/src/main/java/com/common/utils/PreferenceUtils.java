package com.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.common.log.MyLog;
import com.common.utils.U;

import java.util.Iterator;
import java.util.Map;

public class PreferenceUtils {
    private static final String TAG = "PreferenceUtils";

    SharedPreferences mSharedPreferencesSp2 = U.app().getSharedPreferences(U.getAppInfoUtils().getAppName()+"_sp2",Context.MODE_PRIVATE);

    PreferenceUtils() {

    }

    /**
     * 私有的pref 这个Pref退出账号时不会清空
     * 伴随整个 APP 生命周期
     * @return
     */
    public SharedPreferences longlySp() {
        return mSharedPreferencesSp2;
    }

    public void setSettingString(String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(U.app()).edit().putString(key, value).apply();
    }

    public String getSettingString(String key, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(U.app()).getString(key, defaultValue);
    }

    public void setSettingBoolean(String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(U.app()).edit().putBoolean(key, value).apply();
    }

    public boolean getSettingBoolean(String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(U.app()).getBoolean(key, defaultValue);
    }

    public void setSettingInt(String key, int value) {
        PreferenceManager.getDefaultSharedPreferences(U.app()).edit().putInt(key, value).apply();
    }

    public int getSettingInt(String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(U.app()).getInt(key, defaultValue);
    }

    public void setSettingFloat(String key, float value) {
        PreferenceManager.getDefaultSharedPreferences(U.app()).edit().putFloat(key, value).apply();
    }

    public float getSettingFloat(String key, float defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(U.app()).getFloat(key, defaultValue);
    }

    public void setSettingLong(String key, long value) {
        PreferenceManager.getDefaultSharedPreferences(U.app()).edit().putLong(key, value).apply();
    }

    public long getSettingLong(String key, long defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(U.app()).getLong(key, defaultValue);
    }

    public boolean hasKey(String key) {
        return PreferenceManager.getDefaultSharedPreferences(U.app()).contains(key);
    }

    public void removePreference(String key) {
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
    public void setSettingString(SharedPreferences sp, String key, String value) {
        sp.edit().putString(key, value).apply();
    }

    public String getSettingString(SharedPreferences sp, String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public void setSettingBoolean(SharedPreferences sp, String key, boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }

    public boolean getSettingBoolean(SharedPreferences sp, String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    public void setSettingInt(SharedPreferences sp, String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    public int getSettingInt(SharedPreferences sp, String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    public void setSettingFloat(SharedPreferences sp, String key, float value) {
        sp.edit().putFloat(key, value).apply();
    }

    public float getSettingFloat(SharedPreferences sp, String key, float defaultValue) {
        return sp.getFloat(key, defaultValue);
    }

    public void setSettingLong(SharedPreferences sp, String key, long value) {
        sp.edit().putLong(key, value).apply();
    }

    public long getSettingLong(SharedPreferences sp, String key, long defaultValue) {
        return sp.getLong(key, defaultValue);
    }

    public void clearPreference(SharedPreferences sp) {
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    public void clearPreference() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(U.app()).edit();
        editor.clear();
        editor.apply();
    }

    public void dumpDefaultPreference() {
        dumpPreference(PreferenceManager.getDefaultSharedPreferences(U.app()), "default preference");
    }

    public void dumpDefaultPreference(SharedPreferences sp, String name) {
        dumpPreference(sp, name);
    }

    private void dumpPreference(SharedPreferences sp, String name) {
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
