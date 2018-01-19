package com.wali.live.watchsdk.contest.cache;

import com.wali.live.watchsdk.contest.model.ContestNoticeModel;

/**
 * Created by lan on 2018/1/11.
 *
 * @description 全局信息，不用清空
 */
public class ContestGlobalCache {
    private static String sRevivalCode;             //复活卡，邀请码
    private static int sRevivalNum;                 //复活次数

    private static float sTotalIncome;              //用户收入
    private static int sRank;                       //用户排名

    /**
     * @notice 这个属性是本场次的，但是是外面准备页面获取的，还是放在这里吧，如果房间内获取到，再放在CurrentCache
     */
    private static float sBonus;                    //本场总奖金

    /*Get/Set方法*/
    public static String getRevivalCode() {
        return sRevivalCode;
    }

    public static void setRevivalCode(String revivalCode) {
        sRevivalCode = revivalCode;
    }

    public static int getRevivalNum() {
        return sRevivalNum;
    }

    public static void setRevivalNum(int revivalNum) {
        sRevivalNum = revivalNum;
    }

    public static void setContestNotice(ContestNoticeModel model) {
        sRevivalNum = model.getRevivalNum();
        sTotalIncome = model.getTotalIncome();
        sRank = model.getRank();
        sBonus = model.getBonus();
    }

    public static float getTotalIncome() {
        return sTotalIncome;
    }

    public static int getRank() {
        return sRank;
    }

    public static long getBonus() {
        return (long) sBonus;
    }

    public static void clear() {
        sRevivalCode = null;
        sRevivalNum = 0;

        sTotalIncome = 0f;
        sRank = 0;
        sBonus = 0;
    }
}
