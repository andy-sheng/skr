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
        int userID;
        float coinBalance;
        float hongZuanBalance;
        long lastChangeMs;
        int roundSeq;
        float curRoundSeqMeiliTotal;

        public PropertyModel(int userID, float coinBalance, float hongZuanBalance, long lastChangeMs, int roundSeq, float curRoundSeqMeiliTotal) {
            this.userID = userID;
            this.coinBalance = coinBalance;
            this.hongZuanBalance = hongZuanBalance;
            this.lastChangeMs = lastChangeMs;
            this.roundSeq = roundSeq;
            this.curRoundSeqMeiliTotal = curRoundSeqMeiliTotal;
        }

        public int getUserID() {
            return userID;
        }

        public float getCoinBalance() {
            return coinBalance;
        }

        public float getHongZuanBalance() {
            return hongZuanBalance;
        }

        public long getLastChangeMs() {
            return lastChangeMs;
        }

        public static List<PropertyModel> toModel(List<Property> propertyList) {
            ArrayList<PropertyModel> propertyModelArrayList = new ArrayList<>();
            for (Property property : propertyList) {
                PropertyModel propertyModel = new PropertyModel(property.getUserID(),
                        property.getCoinBalance(), property.getHongZuanBalance(), property.getLastChangeMs(),
                        property.getRoundSeq(), property.getCurRoundSeqMeiliTotal());
                propertyModelArrayList.add(propertyModel);
            }

            return propertyModelArrayList;
        }
    }
}
