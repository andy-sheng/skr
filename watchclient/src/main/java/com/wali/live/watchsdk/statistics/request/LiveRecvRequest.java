package com.wali.live.watchsdk.statistics.request;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.StatisticsProto.LiveRecvFlagItem;
import com.wali.live.proto.StatisticsProto.LiveRecvFlagReq;
import com.wali.live.proto.StatisticsProto.LiveRecvFlagRsp;
import com.wali.live.watchsdk.statistics.item.MilinkStatisticsItem;

/**
 * Created by lan on 2017/6/28.
 */
public class LiveRecvRequest extends BaseRequest {

    public LiveRecvRequest(MilinkStatisticsItem item) {
        super(MiLinkCommand.COMMAND_STATISTICS_RECOMMEND_TAG, "LiveRecvFlag");
        build(item);
    }

    private void build(MilinkStatisticsItem item) {
        LiveRecvFlagItem flagItem = LiveRecvFlagItem.newBuilder()
                .setDate(item.getDate())
                .setType(item.getType())
                .setLog(item.getCommonLog())
                .build();

        mRequest = LiveRecvFlagReq.newBuilder()
                .addItems(flagItem)
                .build();
        MyLog.d(TAG, "request=" + mRequest);
    }

    @Override
    protected LiveRecvFlagRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveRecvFlagRsp.parseFrom(bytes);
    }
}
