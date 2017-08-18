package com.base.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.base.global.GlobalData;
import com.base.log.MyLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class PreferenceUtils {
    private static final String TAG = PreferenceUtils.class.getSimpleName();
    private static final ArrayList<PrefObserver> sPrefObs = new ArrayList<>();

    public static final String KEY_RELEASE_CHANNEL = "pref_channel";

    // 生成attachment_id
    public static final String PREF_KEY_ATTACHMENT_BASE_ID = "pref_key_attachment_base_id";

    // 是否开启debug信息，boolean值
    public static final String KEY_DEBUG_INFO = "pref_debug_info";
    // 是否开启媒体参数调节界面，boolean值
    public static final String KEY_DEBUG_MEDIA_INFO = "pref_debug_media_info";
    // 是否开启低码率，boolean值
    public static final String KEY_DEBUG_DROP_BITRATE = "pref_debug_drop_bitrate";

    public static final String KEY_DEBUG_FIX_STREAMER = "pref_debug_fix_streamer";

    public static final String KEY_LIVE_ID = "key_live_id";

    public static final String KEY_LIVE_TIMESTAMP = "key_live_timestamp";

    public static final String KEY_LIVE_CLARITY = "key_live_clarity";

    public static final String PREF_KEY_UNZIP_MEDIA_FILE = "pref_key_unzip_media_file";

    public static final String PREF_KEY_MEDIA_FILE_VERSION_CODE = "pref_key_media_file_version_code";

    public static final String PREF_KEY_FACE_BEAUTY_LEVEL = "pref_key_face_beauty_level"; //保存上次选中的美颜等级

    public static final String PREF_KEY_FILTER_INTENSITY = "pref_key_filter_intensity";

    public static final String PREF_KEY_FILTER_CATEGORY = "pref_key_filter_category";

    public static final String KEY_ACCEPT_CONVENTION = "key_accept_convention";

    public static final String KEY_FEED_NOTIFY_UNREAD_COUNT = "key_feed_notify_unread_count";

    public static final String KEY_FEED_NOTIFY_AVATAR_URL = "key_feed_notify_avatar_url";

    public static final String KET_HAS_NEW_FEEDS = "ket_has_new_feeds";

    public static final String KEY_LAST_SYNC_NEW_FEEDS_TIME = "key_last_sync_new_feeds_time";

    public static final String KEY_CREATE_FAILED_FEEDS_ID = "key_create_failed_feeds_id";

    public static final String PREF_KEY_SIXIN_SYNC_PAGE_ID = "pref_sixin_sync_page_id";

    public static final String PREF_ANCHOR_RANK_LIST_CONFIG = "pref_anchor_rank_list_config";

    public static final String PREF_ANCHOR_RANK_LIST_CONFIG_TIME = "pref_anchor_rank_list_config_time";

    //搜索框显示的文案
    public static final String PREF_FUZZY_SEARCH_DEFAULT_KEY = "fuzzy_search_default_key_list";

    //搜索框显示的文案以及对应的搜索词
    public static final String PREF_FUZZY_SEARCH_DEFAULT_TEXT = "fuzzy_search_default_text_list";

    public static final String PREF_KEY_LIVE_NORMAL_TAG = "pref_key_live_normal_tag";
    public static final String PREF_KEY_LIVE_GAME_TAG = "pref_key_live_game_tag";
    public static final String PREF_KEY_LIVE_GAME_CLARITY = "pref_key_live_game_clarity";

    public interface PrefObserver {
        void notifyPrefChange(final String key, Object value);
    }

    public static void addPrefObserver(final PrefObserver ob) {
        sPrefObs.add(ob);
    }

    public static void removePrefObserver(final PrefObserver obToRm) {
        for (final PrefObserver ob : sPrefObs) {
            if (ob == obToRm) {
                sPrefObs.remove(ob);
                break;
            }
        }
    }

    public static void notifyPrefChange(final String key, final Object value) {
        for (final PrefObserver ob : sPrefObs) {
            ob.notifyPrefChange(key, value);
        }
    }

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

    public static void setSettingInt(final String key, final int value) {
        PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).edit().putInt(key, value).apply();
    }

    public static void clear(final Context c, final String key) {
        PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).edit().remove(key).apply();
    }


    public static void increaseSettingInt(final String key) {
        increaseSettingInt(PreferenceManager.getDefaultSharedPreferences(GlobalData.app()), key);
    }

    public static int getSettingInt(final String key, final int defaultValue) {
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

    public static void setSettingLong(final String key, final long value) {
        PreferenceManager.getDefaultSharedPreferences(GlobalData.app()).edit().putLong(key, value).apply();
    }

    public static long getSettingLong(final String key, final long defaultValue) {
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
