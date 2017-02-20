package com.mi.live.data.relation;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.event.BlockOrUnblockEvent;
import com.mi.live.data.event.DatabaseChangedEvent;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.greendao.GreenDaoManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.repository.datasource.WatchHistoryInfoDaoAdapter;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.dao.Relation;
import com.wali.live.dao.RelationDao;
import com.wali.live.proto.Rank;
import com.wali.live.proto.RelationProto;


import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by chengsimin on 16/9/20.
 */
public class RelationApi {
    public final static String TAG = RelationApi.class.getSimpleName();
    public static final int RESULT_CODE_SUCCESS = 0; //表示成功的返回码

    //以下为关注返回的值 大于等于0说明成功 返回FOLLOW_STATE_BOTH_WAY 说明双向关注
    public static final int FOLLOW_STATE_FAILED = -1;

    public static final int FOLLOW_STATE_SUCCESS = 0;

    public static final int FOLLOW_STATE_BOTH_WAY = 1;

    public static final int ERROR_CODE_BLACK = 7506;

    public static int errorCode = 0;

    public static final int LOADING_FOLLOWING_PAGE_COUNT = 300;

    public static Relation getRelationByUUid(long uuid) {
        return GreenDaoManager.getDaoSession(GlobalData.app()).getRelationDao().queryBuilder().where(RelationDao.Properties.UserId.eq(uuid)).unique();
    }

    public static boolean insertRelation(Relation Relation) {
        if (null != Relation) {
            try {
                GreenDaoManager.getDaoSession(GlobalData.app()).getRelationDao().insertOrReplace(Relation);
                List<Relation> RelationList = new ArrayList<>();
                RelationList.add(Relation);
                EventBus.getDefault().post(new DatabaseChangedEvent(DatabaseChangedEvent.EVENT_TYPE_DB_RELATION, DatabaseChangedEvent.ACTION_ADD, RelationList));
            } catch (Exception e) {
                MyLog.e(TAG, e);
                return false;
            }
            return true;
        }
        return false;
    }

