package com.common.core.userinfo;

import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 *
 */
public interface UserInfoServerApi {

    /**
     * 拿到某个人基本的信息
     * @param userID
     * @return
     */
    @GET("v1/uprofile/information")
    Observable<ApiResult> getUserInfo(@Query("userID") int userID);


    /**
     * 处理关系
     *
     * @param body "toUserID" : 关系被动接受者id
     *             "action" :  创建关系 or 解除关系
     *             0  未知 RA_UNKNOWN
     *             1  创建关系 RA_BUILD
     *             2  解除关系 RA_UNBUILD
     * @return
     */
    @PUT("/v1/mate/relation")
    Observable<ApiResult> mateRelation(@Body RequestBody body);

    /**
     * 获取指定关系列表
     * @param relation  [必选]关系类型   0 未知  RC_UNKNOWN
     *                                 1 关注  RC_Follow
     *                                 2 粉丝  RC_Fans
     *                                 3 好友  RC_Friend
     * @param offset    [必选]偏移
     * @param limit     [必选]限制数量,最大50
     * @return
     */
    @GET("/v1/mate/relation")
    Observable<ApiResult> getRelationList(@Query("relation") int relation,
                                          @Query("offset") int offset,
                                          @Query("limit") int limit);

    /**
     * 获取指定用户的关系数量
     * @param userID
     * @return
     */
    @GET("/v1/mate/cnt")
    Observable<ApiResult> getRelationNum(@Query("userID") int userID);


//    /**
//     * 获取一个用户的信息
//     *
//     * @param uuid
//     * @return
//     */
//    public static GetUserInfoByIdRsp getUserInfoByUuid(long uuid) {
//        if (uuid <= 0) {
//            MyLog.w("getUserInfoByUuid Illegal parameter");
//            return null;
//        }
//        GetUserInfoByIdReq req = new GetUserInfoByIdReq.Builder().setZuid(uuid).build();
//        PacketData data = new PacketData();
//        data.setCommand(MiLinkCommand.COMMAND_GET_USER_INFO_BY_ID);
//        data.setData(req.toByteArray());
//        PacketData result = MiLinkClientAdapter.getInstance().sendSync(data, MiLinkConstant.TIME_OUT);
//
//        if (result != null && result.getData() != null) {
//            try {
//                GetUserInfoByIdRsp response = GetUserInfoByIdRsp.parseFrom(result.getData());
//                return response;
//            } catch (Exception e) {
//                MyLog.e(e);
//            }
//        }
//
//        return null;
//    }
//
//    /**
//     * 获取一个用户个人主页信息
//     *
//     * @param needPullLiveInfo 是否需要拉取直播信息
//     * @uuid 用户id
//     */
//    public static GetHomepageResp getHomepageByUuid(long uuid, boolean needPullLiveInfo) {
//        if (uuid <= 0) {
//            MyLog.w("getUserInfoByUuid Illegal parameter");
//            return null;
//        }
//        GetHomepageReq req = new GetHomepageReq.Builder().setZuid(uuid).setGetLiveInfo(needPullLiveInfo).build();
//        PacketData data = new PacketData();
//        data.setCommand(MiLinkCommand.COMMAND_GET_HOMEPAGE);
//        data.setData(req.toByteArray());
//        PacketData result = MiLinkClientAdapter.getInstance().sendSync(data, MiLinkConstant.TIME_OUT);
//
//        if (result != null && result.getData() != null) {
//            try {
//                GetHomepageResp response = GetHomepageResp.parseFrom(result.getData());
//                return response;
//            } catch (Exception e) {
//                MyLog.e(e);
//            }
//        }
//
//        return null;
//    }
//
//    /**
//     * 获取用户列表的信息
//     *
//     * @param uuidList
//     * @return
//     */
//    public static MutiGetUserInfoRsp getHomepageListById(List<Long> uuidList) {
//        if (uuidList == null || uuidList.size() <= 0) {
//            MyLog.w("getUserListById Illegal parameter");
//            return null;
//        }
//        MutiGetUserInfoReq req = new MutiGetUserInfoReq.Builder()
//                .addAllZuid(uuidList)
//                .build();
//        PacketData data = new PacketData();
//        data.setCommand(MiLinkCommand.COMMAND_GET_USER_LIST_BY_ID);
//        data.setData(req.toByteArray());
//        PacketData result = MiLinkClientAdapter.getInstance().sendSync(data, MiLinkConstant.TIME_OUT);
//
//        if (result != null && result.getData() != null) {
//            try {
//                MutiGetUserInfoRsp rsp = MutiGetUserInfoRsp.parseFrom(result.getData());
//            } catch (Exception e) {
//                MyLog.e(e);
//            }
//        }
//
//        return null;
//    }
//
//    /**
//     * 获取在线的用户列表
//     * "zhibo.relation.getmultionlineuser"
//     *
//     * @param uuids
//     * @return
//     */
//    public static GetMultiOnlineUserResponse getMultiOnlineUserResponse(List<Long> uuids) {
//        if (uuids == null || uuids.size() <= 0) {
//            MyLog.w("getMultiOnlineUserResponse Illegal parameter");
//            return null;
//        }
//        GetMultiOnlineUserRequest getMultiOnlineUserRequest = new GetMultiOnlineUserRequest.Builder()
//                .addAllUids(uuids).build();
//
//        PacketData packetData = new PacketData();
//        packetData.setCommand("zhibo.relation.getmultionlineuser");
//        packetData.setData(getMultiOnlineUserRequest.toByteArray());
//
//        PacketData result = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
//
//        if (result != null && result.getData() != null) {
//            try {
//                GetMultiOnlineUserResponse response = GetMultiOnlineUserResponse.parseFrom(result.getData());
//                return response;
//            } catch (Exception e) {
//                MyLog.e(e);
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 查询某个主播的订阅情况
//     *
//     * @param uid
//     * @param targetId
//     * @return
//     */
//    public static GetSubscribeInfoResponse getSubscribeInfo(final long uid,
//                                                            final long targetId) {
//        if (uid <= 0 || targetId <= 0) {
//            MyLog.w("getSubscribeInfo Illegal parameter");
//            return null;
//        }
//        GetSubscribeInfoRequest request = new GetSubscribeInfoRequest.Builder()
//                .setUserId(uid)
//                .setTargetId(targetId)
//                .build();
//        PacketData packetData = new PacketData();
//        packetData.setCommand(MiLinkCommand.COMMAND_GET_SUBSCRIBE_INFO_REQUEST);
//        packetData.setData(request.toByteArray());
//        PacketData result = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
//
//        if (result != null && result.getData() != null) {
//            try {
//                GetSubscribeInfoResponse response = GetSubscribeInfoResponse.parseFrom(result.getData());
//                return response;
//            } catch (Exception e) {
//                MyLog.e(e);
//            }
//        }
//
//        return null;
//    }
//
//    /**
//     * 订阅请求
//     *
//     * @param uid
//     * @param targetId
//     * @return
//     */
//    public static SubscribeResponse subscribe(final long uid, final long targetId) {
//        if (uid <= 0 || targetId <= 0) {
//            MyLog.w("getSubscribeInfo Illegal parameter");
//            return null;
//        }
//
//        SubscribeRequest request = new SubscribeRequest.Builder()
//                .setUserId(uid)
//                .setTargetId(targetId)
//                .build();
//        PacketData packetData = new PacketData();
//        packetData.setCommand(MiLinkCommand.COMMAND_SUBSCRIBE_REQUEST);
//        packetData.setData(request.toByteArray());
//        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
//
//        try {
//            if (responseData != null) {
//                SubscribeResponse response = SubscribeResponse.parseFrom(responseData.getData());
//                return response;
//            }
//        } catch (Exception e) {
//            MyLog.e(e);
//        }
//        return null;
//    }
//
//    /**
//     * 踢人
//     *
//     * @param roomId     房间ID
//     * @param anchorId   主播ID
//     * @param operatorId 操作人
//     * @param kickedId   被踢人ID
//     * @return
//     */
//    public static RoomKickViewerRsp kickViewer(String roomId, long anchorId, long operatorId,
//                                               long kickedId) {
//        if (TextUtils.isEmpty(roomId) || anchorId == 0 || operatorId == 0 || kickedId == 0) {
//            MyLog.w("kickViewer Illegal parameter");
//            return null;
//        }
//
//        List<Long> kickedIds = new ArrayList<>();
//        kickedIds.add(kickedId);
//        RoomKickViewerReq request = new RoomKickViewerReq.Builder()
//                .setLiveId(roomId)
//                .setZuid(anchorId)
//                .setOperatorId(operatorId)
//                .addAllKickedId(kickedIds)
//                .build();
//        PacketData packetData = new PacketData();
//        packetData.setCommand(MiLinkCommand.COMMAND_KICK_VIEWER);
//        packetData.setData(request.toByteArray());
//        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
//
//        try {
//            if (responseData != null) {
//                RoomKickViewerRsp response = RoomKickViewerRsp.parseFrom(responseData.getData());
//                return response;
//            }
//        } catch (Exception e) {
//            MyLog.e(e);
//        }
//        return null;
//    }
//
//    /**
//     * 查询可以连麦的用户列表
//     *
//     * @param uuid
//     * @param liveId
//     * @return
//     */
//    public static MicUserListResponse getMICUserListResponse(long uuid, String liveId) {
//        if (TextUtils.isEmpty(liveId) || uuid <= 0) {
//            MyLog.w("getMICUserListResponse Illegal parameter");
//            return null;
//        }
//
//        MicUserListRequest request = new MicUserListRequest.Builder()
//                .setUserId(uuid)
//                .setRoomId(liveId)
//                .build();
//        PacketData packetData = new PacketData();
//        packetData.setCommand(MiLinkCommand.COMMAND_GET_MICUSER_LIST);
//        packetData.setData(request.toByteArray());
//        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
//
//        try {
//            if (responseData != null) {
//                return MicUserListResponse.parseFrom(responseData.getData());
//            }
//        } catch (Exception e) {
//            MyLog.e(e);
//        }
//        return null;
//    }
//
//    /**
//     * 查询可以PK的用户列表
//     */
//    public static PkUserListResponse getPkUserListResponse(long uuid) {
//        if (uuid <= 0) {
//            MyLog.w("getPkUserListResponse Illegal parameter");
//            return null;
//        }
//
//        PkUserListRequest request = new PkUserListRequest.Builder()
//                .setUserId(uuid)
//                .build();
//        PacketData packetData = new PacketData();
//        packetData.setCommand(MiLinkCommand.COMMAND_GET_PKUSER_LIST);
//        packetData.setData(request.toByteArray());
//        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
//
//        try {
//            if (responseData != null) {
//                return PkUserListResponse.parseFrom(responseData.getData());
//            }
//        } catch (Exception e) {
//            MyLog.e(e);
//        }
//        return null;
//    }
//
//    /**
//     * 查询黑名单列表
//     *
//     * @param uuid   用户id
//     * @param count  拉取数量
//     * @param offset 偏移量
//     * @return
//     */
//    public static BlockerListResponse getBlockerListResponse(long uuid, int count,
//                                                             int offset) {
//        if (uuid <= 0 || count <= 0) {
//            MyLog.w("getBlockerListResponse Illegal parameter");
//            return null;
//        }
//
//        BlockerListRequest request = new BlockerListRequest.Builder()
//                .setUserId(uuid)
//                .setLimit(count)
//                .setOffset(offset)
//                .build();
//        PacketData packetData = new PacketData();
//        packetData.setCommand(MiLinkCommand.COMMAND_GET_BLOCK_LIST);
//        packetData.setData(request.toByteArray());
//        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
//
//        try {
//            if (responseData != null) {
//                return BlockerListResponse.parseFrom(responseData.getData());
//            }
//        } catch (Exception e) {
//            MyLog.e(e);
//        }
//        return null;
//    }
//
//    /**
//     * 查询粉丝列表
//     *
//     * @param uuid   用户id
//     * @param count  拉取数量
//     * @param offset 偏移量
//     * @return
//     */
//    public static FollowerListResponse getFollowerListResponse(long uuid, int count,
//                                                               int offset) {
//        if (uuid <= 0 || count <= 0) {
//            MyLog.w("getFollowerListResponse Illegal parameter");
//            return null;
//        }
//
//        FollowerListRequest request = new FollowerListRequest.Builder()
//                .setUserId(uuid)
//                .setLimit(count)
//                .setOffset(offset)
//                .build();
//        PacketData packetData = new PacketData();
//        packetData.setCommand(MiLinkCommand.COMMAND_GET_FOLLOWER_LIST);
//        packetData.setData(request.toByteArray());
//        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
//
//        try {
//            if (responseData != null) {
//                return FollowerListResponse.parseFrom(responseData.getData());
//            }
//        } catch (Exception e) {
//            MyLog.e(e);
//        }
//        return null;
//    }
//
//    /**
//     * 查询关注列表
//     *
//     * @param uuid
//     * @param count
//     * @param offset
//     * @param bothway
//     * @param loadByWater
//     * @return
//     */
//    public static FollowingListResponse getFollowingListResponse(long uuid, int count,
//                                                                 int offset, boolean bothway, boolean loadByWater) {
//        long syncTime = 0;
//        // todo 等引入Preference再补全
//        if (loadByWater) {
//            syncTime = U.getPreferenceUtils().getSettingLong(PRE_KEY_SIX_LOAD_BY_WATER, 0);
//        }
//        FollowingListRequest request = new FollowingListRequest.Builder()
//                .setUserId(uuid)
//                .setLimit(count)
//                .setIsBothway(bothway)
//                .setOffset(offset)
//                .setSyncTime(syncTime)
//                .build();
//        PacketData packetData = new PacketData();
//        packetData.setCommand(MiLinkCommand.COMMAND_GET_FOLLOWING_LIST);
//        packetData.setData(request.toByteArray());
//
//        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
//        try {
//            if (responseData != null) {
//                FollowingListResponse response = FollowingListResponse.parseFrom(responseData.getData());
//                if (response != null) {
//                    U.getPreferenceUtils().setSettingLong(PRE_KEY_SIX_LOAD_BY_WATER, response.getSyncTime());
//                }
//                return response;
//            }
//        } catch (Exception e) {
//            MyLog.e(e);
//        }
//        return null;
//    }
//
//    /**
//     * 关注推送设置
//     *
//     * @param uuid
//     * @param target
//     * @param pushable
//     * @return
//     */
//    public static SetPushResponse setPushRequest(long uuid, long target, boolean pushable) {
//        if (uuid <= 0 || target <= 0) {
//            MyLog.w("setPushRequest Illegal parameter");
//            return null;
//        }
//
//        SetPushRequest request = new SetPushRequest.Builder()
//                .setUserId(uuid)
//                .setTargetId(target)
//                .setPushable(pushable)
//                .build();
//        PacketData packetData = new PacketData();
//        packetData.setCommand(MiLinkCommand.COMMAND_SET_PUSH);
//        packetData.setData(request.toByteArray());
//        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
//
//        try {
//
//            if (responseData != null) {
//                SetPushResponse response = SetPushResponse.parseFrom(responseData.getData());
//                return response;
//            }
//        } catch (Exception e) {
//            MyLog.e(e);
//        }
//        return null;
//    }
//
//    /**
//     * 拉黑
//     *
//     * @param uuid
//     * @param target
//     * @return
//     */
//    protected static BlockResponse block(long uuid, long target) {
//        if (uuid <= 0 || target <= 0) {
//            MyLog.w("block Illegal parameter");
//            return null;
//        }
//
//        BlockRequest request = new BlockRequest.Builder()
//                .setUserId(uuid)
//                .setTargetId(target)
//                .build();
//        PacketData packetData = new PacketData();
//        packetData.setCommand(MiLinkCommand.COMMAND_BLOCK_REQUEST);
//        packetData.setData(request.toByteArray());
//        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
//
//        try {
//            if (responseData != null) {
//                BlockResponse response = BlockResponse.parseFrom(responseData.getData());
//                return response;
//            }
//        } catch (Exception e) {
//            MyLog.e(e);
//        }
//        return null;
//    }
//
//    /**
//     * 取消拉黑
//     *
//     * @param uuid
//     * @param target
//     * @return
//     */
//    protected static UnBlockResponse unBlock(long uuid, long target) {
//        if (uuid <= 0 || target <= 0) {
//            MyLog.w("unBlock Illegal parameter");
//            return null;
//        }
//
//        UnBlockRequest request = new UnBlockRequest.Builder()
//                .setUserId(uuid)
//                .setTargetId(target)
//                .build();
//        PacketData packetData = new PacketData();
//        packetData.setCommand(MiLinkCommand.COMMAND_UNBLOCK_REQUEST);
//        packetData.setData(request.toByteArray());
//        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
//
//        try {
//            if (responseData != null) {
//                UnBlockResponse response = UnBlockResponse.parseFrom(responseData.getData());
//                return response;
//            }
//        } catch (Exception e) {
//            MyLog.e(e);
//        }
//        return null;
//    }
//
//    /**
//     * 关注
//     *
//     * @param uuid
//     * @param target
//     * @param roomId 仅在房间关注主播时设置
//     * @return
//     */
//    protected static FollowResponse follow(final long uuid, final long target,
//                                           final String roomId) {
//        if (uuid <= 0 || target <= 0) {
//            MyLog.w("follow Illegal parameter");
//            return null;
//        }
//
//        FollowRequest.Builder builder = new FollowRequest.Builder()
//                .setUserId(uuid)
//                .setTargetId(target);
//        if (!TextUtils.isEmpty(roomId)) {
//            builder.setRoomId(roomId);
//        }
//        FollowRequest request = builder.build();
//        PacketData packetData = new PacketData();
//        packetData.setCommand(MiLinkCommand.COMMAND_FOLLOW_REQUEST);
//        packetData.setData(request.toByteArray());
//        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
//
//        try {
//            if (responseData != null) {
//                FollowResponse response = FollowResponse.parseFrom(responseData.getData());
//                return response;
//            }
//        } catch (Exception e) {
//            MyLog.e(e);
//        }
//
//        return null;
//    }
//
//    /**
//     * 取消关注
//     *
//     * @param uuid
//     * @param target
//     * @return
//     */
//    protected static UnFollowResponse unFollow(long uuid, long target) {
//        if (uuid <= 0 || target <= 0) {
//            MyLog.w("unFollow Illegal parameter");
//            return null;
//        }
//
//        UnFollowRequest request = new UnFollowRequest.Builder()
//                .setUserId(uuid)
//                .setTargetId(target)
//                .build();
//        PacketData packetData = new PacketData();
//        packetData.setCommand(MiLinkCommand.COMMAND_UNFOLLOW_REQUEST);
//        packetData.setData(request.toByteArray());
//        PacketData responseData = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
//
//        try {
//            if (responseData != null) {
//                UnFollowResponse response = UnFollowResponse.parseFrom(responseData.getData());
//                return response;
//            }
//        } catch (Exception e) {
//            MyLog.e(e);
//        }
//        return null;
//    }


}
