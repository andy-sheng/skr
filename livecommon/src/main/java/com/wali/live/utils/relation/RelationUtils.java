package com.wali.live.utils.relation;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.data.UserListData;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.preference.MLPreferenceUtils;
import com.mi.live.data.preference.PreferenceKeys;
import com.mi.live.data.repository.datasource.WatchHistoryInfoDaoAdapter;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.Rank;
import com.wali.live.proto.RelationProto;
import com.wali.live.proto.RelationProto.BlockerListRequest;
import com.wali.live.proto.RelationProto.BlockerListResponse;
import com.wali.live.proto.RelationProto.FollowRequest;
import com.wali.live.proto.RelationProto.FollowResponse;
import com.wali.live.proto.RelationProto.FollowerListRequest;
import com.wali.live.proto.RelationProto.FollowerListResponse;
import com.wali.live.proto.RelationProto.FollowingListRequest;
import com.wali.live.proto.RelationProto.FollowingListResponse;
import com.wali.live.proto.RelationProto.SetPushRequest;
import com.wali.live.proto.RelationProto.SetPushResponse;
import com.wali.live.proto.RelationProto.UnFollowRequest;
import com.wali.live.proto.RelationProto.UnFollowResponse;
import com.wali.live.proto.RoomRecommend;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 2/24/16.
 */
public class RelationUtils {
    private static final String TAG = RelationUtils.class.getSimpleName();

    //以下为关注返回的值 大于等于0说明成功 返回FOLLOW_STATE_BOTH_WAY 说明双向关注
    public static final int FOLLOW_STATE_FAILED = -1;
    public static final int FOLLOW_STATE_SUCCESS = 0;
    public static final int FOLLOW_STATE_BOTH_WAY = 1;

    public static int sErrorCode = 0;

    public static final int LOADING_FOLLOWING_PAGE_COUNT = 300;

    public static int follow(long uuid, long target) {
        return follow(uuid, target, null);
    }

