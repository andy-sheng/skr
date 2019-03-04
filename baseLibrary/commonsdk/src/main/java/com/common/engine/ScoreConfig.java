package com.common.engine;

import android.support.v4.util.Pair;

import com.common.utils.U;

public class ScoreConfig {
    static final int mConfig;

    static {
        mConfig = U.getPreferenceUtils().getSettingInt("score_config", 3);
    }

    public static void setPrefConfig(int config) {
        U.getPreferenceUtils().setSettingInt("score_config", config);
    }

    public static boolean isAcrEnable() {
        return mConfig == 3 || mConfig == 2;
    }

    public static boolean isMelpEnable() {
        return mConfig == 3 || mConfig == 1;
    }

    public static String getDesc() {
        if (mConfig == 3) {
            return "ACR+MELP";
        } else if (mConfig == 2) {
            return "仅ACR";
        } else if (mConfig == 1) {
            return "仅MELP";
        }
        return null;
    }
}
