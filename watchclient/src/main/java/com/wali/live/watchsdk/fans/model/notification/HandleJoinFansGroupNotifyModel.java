package com.wali.live.watchsdk.fans.model.notification;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.push.type.GroupNotifyType;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 加群处理结果
 * Created by zjn on 16-10-28.
 */
public class HandleJoinFansGroupNotifyModel extends GroupNotifyBaseModel {
    private long handler;                                                   //处理人
    private String handlerName;                                             //处理人名称
    private VFansCommonProto.ApplyJoinResult fansJoinResult;                //vfans 加入结果
    private VFansCommonProto.GroupMemType fansMemType;                      //vfans的memberType
    private VFansCommonProto.JoinIntentionType fansJoinIntentionType;       // vfans 的加群动机
    long referrer;                                                          //推荐人

    @Override
    protected String getTAG() {
        return "HandleJoinFansGroupNotifyModel";
    }

    @Override
    protected void fillMsgBrief() {
        if (TextUtils.isEmpty(handlerName)) {
            handlerName = String.valueOf(handler);
        }
        if (notificationType == GroupNotifyType.AGREE_JOIN_GROUP_NOTIFY) {
            if (handler != groupOwner) {
                String manager = GlobalData.app().getResources().getString(R.string.vfans_admin_or_deput_admin);
                msgBrief = GlobalData.app().getResources().getString(R.string.notify_agree_join_group, manager, candidateName, groupName);
            } else {
                String owner = GlobalData.app().getResources().getString(R.string.vfans_owner);
                msgBrief = GlobalData.app().getResources().getString(R.string.notify_agree_join_group, owner, candidateName, groupName);
            }
        } else if (notificationType == GroupNotifyType.REJECT_JOIN_GROUP_NOTIFY) {
            if (handler != groupOwner) {
                String manager = GlobalData.app().getResources().getString(R.string.vfans_admin_or_deput_admin);
                msgBrief = GlobalData.app().getResources().getString(R.string.notify_reject_join_group, manager, candidateName, groupName);
            } else {
                String owner = GlobalData.app().getResources().getString(R.string.vfans_owner);
                msgBrief = GlobalData.app().getResources().getString(R.string.notify_reject_join_group, owner, candidateName, groupName);
            }
        }
    }

    @Override
    protected void loadNotifyInfoInternal(ByteString content) {
        try {
            VFansProto.HandleJoinFansGroupNotify notifyInfo = VFansProto.HandleJoinFansGroupNotify.parseFrom(content);
            handler = notifyInfo.getHandler();
            handlerName = notifyInfo.getHandlerName();

            candidate = notifyInfo.getCandidate();
            candidateName = notifyInfo.getCandiName();
            candidateTs = notifyInfo.getCandiHeadTs();

            msg = notifyInfo.getMsg();

            groupId = notifyInfo.getFgId();
            groupOwner = notifyInfo.getFgOwner();
            groupName = notifyInfo.getGroupName();
            groupIcon = notifyInfo.getGroupIcon();
            groupOwnerTs = notifyInfo.getFgOwnerHeadTs();


            fansJoinResult = notifyInfo.getHandleResult();
            fansMemType = notifyInfo.getMemType();
            fansJoinIntentionType = notifyInfo.getJoinType();
            referrer = notifyInfo.getReferrer();

            if (fansJoinResult == null) {
                fansJoinResult = VFansCommonProto.ApplyJoinResult.PASS;
            }
            if (fansMemType == null) {
                fansMemType = VFansCommonProto.GroupMemType.NONE;
            }
            if (fansJoinIntentionType == null) {
                fansJoinIntentionType = VFansCommonProto.JoinIntentionType.ACTIVE;
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromJson(JSONObject jsonObject) {
        handler = jsonObject.optLong("handler");
        handlerName = jsonObject.optString("handlerName");
        fansJoinResult = VFansCommonProto.ApplyJoinResult.valueOf(jsonObject.optString("handleFGResultType"));
        fansMemType = VFansCommonProto.GroupMemType.valueOf(jsonObject.optString("fansGroupMemType"));
        fansJoinIntentionType = VFansCommonProto.JoinIntentionType.valueOf(jsonObject.optString("joinFGItentionType"));
        referrer = jsonObject.optLong("referrer");
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("handler", handler);
            jsonObject.put("handlerName", handlerName);
            jsonObject.put("handleFGResultType", fansJoinResult.name());
            jsonObject.put("fansGroupMemType", fansMemType.name());
            jsonObject.put("joinIntentionType", fansJoinIntentionType.name());
            jsonObject.put("referrer", referrer);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public long getHandler() {
        return handler;
    }

    public void setHandler(long handler) {
        this.handler = handler;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public VFansCommonProto.JoinIntentionType getFansJoinIntentionType() {
        return fansJoinIntentionType;
    }

    public void setFansJoinIntentionType(VFansCommonProto.JoinIntentionType fansJoinIntentionType) {
        this.fansJoinIntentionType = fansJoinIntentionType;
    }

    public VFansCommonProto.ApplyJoinResult getFansJoinResult() {
        return fansJoinResult;
    }

    public void setFansJoinResult(VFansCommonProto.ApplyJoinResult fansJoinResult) {
        this.fansJoinResult = fansJoinResult;
    }

    public VFansCommonProto.GroupMemType getFansMemType() {
        return fansMemType;
    }

    public void setFansMemType(VFansCommonProto.GroupMemType fansMemType) {
        this.fansMemType = fansMemType;
    }

    public long getReferrer() {
        return referrer;
    }

    public void setReferrer(long referrer) {
        this.referrer = referrer;
    }
}
