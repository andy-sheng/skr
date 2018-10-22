package com.common.core.userinfo;

import android.text.TextUtils;

import com.common.preference.PreferenceUtils;
import com.common.log.MyLog;
import com.common.milink.MiLinkClientAdapter;
import com.common.milink.command.MiLinkCommand;
import com.common.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.Relation.BlockRequest;
import com.wali.live.proto.Relation.BlockResponse;
import com.wali.live.proto.Relation.BlockerListRequest;
import com.wali.live.proto.Relation.BlockerListResponse;
import com.wali.live.proto.Relation.FollowRequest;
import com.wali.live.proto.Relation.FollowResponse;
import com.wali.live.proto.Relation.FollowerListRequest;
import com.wali.live.proto.Relation.FollowerListResponse;
import com.wali.live.proto.Relation.FollowingListRequest;
import com.wali.live.proto.Relation.FollowingListResponse;
import com.wali.live.proto.Relation.GetMultiOnlineUserRequest;
import com.wali.live.proto.Relation.GetMultiOnlineUserResponse;
import com.wali.live.proto.Relation.GetSubscribeInfoRequest;
import com.wali.live.proto.Relation.GetSubscribeInfoResponse;
import com.wali.live.proto.Relation.MicUserListRequest;
import com.wali.live.proto.Relation.MicUserListResponse;
import com.wali.live.proto.Relation.PkUserListRequest;
import com.wali.live.proto.Relation.PkUserListResponse;
import com.wali.live.proto.Relation.RoomKickViewerReq;
import com.wali.live.proto.Relation.RoomKickViewerRsp;
import com.wali.live.proto.Relation.SetPushRequest;
import com.wali.live.proto.Relation.SetPushResponse;
import com.wali.live.proto.Relation.SubscribeRequest;
import com.wali.live.proto.Relation.SubscribeResponse;
import com.wali.live.proto.Relation.UnBlockRequest;
import com.wali.live.proto.Relation.UnBlockResponse;
import com.wali.live.proto.Relation.UnFollowRequest;
import com.wali.live.proto.Relation.UnFollowResponse;
import com.wali.live.proto.User.GetHomepageReq;
import com.wali.live.proto.User.GetHomepageResp;
import com.wali.live.proto.User.GetUserInfoByIdReq;
import com.wali.live.proto.User.GetUserInfoByIdRsp;
import com.wali.live.proto.User.HisRoomReq;
import com.wali.live.proto.User.HisRoomRsp;
import com.wali.live.proto.User.MutiGetUserInfoReq;
import com.wali.live.proto.User.MutiGetUserInfoRsp;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务器请求关系,提供给RelationManager使用
 */
public class UserInfoServerApi {

    public static final String PRE_KEY_SIX_LOAD_BY_WATER = "pre_key_six_load_by_water";//通过水位拉取用户信息

    /**
     * 获取一个用户的信息
     *
     * @param uuid
     * @return
     */
    public static GetUserInfoByIdRsp getUserInfoByUuid(long uuid) {
        if (uuid <= 0) {
            MyLog.w("getUserInfoByUuid Illegal parameter");
            return null;
        }
        GetUserInfoByIdReq req = new GetUserInfoByIdReq.Builder().setZuid(uuid).build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_GET_USER_INFO_BY_ID);
        data.setData(req.toByteArray());
        PacketData result = MiLinkClientAdapter.getInstance().sendSync(data, MiLinkConstant.TIME_OUT);

        if (result != null && result.getData() != null) {
            try {
                GetUserInfoByIdRsp response = GetUserInfoByIdRsp.parseFrom(result.getData());
                return response;
            } catch (Exception e) {
                MyLog.e(e);
            }
        }

