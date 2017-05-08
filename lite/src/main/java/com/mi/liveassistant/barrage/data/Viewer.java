package com.mi.liveassistant.barrage.data;

import com.mi.liveassistant.barrage.model.ViewerModel;

/**
 * Created by wuxiaoshan on 17-5-8.
 */
public class Viewer {
    private long uid;       // 用户id
    private int level;      // 等级
    private long avatar;    // 头像
    private int certificationType;      // 认证类型
    private boolean redName ; //被社区红名

    public Viewer(){

    }

    public Viewer(ViewerModel viewerModel){
        uid = viewerModel.getUid();
        level = viewerModel.getLevel();
        avatar = viewerModel.getAvatar();
        certificationType = viewerModel.getCertificationType();
        redName = viewerModel.isRedName();
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

    public long getAvatar() {
        return avatar;
    }

    public void setAvatar(long avatar) {
        this.avatar = avatar;
    }

    public int getCertificationType() {
        return certificationType;
    }

    public void setCertificationType(int certificationType) {
        this.certificationType = certificationType;
    }

    public boolean isRedName() {
        return redName;
    }

    public void setRedName(boolean redName) {
        this.redName = redName;
    }
}
