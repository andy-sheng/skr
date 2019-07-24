package com.module.playways.room.gift.model;

import com.common.core.userinfo.model.UserInfoModel;
import com.zq.live.proto.Room.GPrensentGiftMsg;
import com.zq.live.proto.Room.Property;

import java.util.ArrayList;
import java.util.List;

public class GPrensentGiftMsgModel {
    BaseGift giftInfo;
    int count;
    UserInfoModel sendUserInfo;
    UserInfoModel receiveUserInfo;
    long roomID;
    long continueID;
    int continueCnt;
    float receiveUserCoin;
    List<PropertyModel> mPropertyModelList;

    public List<PropertyModel> getPropertyModelList() {
        return mPropertyModelList;
    }

    public void setPropertyModelList(List<PropertyModel> propertyModelList) {
        mPropertyModelList = propertyModelList;
    }

    public float getReceiveUserCoin() {
        return receiveUserCoin;
    }

    public void setReceiveUserCoin(float receiveUserCoin) {
        this.receiveUserCoin = receiveUserCoin;
    }

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
        gPrensentGiftMsgModel.setReceiveUserCoin(gPrensentGiftMsg.getReceiveUserCoin());
        gPrensentGiftMsgModel.setPropertyModelList(PropertyModel.toModel(gPrensentGiftMsg.getPropertyChangeListList()));
        return gPrensentGiftMsgModel;
    }

    public static class PropertyModel {
        public int userID;
        public float coinBalance;
        public float hongZuanBalance;
        public long lastChangeMs;
        public int roundSeq;
        public float curRoundSeqMeiliTotal;

        public static PropertyModel parse(Property property){
            PropertyModel propertyModel = new PropertyModel();
            propertyModel.userID = property.getUserID();
            propertyModel.coinBalance = property.getCoinBalance();
            propertyModel.hongZuanBalance = property.getHongZuanBalance();
            propertyModel.lastChangeMs = property.getLastChangeMs();
            propertyModel.roundSeq = property.getRoundSeq();
            propertyModel.curRoundSeqMeiliTotal = property.getCurRoundSeqMeiliTotal();
            return propertyModel;
        }

        public static List<PropertyModel> toModel(List<Property> propertyList) {
            ArrayList<PropertyModel> propertyModelArrayList = new ArrayList<>();
            for (Property property : propertyList) {
                PropertyModel propertyModel = PropertyModel.parse(property);
                propertyModelArrayList.add(propertyModel);
            }
            return propertyModelArrayList;
        }
    }
}
