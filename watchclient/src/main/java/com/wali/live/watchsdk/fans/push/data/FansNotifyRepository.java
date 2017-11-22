package com.wali.live.watchsdk.fans.push.data;

import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.preference.MLPreferenceUtils;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.GroupMessageProto;

/**
 * Created by anping on 17/7/3.
 */
public class FansNotifyRepository {
    private static final String TAG = "FansNotifyRepository";
    public final static String PREF_KEY_PULL_GROUP_NOTIFICATION_TS = "key_pull_fans_notification_ts_"; //注意多账号

    public void syncFansNotify() {
        GroupMessageProto.GetGroupNotificationRequest.Builder reqBuilder = GroupMessageProto
                .GetGroupNotificationRequest.newBuilder()
                .setUserId(UserAccountManager.getInstance().getUuidAsLong())
                .setLastTs(getPullFansNotificationTs())
                .setCid(System.currentTimeMillis());

        GroupMessageProto.GetGroupNotificationRequest req = reqBuilder.build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_VFANS_GETNOTIFICATION);
        data.setData(req.toByteArray());
        data.setNeedCached(true);
        MyLog.w(TAG, "requestGroupNotificationFromServe req:" + req);
        MiLinkClientAdapter.getsInstance().sendAsync(data);
    }

    /**
     * 数据库写成功后要写时间戳
     */
    public static void setPullGroupNotificationTs(long ts) {
        if (ts > getPullFansNotificationTs()) {
            MLPreferenceUtils.setSettingLong(PREF_KEY_PULL_GROUP_NOTIFICATION_TS + UserAccountManager.getInstance().getUuidAsLong(), ts);
        }
    }

    public static long getPullFansNotificationTs() {
        return MLPreferenceUtils.getSettingLong(PREF_KEY_PULL_GROUP_NOTIFICATION_TS + UserAccountManager.getInstance().getUuidAsLong(), 0);
    }

    /**
     * 回ack给服务器
     */
    public static void sendFansNotifyAck(long id, long cid, long maxTs) {
        GroupMessageProto.AckGroupNotificationRequest request = GroupMessageProto.AckGroupNotificationRequest.newBuilder()
                .setId(id)
                .setCid(cid)
                .setTs(maxTs)
                .setUserId(UserAccountManager.getInstance().getUuidAsLong())
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_VFANS_ACKNOTIFICATION);
        data.setData(request.toByteArray());
        data.setNeedCached(true);
        MyLog.w(TAG, "sendGroupNotifyAck req:" + request);
        MiLinkClientAdapter.getsInstance().sendAsync(data);
    }
}
