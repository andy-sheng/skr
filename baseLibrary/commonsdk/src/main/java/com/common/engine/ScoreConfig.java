package com.common.engine;

import com.common.utils.U;

public class ScoreConfig {
    static int mConfig;

    static {
        mConfig = U.getPreferenceUtils().getSettingInt("score_config", 0x0002 | 0x0004 );
    }

    public static void setMelpEnable(boolean enable) {
        if (enable) {
            mConfig = (mConfig | 0x0001);
        } else {
            mConfig = (mConfig ^ 0x0001);
        }
        U.getPreferenceUtils().setSettingInt("score_config", mConfig);
    }

    public static void setAcrEnable(boolean enable) {
        if (enable) {
            mConfig = (mConfig | 0x0002);
        } else {
            mConfig = (mConfig ^ 0x0002);
        }
        U.getPreferenceUtils().setSettingInt("score_config", mConfig);
    }

    public static void setMelp2Enable(boolean enable) {
        if (enable) {
            mConfig = (mConfig | 0x0004);
        } else {
            mConfig = (mConfig ^ 0x0004);
        }
        U.getPreferenceUtils().setSettingInt("score_config", mConfig);
    }

    public static boolean isAcrEnable() {
        return (mConfig & 0x0002) == 0x0002;
    }

    public static boolean isMelpEnable() {
        return (mConfig & 0x0001) == 0x0001;
    }

    public static boolean isMelp2Enable() {
        return (mConfig & 0x0004) == 0x0004;
    }

    public static String getDesc() {
        StringBuilder sb = new StringBuilder();
        if (isAcrEnable()) {
            sb.append("ACR").append("+");
        }
        if (isMelpEnable()) {
            sb.append("MELP").append("+");
        }
        if (isMelp2Enable()) {
            sb.append("MELP2").append("+");
        }
        if (sb.toString().length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        } else {
            return "无";
        }
    }
}
