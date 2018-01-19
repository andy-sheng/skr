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

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * @module 单独给冲顶大会房间用
 */
public class ContestRoomQuery {
    public final static String TAG = ContestRoomQuery.class.getSimpleName();

    public static Observable<EnterRoomInfo> enterContestRoom(final long uid, final long anchorId, final String roomId, final String password) {
        RoomInfoGlobalCache.getsInstance().enterContestRoom(roomId);
        return Observable
                .create(new Observable.OnSubscribe<LiveProto.EnterLiveRsp>() {
                    @Override
                    public void call(Subscriber<? super LiveProto.EnterLiveRsp> subscriber) {
                        MyLog.w(TAG, "enterRoom call req: uuid =" + uid + " anchorId=" + anchorId + " roomId=" + roomId);
                        LiveProto.EnterLiveRsp rsp = new EnterLiveRequest(anchorId, roomId).syncRsp();
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
                        MyLog.w(TAG, "enterRoom call rsp=" + ((enterLiveRsp == null) ? "null" : enterLiveRsp.toString()));
                        return RoomDataMapper.loadLiveBeanFromPB(enterLiveRsp);
                    }
                });
    }


    public static Observable<Boolean> leaveContestRoom(final long anchorId, final String roomId) {
        RoomInfoGlobalCache.getsInstance().leaveContestRoom(roomId);
        return Observable
                .create(new Observable.OnSubscribe<LiveProto.LeaveLiveRsp>() {
                    @Override
                    public void call(Subscriber<? super LiveProto.LeaveLiveRsp> subscriber) {
                        MyLog.w(TAG, "leaveRoom request call");
                        LiveProto.LeaveLiveRsp rsp = new LeaveLiveRequest(anchorId, roomId).syncRsp();
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

    public static Observable<LiveProto.RoomInfoRsp> roomInfo(final long uid, final long anchorId, final String roomId) {
        return Observable.create(new Observable.OnSubscribe<LiveProto.RoomInfoRsp>() {
            @Override
            public void call(Subscriber<? super LiveProto.RoomInfoRsp> subscriber) {
                LiveProto.RoomInfoReq req = LiveProto.RoomInfoReq.newBuilder()
                        .setUuid(uid)
                        .setZuid(anchorId)
                        .setGetLatestLive(false)
                        .setLiveId(roomId)
                        .build();

                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_LIVE_ROOM_INFO);
                data.setData(req.toByteArray());
                MyLog.e(TAG, "roomInfo request : \n" + req.toString());

                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
                if (rspData != null) {
                    try {
                        LiveProto.RoomInfoRsp rsp = LiveProto.RoomInfoRsp.parseFrom(rspData.getData());
                        MyLog.e(TAG, "roomInfo response : \n" + rsp.toString());
                        if (rsp == null) {
                            subscriber.onError(new Exception("roomInfo rsp == null"));
                        } else {
                            subscriber.onNext(rsp);
                            subscriber.onCompleted();
                        }
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                } else {
                    subscriber.onError(new Exception("roomInfo rspData == null"));
                }
            }
        });
    }
}
