package com.wali.live.modulewatch.watch.model.roominfo;

import com.common.core.UserInfoConstans;
import com.common.core.userinfo.UserInfo;

public class AnchorModel {

    private UserInfo mUserInfo;

    private String mViewUrl;            //直播地址

    private String mRoomId = null;           //正在直播的id, 房间号

    /*这个用户对应的付费弹幕礼物id，确定这个用户开的直播的付费弹幕价格*/
    private int payBarrageGiftId = -1;

    /*以下是用户的详细信息*/
    private int liveTicketNum;      //星票数
    private int fansNum;            //粉丝数

    public AnchorModel() {
        if (mUserInfo == null) {
            mUserInfo = new UserInfo();
        }
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.mUserInfo = userInfo;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String mRoomId) {
        this.mRoomId = mRoomId;
    }

    public String getViewUrl() {
        return mViewUrl;
    }

    public void setViewUrl(String mViewUrl) {
        this.mViewUrl = mViewUrl;
    }

    public int getFansNum() {
        return fansNum;
    }

    public void setFansNum(int fansNum) {
        this.fansNum = fansNum;
    }

    public void setLiveTicketNum(int liveTicketNum) {
        this.liveTicketNum = liveTicketNum;
    }

    public int getLiveTicketNum() {
        return liveTicketNum;
    }

    public int getPayBarrageGiftId() {
        return payBarrageGiftId;
    }

    public boolean isNoble() {
        int nobleLevel = 0;
        if (mUserInfo.getNobleLevel() != null) {
            nobleLevel = mUserInfo.getNobleLevel();
        }

        return nobleLevel == UserInfoConstans.NOBLE_LEVEL_FIFTH || nobleLevel == UserInfoConstans.NOBLE_LEVEL_FOURTH
                || nobleLevel == UserInfoConstans.NOBLE_LEVEL_THIRD || nobleLevel == UserInfoConstans.NOBLE_LEVEL_SECOND
                || nobleLevel == UserInfoConstans.NOBLE_LEVEL_TOP;
    }
}
