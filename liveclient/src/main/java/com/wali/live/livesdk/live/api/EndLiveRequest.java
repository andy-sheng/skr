package com.wali.live.livesdk.live.api;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.live.BaseLiveRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.wali.live.proto.AccountProto;
import com.wali.live.proto.LiveProto.EndLiveReq;
import com.wali.live.proto.LiveProto.EndLiveRsp;

/**
 * Created by lan on 16-3-18.
 * 注意修改命令字和Action
 */
public class EndLiveRequest extends BaseLiveRequest {
    public EndLiveRequest() {
        super(MiLinkCommand.COMMAND_LIVE_END, "EndLive", null);
    }

    public EndLiveRequest(String liveId) {
        this();
        if (TextUtils.isEmpty(liveId)) {
            return;
        }
        mRequest = EndLiveReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setLiveId(liveId)
                .build();
    }

    public EndLiveRequest(String liveId, AccountProto.AppInfo appInfo) {
        this();
        if (TextUtils.isEmpty(liveId) || appInfo == null) {
            return;
        }
        mRequest = EndLiveReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setLiveId(liveId)
                .setAppInfo(appInfo)
                .setAppType(MiLinkConstant.THIRD_APP_TYPE)
                .build();
    }

    protected EndLiveRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return EndLiveRsp.parseFrom(bytes);
    }
}
