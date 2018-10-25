package com.wali.live.modulewatch.model.roominfo;

import com.common.core.UserInfoConstans;
import com.common.core.userinfo.UserInfo;
import com.wali.live.proto.LiveCommon.Viewer;

/**
 * Created by lan on 16-3-5.
 */
public class ViewerModel {

    private UserInfo mUserInfo;
    private boolean redName; //被社区红名
    private int currentLiveTicket;             //该观众本场贡献的星票数
    private String userNobelID;

    public ViewerModel(long uid) {
        if (mUserInfo == null) {
            mUserInfo = new UserInfo();
        }
        mUserInfo.setUserId(uid);
    }

    public ViewerModel(Viewer protoViewer) {
        if (mUserInfo == null) {
            mUserInfo = new UserInfo();
        }
        parse(protoViewer);
    }

    public ViewerModel(long uid, int level, long avatar, int certificationType, boolean redName) {
        mUserInfo.setUserId(uid);
        mUserInfo.setLevel(level);
        mUserInfo.setAvatar(avatar);
        mUserInfo.setCertificationType(certificationType);
        this.redName = redName;
    }

    public void parse(Viewer protoViewer) {

        mUserInfo.setUserId(protoViewer.getUuid());
        mUserInfo.setLevel(protoViewer.getLevel());
        mUserInfo.setAvatar(protoViewer.getAvatar());
        mUserInfo.setCertificationType(protoViewer.getCertificationType());
        mUserInfo.setVipLevel(protoViewer.getVipLevel());
        mUserInfo.setIsVipFrozen(protoViewer.getVipDisable());
        mUserInfo.setNobleLevel(protoViewer.getNobleLevel());


        this.redName = protoViewer.getRedName();
        this.currentLiveTicket = protoViewer.getTicket();
        this.userNobelID = protoViewer.getUserNobleMedal().getPicId();
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


    public boolean equals(Object o) {
        if (o == null || !(o instanceof ViewerModel)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        return mUserInfo.getUserId() == ((ViewerModel) o).getUserInfo().getUserId();
    }


    public int hashCode() {
        int result = 17;
        int elementHash = (int) (mUserInfo.getUserId() ^ (mUserInfo.getUserId() >>> 32));
        result = 31 * result + elementHash;
        return result;
    }


    public String toString() {
        return "ViewerAction{" +
                "uid=" + mUserInfo.getUserId() +
                '}';
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.mUserInfo = userInfo;
    }

    public boolean isRedName() {
        return redName;
    }


    public void setRedName(boolean redName) {
        this.redName = redName;
    }


    public int getCurrentLiveTicket() {
        return currentLiveTicket;
    }


    public void setCurrentLiveTicket(int currentLiveTicket) {
        this.currentLiveTicket = currentLiveTicket;
    }

    public String getUserNobelID() {
        return userNobelID;
    }

    public void setUserNobelID(String userNobelID) {
        this.userNobelID = userNobelID;
    }

}
