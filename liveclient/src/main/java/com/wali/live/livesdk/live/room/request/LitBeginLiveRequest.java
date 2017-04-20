package com.wali.live.livesdk.live.room.request;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.location.Location;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.wali.live.proto.Live2Proto.LiveCover;
import com.wali.live.proto.LiveProto.BeginLiveReq;
import com.wali.live.proto.LiveProto.BeginLiveRsp;

/**
 * Created by lan on 16-3-18.
 *
 * @version lit structure
 * @description 注意修改命令字和Action
 */
public class LitBeginLiveRequest extends BaseRequest {
    protected BeginLiveReq.Builder mBuilder;

    {
        mBuilder = BeginLiveReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong());
    }

    public LitBeginLiveRequest(Location location, int type, String title, String coverUrl) {
        super(MiLinkCommand.COMMAND_LIVE_BEGIN, "BeginLive", null);
        build(location, type, title, coverUrl);
    }

    private void build(Location location, int type, String title, String coverUrl) {
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
