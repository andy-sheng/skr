package com.wali.live.livesdk.live.api;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.live.BaseLiveRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.wali.live.proto.AccountProto;
import com.wali.live.proto.LiveProto;

/**
 * Created by zyh on 16-6-12.
 * 注意修改命令字和Action
 */
public class GetRoomIdRequest extends BaseLiveRequest {

    public GetRoomIdRequest() {
        super(MiLinkCommand.COMMAND_LIVE_GET_ROOM_ID, "GetRoomId", null);

        LiveProto.GetRoomIdReq.Builder builder = LiveProto.GetRoomIdReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong());
        mRequest = builder.build();
        MyLog.v("GetRoomIdRequest = " + mRequest.toString());
    }

    public GetRoomIdRequest(AccountProto.AppInfo appInfo) {
        super(MiLinkCommand.COMMAND_LIVE_GET_ROOM_ID, "GetRoomId", null);

        LiveProto.GetRoomIdReq.Builder builder = LiveProto.GetRoomIdReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setAppType(MiLinkConstant.THIRD_APP_TYPE).setAppInfo(appInfo);
        mRequest = builder.build();
        MyLog.v("GetRoomIdRequest = " + mRequest.toString());
    }

    protected LiveProto.GetRoomIdRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveProto.GetRoomIdRsp.parseFrom(bytes);
    }
}
