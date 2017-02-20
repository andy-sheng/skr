package com.mi.live.data.query.model;

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
