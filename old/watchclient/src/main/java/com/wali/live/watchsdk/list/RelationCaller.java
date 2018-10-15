package com.wali.live.watchsdk.list;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.RelationProto;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by zyh on 2017/3/29.
 *
 * @module 给宿主提供拉列表的接口
 */

public class RelationCaller {
    private final static String STAG = RelationCaller.class.getSimpleName();

    public static Observable<RelationProto.FollowingListResponse> getFollowingList(final long uuid, final boolean isBothWay, final long timeStamp) {
        return (Observable<RelationProto.FollowingListResponse>) Observable.create(new Observable.OnSubscribe<RelationProto.FollowingListResponse>() {
            @Override
            public void call(Subscriber<? super RelationProto.FollowingListResponse> subscriber) {
                RelationProto.FollowingListRequest.Builder builder = RelationProto.FollowingListRequest.newBuilder();
                builder.setUserId(uuid)
                        .setIsBothway(isBothWay)
                        .setSyncTime(timeStamp);

                MyLog.w(STAG, "getFollowingUserList request=" + builder.build().toString());
                PacketData packetData = new PacketData();
                packetData.setCommand(MiLinkCommand.COMMAND_GET_FOLLOWING_LIST);
                packetData.setData(builder.build().toByteArray());
                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
                if (rspData != null) {
                    try {
                        RelationProto.FollowingListResponse rsp = RelationProto.FollowingListResponse.parseFrom(rspData.getData());
                        MyLog.w(STAG, "getFollowingUserList rsp : \n" + rsp.toString());
                        subscriber.onNext(rsp);
                        subscriber.onCompleted();
                    } catch (InvalidProtocolBufferException e) {
                        MyLog.e(e);
                        subscriber.onError(new Throwable(e));
                    }
                } else {
                    subscriber.onError(new Throwable("rsp == null"));
                }
            }
        });
    }
}
