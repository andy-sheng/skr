package com.wali.live.watchsdk.fans.push;

import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.callback.MiLinkPacketDispatcher;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.GroupMessageProto;
import com.wali.live.watchsdk.fans.model.notification.GroupNotifyBaseModel;
import com.wali.live.watchsdk.fans.model.notification.HandleJoinFansGroupNotifyModel;
import com.wali.live.watchsdk.fans.push.data.FansNotifyRepository;
import com.wali.live.watchsdk.fans.push.event.GroupNotifyUpdateEvent;
import com.wali.live.watchsdk.fans.push.mapper.GroupNotifyMapper;
import com.wali.live.watchsdk.fans.push.type.GroupNotifyType;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by zyh on 2017/11/21.
 *
 * @module群通知的push类
 */

public class FansNotificationManager implements MiLinkPacketDispatcher.PacketDataHandler {
    private final String TAG = "FansNotificationManager";

    @Override
    public boolean processPacketData(PacketData data) {
        String command = data.getCommand();
        MyLog.w(TAG, "processPacketData command=" + command);
        switch (command) {
            case MiLinkCommand.COMMAND_VFANS_GETNOTIFICATION:
                break;
            case MiLinkCommand.COMMAND_VFANS_PUSHNOTIFICATION:
                processPushNotificationPackData(data);
                break;
        }
        return false;
    }

    @Override
    public String[] getAcceptCommand() {
        return new String[]{
                MiLinkCommand.COMMAND_VFANS_GETNOTIFICATION,
                MiLinkCommand.COMMAND_VFANS_PUSHNOTIFICATION
        };
    }

    private void processPushNotificationPackData(PacketData packetData) {
        try {
            GroupMessageProto.GroupNotification rsp = GroupMessageProto.GroupNotification.parseFrom(packetData.getData());
            GroupNotifyBaseModel groupNotifyBaseModel = GroupNotifyMapper.loadFromPB(rsp);
            if (groupNotifyBaseModel != null) {
                boolean needHandle = true;
                if (groupNotifyBaseModel.getNotificationType() == GroupNotifyType.AGREE_JOIN_GROUP_NOTIFY
                        || groupNotifyBaseModel.getNotificationType() == GroupNotifyType.REJECT_JOIN_GROUP_NOTIFY) {
                    HandleJoinFansGroupNotifyModel handleJoinFansGroupNotifyModel = (HandleJoinFansGroupNotifyModel) groupNotifyBaseModel;
                    if (handleJoinFansGroupNotifyModel.getHandler() == UserAccountManager.getInstance().getUuidAsLong()) {
                        needHandle = false;
                    }
                }
                if (needHandle) {
                    // 插入数据库
                    if (GroupNotifyLocalStore.getInstance().insertOrReplaceGroupNotifyBaseModel(groupNotifyBaseModel)) {
                        GroupNotifyUpdateEvent event = GroupNotifyLocalStore.getInstance().getGroupNotifyBaseModelListEventFromDB();
                        EventBus.getDefault().post(event);
                        FansNotifyRepository.setPullGroupNotificationTs(groupNotifyBaseModel.getTs());
                        FansNotifyRepository.sendFansNotifyAck(groupNotifyBaseModel.getId(), 0, groupNotifyBaseModel.getTs());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
