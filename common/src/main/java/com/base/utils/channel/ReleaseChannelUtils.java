package com.base.utils.channel;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.preference.PreferenceUtils;
import com.base.utils.CommonUtils;
import com.base.utils.Constants;

/**
 * @module 基础类
 */
public class ReleaseChannelUtils {
    private static String DEFAULT_CHANNEL = "DEFAULT";
    private static String curChannel;

    public static String getReleaseChannel() {
        if (TextUtils.isEmpty(curChannel)) {
            if (!Constants.ReleaseChannel.equals(DEFAULT_CHANNEL)) {
                if (Constants.isDefaultChanel) {
                    curChannel = Constants.DEBUG_CHANNEL;
                } else {
                    curChannel = Constants.ReleaseChannel;
                }
                if (GlobalData.app() != null) {
                    PreferenceUtils.setSettingString(GlobalData.app(),
                            PreferenceUtils.KEY_RELEASE_CHANNEL, Constants.ReleaseChannel);
                }
            } else {
                if (GlobalData.app() != null) {
                    curChannel =  DEFAULT_CHANNEL;
                    PreferenceUtils.setSettingString(GlobalData.app(),
                            PreferenceUtils.KEY_RELEASE_CHANNEL, DEFAULT_CHANNEL);
                }
            }
            if (curChannel.equals("3000_1_android")
                    && !CommonUtils.isChineseLocale(GlobalData.app())) {
                // 如果是android market的渠道，并且语言不是中文，更改渠道为android market国际版
                curChannel = "3000_2_android";
            }
        }

        return curChannel;
    }


    /**
     * 是否是工信部的包
     *
     * @return
     */
    public static boolean isMIUICTAPkg() {
        return "5005_1_android".equalsIgnoreCase(Constants.ReleaseChannel) || "meng_1332_1_android".equalsIgnoreCase(Constants.ReleaseChannel);
    }

}
