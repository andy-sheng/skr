package com.wali.live.livesdk.live.api;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.Live2Proto.ChangeRoomInfoReq;
import com.wali.live.proto.Live2Proto.ChangeRoomInfoRsp;
import com.wali.live.proto.Live2Proto.LiveCover;

/**
 * Created by lan on 16/12/16.
 */
public class RoomInfoChangeRequest extends BaseRequest {
    public RoomInfoChangeRequest(long zuid, String liveId, String coverUrl, String packageName) {
        super(MiLinkCommand.COMMAND_LIVE_ROOM_INFO_CHANGE, "RoomInfoChange", null);

        ChangeRoomInfoReq.Builder builder = ChangeRoomInfoReq.newBuilder()
                .setZuid(zuid)
                .setLiveId(liveId);
        if (!TextUtils.isEmpty(coverUrl)) {
            builder.setLiveCover(LiveCover.newBuilder().setCoverUrl(coverUrl).build());
        }
        if (!TextUtils.isEmpty(packageName)) {
            builder.setGamePackName(packageName);
        }
        mRequest = builder.build();
    }

    protected ChangeRoomInfoRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return ChangeRoomInfoRsp.parseFrom(bytes);
    }
}
