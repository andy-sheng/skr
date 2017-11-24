package com.wali.live.watchsdk.fans.model.notification;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zjn on 16-10-28.
 */
public class RemoveFansGroupMemNotifyModel extends GroupNotifyBaseModel {
    private long handler;
    private String handlerName;

    @Override
    protected String getTAG() {
        return "RemoveFansGroupMemNotifyModel";
    }

    @Override
    protected void fillMsgBrief() {
        if (TextUtils.isEmpty(handlerName)) {
            handlerName = String.valueOf(handler);
        }
        if (candidate == UserAccountManager.getInstance().getUuidAsLong()) {
            if (handler != groupOwner) {
                String manager = GlobalData.app().getResources().getString(R.string.vfans_admin_or_deput_admin);
                msgBrief = GlobalData.app().getResources().getString(R.string.notify_remove_from_group, manager, groupName);
            } else {
                String owner = GlobalData.app().getResources().getString(R.string.vfans_owner);
                msgBrief = GlobalData.app().getResources().getString(R.string.notify_remove_from_group, owner, groupName);
            }
        } else {
            msgBrief = GlobalData.app().getResources().getString(R.string.notify_remove_someone_from_group, handlerName, candidateName, groupName);
        }

    }

    @Override
    protected void loadNotifyInfoInternal(ByteString content) {
        try {
            VFansProto.RemoveFansGroupMemNotify notifyInfo = VFansProto.RemoveFansGroupMemNotify.parseFrom(content);
            candidate = notifyInfo.getCandidate();
            candidateName = notifyInfo.getCandiName();
            candidateTs = notifyInfo.getCandiHeadTs();

            handler = notifyInfo.getHandler();
            handlerName = notifyInfo.getHandlerName();

            groupId = notifyInfo.getFgId();
            groupOwner = notifyInfo.getFgOwner();
            groupName = notifyInfo.getGroupName();
            groupOwnerTs = notifyInfo.getFgOwnerHeadTs();
            groupIcon = notifyInfo.getGroupIcon();

            ts = notifyInfo.getTs();
            msg = notifyInfo.getMsg();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromJson(JSONObject jsonObject) {
        handler = jsonObject.optLong("handler");
        handlerName = jsonObject.optString("handlerName");
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("handler", handler);
            jsonObject.put("handlerName", handlerName);
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
}
