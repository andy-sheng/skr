package com.zq.level.utils;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.drawable.DrawableCreator;
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

    public static Drawable getHomePageTopBg(int mainLevel) {
        switch (mainLevel) {
            case UserLevelType.SKRER_LEVEL_POTENTIAL:
                return new DrawableCreator.Builder()
                        .setGradientColor(Color.parseColor("#E3B081"), Color.parseColor("#856054"))
                        .setGradientAngle(0)
                        .build();
            case UserLevelType.SKRER_LEVEL_SILVER:
                return new DrawableCreator.Builder()
                        .setGradientColor(Color.parseColor("#D1E1F1"), Color.parseColor("#727CA0"))
                        .setGradientAngle(0)
                        .build();
            case UserLevelType.SKRER_LEVEL_GOLD:
                return new DrawableCreator.Builder()
                        .setGradientColor(Color.parseColor("#ECB246"), Color.parseColor("#BE6B2F"))
                        .setGradientAngle(0)
                        .build();
            case UserLevelType.SKRER_LEVEL_PLATINUM:
                return new DrawableCreator.Builder()
                        .setGradientColor(Color.parseColor("#85DCFF"), Color.parseColor("#4D42C3"))
                        .setGradientAngle(0)
                        .build();
            case UserLevelType.SKRER_LEVEL_DIAMOND:
                return new DrawableCreator.Builder()
                        .setGradientColor(Color.parseColor("#C37823"), Color.parseColor("#445AFF"))
                        .setGradientAngle(0)
                        .build();
            case UserLevelType.SKRER_LEVEL_KING:
                return new DrawableCreator.Builder()
                        .setGradientColor(Color.parseColor("#FF616B"), Color.parseColor("#4D42C3"))
                        .setGradientAngle(0)
                        .build();
            default:
                return null;
        }
    }


    public static String getHomePageTopBgColor(int mainLevel) {
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


    public static String getHomePageLevelTextColor(int mainLevel) {
        switch (mainLevel) {
            case UserLevelType.SKRER_LEVEL_POTENTIAL:
                return "#55352E";
            case UserLevelType.SKRER_LEVEL_SILVER:
                return "#495378";
            case UserLevelType.SKRER_LEVEL_GOLD:
                return "#814A39";
            case UserLevelType.SKRER_LEVEL_PLATINUM:
                return "#2A54A0";
            case UserLevelType.SKRER_LEVEL_DIAMOND:
                return "#0A3FBD";
            case UserLevelType.SKRER_LEVEL_KING:
                return "#7B2A27";
            default:
                MyLog.w(TAG, "getHomePageLevelTopBg null" + " mainLevel = " + mainLevel);
                return "#55352E";
        }
    }


    public static int getAvatarLevelBg(int mainLevel) {
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
                return R.drawable.qianli;
            case UserLevelType.SKRER_LEVEL_SILVER:
                return R.drawable.baiyin;
            case UserLevelType.SKRER_LEVEL_GOLD:
                return R.drawable.jinpai;
            case UserLevelType.SKRER_LEVEL_PLATINUM:
                return R.drawable.bojin;
            case UserLevelType.SKRER_LEVEL_DIAMOND:
                return R.drawable.zuanshi;
            case UserLevelType.SKRER_LEVEL_KING:
                return R.drawable.gewang;
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
                        return R.drawable.qianli_1;
                    case 2:
                        return R.drawable.qianli_2;
                    case 3:
                        return R.drawable.qianli_3;
                    case 4:
                        return R.drawable.qianli_4;
                    case 5:
                        return R.drawable.qianli_5;
                    case 6:
                        return R.drawable.qianli_6;
                    case 7:
                        return R.drawable.qianli_7;
                    case 8:
                        return R.drawable.qianli_8;
                    case 9:
                        return R.drawable.qianli_9;
                    case 10:
                        return R.drawable.qianli_10;
                    default:
                        MyLog.w(TAG, "getImageResoucesSubLevel null" + " level = " + level + " subLevel = " + subLevel);
                        return 0;
                }
            }

            case UserLevelType.SKRER_LEVEL_SILVER: {
                switch (subLevel) {
                    case 1:
                        return R.drawable.baiyin_1;
                    case 2:
                        return R.drawable.baiyin_2;
                    case 3:
                        return R.drawable.baiyin_3;
                    case 4:
                        return R.drawable.baiyin_4;
                    case 5:
                        return R.drawable.baiyin_5;
                    case 6:
                        return R.drawable.baiyin_6;
                    case 7:
                        return R.drawable.baiyin_7;
                    case 8:
                        return R.drawable.baiyin_8;
                    case 9:
                        return R.drawable.baiyin_9;
                    case 10:
                        return R.drawable.baiyin_10;
                    default:
                        MyLog.w(TAG, "getImageResoucesSubLevel null" + " level = " + level + " subLevel = " + subLevel);
                        return 0;

                }
            }
            case UserLevelType.SKRER_LEVEL_GOLD: {
                switch (subLevel) {
                    case 1:
                        return R.drawable.jinpai_1;
                    case 2:
                        return R.drawable.jinpai_2;
                    case 3:
                        return R.drawable.jinpai_3;
                    case 4:
                        return R.drawable.jinpai_4;
                    case 5:
                        return R.drawable.jinpai_5;
                    case 6:
                        return R.drawable.jinpai_6;
                    case 7:
                        return R.drawable.jinpai_7;
                    case 8:
                        return R.drawable.jinpai_8;
                    case 9:
                        return R.drawable.jinpai_9;
                    case 10:
                        return R.drawable.jinpai_10;
                    default:
                        MyLog.w(TAG, "getImageResoucesSubLevel null" + " level = " + level + " subLevel = " + subLevel);
                        return 0;

                }
            }

            case UserLevelType.SKRER_LEVEL_PLATINUM: {
                switch (subLevel) {
                    case 1:
                        return R.drawable.bojin_1;
                    case 2:
                        return R.drawable.bojin_2;
                    case 3:
                        return R.drawable.bojin_3;
                    case 4:
                        return R.drawable.bojin_4;
                    case 5:
                        return R.drawable.bojin_5;
                    case 6:
                        return R.drawable.bojin_6;
                    case 7:
                        return R.drawable.bojin_7;
                    case 8:
                        return R.drawable.bojin_8;
                    case 9:
                        return R.drawable.bojin_9;
                    case 10:
                        return R.drawable.bojin_10;
                    default:
                        MyLog.w(TAG, "getImageResoucesSubLevel null" + " level = " + level + " subLevel = " + subLevel);
                        return 0;

                }
            }

            case UserLevelType.SKRER_LEVEL_DIAMOND: {
                switch (subLevel) {
                    case 1:
                        return R.drawable.zuanshi_1;
                    case 2:
                        return R.drawable.zuanshi_2;
                    case 3:
                        return R.drawable.zuanshi_3;
                    case 4:
                        return R.drawable.zuanshi_4;
                    case 5:
                        return R.drawable.zuanshi_5;
                    case 6:
                        return R.drawable.zuanshi_6;
                    case 7:
                        return R.drawable.zuanshi_7;
                    case 8:
                        return R.drawable.zuanshi_8;
                    case 9:
                        return R.drawable.zuanshi_9;
                    case 10:
                        return R.drawable.zuanshi_10;
                    default:
                        MyLog.w(TAG, "getImageResoucesSubLevel null" + " level = " + level + " subLevel = " + subLevel);
                        return 0;

                }
            }

            case UserLevelType.SKRER_LEVEL_KING: {
                switch (subLevel) {
                    case 1:
                        return R.drawable.gewang_1;
                    case 2:
                        return R.drawable.gewang_2;
                    case 3:
                        return R.drawable.gewang_3;
                    case 4:
                        return R.drawable.gewang_4;
                    case 5:
                        return R.drawable.gewang_5;
                    case 6:
                        return R.drawable.gewang_6;
                    case 7:
                        return R.drawable.gewang_7;
                    case 8:
                        return R.drawable.gewang_8;
                    case 9:
                        return R.drawable.gewang_9;
                    case 10:
                        return R.drawable.gewang_10;
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
