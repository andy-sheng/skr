package com.mi.liveassistant.room.request;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.liveassistant.account.UserAccountManager;
import com.mi.liveassistant.common.api.BaseRequest;
import com.mi.liveassistant.data.model.Location;
import com.mi.liveassistant.milink.command.MiLinkCommand;
import com.mi.liveassistant.milink.constant.MiLinkConstant;
import com.mi.liveassistant.proto.LiveCommonProto.LiveCover;
import com.mi.liveassistant.proto.LiveProto.BeginLiveReq;
import com.mi.liveassistant.proto.LiveProto.BeginLiveRsp;

/**
 * Created by lan on 16-3-18.
 *
 * @version lit structure
 * @notice 注意修改命令字和Action
 */
public class BeginLiveRequest extends BaseRequest {
    protected BeginLiveReq.Builder mBuilder;

    public BeginLiveRequest(Location location, int type, String title, String coverUrl) {
        super(MiLinkCommand.COMMAND_LIVE_BEGIN, "BeginLive");
        build(location, type, title, coverUrl);
    }

    private void build(Location location, int type, String title, String coverUrl) {
        mBuilder = BeginLiveReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong());

        if (location != null) {
            mBuilder.setLocation(location.build());
        }
        mBuilder.setType(type);
        if (!TextUtils.isEmpty(title)) {
            mBuilder.setLiveTitle(title);
        }
        if (!TextUtils.isEmpty(coverUrl)) {
            mBuilder.setLiveCover(LiveCover.newBuilder().setCoverUrl(coverUrl));
        }

        extraBuildRequest();

        mRequest = mBuilder.build();
    }

    private void extraBuildRequest() {
        // 支持保留回放
        mBuilder.setAddHistory(true);
        // 支持保留魔法表情
        mBuilder.setSupportMagicFace(true);
        // 默认本身应用
        mBuilder.setAppType(MiLinkConstant.MY_APP_TYPE);
        // 默认无标签
        // mBuilder.addTagInfos(roomTag.build());
        // 默认不接上次直播
        // mBuilder.setLiveId(liveId);
    }

    protected BeginLiveRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return BeginLiveRsp.parseFrom(bytes);
    }
}