    public static int follow(long uuid, long target, String roomid) {
        RelationProto.FollowRequest.Builder builder = FollowRequest.newBuilder().setUserId(uuid).setTargetId(target);
        if (!TextUtils.isEmpty(roomid)) {
            builder.setRoomId(roomid);
        }
        FollowRequest request = builder.build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_FOLLOW_REQUEST);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        MyLog.v(TAG + " follow:" + responseData);
        try {
            if (responseData != null) {
                FollowResponse response = FollowResponse.parseFrom(responseData.getData());
                MyLog.v(TAG + " followRequest result:" + response.getCode());
                sErrorCode = response.getCode();
                if (response.getCode() == ErrorCode.CODE_SUCCESS) {
                    FollowOrUnfollowEvent event = new FollowOrUnfollowEvent(FollowOrUnfollowEvent.EVENT_TYPE_FOLLOW, target);
                    event.isBothFollow = response.getIsBothway();
                    EventBus.getDefault().post(event);
                    WatchHistoryInfoDaoAdapter.getInstance().updateWatchHistoryInfo(event);
                    return response.getIsBothway() ? FOLLOW_STATE_BOTH_WAY : FOLLOW_STATE_SUCCESS;
                }
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return FOLLOW_STATE_FAILED;
    }

    /**
     * 取消关注
     */
    public static boolean unFollow(long uuid, long target) {
        UnFollowRequest request = UnFollowRequest.newBuilder().setUserId(uuid).setTargetId(target).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_UNFOLLOW_REQUEST);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        MyLog.v(TAG + "unFollow:" + responseData);
        try {
            if (responseData != null) {
                UnFollowResponse response = UnFollowResponse.parseFrom(responseData.getData());
                MyLog.v(TAG + "unFollow result:" + response.getCode());
                if (response.getCode() == ErrorCode.CODE_SUCCESS) {
                    EventBus.getDefault().post(new FollowOrUnfollowEvent(FollowOrUnfollowEvent.EVENT_TYPE_UNFOLLOW, target));
                    WatchHistoryInfoDaoAdapter.getInstance().updateWatchHistoryInfo(new FollowOrUnfollowEvent(FollowOrUnfollowEvent.EVENT_TYPE_UNFOLLOW, target));
                    return true;
                }
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return false;
    }

    /**
     * 关注推送设置
     */
    public static boolean setPushRequest(long uuid, long target, boolean pushable) {
        SetPushRequest request = SetPushRequest.newBuilder().setUserId(uuid).setTargetId(target).setPushable(pushable).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_SET_PUSH);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        try {
            MyLog.v(TAG + " setPushRequest responseData:" + responseData);
            if (responseData != null) {
                SetPushResponse response = SetPushResponse.parseFrom(responseData.getData());
                MyLog.v(TAG + " setPushRequest result:" + response.getCode());
                return response.getCode() == ErrorCode.CODE_SUCCESS;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return false;
    }

    public static int loadFollowingData(long uuid, int pageCount, int offset, List<Object> dataList, boolean bothway, boolean loadByWater) {
        int total = 0;
        RelationProto.FollowingListResponse response = RelationUtils.getBothFollowingListResponse(uuid, pageCount, offset, bothway, loadByWater);
        if (response != null && response.getCode() == ErrorCode.CODE_SUCCESS) {
            MyLog.v(TAG + " FollowingListResponse total:" + response.getTotal());
            total = response.getTotal();
            List<Object> mDataList = UserListData.parseUserList(response, uuid == UserAccountManager.getInstance().getUuidAsLong());
            if (mDataList.size() > 0) {
                dataList.addAll(mDataList);
                if (total > 0 && offset < total) {
                    offset += pageCount;
                    loadFollowingData(uuid, pageCount, offset, dataList, bothway, loadByWater);
                }
            } else {
                return total;
            }
        }
        return total;
    }

    public static int loadFollowingData(long uuid, int pageCount, int offset, List<Object> mAllDataList, boolean bothway) {
        return loadFollowingData(uuid, pageCount, offset, mAllDataList, bothway, false);
    }

    /**
     * 查询关注列表
     */
    public static FollowingListResponse getFollowingListResponse(long uuid, int count, int offset) {
        return getBothFollowingListResponse(uuid, count, offset, false);
    }

    public static FollowingListResponse getBothFollowingListResponse(long uuid, int count, int offset, boolean bothway, boolean loadByWater) {
        long syncTime = 0;
        if (loadByWater) {
            syncTime = MLPreferenceUtils.getSettingLong(GlobalData.app(), PreferenceKeys.PRE_KEY_SIX_LOAD_BY_WATER, 0);
        }
        FollowingListRequest request = FollowingListRequest.newBuilder().setUserId(uuid).setLimit(count).setIsBothway(bothway).setOffset(offset).setSyncTime(syncTime).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_FOLLOWING_LIST);
        packetData.setData(request.toByteArray());
        MyLog.v(TAG, " getFollowingListResponse request : \n" + request.toString());

        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        MyLog.v(TAG, " responseData=" + responseData);
        try {
            if (responseData != null) {
                MyLog.v(TAG, " getMnsCode:" + responseData.getMnsCode());
                FollowingListResponse response = FollowingListResponse.parseFrom(responseData.getData());
                if (response != null) {
                    MLPreferenceUtils.setSettingLong(GlobalData.app(), PreferenceKeys.PRE_KEY_SIX_LOAD_BY_WATER, response.getSyncTime());
                }
                return response;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return null;
    }

    public static FollowingListResponse getBothFollowingListResponse(long uuid, int count, int offset, boolean bothway) {
        return getBothFollowingListResponse(uuid, count, offset, bothway, false);
    }

    /**
     * 查询粉丝列表
     */
    public static FollowerListResponse getFollowerListResponse(long uuid, int count, int offset) {
        FollowerListRequest request = FollowerListRequest.newBuilder().setUserId(uuid).setLimit(count).setOffset(offset).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_FOLLOWER_LIST);
        packetData.setData(request.toByteArray());
        MyLog.v(TAG, "getFollowerListResponse request : \n" + request.toString());

        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        MyLog.v(TAG, " responseData=" + responseData);
        try {
            if (responseData != null) {
                MyLog.v(TAG, " getMnsCode:" + responseData.getMnsCode());
                return FollowerListResponse.parseFrom(responseData.getData());
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return null;
    }

    /**
     * 查询黑名单列表
     */
    public static BlockerListResponse getBlockerListResponse(long uuid, int count, int offset) {
        BlockerListRequest request = BlockerListRequest.newBuilder().setUserId(uuid).setLimit(count).setOffset(offset).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_BLOCK_LIST);
        packetData.setData(request.toByteArray());
        MyLog.v(TAG, "getBlockerListResponse request : \n" + request.toString());

        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        MyLog.v(TAG, " responseData=" + responseData);
        try {
            if (responseData != null) {
                return BlockerListResponse.parseFrom(responseData.getData());
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return null;
    }

    /**
     * 星票榜单
     */
    public static List<Rank.RankUser> getTicketListResponse(long uuid, int pageCount, int offset) {
        List<Rank.RankUser> rankItemList = new ArrayList<>();
        Rank.GetRankListRequestV2 request = Rank.GetRankListRequestV2.newBuilder()
                .setZuid(uuid).setLimit(pageCount).setOffset(offset).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_RANK_LIST_V2);
        packetData.setData(request.toByteArray());
        MyLog.v(TAG, "getRankItemList request : \n" + request.toString());

        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        try {
            if (responseData != null) {
                Rank.GetRankListResponseV2 response = Rank.GetRankListResponseV2.parseFrom(responseData.getData());
                MyLog.v(TAG, "getRankItemList responseData=" + response);
                List<Rank.RankUser> rankList = new ArrayList<Rank.RankUser>();
                if (response.getRetCode() == 0) {
                    rankList = response.getItemsList();
                }
                return rankList;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return rankItemList;

    }

    /**
     * 本场星票榜单
     */
    public static List<Rank.RankUser> getRankRoomList(String liveId, int pageCount, int offset) {
        List<Rank.RankUser> rankItemList = new ArrayList<>();
        Rank.GetRankRoomListRequest request = Rank.GetRankRoomListRequest.newBuilder().setLiveId(liveId).setLimit(pageCount).setOffset(offset).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_RANK_ROOM_LIST);
        packetData.setData(request.toByteArray());
        MyLog.v(TAG, "getRankRoomList request : \n" + request.toString());

        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        try {
            if (responseData != null) {
                Rank.GetRankRoomListResponse response = Rank.GetRankRoomListResponse.parseFrom(responseData.getData());
                MyLog.v(TAG, "getRankItemList responseData=" + response);
                List<Rank.RankUser> rankList = new ArrayList<Rank.RankUser>();
                if (response.getRetCode() == 0) {
                    rankList = response.getItemsList();
                }
                return rankList;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return rankItemList;

    }

    /**
     * 本场近十分鐘星票榜单
     */
    public static List<Rank.RankUser> getRankRoomTenMinList(String liveId, int pageCount, int offset) {
        List<Rank.RankUser> rankItemList = new ArrayList<>();
        Rank.GetRankRoomListRequest request = Rank.GetRankRoomListRequest.newBuilder().setLiveId(liveId).setLimit(pageCount).setOffset(offset).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_RANK_ROOM_TEN_MIN_LIST);
        packetData.setData(request.toByteArray());
        MyLog.v(TAG, "getRankRoomTenMinList request : \n" + request.toString());

        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        try {
            if (responseData != null) {
                Rank.GetRankRoomListResponse response = Rank.GetRankRoomListResponse.parseFrom(responseData.getData());
                MyLog.v(TAG, "getRankRoomTenMinList responseData=" + response);
                List<Rank.RankUser> rankList = new ArrayList<Rank.RankUser>();
                if (response.getRetCode() == 0) {
                    rankList = response.getItemsList();
                }
                return rankList;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return rankItemList;

    }

    public static List<RoomRecommend.RecommendRoom> getRoomRecommendList(String liveId, long zuid, long uuid) {
        List<RoomRecommend.RecommendRoom> roomItemList = new ArrayList<>();
        RoomRecommend.GetRecommendInLiveEndReq request = RoomRecommend.GetRecommendInLiveEndReq.newBuilder().setLiveId(liveId).setZuid(zuid).setUuid(uuid).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_RECOMMEND_ROOM);
        packetData.setData(request.toByteArray());
        MyLog.v(TAG, "getRankRoomTenMinList request : \n" + request.toString());

        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        try {
            if (responseData != null) {
                RoomRecommend.GetRecommendInLiveEndRsp response = RoomRecommend.GetRecommendInLiveEndRsp.parseFrom(responseData.getData());
                MyLog.v(TAG, "getRankRoomTenMinList responseData=" + response);
                List<RoomRecommend.RecommendRoom> roomList = new ArrayList<>();
                if (response.getRetCode() == 0) {
                    roomList = response.getRecommendRoomList();
                }
                return roomList;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return roomItemList;

    }

    /**
     * 查询可以PK的用户列表
     */
    public static RelationProto.PkUserListResponse getPkUserListResponse(long uuid) {
        RelationProto.PkUserListRequest request = RelationProto.PkUserListRequest.newBuilder().setUserId(uuid).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_PKUSER_LIST);
        packetData.setData(request.toByteArray());
        MyLog.v(TAG, "getPkUserListResponse request : \n" + request.toString());

        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        MyLog.v(TAG, " responseData=" + responseData);
        try {
            if (responseData != null) {
                MyLog.v(TAG, " getMnsCode:" + responseData.getMnsCode());
                return RelationProto.PkUserListResponse.parseFrom(responseData.getData());
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return null;
    }

    /**
     * 查询可以连麦的用户列表
     */
    public static RelationProto.MicUserListResponse getMICUserListResponse(long uuid, String liveId) {
        RelationProto.MicUserListRequest request = RelationProto.MicUserListRequest.newBuilder().setUserId(uuid).setRoomId(liveId).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_MICUSER_LIST);
        packetData.setData(request.toByteArray());
        MyLog.v(TAG, "getMICUserListResponse request : \n" + request.toString());

        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        MyLog.v(TAG, " responseData=" + responseData);
        try {
            if (responseData != null) {
                MyLog.v(TAG, " getMnsCode:" + responseData.getMnsCode());
                return RelationProto.MicUserListResponse.parseFrom(responseData.getData());
            }
        } catch (InvalidProtocolBufferException e) {
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
    public static int kickViewer(String roomId, long anchorId, long operatorId, long kickedId) {
        if (TextUtils.isEmpty(roomId) || anchorId == 0 || operatorId == 0 || kickedId == 0) {
            return ErrorCode.CODE_ERROR_NORMAL;
        }
        RelationProto.RoomKickViewerReq.Builder builder = RelationProto.RoomKickViewerReq.newBuilder();
        builder.setLiveId(roomId);
        builder.setZuid(anchorId);
        builder.setOperatorId(operatorId);
        builder.addKickedId(kickedId);
        RelationProto.RoomKickViewerReq request = builder.build();
        MyLog.w(TAG, "kickView request:" + request.toString());
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_KICK_VIEWER);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        MyLog.v(TAG, " responseData=" + responseData);
        try {
            if (responseData != null) {
                MyLog.v(TAG, " getMnsCode:" + responseData.getMnsCode());
                RelationProto.RoomKickViewerRsp response = RelationProto.RoomKickViewerRsp.parseFrom(responseData.getData());
                return response.getRetCode();
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
        return ErrorCode.CODE_ERROR_NORMAL;
    }
}
