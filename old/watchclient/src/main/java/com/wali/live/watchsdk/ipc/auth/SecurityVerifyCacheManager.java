package com.wali.live.watchsdk.ipc.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;

import org.json.JSONObject;

public class SecurityVerifyCacheManager {

    public final static String TAG = SecurityVerifyCacheManager.class.getSimpleName();

    public static final String PREF_SECURITY_VERIFY_CONFIG = "security_verify_config";
    public static final String SECURITY_VERIFY_CODE = "security_verify_config_code";

    public static final long DEFAULF_TIME = 10 * 60 * 60 * 1000;


    public static boolean checkSPreferences(int channelId, String packageName) {

        if(TextUtils.isEmpty(packageName) || channelId == 0){
            return false;
        }

        SharedPreferences settingPreferences = GlobalData.app().getSharedPreferences(
                PREF_SECURITY_VERIFY_CONFIG, Context.MODE_PRIVATE);
        String config = PreferenceUtils.getSettingString(settingPreferences, SECURITY_VERIFY_CODE + channelId, "");

        if( TextUtils.isEmpty(config)){
            return false;
        }
        try {
            JSONObject jsonObject = new JSONObject(config);
            int chaId = jsonObject.getInt("channelId");
            String packName = jsonObject.getString("packageName");
            long timeStamp = jsonObject.getLong("timestamp");
            if( (System.currentTimeMillis()-timeStamp) <= DEFAULF_TIME
                    && timeStamp!= 0
                    && channelId == chaId
                    && !TextUtils.isEmpty(packName)
                    && packageName.equals(packName)){
                return true;
            }
        } catch (Exception e) {
            MyLog.e(TAG, " checkSPreferences " + e);

        }

        return false;
    }

    public static void saveSPreferences(int channelId, String packageName,long timestamp){
        try {
            SharedPreferences settingPreferences = GlobalData.app().getSharedPreferences(
                    PREF_SECURITY_VERIFY_CONFIG, Context.MODE_PRIVATE);
            PreferenceUtils.setSettingString(settingPreferences, SECURITY_VERIFY_CODE + channelId, new JSONObject()
                    .put("channelId", channelId)
                    .put("packageName", packageName)
                    .put("timestamp", timestamp).toString());
        } catch (Exception e) {
            MyLog.d(TAG, "saveSPreferences" + e);
        }
    }
}
