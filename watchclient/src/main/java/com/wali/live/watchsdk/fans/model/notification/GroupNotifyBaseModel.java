package com.wali.live.watchsdk.fans.model.notification;

import android.text.TextUtils;

import com.google.protobuf.ByteString;
import com.wali.live.dao.GroupNotify;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * 各种群通知消息的基础基类，别的类继承这个类
 * Created by zjn on 16-10-26.
 */
public abstract class GroupNotifyBaseModel implements Serializable {
    protected final String TAG = getTAG();
    /*
    * status的每个bit代表相应的含义
    *  */
    public static final int STATUS_HAS_DEAL = 0x0001;
    public static final int STATUS_HAS_READ = 0x0010;

    protected long id;// 唯一标示通知的
    protected int notificationType;//消息类型
    protected long ts;//时间戳
    protected int status;// 状态

    protected long candidate;// 被处理人
    protected String candidateName;// 被处理人名称
    protected long candidateTs;// 被处理人头像时间戳

    protected long groupId; // 群id
    protected long groupOwner;// 群主id
    protected String groupName;// 群名称
    protected String groupIcon;// 群头像
    protected long groupOwnerTs;// 群主头像时间戳

    protected String msg;// 文案
    protected String msgBrief; // 在会话栏显示的内容

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }

    public boolean hasDeal() {
        return (status & STATUS_HAS_DEAL) != 0;
    }

    public boolean hasRead() {
        return (status & STATUS_HAS_READ) != 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void addStatus(int st) {
        this.status = (this.status | st);
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getGroupOwner() {
        return groupOwner;
    }

    public void setGroupOwner(long groupOwner) {
        this.groupOwner = groupOwner;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public long getGroupOwnerTs() {
        return groupOwnerTs;
    }

    public void setGroupOwnerTs(long groupOwnerTs) {
        this.groupOwnerTs = groupOwnerTs;
    }

    public String getGroupIcon() {
        return groupIcon;
    }

    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;
    }

    public long getCandidate() {
        return candidate;
    }

    public void setCandidate(long candidate) {
        this.candidate = candidate;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public long getCandidateTs() {
        return candidateTs;
    }

    public void setCandidateTs(long candidateTs) {
        this.candidateTs = candidateTs;
    }

    public String getMsgBrief() {
        return msgBrief;
    }

    public void setMsgBrief(String msgBrief) {
        this.msgBrief = msgBrief;
    }

    public final void loadNotifyInfo(ByteString content) {
        loadNotifyInfoInternal(content);
        afterCompleteField();
        fillMsgBrief();
    }

    public void afterCompleteField() {
        if (TextUtils.isEmpty(candidateName)) {
            candidateName = String.valueOf(candidate);
        }
        if (TextUtils.isEmpty(groupName)) {
            groupName = String.valueOf(groupId);
        }
    }

    protected abstract String getTAG();

    protected abstract void fillMsgBrief();

    protected abstract void loadNotifyInfoInternal(ByteString content);

    public abstract JSONObject toJson();

    public abstract void loadFromJson(JSONObject jsonObject);

    public void parse(GroupNotify gn) {
        this.id = gn.getNotifyId();
        this.notificationType = gn.getType();
        this.ts = gn.getTime();
        this.status = gn.getStatus();
        this.candidate = gn.getCandidate();
        this.candidateName = gn.getCandidateName();
        this.candidateTs = gn.getCandidateTs();
        this.groupId = gn.getGroupId();
        this.groupOwner = gn.getGroupOwner();
        this.msg = gn.getMsg();
        this.groupName = gn.getGroupName();
        this.groupIcon = gn.getGroupIcon();
        this.groupOwnerTs = gn.getGroupOwnerTs();
        this.msgBrief = gn.getMsgBrief();
        String contentStr = gn.getContent();
        try {
            JSONObject jsonObject = new JSONObject(contentStr);
            loadFromJson(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
