package com.wali.live.watchsdk.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.live.BaseLiveRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveProto.ViewerTopReq;
import com.wali.live.proto.LiveProto.ViewerTopRsp;

/**
 * Created by lan on 16-3-18.
 * 注意修改命令字和Action
 */
public class ViewerTopRequest extends BaseLiveRequest {

    public ViewerTopRequest() {
        super(MiLinkCommand.COMMAND_LIVE_VIEWER_TOP,"ViewerTop",null);
    }

    public ViewerTopRequest(long ownerId, String liveId) {
        this();
        mRequest = ViewerTopReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(ownerId)
                .setLiveId(liveId)
                .build();
    }

    protected ViewerTopRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return ViewerTopRsp.parseFrom(bytes);
    }
}
