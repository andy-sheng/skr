package com.mi.live.data.api.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveProto.RoomInfoReq;
import com.wali.live.proto.LiveProto.RoomInfoRsp;

/**
 * Created by lan on 2017/7/12.
 *
 * @description 查询房间状态
 */
public class RoomInfoRequest extends BaseRequest {
    private RoomInfoReq.Builder mBuilder = RoomInfoReq.newBuilder();


    public RoomInfoRequest(long zuid, String liveId) {
        this(zuid, liveId, false);
    }

    public RoomInfoRequest(long zuid, String liveId, boolean isGameOnly) {
        super(MiLinkCommand.COMMAND_LIVE_ROOM_INFO, "getRoomInfo");
        build(zuid, liveId, isGameOnly);
    }

    private void build(long zuid, String liveId, boolean isGameOnly) {
        mBuilder = RoomInfoReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(zuid)
                .setLiveId(liveId)
                .setGetLatestLive(false)
                .setGetGameInfoOnly(isGameOnly);

        mRequest = mBuilder.build();
    }

    protected RoomInfoRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return RoomInfoRsp.parseFrom(bytes);
    }
}
