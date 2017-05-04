package com.mi.liveassistant.room.viewer.model;

import com.mi.liveassistant.proto.LiveCommonProto;

/**
 * Created by lan on 16-3-5.
 */
public class Viewer {
    private long mUid;                  // 用户id
    private int mLevel;                 // 等级
    private long mAvatar;               // 头像
    private int mCertificationType;     // 认证类型
    private boolean mRedName;           // 被社区红名

    public Viewer(long uid) {
        mUid = uid;
    }

    public Viewer(LiveCommonProto.Viewer protoViewer) {
        parse(protoViewer);
    }

    public void parse(LiveCommonProto.Viewer protoViewer) {
        mUid = protoViewer.getUuid();
        mLevel = protoViewer.getLevel();
        mAvatar = protoViewer.getAvatar();
        mCertificationType = protoViewer.getCertificationType();
        mRedName = protoViewer.getRedName();
    }

    public long getUid() {
        return mUid;
    }

    public int getLevel() {
        return mLevel;
    }

    public int getCertificationType() {
        return mCertificationType;
    }

    public long getAvatar() {
        return mAvatar;
    }

    public boolean isRedName() {
        return mRedName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Viewer)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        return mUid == ((Viewer) o).mUid;
    }

    @Override
    public int hashCode() {
        int result = 17;
        int elementHash = (int) (mUid ^ (mUid >>> 32));
        result = 31 * result + elementHash;
        return result;
    }

    @Override
    public String toString() {
        return "Viewer{" +
                "uid=" + mUid +
                '}';
    }
}
