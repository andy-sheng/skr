package com.mi.live.data.gift.redenvelope;

/**
 * @module 红包
 * Created by chengsimin on 16/2/21.
 */
public class RedEnvelopeModel {
    private String redEnvelopeId;
    private long userId; //用户id
    private String nickName; // 发送者名字
    private long avatarTimestamp;// 时间戳
    private int level; // 发送者等级
    private String roomId; // 房间id
    private String msg;
    private int type;

    public RedEnvelopeModel() {}

    public RedEnvelopeModel(SendRedEnvelopModel model, String redId) {
        this.redEnvelopeId = redId;
        setGemCnt(model.getGemCnt());
        setMsg(model.getMsg());
        setRoomId(model.getRoomId());
        setUserId(model.getZuId());
    }

    public int getGemCnt() {
        return gemCnt;
    }

    public void setGemCnt(int gemCnt) {
        this.gemCnt = gemCnt;
    }

    private int gemCnt;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getRedEnvelopeId() {
        return redEnvelopeId;
    }

    public long getUserId() {
        return userId;
    }

    public int getLevel() {
        return level;
    }

    public long getAvatarTimestamp() {
        return avatarTimestamp;
    }

    public void setRedEnvelopeId(String redEnvelopeId) {
        this.redEnvelopeId = redEnvelopeId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setAvatarTimestamp(long avatarTimestamp) {
        this.avatarTimestamp = avatarTimestamp;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @Override
    public String toString() {
        return "RedEnvelopeModel{" +
                "redEnvelopeId='" + redEnvelopeId + '\'' +
                ", userId=" + userId +
                ", avatarTimestamp=" + avatarTimestamp +
                ", level=" + level +
                ", roomId=" + roomId +
                ", msg=" + msg +
                ", nickName=" + nickName +
                ", gemCnt=" + gemCnt +
                '}';
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static class RedEnvelopeModelExtra {
        public RedEnvelopeModel model;
        public int gain;

        public RedEnvelopeModelExtra(RedEnvelopeModel model) {
            this.model = model;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RedEnvelopeModel) {
            RedEnvelopeModel p = (RedEnvelopeModel) o;
            if (p.toString().equals(toString())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
