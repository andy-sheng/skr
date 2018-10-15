package com.mi.liveassistant.barrage.request;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.liveassistant.common.api.BaseRequest;
import com.mi.liveassistant.milink.command.MiLinkCommand;
import com.mi.liveassistant.proto.LiveMessageProto;

/**
 * 拉取房间弹幕
 *
 * Created by wuxiaoshan on 17-4-28.
 */
public class PullRoomMsgRequest extends BaseRequest {
    /**
     *
     *
     * @param fromUserId
     * @param roomId
     * @param lastSyncImportantTs
     * @param lastSyncNormalTs
     */
    public PullRoomMsgRequest(long fromUserId, final String roomId, long lastSyncImportantTs, long lastSyncNormalTs) {
        super(MiLinkCommand.COMMAND_PULL_ROOM_MESSAGE , "PullRoomMsg");
        mRequest = LiveMessageProto.SyncRoomMessageRequest.newBuilder()
                .setFromUser(fromUserId)
                .setRoomId(roomId)
                .setLastSyncImportantTs(lastSyncImportantTs)
                .setLastSyncNormalTs(lastSyncNormalTs)
                .build();
    }

    @Override
    protected LiveMessageProto.SyncRoomMessageResponse parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveMessageProto.SyncRoomMessageResponse.parseFrom(bytes);
    }
}
