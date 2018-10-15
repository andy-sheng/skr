package com.wali.live.watchsdk.ranking.data;

import com.mi.live.data.data.UserListData;
import com.wali.live.proto.RankListProto;

/**
 * Created by zhaomin on 16-10-14.
 *
 * @module 主播排行榜
 */
public class RankUserData extends UserListData {
    public boolean isBlocked; //是否被拉黑
    public String redirectUrl; // 跳转地址，没有跳个人主页
    public String rankInfo; // 显示的文案

    public RankUserData(RankListProto.RankUserInfo userInfo) {
        userId = userInfo.getZuid();
        avatar = userInfo.getAvatar();
        userNickname = userInfo.getNickname();
        signature = userInfo.getSign();
        level = userInfo.getLevel();
        gender = userInfo.getGender();
        isFollowing = userInfo.getIsFocused();
        isBothway = userInfo.getIsBothwayFollowing();
        certificationType = userInfo.getCertificationType();
        isBlocked = userInfo.getIsBlocked();
        mIsShowing = userInfo.getIsPlaying();
        redirectUrl = userInfo.getRedirectUrl();
        rankInfo = userInfo.getRankInfo();
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public String isRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getRankInfo() {
        return rankInfo;
    }

    public void setRankInfo(String rankInfo) {
        this.rankInfo = rankInfo;
    }
}
