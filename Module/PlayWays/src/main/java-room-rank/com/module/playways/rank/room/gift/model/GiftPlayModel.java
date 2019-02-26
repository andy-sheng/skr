package com.module.playways.rank.room.gift.model;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.RoomData;
import com.module.playways.rank.msg.event.SpecialEmojiMsgEvent;
import com.zq.live.proto.Room.SpecialEmojiMsgType;

public class GiftPlayModel {
    private long continueId;//continueId相等的代表是同一次连送
    private long timeMs;          //房间消息产生时间，单位毫秒
    private int roomID;           //房间ID
    private UserInfoModel sender;      //发送者简要信息
    SpecialEmojiMsgType emojiType = SpecialEmojiMsgType.SP_EMOJI_TYPE_UNKNOWN;
    String action;
    int beginCount;
    int endCount;


    public static GiftPlayModel parseFromEvent(SpecialEmojiMsgEvent event, RoomData roomData) {
        GiftPlayModel giftPlayModel = new GiftPlayModel();
        giftPlayModel.setContinueId(event.coutinueId);
        giftPlayModel.setEmojiType(event.emojiType);
        giftPlayModel.setRoomID(event.info.getRoomID());
        giftPlayModel.setAction(event.action);
        giftPlayModel.setBeginCount(event.count);
        giftPlayModel.setEndCount(event.count);
        giftPlayModel.setTimeMs(event.info.getTimeMs());

        UserInfoModel userInfoModel;
        if (roomData != null) {
            userInfoModel = roomData.getUserInfo(event.info.getSender().getUserID());
            if (userInfoModel == null) {
                userInfoModel = UserInfoModel.parseFromPB(event.info.getSender());
            }
        } else {
            userInfoModel = UserInfoModel.parseFromPB(event.info.getSender());
        }
        giftPlayModel.setSender(userInfoModel);
        return giftPlayModel;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public void setTimeMs(long timeMs) {
        this.timeMs = timeMs;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public UserInfoModel getSender() {
        return sender;
    }

    public void setSender(UserInfoModel sender) {
        this.sender = sender;
    }

    public SpecialEmojiMsgType getEmojiType() {
        return emojiType;
    }

    public void setEmojiType(SpecialEmojiMsgType emojiType) {
        this.emojiType = emojiType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    public long getContinueId() {
        return continueId;
    }

    public void setContinueId(long continueId) {
        this.continueId = continueId;
    }

    public int getBeginCount() {
        return beginCount;
    }

    public void setBeginCount(int beginCount) {
        this.beginCount = beginCount;
    }

    public int getEndCount() {
        return endCount;
    }

    public void setEndCount(int endCount) {
        this.endCount = endCount;
    }
}
