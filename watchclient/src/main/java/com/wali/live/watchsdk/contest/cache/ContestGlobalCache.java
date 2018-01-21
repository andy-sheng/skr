package com.wali.live.watchsdk.contest.cache;

import android.text.TextUtils;

import com.wali.live.proto.LiveSummitProto;

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
    private static String sLiveId;

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

    public static void setContestNotice(LiveSummitProto.ContestNoticeInfo protoInfo) {
        if (protoInfo.hasRevivalNum()) {
            sRevivalNum = protoInfo.getRevivalNum();
        }

        sTotalIncome = protoInfo.getTotalIncome();
        sRank = protoInfo.getRank();

        sBonus = protoInfo.getBonus();
        if (protoInfo.hasLiveid()) {
            sLiveId = protoInfo.getLiveid();
        }
    }

    public static void setContestInviteCode(LiveSummitProto.GetContestInviteCodeRsp rsp) {
        if (rsp.hasInviteCode()) {
            sRevivalCode = rsp.getInviteCode();
        }
        if (rsp.hasRevivalNum()) {
            sRevivalNum = rsp.getRevivalNum();
        }
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

    /**
     * 之前房间号不存在，或者不匹配，或者没有获取到奖金，房间内都重新拉一边
     */
    public static boolean needGetNoticeAgain(String liveId) {
        if (TextUtils.isEmpty(sLiveId) || !liveId.equals(sLiveId)) {
            return true;
        }
        if (sBonus == 0) {
            return true;
        }
        return false;
    }

    public static boolean needGetCodeAgain() {
        return TextUtils.isEmpty(sRevivalCode);
    }

    public static void clear() {
        sRevivalCode = null;
        sRevivalNum = 0;

        sTotalIncome = 0f;
        sRank = 0;

        sBonus = 0;
        sLiveId = null;
    }
}