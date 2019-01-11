package com.zq.level.utils;

import com.common.log.MyLog;
import com.component.busilib.R;
import com.zq.level.mode.UserLevelType;

public class LevelConfigUtils {

    public final static String TAG = "LevelConfigUtils";

    // 父段位资源
    public static int getImageResoucesLevel(int level) {
        switch (level) {
            case UserLevelType.SKRER_1_BUDDHA:
                return R.drawable.dazhanji;
            case UserLevelType.SKRER_2_POTENTIAL:
                return R.drawable.dazhanji;
            case UserLevelType.SKRER_3_SILVER:
                return R.drawable.dazhanji;
            case UserLevelType.SKRER_4_GOLD:
                return R.drawable.dazhanji;
            case UserLevelType.SKRER_5_PLATINUM:
                return R.drawable.dazhanji;
            case UserLevelType.SKRER_6_KING:
                return R.drawable.dazhanji;
            default:
                MyLog.w(TAG, "getImageResoucesLevel null" + " level = " + level);
                return 0;
        }
    }

    // 子段位资源
    public static int getImageResoucesSubLevel(int subLevel) {
        switch (subLevel) {
            case 1:
                return R.drawable.zhanji_yiji;
            case 2:
                return R.drawable.zhanji_erji;
            case 3:
                return R.drawable.zhanji_sanji;
            default:
                MyLog.w(TAG, "getImageResoucesSubLevel null" + " subLevel = " + subLevel);
                return 0;
        }
    }
}
