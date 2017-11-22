package com.wali.live.watchsdk.fans.push.mapper;

import com.mi.live.data.account.UserAccountManager;
import com.wali.live.dao.GroupNotify;
import com.wali.live.proto.GroupMessageProto;
import com.wali.live.watchsdk.fans.model.notification.ApplyJoinFansModel;
import com.wali.live.watchsdk.fans.model.notification.GroupNotifyBaseModel;
import com.wali.live.watchsdk.fans.model.notification.HandleJoinFansGroupNotifyModel;
import com.wali.live.watchsdk.fans.model.notification.RemoveFansGroupMemNotifyModel;
import com.wali.live.watchsdk.fans.model.notification.UpdateFansGroupMemNotifyModel;
import com.wali.live.watchsdk.fans.push.type.GroupNotifyType;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chengsimin on 16/10/28.
 */

public class GroupNotifyMapper {
    private static final String TAG = "GroupNotifyMapper";

    /**
     * GroupNotification为GroupNotifyBaseModel
     */
    public static GroupNotifyBaseModel loadFromPB(GroupMessageProto.GroupNotification rsp) {
        if (rsp == null) {
            return null;
        }
        GroupNotifyBaseModel model = null;
        switch (rsp.getType()) {
            case GroupNotifyType.APPLY_JOIN_GROUP_NOTIFY: {
                model = new ApplyJoinFansModel();
            }
            break;
            case GroupNotifyType.REJECT_JOIN_GROUP_NOTIFY:
            case GroupNotifyType.AGREE_JOIN_GROUP_NOTIFY: {
                model = new HandleJoinFansGroupNotifyModel();
            }
            break;
            case GroupNotifyType.BE_GROUP_MANAGER_NOTIFY:
            case GroupNotifyType.CANCEL_GROUP_MANAGER_NOTIFY:
            case GroupNotifyType.BE_GROUP_MEM_NOTIFY: {
                model = new UpdateFansGroupMemNotifyModel();
            }
            break;
            case GroupNotifyType.REMOVE_GROUP_MEM_NOTIFY: {
                model = new RemoveFansGroupMemNotifyModel();
            }
            break;
            //TODO 粉丝团禁言的操作入口暂时没找到,等到需要的时候再加
            case GroupNotifyType.FORBID_GROUP_MEM_NOTIFY: {
            }
            break;
            case GroupNotifyType.CANCEL_FORBID_GROUP_MEM_NOTIFY: {
            }
            break;
            case GroupNotifyType.GROUP_MEM_QUIT_GROUP_NOTIFY:
                break;
        }
        if (model != null) {
            model.setNotificationType(rsp.getType());
            model.setId(rsp.getId());
            model.setTs(rsp.getTs());
            model.loadNotifyInfo(rsp.getContent());
        }
        return model;
    }

    /**
     * 从数据库中读取GroupNotifyModel
     */
    public static GroupNotifyBaseModel transformGroupNotifyToGroupNotifyBaseModel(GroupNotify gn) {
        GroupNotifyBaseModel baseModel = null;
        switch (gn.getType()) {
            case GroupNotifyType.APPLY_JOIN_GROUP_NOTIFY: {
                baseModel = new ApplyJoinFansModel();
            }
            break;
            case GroupNotifyType.REJECT_JOIN_GROUP_NOTIFY:
            case GroupNotifyType.AGREE_JOIN_GROUP_NOTIFY: {
                baseModel = new HandleJoinFansGroupNotifyModel();
            }
            break;
            case GroupNotifyType.BE_GROUP_MANAGER_NOTIFY:
            case GroupNotifyType.CANCEL_GROUP_MANAGER_NOTIFY:
            case GroupNotifyType.BE_GROUP_MEM_NOTIFY: {
                baseModel = new UpdateFansGroupMemNotifyModel();
            }
            break;
            case GroupNotifyType.REMOVE_GROUP_MEM_NOTIFY: {
                baseModel = new RemoveFansGroupMemNotifyModel();
            }
            break;
            //TODO 粉丝团禁言的操作入口暂时没找到,等到需要的时候再加
            case GroupNotifyType.FORBID_GROUP_MEM_NOTIFY: {
            }
            break;
            case GroupNotifyType.CANCEL_FORBID_GROUP_MEM_NOTIFY: {
            }
            break;
            case GroupNotifyType.GROUP_MEM_QUIT_GROUP_NOTIFY:
                break;
        }
        if (baseModel != null) {
            baseModel.setId(gn.getNotifyId());
            baseModel.setNotificationType(gn.getType());
            baseModel.setTs(gn.getTime());
            baseModel.setStatus(gn.getStatus());

            baseModel.setCandidate(gn.getCandidate());
            baseModel.setCandidateName(gn.getCandidateName());
            baseModel.setCandidateTs(gn.getCandidateTs());

            baseModel.setGroupId(gn.getGroupId());
            baseModel.setGroupOwner(gn.getGroupOwner());
            baseModel.setMsg(gn.getMsg());
            baseModel.setGroupName(gn.getGroupName());
            baseModel.setGroupIcon(gn.getGroupIcon());
            baseModel.setGroupOwnerTs(gn.getGroupOwnerTs());

            baseModel.setMsgBrief(gn.getMsgBrief());
            String contentStr = gn.getContent();

            try {
                JSONObject jsonObject = new JSONObject(contentStr);
                baseModel.loadFromJson(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return baseModel;
    }

    public static GroupNotify transformGroupNotifyBaseModelToGroupNotify(GroupNotifyBaseModel groupNotifyBaseModel) {
        GroupNotify gn = null;
        if (groupNotifyBaseModel != null) {
            gn = new GroupNotify();
            gn.setNotifyId(groupNotifyBaseModel.getId());
            gn.setType(groupNotifyBaseModel.getNotificationType());
            gn.setTime(groupNotifyBaseModel.getTs());
            gn.setStatus(groupNotifyBaseModel.getStatus());
            gn.setCandidate(groupNotifyBaseModel.getCandidate());
            gn.setCandidateName(groupNotifyBaseModel.getCandidateName());
            gn.setCandidateTs(groupNotifyBaseModel.getCandidateTs());
            gn.setGroupId(groupNotifyBaseModel.getGroupId());
            gn.setGroupOwner(groupNotifyBaseModel.getGroupOwner());
            gn.setMsg(groupNotifyBaseModel.getMsg());
            gn.setGroupName(groupNotifyBaseModel.getGroupName());
            gn.setGroupIcon(groupNotifyBaseModel.getGroupIcon());
            gn.setGroupOwnerTs(groupNotifyBaseModel.getTs());
            gn.setMsgBrief(groupNotifyBaseModel.getMsgBrief());
            gn.setContent(groupNotifyBaseModel.toJson().toString());
            gn.setLocalUserId(UserAccountManager.getInstance().getUuidAsLong());
        }
        return gn;
    }
}
