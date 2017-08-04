package com.wali.live.watchsdk.channel.sublist.request;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.HotChannelProto;
import com.wali.live.watchsdk.channel.sublist.presenter.SubChannelParam;

/**
 * Created by lan on 16-3-18.
 *
 * @module 频道
 * @description 请求推荐频道二级页面的数据
 */
public class GetSubListRequest extends BaseRequest {
    public GetSubListRequest(SubChannelParam param, int gender) {
        super(MiLinkCommand.COMMAND_HOT_CHANNEL_SUB_LIST, "GetSubList");
        generateRequest(param, gender);
    }

    private HotChannelProto.GetRecommendSublistReq.Builder generateBuilder(SubChannelParam param) {
        return HotChannelProto.GetRecommendSublistReq.newBuilder()
                .setUid(UserAccountManager.getInstance().getUuidAsLong())
                .setSubListId(param.getId());
    }

    private void generateRequest(SubChannelParam param, int gender) {
        HotChannelProto.GetRecommendSublistReq.Builder builder = generateBuilder(param);
        if (param.getChannelId() != 0) {
            builder.setChannelId((int) param.getChannelId());
        }
        if (!TextUtils.isEmpty(param.getKey())) {
            builder.setKey(param.getKey());
        }
        if (param.getKeyId() != 0) {
            builder.setKeyId(param.getKeyId());
        }
        builder.setSource(param.getSource());
        builder.setGender(gender);
        mRequest = builder.build();
    }

    protected HotChannelProto.GetRecommendSublistRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return HotChannelProto.GetRecommendSublistRsp.parseFrom(bytes);
    }
}
