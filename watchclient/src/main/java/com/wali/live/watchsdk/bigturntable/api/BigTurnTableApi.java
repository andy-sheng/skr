package com.wali.live.watchsdk.bigturntable.api;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.BigTurnTableProto;
import com.wali.live.proto.LiveProto;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by zhujianning on 18-4-16.
 *  大转盘底层数据请求api
 */

public class BigTurnTableApi {
    private static final String TAG = "BigTurnTableApi";

    private static PacketData sendToMiLinkClient(byte[] data, String command) {
        PacketData packetData = new PacketData();
        packetData.setCommand(command);
        packetData.setData(data);
        return MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
    }

    public static Observable<BigTurnTableProto.StartTurntableRsp> startTurnTableReq(final long zuid, final String roomId, final BigTurnTableProto.TurntableType type, final String customDes) {
        return Observable.create(new Observable.OnSubscribe<BigTurnTableProto.StartTurntableRsp>() {
            @Override
            public void call(Subscriber<? super BigTurnTableProto.StartTurntableRsp> subscriber) {
                BigTurnTableProto.StartTurntableReq.Builder builder = BigTurnTableProto.StartTurntableReq.newBuilder()
                        .setZuid(zuid)
                        .setLiveId(roomId)
                        .setType(type)
                        .setCustomPrizeName(customDes);
                BigTurnTableProto.StartTurntableReq request = builder.build();
                PacketData rspData = sendToMiLinkClient(request.toByteArray(), MiLinkCommand.COMMAND_START_TURNTABLE);
                if(rspData != null) {
                    try {
                        BigTurnTableProto.StartTurntableRsp rsp = BigTurnTableProto.StartTurntableRsp.parseFrom(rspData.getData());
                        if(rsp == null) {
                            subscriber.onError(new Exception("StartTurntableRsp rsp is null"));
                        }
                        subscriber.onNext(rsp);
                        subscriber.onCompleted();
                    } catch (InvalidProtocolBufferException e) {
                        MyLog.d(TAG, e);
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    public static Observable<BigTurnTableProto.StopTurntableRsp> stopTurnTableReq(final long zuid, final String roomId, final BigTurnTableProto.TurntableType type) {

        return Observable.create(new Observable.OnSubscribe<BigTurnTableProto.StopTurntableRsp>() {
            @Override
            public void call(Subscriber<? super BigTurnTableProto.StopTurntableRsp> subscriber) {
                BigTurnTableProto.StopTurntableReq.Builder builder = BigTurnTableProto.StopTurntableReq.newBuilder()
                        .setZuid(zuid)
                        .setLiveId(roomId)
                        .setType(type);
                BigTurnTableProto.StopTurntableReq request = builder.build();
                PacketData rspData = sendToMiLinkClient(request.toByteArray(), MiLinkCommand.COMMAND_STOP_TURNTABLE);
                if(rspData != null) {
                    try {
                        BigTurnTableProto.StopTurntableRsp rsp = BigTurnTableProto.StopTurntableRsp.parseFrom(rspData.getData());
                        if(rsp == null) {
                            subscriber.onError(new Exception("StopTurntableRsp rsp is null"));
                        }
                        subscriber.onNext(rsp);
                        subscriber.onCompleted();
                    } catch (InvalidProtocolBufferException e) {
                        MyLog.d(TAG, e);
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    public static LiveProto.GetRoomAttachmentRsp getTurnTableInfoReq(long zuid, String liveId) {
        LiveProto.GetRoomAttachmentReq request = LiveProto.GetRoomAttachmentReq.newBuilder()
                .setZuid(zuid)
                .setLiveid(liveId)
                .setIsGetTurntable(true)
                .build();
        PacketData rspData = sendToMiLinkClient(request.toByteArray(), MiLinkCommand.COMMAND_ROOM_ATTACHMENT);
        if(rspData != null) {
            try {
                LiveProto.GetRoomAttachmentRsp rsp = LiveProto.GetRoomAttachmentRsp.parseFrom(rspData.getData());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                MyLog.d(TAG, e);
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Observable<BigTurnTableProto.DrawTurntableRsp> drawTurnTableReq(final long uid, final long zuid, final String roomId, final BigTurnTableProto.TurntableType type) {
        return Observable.create(new Observable.OnSubscribe<BigTurnTableProto.DrawTurntableRsp>() {
            @Override
            public void call(Subscriber<? super BigTurnTableProto.DrawTurntableRsp> subscriber) {
                BigTurnTableProto.DrawTurntableReq.Builder builder = BigTurnTableProto.DrawTurntableReq.newBuilder()
                        .setUuid(uid)
                        .setZuid(zuid)
                        .setLiveId(roomId)
                        .setType(type);
                BigTurnTableProto.DrawTurntableReq request = builder.build();
                PacketData rspData = sendToMiLinkClient(request.toByteArray(), MiLinkCommand.COMMAND_DRAW_TURNTABLE);
                if(rspData != null) {
                    try {
                        BigTurnTableProto.DrawTurntableRsp rsp = BigTurnTableProto.DrawTurntableRsp.parseFrom(rspData.getData());
                        if(rsp == null) {
                            subscriber.onError(new Exception("DrawTurntableRsp rsp is null"));
                        }
                        subscriber.onNext(rsp);
                        subscriber.onCompleted();
                    } catch (InvalidProtocolBufferException e) {
                        MyLog.d(TAG, e);
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    public static Observable<BigTurnTableProto.RewardTurntableRsp> rewardTurntableReq(final long uid, final long zuid, final String roomId, final String prizekey) {

        return Observable.create(new Observable.OnSubscribe<BigTurnTableProto.RewardTurntableRsp>() {
            @Override
            public void call(Subscriber<? super BigTurnTableProto.RewardTurntableRsp> subscriber) {
                BigTurnTableProto.RewardTurntableReq.Builder builder = BigTurnTableProto.RewardTurntableReq.newBuilder()
                        .setUuid(uid)
                        .setZuid(zuid)
                        .setLiveId(roomId)
                        .setPrizeKey(prizekey);
                BigTurnTableProto.RewardTurntableReq request = builder.build();
                PacketData rspData = sendToMiLinkClient(request.toByteArray(), MiLinkCommand.COMMAND_REWARD_TURNTABLE);
                if(rspData != null) {
                    try {
                        BigTurnTableProto.RewardTurntableRsp rsp = BigTurnTableProto.RewardTurntableRsp.parseFrom(rspData.getData());
                        if(rsp == null) {
                            subscriber.onError(new Exception("RewardTurntableRsp rsp is null"));
                        }
                        subscriber.onNext(rsp);
                        subscriber.onCompleted();
                    } catch (InvalidProtocolBufferException e) {
                        MyLog.d(TAG, e);
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

}