        return null;
    }

    /**
     * 获取一个用户个人主页信息
     *
     * @param needPullLiveInfo 是否需要拉取直播信息
     * @uuid 用户id
     */
    public static GetHomepageResp getHomepageByUuid(long uuid, boolean needPullLiveInfo) {
        if (uuid <= 0) {
            MyLog.w("getUserInfoByUuid Illegal parameter");
            return null;
        }
        GetHomepageReq req = new GetHomepageReq.Builder().setZuid(uuid).setGetLiveInfo(needPullLiveInfo).build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_GET_HOMEPAGE);
        data.setData(req.toByteArray());
        PacketData result = MiLinkClientAdapter.getInstance().sendSync(data, MiLinkConstant.TIME_OUT);

        if (result != null && result.getData() != null) {
            try {
                GetHomepageResp response = GetHomepageResp.parseFrom(result.getData());
                return response;
            } catch (Exception e) {
                MyLog.e(e);
            }
        }

        return null;
    }

    /**
     * 获取用户列表的信息
     *
     * @param uuidList
     * @return
     */
    public static MutiGetUserInfoRsp getHomepageListById(List<Long> uuidList) {
        if (uuidList == null || uuidList.size() <= 0) {
            MyLog.w("getUserListById Illegal parameter");
            return null;
        }
        MutiGetUserInfoReq req = new MutiGetUserInfoReq.Builder()
                .addAllZuid(uuidList)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_GET_USER_LIST_BY_ID);
        data.setData(req.toByteArray());
        PacketData result = MiLinkClientAdapter.getInstance().sendSync(data, MiLinkConstant.TIME_OUT);

        if (result != null && result.getData() != null) {
            try {
                MutiGetUserInfoRsp rsp = MutiGetUserInfoRsp.parseFrom(result.getData());
            } catch (Exception e) {
                MyLog.e(e);
            }
        }

        return null;
    }

    /**
     * 根据uuid获得用户正在直播的房间信息
     *
     * @param uuid
     * @return 返回正在直播的liveShow, 如果房间不存在或者出错,　返回一个空值, 注意不是null.
     */
    public static HisRoomRsp getLiveShowByUserId(long uuid) {
        if (uuid <= 0) {
            return null;
        }
        HisRoomReq request = new HisRoomReq.Builder().setZuid(uuid).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_LIVE_ROOM);
        packetData.setData(request.toByteArray());
        PacketData result = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        if (result != null && result.getData() != null) {
            try {
                HisRoomRsp response = HisRoomRsp.parseFrom(result.getData());
                return response;
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
        return null;
    }

    /**
     * 获取在线的用户列表
     * "zhibo.relation.getmultionlineuser"
     *
     * @param uuids
     * @return
     */
    public static GetMultiOnlineUserResponse getMultiOnlineUserResponse(List<Long> uuids) {
        if (uuids == null || uuids.size() <= 0) {
            MyLog.w("getMultiOnlineUserResponse Illegal parameter");
            return null;
        }
        GetMultiOnlineUserRequest getMultiOnlineUserRequest = new GetMultiOnlineUserRequest.Builder()
                .addAllUids(uuids).build();

        PacketData packetData = new PacketData();
        packetData.setCommand("zhibo.relation.getmultionlineuser");
        packetData.setData(getMultiOnlineUserRequest.toByteArray());

        PacketData result = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        if (result != null && result.getData() != null) {
            try {
                GetMultiOnlineUserResponse response = GetMultiOnlineUserResponse.parseFrom(result.getData());
                return response;
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
        return null;
    }

    /**
     * 查询某个主播的订阅情况
     *
     * @param uid
     * @param targetId
     * @return
     */
    public static GetSubscribeInfoResponse getSubscribeInfo(final long uid,
                                                            final long targetId) {
        if (uid <= 0 || targetId <= 0) {
            MyLog.w("getSubscribeInfo Illegal parameter");
            return null;
        }
        GetSubscribeInfoRequest request = new GetSubscribeInfoRequest.Builder()
                .setUserId(uid)
                .setTargetId(targetId)
                .build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_SUBSCRIBE_INFO_REQUEST);
        packetData.setData(request.toByteArray());
        PacketData result = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        if (result != null && result.getData() != null) {
            try {
                GetSubscribeInfoResponse response = GetSubscribeInfoResponse.parseFrom(result.getData());
                return response;
            } catch (Exception e) {
                MyLog.e(e);
            }
        }

        return null;
    }

    /**
     * 订阅请求
     *
     * @param uid
     * @param targetId
     * @return
     */
    public static SubscribeResponse subscribe(final long uid, final long targetId) {
        if (uid <= 0 || targetId <= 0) {
            MyLog.w("getSubscribeInfo Illegal parameter");
            return null;
        }

        SubscribeRequest request = new SubscribeRequest.Builder()
                .setUserId(uid)
                .setTargetId(targetId)
                .build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_SUBSCRIBE_REQUEST);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        try {
            if (responseData != null) {
                SubscribeResponse response = SubscribeResponse.parseFrom(responseData.getData());
                return response;
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        return null;
    }

    /**
     * 踢人
     *
     * @param roomId     房间ID
     * @param anchorId   主播ID
     * @param operatorId 操作人
     * @param kickedId   被踢人ID
     * @return
     */
    public static RoomKickViewerRsp kickViewer(String roomId, long anchorId, long operatorId,
                                               long kickedId) {
        if (TextUtils.isEmpty(roomId) || anchorId == 0 || operatorId == 0 || kickedId == 0) {
            MyLog.w("kickViewer Illegal parameter");
            return null;
        }

        List<Long> kickedIds = new ArrayList<>();
        kickedIds.add(kickedId);
        RoomKickViewerReq request = new RoomKickViewerReq.Builder()
                .setLiveId(roomId)
                .setZuid(anchorId)
                .setOperatorId(operatorId)
                .addAllKickedId(kickedIds)
                .build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_KICK_VIEWER);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        try {
            if (responseData != null) {
                RoomKickViewerRsp response = RoomKickViewerRsp.parseFrom(responseData.getData());
                return response;
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        return null;
    }

    /**
     * 查询可以连麦的用户列表
     *
     * @param uuid
     * @param liveId
     * @return
     */
    public static MicUserListResponse getMICUserListResponse(long uuid, String liveId) {
        if (TextUtils.isEmpty(liveId) || uuid <= 0) {
            MyLog.w("getMICUserListResponse Illegal parameter");
            return null;
        }

        MicUserListRequest request = new MicUserListRequest.Builder()
                .setUserId(uuid)
                .setRoomId(liveId)
                .build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_MICUSER_LIST);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        try {
            if (responseData != null) {
                return MicUserListResponse.parseFrom(responseData.getData());
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        return null;
    }

    /**
     * 查询可以PK的用户列表
     */
    public static PkUserListResponse getPkUserListResponse(long uuid) {
        if (uuid <= 0) {
            MyLog.w("getPkUserListResponse Illegal parameter");
            return null;
        }

        PkUserListRequest request = new PkUserListRequest.Builder()
                .setUserId(uuid)
                .build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_PKUSER_LIST);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        try {
            if (responseData != null) {
                return PkUserListResponse.parseFrom(responseData.getData());
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        return null;
    }

    /**
     * 查询黑名单列表
     *
     * @param uuid   用户id
     * @param count  拉取数量
     * @param offset 偏移量
     * @return
     */
    public static BlockerListResponse getBlockerListResponse(long uuid, int count,
                                                             int offset) {
        if (uuid <= 0 || count <= 0) {
            MyLog.w("getBlockerListResponse Illegal parameter");
            return null;
        }

        BlockerListRequest request = new BlockerListRequest.Builder()
                .setUserId(uuid)
                .setLimit(count)
                .setOffset(offset)
                .build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_BLOCK_LIST);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        try {
            if (responseData != null) {
                return BlockerListResponse.parseFrom(responseData.getData());
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        return null;
    }

    /**
     * 查询粉丝列表
     *
     * @param uuid   用户id
     * @param count  拉取数量
     * @param offset 偏移量
     * @return
     */
    public static FollowerListResponse getFollowerListResponse(long uuid, int count,
                                                               int offset) {
        if (uuid <= 0 || count <= 0) {
            MyLog.w("getFollowerListResponse Illegal parameter");
            return null;
        }

        FollowerListRequest request = new FollowerListRequest.Builder()
                .setUserId(uuid)
                .setLimit(count)
                .setOffset(offset)
                .build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_FOLLOWER_LIST);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        try {
            if (responseData != null) {
                return FollowerListResponse.parseFrom(responseData.getData());
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        return null;
    }

    /**
     * 查询关注列表
     *
     * @param uuid
     * @param count
     * @param offset
     * @param bothway
     * @param loadByWater
     * @return
     */
    public static FollowingListResponse getFollowingListResponse(long uuid, int count,
                                                                 int offset, boolean bothway, boolean loadByWater) {
        long syncTime = 0;
        // todo 等引入Preference再补全
        if (loadByWater) {
            syncTime = PreferenceUtils.getSettingLong(PRE_KEY_SIX_LOAD_BY_WATER, 0);
        }
        FollowingListRequest request = new FollowingListRequest.Builder()
                .setUserId(uuid)
                .setLimit(count)
                .setIsBothway(bothway)
                .setOffset(offset)
                .setSyncTime(syncTime)
                .build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_FOLLOWING_LIST);
        packetData.setData(request.toByteArray());

        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        try {
            if (responseData != null) {
                FollowingListResponse response = FollowingListResponse.parseFrom(responseData.getData());
                if (response != null) {
                    PreferenceUtils.setSettingLong(PRE_KEY_SIX_LOAD_BY_WATER, response.getSyncTime());
                }
                return response;
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        return null;
    }

    /**
     * 关注推送设置
     *
     * @param uuid
     * @param target
     * @param pushable
     * @return
     */
    public static SetPushResponse setPushRequest(long uuid, long target, boolean pushable) {
        if (uuid <= 0 || target <= 0) {
            MyLog.w("setPushRequest Illegal parameter");
            return null;
        }

        SetPushRequest request = new SetPushRequest.Builder()
                .setUserId(uuid)
                .setTargetId(target)
                .setPushable(pushable)
                .build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_SET_PUSH);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        try {

            if (responseData != null) {
                SetPushResponse response = SetPushResponse.parseFrom(responseData.getData());
                return response;
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        return null;
    }

    /**
     * 拉黑
     *
     * @param uuid
     * @param target
     * @return
     */
    protected static BlockResponse block(long uuid, long target) {
        if (uuid <= 0 || target <= 0) {
            MyLog.w("block Illegal parameter");
            return null;
        }

        BlockRequest request = new BlockRequest.Builder()
                .setUserId(uuid)
                .setTargetId(target)
                .build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_BLOCK_REQUEST);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        try {
            if (responseData != null) {
                BlockResponse response = BlockResponse.parseFrom(responseData.getData());
                return response;
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        return null;
    }

    /**
     * 取消拉黑
     *
     * @param uuid
     * @param target
     * @return
     */
    protected static UnBlockResponse unBlock(long uuid, long target) {
        if (uuid <= 0 || target <= 0) {
            MyLog.w("unBlock Illegal parameter");
            return null;
        }

        UnBlockRequest request = new UnBlockRequest.Builder()
                .setUserId(uuid)
                .setTargetId(target)
                .build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_UNBLOCK_REQUEST);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        try {
            if (responseData != null) {
                UnBlockResponse response = UnBlockResponse.parseFrom(responseData.getData());
                return response;
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        return null;
    }

    /**
     * 关注
     *
     * @param uuid
     * @param target
     * @param roomId 仅在房间关注主播时设置
     * @return
     */
    protected static FollowResponse follow(final long uuid, final long target,
                                           final String roomId) {
        if (uuid <= 0 || target <= 0) {
            MyLog.w("follow Illegal parameter");
            return null;
        }

        FollowRequest.Builder builder = new FollowRequest.Builder()
                .setUserId(uuid)
                .setTargetId(target);
        if (!TextUtils.isEmpty(roomId)) {
            builder.setRoomId(roomId);
        }
        FollowRequest request = builder.build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_FOLLOW_REQUEST);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        try {
            if (responseData != null) {
                FollowResponse response = FollowResponse.parseFrom(responseData.getData());
                return response;
            }
        } catch (Exception e) {
            MyLog.e(e);
        }

        return null;
    }

    /**
     * 取消关注
     *
     * @param uuid
     * @param target
     * @return
     */
    protected static UnFollowResponse unFollow(long uuid, long target) {
        if (uuid <= 0 || target <= 0) {
            MyLog.w("unFollow Illegal parameter");
            return null;
        }

        UnFollowRequest request = new UnFollowRequest.Builder()
                .setUserId(uuid)
                .setTargetId(target)
                .build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_UNFOLLOW_REQUEST);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        try {
            if (responseData != null) {
                UnFollowResponse response = UnFollowResponse.parseFrom(responseData.getData());
                return response;
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        return null;
    }


}
