package com.wali.live.livesdk.live.api;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.live.BaseLiveRequest;
import com.mi.live.data.location.Location;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.wali.live.livesdk.live.viewmodel.RoomTag;
import com.wali.live.proto.AccountProto;
import com.wali.live.proto.Live2Proto.LiveCover;
import com.wali.live.proto.LiveProto.BeginLiveReq;
import com.wali.live.proto.LiveProto.BeginLiveRsp;

import java.util.List;

/**
 * Created by lan on 16-3-18.
 * 注意修改命令字和Action
 */
public class BeginLiveRequest extends BaseLiveRequest {
    BeginLiveReq.Builder mBuilder;

    {
        mCommand = MiLinkCommand.COMMAND_LIVE_BEGIN;
        mAction = "BeginLive";
        mBuilder = BeginLiveReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong());
    }

    //普通直播
    //游戏直播
    public BeginLiveRequest(Location location, int liveType, List<Long> inviteeList, boolean addHistory,
                            String liveTitle, String CoverUrl, String liveId, AccountProto.AppInfo appInfo,
                            Integer playUi, int appType, RoomTag roomTag, boolean supportMagicFaceFlag) {
        build(location, liveType, inviteeList, addHistory, liveTitle, CoverUrl, liveId, appInfo, playUi, appType, roomTag, supportMagicFaceFlag);
    }

    private void build(Location location, int liveType, List<Long> inviteeList, boolean addHistory,
                       String liveTitle, String CoverUrl, String liveId, AccountProto.AppInfo appInfo,
                       Integer playUi, int appType, RoomTag roomTag, boolean supportMagicFaceFlag) {
        if (roomTag != null) {
            mBuilder.addTagInfos(roomTag.build());
        }
        if (location != null) {
            mBuilder.setLocation(location.build());
        }
        if (!TextUtils.isEmpty(CoverUrl)) {
            mBuilder.setLiveCover(LiveCover.newBuilder().setCoverUrl(CoverUrl));
        }
        if (!TextUtils.isEmpty(liveId)) {
            mBuilder.setLiveId(liveId);
        }
        mBuilder.setType(liveType);
        if (inviteeList != null) {
            mBuilder.addAllInvitee(inviteeList);
        }
        mBuilder.setAddHistory(addHistory);
        if (!TextUtils.isEmpty(liveTitle)) {
            mBuilder.setLiveTitle(liveTitle);
        }

        if (appInfo != null) {
            mBuilder.setAppInfo(appInfo);
            mBuilder.setPlayUI(playUi);
            mBuilder.setAppType(MiLinkConstant.THIRD_APP_TYPE);
        } else {
            mBuilder.setAppType(appType);
        }

        mBuilder.setSupportMagicFace(supportMagicFaceFlag);
        mRequest = mBuilder.build();
        MyLog.w("BeginLiveRequest = " + mRequest.toString());

    }

    protected BeginLiveRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return BeginLiveRsp.parseFrom(bytes);
    }
}
