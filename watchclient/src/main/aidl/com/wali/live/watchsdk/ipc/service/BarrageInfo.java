package com.wali.live.watchsdk.ipc.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 * @module 提供給宿主app的数据结构
 */
public class BarrageInfo implements Parcelable {
    // 发送者id
    private long sender;
    // 发送者名称
    private String senderName;
    // 主播id
    private long anchorId;
    // 房间id
    private String roomId;
    // 消息主体
    private String body;
    // 消息类型
    private int msgType;
    //额外的信息，用json存
    private String ext;


    public BarrageInfo() {
    }

    protected BarrageInfo(Parcel in) {
        this.sender = in.readLong();
        this.senderName = in.readString();
        this.anchorId = in.readLong();
        this.roomId = in.readString();
        this.body = in.readString();
        this.msgType = in.readInt();
        this.ext = in.readString();
    }

    public long getSender() {
        return sender;
    }

    public void setSender(long sender) {
        this.sender = sender;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public long getAnchorId() {
        return anchorId;
    }

    public void setAnchorId(long anchorId) {
        this.anchorId = anchorId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public static final Creator<BarrageInfo> CREATOR = new Creator<BarrageInfo>() {
        @Override
        public BarrageInfo createFromParcel(Parcel source) {
            return new BarrageInfo(source);
        }

        @Override
        public BarrageInfo[] newArray(int size) {
            return new BarrageInfo[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(sender);
        dest.writeString(senderName);
        dest.writeLong(anchorId);
        dest.writeString(roomId);
        dest.writeString(body);
        dest.writeInt(msgType);
        dest.writeString(ext);
    }


}
