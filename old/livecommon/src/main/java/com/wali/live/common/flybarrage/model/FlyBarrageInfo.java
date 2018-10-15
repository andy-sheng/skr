package com.wali.live.common.flybarrage.model;

/**
 * Created by chengsimin on 16/3/22.
 */
public class FlyBarrageInfo {
    private long senderId;
    private String name;
    private int level;
    private int certificationType;
    private String content;
    private long avatarTimestamp;

    public void setAvatarTimestamp(long avatarTimestamp) {
        this.avatarTimestamp = avatarTimestamp;
    }

    public long getAvatarTimestamp() {

        return avatarTimestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public int getCertificationType() {
        return certificationType;
    }

    public void setCertificationType(int certificationType) {
        this.certificationType = certificationType;
    }
}
