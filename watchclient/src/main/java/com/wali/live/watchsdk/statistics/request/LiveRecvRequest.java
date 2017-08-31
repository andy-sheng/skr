package com.wali.live.watchsdk.statistics.request;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.StatisticsProto.LiveRecvFlagItem;
import com.wali.live.proto.StatisticsProto.LiveRecvFlagReq;
import com.wali.live.proto.StatisticsProto.LiveRecvFlagRsp;

import java.util.List;

/**
 * Created by lan on 2017/6/28.
 */
public class LiveRecvRequest extends BaseRequest {

    public LiveRecvRequest(LiveRecvFlagItem item) {
        super(MiLinkCommand.COMMAND_STATISTICS_RECOMMEND_TAG, "LiveRecvFlag");
        build(item);
    }

    public LiveRecvRequest(List<LiveRecvFlagItem> itemList) {
        super(MiLinkCommand.COMMAND_STATISTICS_RECOMMEND_TAG, "LiveRecvFlag");
        build(itemList);
    }

    private void build(LiveRecvFlagItem item) {
        mRequest = LiveRecvFlagReq.newBuilder()
                .addItems(item)
                .build();
        MyLog.d(TAG, "request=" + mRequest);
    }

    private void build(List<LiveRecvFlagItem> itemList) {
        mRequest = LiveRecvFlagReq.newBuilder()
                .addAllItems(itemList)
                .build();
        MyLog.d(TAG, "request=" + mRequest);
    }

    @Override
    protected LiveRecvFlagRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveRecvFlagRsp.parseFrom(bytes);
    }
}
