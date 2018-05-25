package com.mi.live.data.gift.model;

import com.base.log.MyLog;
import com.base.utils.language.LocaleUtil;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.repository.GiftRepository;
import com.wali.live.dao.Gift;
import com.wali.live.proto.EffectProto;

/**
 * Created by chengsimin on 16/2/21.
 */
public class GiftRecvModel<T extends Gift> {
    public static final String TAG = "GiftRecvModel";
    T gift;
    int giftId;
    long continueId;
    long userId; //用户id
    long time; // 送礼时间
    String senderName;
    int startNumber = 1; // 开始数量
    int endNumber = 1; // 结束数量
    int batchCount ; //批量赠送数量
    int mainOrbitId=1;  //若是组合礼物，则需要一个主要播放的轨道ID

    int roomStartTicket;
    private boolean isLeft = true;

    private int certificationType; // 认证类型
    private int level; // 发送者等级
    private long avatarTimestamp;

    private boolean fromSelf = false; // 自己送的

    private long leftTime;//剩余时间
    private String orderId;

    //是否为组合礼物
    public boolean isBatchGift(){
        return batchCount>1;
    }

    public GiftRecvModel() {
    }

    public GiftRecvModel copy(){
        GiftRecvModel model=new GiftRecvModel();
        model.setGift(gift);
        model.setGiftId(giftId);
        model.setContinueId(continueId);
        model.setUserId(userId);
        model.setTime(time);
        model.setSenderName(senderName);
        model.setStartNumber(startNumber);
        model.setEndNumber(endNumber);
        model.setBatchCount(batchCount);
        model.setRoomStartTicket(roomStartTicket);
        model.setIsLeft(isLeft);
        model.setCertificationType(certificationType);
        model.setLevel(level);
        model.setAvatarTimestamp(avatarTimestamp);
        model.setFromSelf(fromSelf);
        model.setLeftTime(leftTime);
        model.setOrderId(orderId);
        model.setMainOrbitId(mainOrbitId);
        return model;
    }


    public long getLeftTime() {
        return leftTime;
    }

    public void setGift(T gift) {
        this.gift = gift;
    }

    public long getAvatarTimestamp() {
        return avatarTimestamp;
    }

    public void setAvatarTimestamp(long avatarTimestamp) {
        this.avatarTimestamp = avatarTimestamp;
    }

    public long getContinueId() {
        return continueId;
    }

    public void setContinueId(long continueId) {
        this.continueId = continueId;
    }

    public long getUserId() {
        return userId;
    }

    public int getGiftId() {
        return giftId;
    }

    public void setGiftId(int giftId) {
        this.giftId = giftId;
    }

    public long getTime() {
        return time;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getGiftName() {
        if (gift == null) {
            return null;
        }
        if (!LocaleUtil.getLanguageCode().equals(LocaleUtil.LOCALE_SIMPLIFIED_CHINESE.toString())) {
            return gift.getInternationalName();
        } else {
            return gift.getName();
        }
    }

    public String getPicPath() {
        if (gift == null) {
            return null;
        }
        return gift.getPicture();
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }


    public void setTime(long time) {
        this.time = time;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public int getStartNumber() {
        return startNumber;
    }

    public int getEndNumber() {
        return endNumber;
    }

    public void setStartNumber(int startNumber) {
        this.startNumber = startNumber;
    }

    public void setEndNumber(int endNumber) {
        this.endNumber = endNumber;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public void setIsLeft(boolean isLeft) {
        this.isLeft = isLeft;
    }

    public int getCertificationType() {
        return certificationType;
    }

    public int getLevel() {
        return level;
    }

    public void setCertificationType(int certificationType) {
        this.certificationType = certificationType;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isFromSelf() {
        return fromSelf;
    }

    public void setFromSelf(boolean fromSelf) {
        this.fromSelf = fromSelf;
    }

    public int getGifType() {
        if (gift == null) {
            return 0;
        }
        return gift.getCatagory();
    }

    public int getGiftOriginType() {
        if(gift == null) {
            return 0;
        }
        return gift.getOriginGiftType();
    }

    public void setLeftTime(long leftTime) {
        this.leftTime = leftTime;
    }

    public void setLeft(boolean left) {
        isLeft = left;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }


    public int getBatchCount() {
        return batchCount;
    }

    public void setBatchCount(int batchCount) {
        this.batchCount = batchCount;
    }

    public int getMainOrbitId() {
        return mainOrbitId;
    }

    public void setMainOrbitId(int mainOrbitId) {
        this.mainOrbitId = mainOrbitId;
    }

    public int getRoomStartTicket() {
        return roomStartTicket;
    }

    public void setRoomStartTicket(int roomStartTicket) {
        this.roomStartTicket = roomStartTicket;
    }

    @Override
    public String toString() {
        return "GiftRecvModel{" +
                "gift=" + gift +
                ", giftId=" + giftId +
                ", continueId=" + continueId +
                ", userId=" + userId +
                ", time=" + time +
                ", senderName='" + senderName + '\'' +
                ", startNumber=" + startNumber +
                ", endNumber=" + endNumber +
                ", batchCount=" + batchCount +
                ", isLeft=" + isLeft +
                ", certificationType=" + certificationType +
                ", level=" + level +
                ", avatarTimestamp=" + avatarTimestamp +
                ", fromSelf=" + fromSelf +
                ", leftTime=" + leftTime +
                ", ifBatchGift="+isBatchGift()+
                ", orderId='" + orderId + '\'' +
                '}';
    }

    public static GiftRecvModel loadFromPB(EffectProto.GiftEffect effect) {
        MyLog.w(TAG, "loadFromPB effect:" + effect);
        GiftRecvModel model = new GiftRecvModel();
        if (effect != null) {
            model.setUserId(effect.getUuid());
            model.setGiftId(effect.getGiftId());
            model.setLeftTime(effect.getLeftTime());
            model.setOrderId(effect.getUniq());
            model.setAvatarTimestamp(effect.getAvatar());
            model.setSenderName(effect.getNickname());
            model.setLevel(effect.getLevel());
            model.setCertificationType(effect.getCertificationType());
            GiftRepository.fillGiftEntityById(model);
        }
        return model;
    }

    public static GiftRecvModel loadFromBarrage(BarrageMsg msg, BarrageMsg.GiftMsgExt ext) {
        GiftRecvModel model = new GiftRecvModel();
        if (msg != null && ext != null) {
            model.setUserId(msg.getSender());
            model.setGiftId(ext.giftId);
            model.setStartNumber(ext.giftCount);
            model.setEndNumber(ext.giftCount);
            model.setTime(msg.getSentTime());
            model.setSenderName(msg.getSenderName());
            model.setContinueId(ext.continueId);
            model.setAvatarTimestamp(ext.avatarTimestamp);
            model.setIsLeft(!msg.isFromPkOpponent());
            model.setCertificationType(msg.getCertificationType());
            model.setLevel(msg.getSenderLevel());
            model.setFromSelf(msg.getSender() == MyUserInfoManager.getInstance().getUser().getUid());
            model.setOrderId(ext.orderId);
            model.setBatchCount(ext.batch_count);
            GiftRepository.fillGiftEntityById(model);
        }
        return model;
    }

    public T getGift() {
        return gift;
    }

    public String getSendDescribe() {
        if (gift == null) {
            return "";
        }
        return gift.getSendDescribe();
    }
}
