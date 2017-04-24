package com.mi.liveassistant.room.utils;

import com.mi.liveassistant.common.preference.PreferenceKey;
import com.mi.liveassistant.common.preference.PreferenceUtils;
import com.mi.liveassistant.engine.base.GalileoConstants;

/**
 * Created by yangli on 2017/3/11.
 */
public class MagicParamUtils {
    private static final String TAG = "MagicParamUtils";

    public static void saveBeautyLevel(int level) {
        PreferenceUtils.setSettingInt(PreferenceKey.PREF_KEY_FACE_BEAUTY_LEVEL, level);
    }

    public static int getBeautyLevel() {
        return PreferenceUtils.getSettingInt(
                PreferenceKey.PREF_KEY_FACE_BEAUTY_LEVEL, GalileoConstants.BEAUTY_LEVEL_HIGH);
    }

    public static void saveFilterIntensity(int intensity) {
        PreferenceUtils.setSettingInt(PreferenceKey.PREF_KEY_FILTER_INTENSITY, intensity);
    }

    public static int getFilterIntensity() {
        return PreferenceUtils.getSettingInt(PreferenceKey.PREF_KEY_FILTER_INTENSITY, 100);
    }
}
