package com.module.playways.room.room.gift.model;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.BaseRoomData;
import com.module.playways.room.gift.model.BaseGift;
import com.module.playways.room.msg.event.SpecialEmojiMsgEvent;
import com.zq.live.proto.Room.GPrensentGiftMsg;
import com.zq.live.proto.Room.SpecialEmojiMsgType;

public class GiftPlayModel {
    private EGiftType mEGiftType = EGiftType.EMOJI;
    private long continueId;//continueId相等的代表是同一次连送
    private long timeMs;          //房间消息产生时间，单位毫秒
    private int roomID;           //房间ID
    private UserInfoModel sender;      //发送者简要信息
    private UserInfoModel receiver;      //发送者简要信息
    SpecialEmojiMsgType emojiType = SpecialEmojiMsgType.SP_EMOJI_TYPE_UNKNOWN;
    String action;
    int beginCount;
    int endCount;
    BaseGift mBaseGift;

    public static GiftPlayModel parseFromEvent(SpecialEmojiMsgEvent event, BaseRoomData roomData) {
        GiftPlayModel giftPlayModel = new GiftPlayModel();
        giftPlayModel.mEGiftType = EGiftType.EMOJI;
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


    public static GiftPlayModel parseFromEvent(GPrensentGiftMsg gPrensentGiftMsg, BaseRoomData roomData) {
        GiftPlayModel giftPlayModel = new GiftPlayModel();
        giftPlayModel.mEGiftType = EGiftType.GIFT;

        giftPlayModel.setContinueId(gPrensentGiftMsg.getContinueID());
        giftPlayModel.setEmojiType(SpecialEmojiMsgType.SP_EMOJI_TYPE_UNKNOWN);
        giftPlayModel.setRoomID(gPrensentGiftMsg.getRoomID());
        giftPlayModel.setAction("");
        giftPlayModel.setBeginCount(gPrensentGiftMsg.getContinueCnt());
        giftPlayModel.setEndCount(gPrensentGiftMsg.getContinueCnt());
        giftPlayModel.setTimeMs(System.currentTimeMillis());
        UserInfoModel userInfoModel = UserInfoModel.parseFromPB(gPrensentGiftMsg.getSendUserInfo());
        giftPlayModel.setSender(userInfoModel);
        UserInfoModel receiverModel = UserInfoModel.parseFromPB(gPrensentGiftMsg.getReceiveUserInfo());
        giftPlayModel.setReceiver(receiverModel);

        BaseGift baseGift = BaseGift.parse(gPrensentGiftMsg.getGiftInfo());

        giftPlayModel.setGift(baseGift);

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

    public UserInfoModel getReceiver() {
        return receiver;
    }

    public void setReceiver(UserInfoModel receiver) {
        this.receiver = receiver;
    }

    public EGiftType getEGiftType() {
        return mEGiftType;
    }

    public BaseGift getGift() {
        return mBaseGift;
    }

    public void setGift(BaseGift baseGift) {
        mBaseGift = baseGift;
    }

    public String getGiftIconUrl() {
        if (mBaseGift != null) {
            return mBaseGift.getGiftURL();
        }
        return "";
    }

    public String getGiftAnimationUrl() {
        if (mBaseGift != null) {
            return mBaseGift.getSourceURL();
        }
        return "";
    }

    public enum EGiftType {
        EMOJI, GIFT
    }

    @Override
    public String toString() {
        return "GiftPlayModel{" +
                "mEGiftType=" + mEGiftType +
                ", continueId=" + continueId +
                ", timeMs=" + timeMs +
                ", roomID=" + roomID +
                ", sender=" + sender +
                ", receiver=" + receiver +
                ", emojiType=" + emojiType +
                ", action='" + action + '\'' +
                ", beginCount=" + beginCount +
                ", endCount=" + endCount +
                ", mBaseGift=" + mBaseGift +
                '}';
    }
}
