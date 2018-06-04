package com.mi.live.data.query.model;

import com.mi.live.data.user.User;
import com.wali.live.proto.LiveCommonProto;

/**
 * Created by lan on 16-3-5.
 */
public class ViewerModel {
    private long uid;       // 用户id
    private int level;      // 等级
    private long avatar;    // 头像
    private int certificationType;      // 认证类型
    private boolean redName ; //被社区红名
    private int vipLevel;                      //该观众的vip等级
    private boolean isVipFrozen;               //该观众的vip是否被冻结（如果是vip才有意义）
    private int currentLiveTicket;             //该观众本场贡献的星票数
    private int nobleLevel;                    //该观众的贵族等级
    private String userNobelID;

    public ViewerModel(long uid) {
        this.uid = uid;
    }

    public ViewerModel(LiveCommonProto.Viewer protoViewer) {
        parse(protoViewer);
    }

    public void parse(LiveCommonProto.Viewer protoViewer) {
        this.uid = protoViewer.getUuid();
        this.level = protoViewer.getLevel();
        this.avatar = protoViewer.getAvatar();
        this.certificationType = protoViewer.getCertificationType();
        this.redName = protoViewer.getRedName();
        this.vipLevel = protoViewer.getVipLevel();
        this.isVipFrozen = protoViewer.getVipDisable();
        this.currentLiveTicket = protoViewer.getTicket();
        this.nobleLevel = protoViewer.getNobleLevel();
        this.userNobelID = protoViewer.getUserNobleMedal().getPicId();
    }

    public ViewerModel(long uid, int level, long avatar, int certificationType,boolean redName) {
        this.uid = uid;
        this.level = level;
        this.avatar = avatar;
        this.certificationType = certificationType;
        this.redName = redName;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCertificationType() {
        return certificationType;
    }

    public void setCertificationType(int certificationType) {
        this.certificationType = certificationType;
    }

    public long getAvatar() {
        return avatar;
    }

    public void setAvatar(long avatar) {
        this.avatar = avatar;
    }

    public boolean isRedName() {
        return redName;
    }

    public void setRedName(boolean redName) {
        this.redName = redName;
    }


    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public boolean isVipFrozen() {
        return isVipFrozen;
    }

    public void setVipFrozen(boolean vipFrozen) {
        isVipFrozen = vipFrozen;
    }

    public int getCurrentLiveTicket() {
        return currentLiveTicket;
    }

    public void setCurrentLiveTicket(int currentLiveTicket) {
        this.currentLiveTicket = currentLiveTicket;
    }

    public int getNobleLevel() {
        return nobleLevel;
    }

    public void setNobleLevel(int nobleLevel) {
        this.nobleLevel = nobleLevel;
    }

    public boolean isNoble() {
        return this.nobleLevel == User.NOBLE_LEVEL_FIFTH || this.nobleLevel == User.NOBLE_LEVEL_FOURTH
                || this.nobleLevel == User.NOBLE_LEVEL_THIRD || this.nobleLevel == User.NOBLE_LEVEL_SECOND
                || this.nobleLevel == User.NOBLE_LEVEL_TOP;
    }

    public String getUserNobelID() {
        return userNobelID;
    }

    public void setUserNobelID(String userNobelID) {
        this.userNobelID = userNobelID;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ViewerModel)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        return uid == ((ViewerModel) o).uid;
    }

    @Override
    public int hashCode() {
        int result = 17;
        int elementHash = (int) (uid ^ (uid >>> 32));
        result = 31 * result + elementHash;
        return result;
    }

    @Override
    public String toString() {
        return "Viewer{" +
                "uid=" + uid +
                '}';
    }
}
