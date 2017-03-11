package com.wali.live.livesdk.live.component.utils;

import com.base.global.GlobalData;
import com.base.preference.PreferenceUtils;
import com.mi.live.engine.base.GalileoConstants;

/**
 * Created by yangli on 2017/3/11.
 */
public class MagicParamUtils {
    private static final String TAG = "MagicParamUtils";

    public static void saveBeautyLevel(int beautyLevel) {
        PreferenceUtils.setSettingInt(GlobalData.app(), PreferenceUtils.PREF_KEY_FACE_BEAUTY_LEVEL, beautyLevel);
    }

    public static int getBeautyLevel() {
        return PreferenceUtils.getSettingInt(GlobalData.app(), PreferenceUtils.PREF_KEY_FACE_BEAUTY_LEVEL, GalileoConstants.BEAUTY_LEVEL_HIGH);
    }

    public static void saveFilterIntensity(int intensity) {
        PreferenceUtils.setSettingInt(GlobalData.app(), PreferenceUtils.PREF_KEY_FILTER_INTENSITY, intensity);
    }

    public static int getFilterIntensity() {
        return PreferenceUtils.getSettingInt(GlobalData.app(), PreferenceUtils.PREF_KEY_FILTER_INTENSITY, 100);
    }

    public static void saveFilterCategory(String category) {
        PreferenceUtils.setSettingString(GlobalData.app(), PreferenceUtils.PREF_KEY_FILTER_CATEGORY, category);
    }

    public static String getFilterCategory() {
        return PreferenceUtils.getSettingString(GlobalData.app(), PreferenceUtils.PREF_KEY_FILTER_CATEGORY, "");
    }
}
