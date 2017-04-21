package com.mi.liveassistant.common.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.common.global.GlobalData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class PreferenceUtils {
    private static final String TAG = PreferenceUtils.class.getSimpleName();

    // 是否开启媒体参数调节界面，boolean值
    public static final String KEY_DEBUG_MEDIA_INFO = "pref_debug_media_info";

    public static void setSettingString(final SharedPreferences sp, final String key, final String value) {
        sp.edit().putString(key, value).apply();
    }

    public static String getSettingString(final SharedPreferences sp, final String key, final String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public static String getSettingString(final Context c, final String key,
                                          final String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).getString(key, defaultValue);
    }

    public static void setSettingString(final Context c, final String key, final String value) {
        PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).edit().putString(key, value).apply();
    }

    public static void setSettingSet(final Context c, final String key, final Set<String> value) {
        PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).edit().putStringSet(key, value).apply();
    }

    public static Set<String> getSettingSet(final Context c, final String key, final Set<String> defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).getStringSet(key, defaultValue);
    }

    public static void setSettingBoolean(final SharedPreferences sp, final String key, final boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }

    public static boolean getSettingBoolean(final SharedPreferences sp, final String key, final boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    public static boolean getSettingBoolean(final Context c, final String key,
                                            final boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).getBoolean(key, defaultValue);
    }

    public static void setSettingBoolean(final Context c, final String key, final boolean value) {
        PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).edit().putBoolean(key, value).apply();
    }

    public static void setSettingInt(final Context c, final String key, final int value) {
        PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).edit().putInt(key, value).apply();
    }

    public static void clear(final Context c, final String key) {
        PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).edit().remove(key).apply();
    }


    public static void increaseSettingInt(final Context c, final String key) {
        increaseSettingInt(PreferenceManager.getDefaultSharedPreferences(GlobalData.app()), key);
    }

    public static int getSettingInt(final Context c, final String key, final int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).getInt(key, defaultValue);
    }

    public static void setSettingFloat(final SharedPreferences sp, final String key, final float value) {
        sp.edit().putFloat(key, value).apply();
    }

    public static float getSettingFloat(final SharedPreferences sp, final String key, final float defaultValue) {
        return sp.getFloat(key, defaultValue);
    }

    public static void setSettingFloat(final Context c, final String key, final float value) {
        PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).edit().putFloat(key, value).apply();
    }

    public static float getSettingFloat(final Context c, final String key, final float defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).getFloat(key, defaultValue);
    }

    public static void setSettingLong(final Context c, final String key, final long value) {
        PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).edit().putLong(key, value).apply();
    }

    public static long getSettingLong(final Context c, final String key, final long defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).getLong(key, defaultValue);
    }

    public static void setSettingLong(final SharedPreferences sp, final String key, final long value) {
        sp.edit().putLong(key, value).apply();
    }

    public static long getSettingLong(final SharedPreferences sp, final String key, final long defaultValue) {
        return sp.getLong(key, defaultValue);
    }

    public static void setSettingInt(final SharedPreferences sp, final String key, final int value) {
        sp.edit().putInt(key, value).apply();
    }

    public static int getSettingInt(final SharedPreferences sp, final String key, final int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    public static void increaseSettingInt(final SharedPreferences sp, final String key) {
        final int v = sp.getInt(key, 0) + 1;
        sp.edit().putInt(key, v).apply();
    }

    public static void increaseSettingInt(final SharedPreferences sp, final String key,
                                          final int increment) {
        final int v = sp.getInt(key, 0) + increment;
        sp.edit().putInt(key, v).apply();
    }

    public static void increaseSettingLong(final SharedPreferences sp, final String key,
                                           final long increment) {
        final long v = sp.getLong(key, 0) + increment;
        sp.edit().putLong(key, v).apply();
    }

    public static boolean hasKey(final Context c, final String key) {
        return PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).contains(key);
    }

    public static void removePreference(final Context context, final String key) {
        PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).edit().remove(key).apply();
    }

    public static void clearPreference(final SharedPreferences p) {
        final SharedPreferences.Editor editor = p.edit();
        editor.clear();
        editor.apply();
    }

    public static void dumpDefaultPreference(final Context context) {
        dumpPreference(PreferenceManager.getDefaultSharedPreferences(GlobalData.app()),
                "default preference:");
    }

    public static void dumpDefaultPreference(final Context context, final String preference) {
        dumpPreference(GlobalData.app().getSharedPreferences(preference, Context.MODE_PRIVATE), preference);
    }

    private static void dumpPreference(final SharedPreferences sp, final String name) {
        final StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append("\n");
        final Map<String, ?> preferenceSettings = sp.getAll();
        final Iterator<String> it = preferenceSettings.keySet().iterator();
        while (it.hasNext()) {
            final String key = it.next();
            sb.append(key);
            sb.append(":");
            sb.append(preferenceSettings.get(key));
            sb.append("\n");
        }
        MyLog.w(sb.toString());
    }
}
