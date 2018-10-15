package com.wali.live.watchsdk.fans.model.notification;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.MyUserInfoManager;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.push.type.GroupNotifyType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by zjn on 16-10-28.
 */
public class UpdateFansGroupMemNotifyModel extends GroupNotifyBaseModel {
    private long handler;
    private String handlerName;
    private FansGroupMemInfo fansGroupMemInfo;

    @Override
    protected String getTAG() {
        return "UpdateFansGroupMemNotifyModel";
    }

    @Override
    protected void fillMsgBrief() {
        if (TextUtils.isEmpty(handlerName)) {
            handlerName = String.valueOf(handler);
        }
        //宠爱团的通知
        if (notificationType == GroupNotifyType.BE_GROUP_MANAGER_NOTIFY || notificationType == GroupNotifyType.CANCEL_GROUP_MANAGER_NOTIFY) {
            String handlerRole = GlobalData.app().getResources().getString(R.string.vfans_owner);
            if (handler != groupOwner) {
                handlerRole = GlobalData.app().getResources().getString(R.string.vfans_admin);
            }
            String treatedSomeOne = candidateName;
            if (candidate == MyUserInfoManager.getInstance().getUuid()) {
                treatedSomeOne = GlobalData.app().getResources().getString(R.string.you);
            }
            String degree = GlobalData.app().getResources().getString(R.string.vfans_admin);
            if (fansGroupMemInfo != null && notificationType == GroupNotifyType.BE_GROUP_MANAGER_NOTIFY) {
                switch (fansGroupMemInfo.fansGroupMemType.getNumber()) {
                    case VFansCommonProto.GroupMemType.ADMIN_VALUE: {
                        degree = GlobalData.app().getResources().getString(R.string.vfans_admin);
                    }
                    break;
                    case VFansCommonProto.GroupMemType.DEPUTY_ADMIN_VALUE: {
                        degree = GlobalData.app().getResources().getString(R.string.vfans_deput_admin);
                    }
                    break;
                }
            } else {
                degree = GlobalData.app().getResources().getString(R.string.vfans_manager_role);
            }
            if (notificationType == GroupNotifyType.BE_GROUP_MANAGER_NOTIFY) {
                msgBrief = GlobalData.app().getResources().getString(R.string.vfans_notify_to_be_admin, handlerRole, treatedSomeOne, groupName, degree);
            } else {
                msgBrief = GlobalData.app().getResources().getString(R.string.vfans_notify_canel_manager, handlerRole, treatedSomeOne, groupName, degree);
            }
        } else if (notificationType == GroupNotifyType.BE_GROUP_MEM_NOTIFY) {
            if (candidate == MyUserInfoManager.getInstance().getUuid()) {
                msgBrief = GlobalData.app().getResources().getString(R.string.notify_be_group_member, groupName);
            } else {
                msgBrief = GlobalData.app().getResources().getString(R.string.notify_someone_be_group_menber, candidateName, groupName);
            }
        }
    }

    @Override
    protected void loadNotifyInfoInternal(ByteString content) {
        try {
            VFansProto.UpdateFansGroupMemNotify notifyInfo = VFansProto.UpdateFansGroupMemNotify.parseFrom(content);
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
            fansGroupMemInfo = FansGroupMemInfo.toFansGroupMemInfo(notifyInfo.getFgMemInfo());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromJson(JSONObject jsonObject) {
        handler = jsonObject.optLong("handler");
        handlerName = jsonObject.optString("handlerName");
        String fansGroupMemInfoStr = jsonObject.optString("fansGroupMemInfo");
        if (fansGroupMemInfoStr != null) {
            try {
                JSONObject jsonObject1 = new JSONObject(fansGroupMemInfoStr);
                fansGroupMemInfo = FansGroupMemInfo.loadFromJson(jsonObject1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("handler", handler);
            jsonObject.put("handlerName", handlerName);
            if (fansGroupMemInfo != null) {
                jsonObject.put("fansGroupMemInfo", fansGroupMemInfo.toJson());
            }
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

    public FansGroupMemInfo getFansGroupMemInfo() {
        return fansGroupMemInfo;
    }

    public void setFansGroupMemInfo(FansGroupMemInfo fansGroupMemInfo) {
        this.fansGroupMemInfo = fansGroupMemInfo;
    }

    /**
     * 宠爱团群成员信息
     */
    public static class FansGroupMemInfo implements Serializable {
        public static final String KEY_UUID = "uuid";
        public static final String KEY_TYPE = "fansGroupMemType";
        public static final String KEY_NICKNAME = "nickName";

        private long uuid;
        private VFansCommonProto.GroupMemType fansGroupMemType;
        private String nickName;

        public static FansGroupMemInfo toFansGroupMemInfo(VFansProto.VfansGroupMemInfo f) {
            FansGroupMemInfo fansGroupMemInfo = new FansGroupMemInfo();
            fansGroupMemInfo.setUuid(f.getUuid());
            fansGroupMemInfo.setFansGroupMemType(f.getMemType());
            fansGroupMemInfo.setNickName(f.getNickname());
            return fansGroupMemInfo;
        }

        public long getUuid() {
            return uuid;
        }

        public void setUuid(long uuid) {
            this.uuid = uuid;
        }

        public VFansCommonProto.GroupMemType getFansGroupMemType() {
            return fansGroupMemType;
        }

        public void setFansGroupMemType(VFansCommonProto.GroupMemType fansGroupMemType) {
            this.fansGroupMemType = fansGroupMemType;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public JSONObject toJson() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(KEY_UUID, uuid);
                jsonObject.put(KEY_TYPE, fansGroupMemType.name());
                jsonObject.put(KEY_NICKNAME, nickName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        public static FansGroupMemInfo loadFromJson(JSONObject jsonObject) {
            FansGroupMemInfo model = new FansGroupMemInfo();
            model.setUuid(jsonObject.optLong(KEY_UUID));
            try {
                model.setFansGroupMemType(VFansCommonProto.GroupMemType.valueOf(jsonObject.optString(KEY_TYPE)));
            } catch (Exception e) {
                MyLog.e(e);
                model.setFansGroupMemType(VFansCommonProto.GroupMemType.MASS);
            }
            model.setNickName(jsonObject.optString(KEY_NICKNAME));
            return model;
        }
    }
}
