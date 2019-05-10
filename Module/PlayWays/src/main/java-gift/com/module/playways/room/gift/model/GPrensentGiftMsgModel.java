package com.module.playways.room.gift.model;

import com.common.core.userinfo.model.UserInfoModel;
import com.zq.live.proto.Room.GPrensentGiftMsg;

public class GPrensentGiftMsgModel {
    BaseGift giftInfo;
    int count;
    UserInfoModel sendUserInfo;
    UserInfoModel receiveUserInfo;
    long roomID;
    long continueID;
    int continueCnt;

    public BaseGift getGiftInfo() {
        return giftInfo;
    }

    public void setGiftInfo(BaseGift giftInfo) {
        this.giftInfo = giftInfo;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public UserInfoModel getSendUserInfo() {
        return sendUserInfo;
    }

    public void setSendUserInfo(UserInfoModel sendUserInfo) {
        this.sendUserInfo = sendUserInfo;
    }

    public UserInfoModel getReceiveUserInfo() {
        return receiveUserInfo;
    }

    public void setReceiveUserInfo(UserInfoModel receiveUserInfo) {
        this.receiveUserInfo = receiveUserInfo;
    }

    public long getRoomID() {
        return roomID;
    }

    public void setRoomID(long roomID) {
        this.roomID = roomID;
    }

    public long getContinueID() {
        return continueID;
    }

    public void setContinueID(long continueID) {
        this.continueID = continueID;
    }

    public int getContinueCnt() {
        return continueCnt;
    }

    public void setContinueCnt(int continueCnt) {
        this.continueCnt = continueCnt;
    }

    public static GPrensentGiftMsgModel parse(GPrensentGiftMsg gPrensentGiftMsg) {
        GPrensentGiftMsgModel gPrensentGiftMsgModel = new GPrensentGiftMsgModel();
        gPrensentGiftMsgModel.setGiftInfo(BaseGift.parse(gPrensentGiftMsg.getGiftInfo()));
        gPrensentGiftMsgModel.setContinueCnt(gPrensentGiftMsg.getContinueCnt());
        gPrensentGiftMsgModel.setContinueID(gPrensentGiftMsg.getContinueID());
        gPrensentGiftMsgModel.setCount(gPrensentGiftMsg.getCount());
        gPrensentGiftMsgModel.setRoomID(gPrensentGiftMsg.getRoomID());
        gPrensentGiftMsgModel.setReceiveUserInfo(UserInfoModel.parseFromPB(gPrensentGiftMsg.getReceiveUserInfo()));
        gPrensentGiftMsgModel.setSendUserInfo(UserInfoModel.parseFromPB(gPrensentGiftMsg.getSendUserInfo()));
        return gPrensentGiftMsgModel;
    }
}
