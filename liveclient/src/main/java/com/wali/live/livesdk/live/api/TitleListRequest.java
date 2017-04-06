package com.wali.live.livesdk.live.api;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.live.BaseLiveRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.livesdk.live.presenter.viewmodel.TitleViewModel;
import com.wali.live.proto.Live2Proto.GetTitleListReq;
import com.wali.live.proto.Live2Proto.GetTitleListRsp;

/**
 * Created by lan on 17/4/5.
 */
public class TitleListRequest extends BaseLiveRequest {
    public TitleListRequest() {
        this(TitleViewModel.SOURCE_NORMAL);
    }

    public TitleListRequest(int source) {
        super(MiLinkCommand.COMMAND_LIVE_GET_TITLE_LIST, "GetTitleList", null);

        GetTitleListReq.Builder builder =
                GetTitleListReq.newBuilder()
                        .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                        .addSource(source);
        mRequest = builder.build();
    }

    protected GetTitleListRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return GetTitleListRsp.parseFrom(bytes);
    }
}
