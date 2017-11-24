package com.wali.live.watchsdk.fans.model.notification;


import com.base.global.GlobalData;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.push.type.GroupNotifyType;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 申请入群
 */
public class ApplyJoinFansModel extends GroupNotifyBaseModel {
    private VFansCommonProto.GroupMemType fansMemType;                  // 申请加入身份
    private String applyMsg;                                             // 申请信息
    private VFansCommonProto.JoinIntentionType joinIntentionType;        // 加入vfans动机

    @Override
    protected String getTAG() {
        return "ApplyJoinFansModel";
    }

    @Override
    protected void fillMsgBrief() {
        msgBrief = GlobalData.app().getResources().getString(R.string.notify_apply_join_group, candidateName, groupName);
    }

    @Override
    protected void loadNotifyInfoInternal(ByteString content) {
        try {
            VFansProto.ApplyJoinFansGroupNotify notifyInfo = VFansProto.ApplyJoinFansGroupNotify.parseFrom(content);
            candidate = notifyInfo.getCandidate();
            groupId = notifyInfo.getFgId();
            fansMemType = notifyInfo.getMemType();
            groupOwner = notifyInfo.getFgOwner();
            applyMsg = notifyInfo.getApplyMsg();
            ts = notifyInfo.getTs();
            joinIntentionType = notifyInfo.getJoinType();
            msg = notifyInfo.getMsg();
            groupName = notifyInfo.getGroupName();

            candidateName = notifyInfo.getCandiName();
            candidateTs = notifyInfo.getCandiHeadTs();

            if (fansMemType == null) {
                fansMemType = VFansCommonProto.GroupMemType.NONE;
            }
            if (joinIntentionType == null) {
                joinIntentionType = VFansCommonProto.JoinIntentionType.ACTIVE;
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromJson(JSONObject jsonObject) {
        fansMemType = VFansCommonProto.GroupMemType.valueOf(jsonObject.optString("fansGroupMemType"));
        applyMsg = jsonObject.optString("applyMsg");
        joinIntentionType = VFansCommonProto.JoinIntentionType.valueOf(jsonObject.optString("joinIntentionType"));
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("fansGroupMemType", fansMemType.name());
            jsonObject.put("applyMsg", applyMsg);
            jsonObject.put("joinIntentionType", joinIntentionType.name());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public String getApplyMsg() {
        return applyMsg;
    }

    public void setApplyMsg(String applyMsg) {
        this.applyMsg = applyMsg;
    }

    /**
     * 同意变成已处理
     */
    public HandleJoinFansGroupNotifyModel toHandleJoinFansGroupNotifyModel(VFansCommonProto.ApplyJoinResult applyJoinResult) {
        HandleJoinFansGroupNotifyModel model = new HandleJoinFansGroupNotifyModel();
        model.setId(id);
        if (applyJoinResult == VFansCommonProto.ApplyJoinResult.PASS) {
            model.setNotificationType(GroupNotifyType.AGREE_JOIN_GROUP_NOTIFY);
        } else {
            model.setNotificationType(GroupNotifyType.REJECT_JOIN_GROUP_NOTIFY);
        }
        model.setTs(ts);
        model.setCandidate(candidate);
        model.setCandidateName(candidateName);
        model.setCandidateTs(candidateTs);
        model.setHandler(UserAccountManager.getInstance().getUuidAsLong());
        model.setHandlerName(MyUserInfoManager.getInstance().getNickname());

        model.setGroupIcon(groupIcon);
        model.setGroupName(groupName);
        model.setGroupId(groupId);
        model.setGroupOwner(groupOwner);
        model.setGroupOwnerTs(groupOwnerTs);
        model.setStatus(status);

        model.setFansJoinIntentionType(joinIntentionType);
        model.setFansMemType(fansMemType);
        model.setFansJoinResult(applyJoinResult);

        model.setMsg(applyMsg);
        model.fillMsgBrief();
        return model;
    }
}
