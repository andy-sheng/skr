package com.wali.live.watchsdk.sixin.message;

import com.base.global.GlobalData;
import com.base.utils.date.DateTimeUtils;
import com.mi.live.data.user.User;
import com.wali.live.dao.SixinMessage;
import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;


/**
 * Created by lan on 16-2-23.
 * <p>
 * Todo: 先实现最小的文本功能
 */
public class SixinMessageModel extends BaseViewModel implements Comparable<SixinMessageModel> {
    private long msgId;
    private long sentTime = 0;
    private boolean isInbound = false;
    private String body;
    private User sender;
    private int msgType;
    private int msgStatus;
    private int outboundStatus;
    private String formatSentTime;
    private long receiveTime;
    private long targetId;
    private int certificationType;
    private int targetType;
    private int serverStoreStatus;  //服务器的状态，已读未读删除
    private long msgSeq;

    public SixinMessageModel(SixinMessage sixinMessage) {
        setMsgId(sixinMessage.getId());
        setSentTime(sixinMessage.getSentTime());
        setIsInbound(sixinMessage.getIsInbound());

        User sender = new User();
        sender.setUid(sixinMessage.getTarget());
        sender.setNickname(sixinMessage.getTargetName());
        setSender(sender);

        setMsgType(sixinMessage.getMsgTyppe());
        setMsgStatus(sixinMessage.getMsgStatus());
        setOutboundStatus(sixinMessage.getOutboundStatus());
        setFormatSentTime(DateTimeUtils.formatTimeStringForCompose(GlobalData.app(), sixinMessage.getReceivedTime()));
        setTargetId(sixinMessage.getTarget());
        setReceiveTime(sixinMessage.getReceivedTime());

        setBody(sixinMessage.getBody());
        setCertificationType(sixinMessage.getCertificationType());
        setTargetType(sixinMessage.getTargetType());
        setServerStoreStatus(sixinMessage.getServerStoreStatus());
        setMsgSeq(sixinMessage.getMsgSeq() == null ? 0 : sixinMessage.getMsgSeq());
    }

    public void updateModel(SixinMessageModel model) {
        setMsgId(model.getMsgId());
        setSentTime(model.getSentTime());
        setIsInbound(model.isInbound());

        setSender(model.getSender());

        setMsgType(model.getMsgType());
        setMsgStatus(model.getMsgStatus());
        setOutboundStatus(model.getOutboundStatus());
        setFormatSentTime(model.getFormatSentTime());
        setTargetId(model.getTargetId());
        setReceiveTime(model.getReceiveTime());

        setBody(model.getBody());
        setCertificationType(model.getCertificationType());
        setTargetType(model.getTargetType());
        setServerStoreStatus(model.getServerStoreStatus());
        setMsgSeq(model.getMsgSeq());
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public long getSentTime() {
        return sentTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    public boolean isInbound() {
        return isInbound;
    }

    public void setIsInbound(boolean isInbound) {
        this.isInbound = isInbound;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public int getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(int msgStatus) {
        this.msgStatus = msgStatus;
    }

    public int getOutboundStatus() {
        return outboundStatus;
    }

    public void setOutboundStatus(int outboundStatus) {
        this.outboundStatus = outboundStatus;
    }

    public String getFormatSentTime() {
        return formatSentTime;
    }

    public void setFormatSentTime(String formatSentTime) {
        this.formatSentTime = formatSentTime;
    }

    public long getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }

    public long getTargetId() {
        return targetId;
    }

    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    public int getCertificationType() {
        return certificationType;
    }

    public void setCertificationType(int certificationType) {
        this.certificationType = certificationType;
    }

    public int getTargetType() {
        return targetType;
    }

    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }

    public int getServerStoreStatus() {
        return serverStoreStatus;
    }

    public void setServerStoreStatus(int serverStoreStatus) {
        this.serverStoreStatus = serverStoreStatus;
    }

    public long getMsgSeq() {
        return msgSeq;
    }

    public void setMsgSeq(long msgSeq) {
        this.msgSeq = msgSeq;
    }

    @Override
    public int compareTo(SixinMessageModel another) {
        //先按照发送的时间排序，在按照id 排序
        if (another == null) {
            return 1;
        }
        if (this.getReceiveTime() > another.getReceiveTime()) {
            return 1;
        } else if (this.getReceiveTime() < another.getReceiveTime()) {
            return -1;
        } else {
            if (this.getMsgId() > another.getMsgId()) {
                return 1;
            } else if (this.getMsgId() < another.getMsgId()) {
                return -1;
            }
        }
        return 0;
    }
}
