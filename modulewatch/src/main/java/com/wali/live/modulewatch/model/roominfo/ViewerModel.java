package com.wali.live.modulewatch.model.roominfo;

import com.common.core.UserInfoConstans;
import com.common.core.userinfo.UserInfo;
import com.wali.live.proto.LiveCommon.Viewer;

/**
 * Created by lan on 16-3-5.
 */
public class ViewerModel extends UserInfo implements ViewerAction {

    private boolean redName; //被社区红名
    private int currentLiveTicket;             //该观众本场贡献的星票数
    private String userNobelID;

    public ViewerModel(long uid) {

    }

    public ViewerModel(Viewer protoViewer) {

        parse(protoViewer);
    }

    public ViewerModel(long uid, int level, long avatar, int certificationType, boolean redName) {
        setUserId(uid);
        setLevel(level);
        setAvatar(avatar);
        setCertificationType(certificationType);
        this.redName = redName;
    }

    public void parse(Viewer protoViewer) {

        setUserId(protoViewer.getUuid());
        setLevel(protoViewer.getLevel());
        setAvatar(protoViewer.getAvatar());
        setCertificationType(protoViewer.getCertificationType());
        setVipLevel(protoViewer.getVipLevel());
        setIsVipFrozen(protoViewer.getVipDisable());
        setNobleLevel(protoViewer.getNobleLevel());


        this.redName = protoViewer.getRedName();
        this.currentLiveTicket = protoViewer.getTicket();
        this.userNobelID = protoViewer.getUserNobleMedal().getPicId();
    }

    public boolean isNoble() {
        int nobleLevel = 0;
        if (getNobleLevel() != null) {
            nobleLevel = getNobleLevel();
        }

        return nobleLevel == UserInfoConstans.NOBLE_LEVEL_FIFTH || nobleLevel == UserInfoConstans.NOBLE_LEVEL_FOURTH
                || nobleLevel == UserInfoConstans.NOBLE_LEVEL_THIRD || nobleLevel == UserInfoConstans.NOBLE_LEVEL_SECOND
                || nobleLevel == UserInfoConstans.NOBLE_LEVEL_TOP;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ViewerModel)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        return getUserId() == ((ViewerModel) o).getUserId();
    }

    @Override
    public int hashCode() {
        int result = 17;
        int elementHash = (int) (getUserId() ^ (getUserId() >>> 32));
        result = 31 * result + elementHash;
        return result;
    }

    @Override
    public String toString() {
        return "ViewerAction{" +
                "uid=" + getUserId() +
                '}';
    }

    @Override
    public boolean isRedName() {
        return redName;
    }

    @Override
    public void setRedName(boolean redName) {
        this.redName = redName;
    }

    @Override
    public int getCurrentLiveTicket() {
        return currentLiveTicket;
    }

    @Override
    public void setCurrentLiveTicket(int currentLiveTicket) {
        this.currentLiveTicket = currentLiveTicket;
    }

    @Override
    public String getUserNobelID() {
        return userNobelID;
    }

    @Override
    public void setUserNobelID(String userNobelID) {
        this.userNobelID = userNobelID;
    }

}
