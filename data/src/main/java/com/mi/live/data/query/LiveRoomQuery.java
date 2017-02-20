package com.mi.live.data.query;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.api.request.live.EnterLiveRequest;
import com.mi.live.data.api.request.live.LeaveLiveRequest;
import com.mi.live.data.cache.RoomInfoGlobalCache;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.query.mapper.RoomDataMapper;
import com.mi.live.data.query.model.EnterRoomInfo;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.LiveProto;
import com.wali.live.proto.RoomRecommend;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by chengsimin on 16/9/8.
 */
public class LiveRoomQuery {
    public final static String TAG = LiveRoomQuery.class.getSimpleName();

    public static Observable<EnterRoomInfo> enterRoom(final long anchorId, final String roomId, final String password) {
        RoomInfoGlobalCache.getsInstance().enterCurrentRoom(roomId);
        return Observable
                .create(new Observable.OnSubscribe<LiveProto.EnterLiveRsp>() {
                    @Override
                    public void call(Subscriber<? super LiveProto.EnterLiveRsp> subscriber) {
                        LiveProto.EnterLiveRsp rsp = new EnterLiveRequest(anchorId, roomId, password).syncRsp();
                        if (rsp != null) {
                            subscriber.onNext(rsp);
                        } else {
                            subscriber.onError(new Exception("enterRoom EnterLiveRsp is null"));
                        }
                        subscriber.onCompleted();
                    }
                })
                .map(new Func1<LiveProto.EnterLiveRsp, EnterRoomInfo>() {
                    @Override
                    public EnterRoomInfo call(LiveProto.EnterLiveRsp enterLiveRsp) {
                        MyLog.d(TAG, "enterRoom call");
                        return RoomDataMapper.loadLiveBeanFromPB(enterLiveRsp);
                    }
                });
    }


    public static Observable<Boolean> leaveRoom(final long anchorId, final String roomId, final int messageMode) {
        RoomInfoGlobalCache.getsInstance().leaveCurrentRoom(roomId);
        return Observable
                .create(new Observable.OnSubscribe<LiveProto.LeaveLiveRsp>() {
                    @Override
                    public void call(Subscriber<? super LiveProto.LeaveLiveRsp> subscriber) {
                        LiveProto.LeaveLiveRsp rsp = new LeaveLiveRequest(anchorId, roomId, messageMode).syncRsp();
                        if (rsp != null) {
                            subscriber.onNext(rsp);
                        } else {
                            subscriber.onError(new Exception("leaveRoom LeaveLiveRsp is null"));
                        }
                        subscriber.onCompleted();
                    }
                })
                .map(new Func1<LiveProto.LeaveLiveRsp, Boolean>() {
                    @Override
                    public Boolean call(LiveProto.LeaveLiveRsp leaveLiveRsp) {
                        MyLog.d(TAG, "leaveRoom call");
                        return leaveLiveRsp.getRetCode() == 0;
                    }
                });
    }


    /**
     * 得到推荐房间列表
     */
    public static Observable<List<RoomRecommend.RecommendRoom>> getRoomRecommendList(final String liveId, final long zuid, final long uuid) {
        return Observable.just(null)
                .map(new Func1<Object, List<RoomRecommend.RecommendRoom>>() {
                    @Override
                    public List<RoomRecommend.RecommendRoom> call(Object o) {

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
                });
    }
}
