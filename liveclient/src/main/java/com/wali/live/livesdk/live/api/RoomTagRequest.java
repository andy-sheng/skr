package com.wali.live.livesdk.live.api;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.Live2Proto.GetRoomTagReq;
import com.wali.live.proto.Live2Proto.GetRoomTagRsp;

/**
 * Created by lan on 16/12/16.
 */
public class RoomTagRequest extends BaseRequest {
    public static final int TAG_TYPE_GAME = 1;
    public static final int TAG_TYPE_NORMAL = 2;

    public RoomTagRequest(int type) {
        super(MiLinkCommand.COMMAND_LIVE_ROOM_TAG, "RoomTag", null);
        GetRoomTagReq.Builder builder = GetRoomTagReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setType(type);
        mRequest = builder.build();
    }

    protected GetRoomTagRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return GetRoomTagRsp.parseFrom(bytes);
    }
}
