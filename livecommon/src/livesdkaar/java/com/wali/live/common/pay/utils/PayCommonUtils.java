package com.wali.live.common.pay.utils;

import android.content.Context;

import com.base.global.GlobalData;
import com.base.preference.PreferenceUtils;
import com.base.utils.language.LocaleUtil;

/**
 * Created by chengsimin on 2016/12/6.
 */

public class PayCommonUtils {

    public static String getLanguageCode4PayPal() {
        String languageCode = LocaleUtil.getLanguageCode();
        switch (languageCode) {
            case "zh_CN":
                return "zh-Hans";
            case "zh_TW":
                return "zh-Hant_TW";
            default:
                return languageCode;
        }
    }

    // 小米钱包相关
    public static final String SP_FILE_NAME_MIWALLET = "miwallet";
    public static final String SP_KEY_MIWALLET_LOGIN_ACCOUNT_TYPE = "login.account.type";// 第一次使用小米钱包时，做出的选择，1：选择使用系统小米账号，2：没有选择使用系统小米账号
    public static final int LOGIN_ACCOUNT_TYPE_NONE = 0;
    public static final int LOGIN_ACCOUNT_TYPE_SYSTEM = 1;
    public static final int LOGIN_ACCOUNT_TYPE_OTHER = 2;

    public static int getMiWalletLoginAccountType() {
        return PreferenceUtils.getSettingInt(GlobalData.app().getSharedPreferences(SP_FILE_NAME_MIWALLET, Context.MODE_PRIVATE),
                SP_KEY_MIWALLET_LOGIN_ACCOUNT_TYPE, LOGIN_ACCOUNT_TYPE_NONE);
    }

    public static void setMiWalletLoginAccountType(int loginAccountType) {
        PreferenceUtils.setSettingInt(GlobalData.app().getSharedPreferences(SP_FILE_NAME_MIWALLET, Context.MODE_PRIVATE),
                SP_KEY_MIWALLET_LOGIN_ACCOUNT_TYPE, loginAccountType);
    }

}