    public static Observable<RelationProto.FollowResponse> follow(final long uuid, final long target, final String roomid) {
        return Observable.create(new Observable.OnSubscribe<RelationProto.FollowResponse>() {
            @Override
            public void call(Subscriber<? super RelationProto.FollowResponse> subscriber) {
                RelationProto.FollowRequest.Builder builder = RelationProto.FollowRequest.newBuilder()
                        .setUserId(uuid)
                        .setTargetId(target);
                if (!TextUtils.isEmpty(roomid)) {
                    builder.setRoomId(roomid);
                }
                RelationProto.FollowRequest request = builder.build();
                PacketData packetData = new PacketData();
                packetData.setCommand(MiLinkCommand.COMMAND_FOLLOW_REQUEST);
                packetData.setData(request.toByteArray());
                PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
                try {
                    if (responseData != null) {
                        RelationProto.FollowResponse response = RelationProto.FollowResponse.parseFrom(responseData.getData());
                        MyLog.v(TAG, " follow:" + response);
                        if (response.getCode() == 0) {
                            FollowOrUnfollowEvent event = new FollowOrUnfollowEvent(FollowOrUnfollowEvent.EVENT_TYPE_FOLLOW, target);
                            event.isBothFollow = response.getIsBothway();
                            EventBus.getDefault().post(event);
                        }
                        subscriber.onNext(response);
                    }
                    subscriber.onCompleted();
                } catch (InvalidProtocolBufferException e) {
                    MyLog.e(e);
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 关注，只返回 失败，单项关注，双向关注
     */
    public static int follow2(long uuid, long target) {
        return follow2(uuid, target, null);
    }

    public static int follow2(long uuid, long target, String roomid) {
        RelationProto.FollowRequest.Builder builder = RelationProto.FollowRequest.newBuilder().setUserId(uuid).setTargetId(target);
        if (!TextUtils.isEmpty(roomid)) {
            builder.setRoomId(roomid);
        }
        RelationProto.FollowRequest request = builder.build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_FOLLOW_REQUEST);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        MyLog.v(TAG + " follow:" + responseData);
        try {
            if (responseData != null) {
                RelationProto.FollowResponse response = RelationProto.FollowResponse.parseFrom(responseData.getData());
                MyLog.v(TAG + " followRequest result:" + response.getCode());
                errorCode = response.getCode();
                if (response.getCode() == RESULT_CODE_SUCCESS) {
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
        RelationProto.UnFollowRequest request = RelationProto.UnFollowRequest.newBuilder().setUserId(uuid).setTargetId(target).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_UNFOLLOW_REQUEST);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        MyLog.v(TAG + "unFollow:" + responseData);
        try {
            if (responseData != null) {
                RelationProto.UnFollowResponse response = RelationProto.UnFollowResponse.parseFrom(responseData.getData());
                MyLog.v(TAG + "unFollow result:" + response.getCode());
                if (response.getCode() == RESULT_CODE_SUCCESS) {
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

    public static Observable<RelationProto.UnFollowResponse> unfollow(final long uuid, final long target) {
        return Observable.create(new Observable.OnSubscribe<RelationProto.UnFollowResponse>() {
            @Override
            public void call(Subscriber<? super RelationProto.UnFollowResponse> subscriber) {
                RelationProto.UnFollowRequest request = RelationProto.UnFollowRequest.newBuilder().setUserId(uuid).setTargetId(target).build();
                PacketData packetData = new PacketData();
                packetData.setCommand(MiLinkCommand.COMMAND_UNFOLLOW_REQUEST);
                packetData.setData(request.toByteArray());
                PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
                MyLog.v(TAG + "unFollow:" + responseData);
                try {
                    if (responseData != null) {
                        RelationProto.UnFollowResponse response = RelationProto.UnFollowResponse.parseFrom(responseData.getData());
                        subscriber.onNext(response);
                    }
                    subscriber.onCompleted();
                } catch (InvalidProtocolBufferException e) {
                    MyLog.e(e);
                    subscriber.onError(e);
                }
            }
        });
    }


    public static Observable<RelationProto.SubscribeResponse> subscribe(final long uid, final long targetId) {
        return Observable.create(new Observable.OnSubscribe<RelationProto.SubscribeResponse>() {
            @Override
            public void call(Subscriber<? super RelationProto.SubscribeResponse> subscriber) {
                RelationProto.SubscribeRequest request = RelationProto.SubscribeRequest.newBuilder()
                        .setUserId(uid)
                        .setTargetId(targetId)
                        .build();
                PacketData packetData = new PacketData();
                packetData.setCommand(MiLinkCommand.COMMAND_SUBSCRIBE_REQUEST);
                packetData.setData(request.toByteArray());
                PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
                MyLog.v(TAG + "subscribe:" + responseData);
                try {
                    if (responseData != null) {
                        RelationProto.SubscribeResponse response = RelationProto.SubscribeResponse.parseFrom(responseData.getData());
                        subscriber.onNext(response);
                    }
                    subscriber.onCompleted();
                } catch (InvalidProtocolBufferException e) {
                    MyLog.e(e);
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<RelationProto.GetSubscribeInfoResponse> getSubscribeInfo(final long uid, final long targetId) {
        return Observable.create(new Observable.OnSubscribe<RelationProto.GetSubscribeInfoResponse>() {
            @Override
            public void call(Subscriber<? super RelationProto.GetSubscribeInfoResponse> subscriber) {
                RelationProto.GetSubscribeInfoRequest request = RelationProto.GetSubscribeInfoRequest.newBuilder()
                        .setUserId(uid)
                        .setTargetId(targetId)
                        .build();
                PacketData packetData = new PacketData();
                packetData.setCommand(MiLinkCommand.COMMAND_GET_SUBSCRIBE_INFO_REQUEST);
                packetData.setData(request.toByteArray());
                PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
                MyLog.v(TAG + "getSubscribeInfo:" + responseData);
                try {
                    if (responseData != null) {
                        RelationProto.GetSubscribeInfoResponse response = RelationProto.GetSubscribeInfoResponse.parseFrom(responseData.getData());
                        subscriber.onNext(response);
                    }
                    subscriber.onCompleted();
                } catch (InvalidProtocolBufferException e) {
                    MyLog.e(e);
                    subscriber.onError(e);
                }
            }
        });
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

    /**
     * 拉黑
     */
    public static boolean block(long uuid, long target) {
        RelationProto.BlockRequest request = RelationProto.BlockRequest.newBuilder().setUserId(uuid).setTargetId(target).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_BLOCK_REQUEST);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        try {
            if (responseData != null) {
                RelationProto.BlockResponse response = RelationProto.BlockResponse.parseFrom(responseData.getData());
                MyLog.v(TAG + " block result:" + response.getCode());
                if (response.getCode() == RESULT_CODE_SUCCESS) {
                    EventBus.getDefault().post(new BlockOrUnblockEvent(BlockOrUnblockEvent.EVENT_TYPE_BLOCK, target));
                    return true;
                }
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return false;
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
}
