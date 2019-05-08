package com.module.playways.room.room.gift.model;

import android.text.TextUtils;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.BaseRoomData;
import com.module.playways.room.gift.model.AnimGiftParamModel;
import com.module.playways.room.gift.model.BaseGift;
import com.module.playways.room.msg.event.GiftBrushMsgEvent;
import com.module.playways.room.msg.event.SpecialEmojiMsgEvent;
import com.zq.live.proto.Room.GPrensentGiftMsg;
import com.zq.live.proto.Room.SpecialEmojiMsgType;

import static com.module.playways.BaseRoomData.ROOM_SPECAIL_EMOJI_AIXIN;

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
    String giftIconUrl;
    AnimGiftParamModel mAnimGiftParamModel;

    public static GiftPlayModel parseFromEvent(SpecialEmojiMsgEvent event, BaseRoomData roomData) {
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


    public static GiftPlayModel parseFromEvent(GPrensentGiftMsg gPrensentGiftMsg, BaseRoomData roomData) {
        GiftPlayModel giftPlayModel = new GiftPlayModel();
        giftPlayModel.setContinueId(gPrensentGiftMsg.getContinueID());
        giftPlayModel.setEmojiType(SpecialEmojiMsgType.SP_EMOJI_TYPE_UNKNOWN);
        giftPlayModel.setRoomID(gPrensentGiftMsg.getRoomID());
        giftPlayModel.setAction("");
        giftPlayModel.setBeginCount(gPrensentGiftMsg.getContinueCnt());
        giftPlayModel.setEndCount(gPrensentGiftMsg.getContinueCnt());
        giftPlayModel.setTimeMs(System.currentTimeMillis());
        giftPlayModel.mEGiftType = EGiftType.GIFT;
        UserInfoModel userInfoModel = UserInfoModel.parseFromPB(gPrensentGiftMsg.getSendUserInfo());
        giftPlayModel.setSender(userInfoModel);
        UserInfoModel receiverModel = UserInfoModel.parseFromPB(gPrensentGiftMsg.getReceiveUserInfo());
        giftPlayModel.setReceiver(receiverModel);
        giftPlayModel.setGiftIconUrl(gPrensentGiftMsg.getGiftInfo().getGiftURL());

        AnimGiftParamModel animGiftParamModel = new AnimGiftParamModel();
        animGiftParamModel.setBottom(gPrensentGiftMsg.getGiftInfo().getExtra().getBottom());
        animGiftParamModel.setLeft(gPrensentGiftMsg.getGiftInfo().getExtra().getLeft());
        animGiftParamModel.setRight(gPrensentGiftMsg.getGiftInfo().getExtra().getRight());
        animGiftParamModel.setTop(gPrensentGiftMsg.getGiftInfo().getExtra().getTop());
        animGiftParamModel.setDuration(gPrensentGiftMsg.getGiftInfo().getExtra().getDuration());
        animGiftParamModel.setWidth(gPrensentGiftMsg.getGiftInfo().getExtra().getWidth());
        animGiftParamModel.setHeight(gPrensentGiftMsg.getGiftInfo().getExtra().getHeight());
        animGiftParamModel.setPlay(gPrensentGiftMsg.getGiftInfo().getPlay());
        animGiftParamModel.setTextContinueCount(gPrensentGiftMsg.getGiftInfo().getTextContinueCount());
        animGiftParamModel.setResUrl(gPrensentGiftMsg.getGiftInfo().getSourceURL());
        animGiftParamModel.setDisplayType(gPrensentGiftMsg.getGiftInfo().getDisplayType());

        giftPlayModel.mAnimGiftParamModel = animGiftParamModel;

        return giftPlayModel;
    }

    public AnimGiftParamModel getAnimGiftParamModel() {
        return mAnimGiftParamModel;
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

    public String getGiftIconUrl() {
        return giftIconUrl;
    }

    public void setGiftIconUrl(String giftIconUrl) {
        this.giftIconUrl = giftIconUrl;
    }

    public enum EGiftType {
        EMOJI, GIFT
    }
}
