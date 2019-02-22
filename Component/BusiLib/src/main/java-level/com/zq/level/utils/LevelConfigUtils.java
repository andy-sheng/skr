package com.zq.level.utils;

import com.common.log.MyLog;
import com.component.busilib.R;
import com.common.core.userinfo.model.UserLevelType;

public class LevelConfigUtils {

    public final static String TAG = "LevelConfigUtils";

    // 首页顶部背景
    public static int getHomePageLevelTopBg(int mainLevel) {
        switch (mainLevel) {
            case UserLevelType.SKRER_LEVEL_POTENTIAL:
                return R.drawable.shouye_qianli;
            case UserLevelType.SKRER_LEVEL_SILVER:
                return R.drawable.shouye_baiyin;
            case UserLevelType.SKRER_LEVEL_GOLD:
                return R.drawable.shouye_jinpai;
            case UserLevelType.SKRER_LEVEL_PLATINUM:
                return R.drawable.shouye_bojin;
            case UserLevelType.SKRER_LEVEL_DIAMOND:
                return R.drawable.shouye_zuanshi;
            case UserLevelType.SKRER_LEVEL_KING:
                return R.drawable.shouye_gewang;
            default:
                MyLog.w(TAG, "getHomePageLevelTopBg null" + " mainLevel = " + mainLevel);
                return 0;
        }
    }

    public static String getHomePageTopBgColor(int mainLevel){
        switch (mainLevel) {
            case UserLevelType.SKRER_LEVEL_POTENTIAL:
                return "#E5B282";
            case UserLevelType.SKRER_LEVEL_SILVER:
                return "#98AEC7";
            case UserLevelType.SKRER_LEVEL_GOLD:
                return "#D09118";
            case UserLevelType.SKRER_LEVEL_PLATINUM:
                return "#73BCF3";
            case UserLevelType.SKRER_LEVEL_DIAMOND:
                return "#478AFF";
            case UserLevelType.SKRER_LEVEL_KING:
                return "#F16753";
            default:
                MyLog.w(TAG, "getHomePageLevelTopBg null" + " mainLevel = " + mainLevel);
                return "#c0F9F4F1";
        }
    }

    public static int getAvatarLevelBg(int mainLevel){
        switch (mainLevel) {
            case UserLevelType.SKRER_LEVEL_POTENTIAL:
                return R.drawable.touxiang_qianli;
            case UserLevelType.SKRER_LEVEL_SILVER:
                return R.drawable.touxiang_baiyin;
            case UserLevelType.SKRER_LEVEL_GOLD:
                return R.drawable.touxiang_jinpai;
            case UserLevelType.SKRER_LEVEL_PLATINUM:
                return R.drawable.touxiang_bojin;
            case UserLevelType.SKRER_LEVEL_DIAMOND:
                return R.drawable.touxiang_zuanshi;
            case UserLevelType.SKRER_LEVEL_KING:
                return R.drawable.touxiang_gewang;
            default:
                MyLog.w(TAG, "getAvatarLevelBg null" + " mainLevel = " + mainLevel);
                return 0;
        }
    }



    // 父段位资源
    public static int getImageResoucesLevel(int level) {
        switch (level) {
            case UserLevelType.SKRER_LEVEL_POTENTIAL:
                return R.drawable.qianlixinxiu_xunzhang;
            case UserLevelType.SKRER_LEVEL_SILVER:
                return R.drawable.baiyingezhe_xunzhang;
            case UserLevelType.SKRER_LEVEL_GOLD:
                return R.drawable.jinpaigeshou_xunzhang;
            case UserLevelType.SKRER_LEVEL_PLATINUM:
                return R.drawable.bojinchangjiang_xunzhang;
            case UserLevelType.SKRER_LEVEL_DIAMOND:
                return R.drawable.zuanshijuxing_xunzhang;
            case UserLevelType.SKRER_LEVEL_KING:
                return R.drawable.rongyaogewang_xunzhang;
            default:
                MyLog.w(TAG, "getImageResoucesLevel null" + " level = " + level);
                return 0;
        }
    }

    // 子段位资源
    public static int getImageResoucesSubLevel(int level, int subLevel) {
        switch (level) {
            case UserLevelType.SKRER_LEVEL_POTENTIAL: {
                switch (subLevel) {
                    case 1:
                        return R.drawable.qianlixinxiu_one;
                    case 2:
                        return R.drawable.qianlixinxiu_two;
                    case 3:
                        return R.drawable.qianlixinxiu_three;
                    default:
                        MyLog.w(TAG, "getImageResoucesSubLevel null" + " level = " + level + " subLevel = " + subLevel);
                        return 0;
                }
            }

            case UserLevelType.SKRER_LEVEL_SILVER: {
                switch (subLevel) {
                    case 1:
                        return R.drawable.baiyingezhe_one;
                    case 2:
                        return R.drawable.baiyingezhe_two;
                    case 3:
                        return R.drawable.baiyingezhe_three;
                    default:
                        MyLog.w(TAG, "getImageResoucesSubLevel null" + " level = " + level + " subLevel = " + subLevel);
                        return 0;

                }
            }
            case UserLevelType.SKRER_LEVEL_GOLD: {
                switch (subLevel) {
                    case 1:
                        return R.drawable.jinpaigeshou_one;
                    case 2:
                        return R.drawable.jinpaigeshou_two;
                    case 3:
                        return R.drawable.jinpaigeshou_three;
                    case 4:
                        return R.drawable.jinpaigeshou_four;
                    default:
                        MyLog.w(TAG, "getImageResoucesSubLevel null" + " level = " + level + " subLevel = " + subLevel);
                        return 0;

                }
            }

            case UserLevelType.SKRER_LEVEL_PLATINUM: {
                switch (subLevel) {
                    case 1:
                        return R.drawable.bojinchangjiang_one;
                    case 2:
                        return R.drawable.bojinchangjiang_two;
                    case 3:
                        return R.drawable.bojinchangjiang_three;
                    case 4:
                        return R.drawable.bojinchangjiang_four;
                    default:
                        MyLog.w(TAG, "getImageResoucesSubLevel null" + " level = " + level + " subLevel = " + subLevel);
                        return 0;

                }
            }

            case UserLevelType.SKRER_LEVEL_DIAMOND: {
                switch (subLevel) {
                    case 1:
                        return R.drawable.zuanshijuxing_one;
                    case 2:
                        return R.drawable.zuanshijuxing_two;
                    case 3:
                        return R.drawable.zuanshijuxing_three;
                    case 4:
                        return R.drawable.zuanshijuxing_four;
                    case 5:
                        return R.drawable.zuanshijuxing_five;
                    default:
                        MyLog.w(TAG, "getImageResoucesSubLevel null" + " level = " + level + " subLevel = " + subLevel);
                        return 0;

                }
            }

            case UserLevelType.SKRER_LEVEL_KING: {
                switch (subLevel) {
                    case 1:
                        return R.drawable.rongyaogewang_one;
                    case 2:
                        return R.drawable.rongyaogewang_two;
                    case 3:
                        return R.drawable.rongyaogewang_three;
                    default:
                        MyLog.w(TAG, "getImageResoucesSubLevel null" + " level = " + level + " subLevel = " + subLevel);
                        return 0;
                }
            }

            default:
                return 0;
        }
    }
}
