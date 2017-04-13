package com.wali.live.watchsdk.channellive.presenter;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.ChannelLiveProto;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by zyh on 2017/3/29.
 *
 * @module 给宿主提供拉列表的接口
 */

public class ChannelLiveCaller extends RxLifeCyclePresenter {
    private final static String STAG = ChannelLiveCaller.class.getSimpleName();
    public final static int TYPE_LIVE = 1;

    public static Observable<ChannelLiveProto.GetChannelLiveDetailRsp> getChannelLive(final int channelId) {
        return (Observable<ChannelLiveProto.GetChannelLiveDetailRsp>) Observable.create(new Observable.OnSubscribe<ChannelLiveProto.GetChannelLiveDetailRsp>() {
            @Override
            public void call(Subscriber<? super ChannelLiveProto.GetChannelLiveDetailRsp> subscriber) {
                ChannelLiveProto.GetChannelLiveDetailReq req = ChannelLiveProto.GetChannelLiveDetailReq.newBuilder()
                        .setChannelId(channelId)
                        .setUid(UserAccountManager.getInstance().getUuidAsLong()).build();
                MyLog.w(STAG, "getChannelLive request=" + req.toString());
                PacketData packetData = new PacketData();
                packetData.setCommand(MiLinkCommand.COMMAND_RECOMMEND_CHANNELLIST);
                packetData.setData(req.toByteArray());
                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
                if (rspData != null) {
                    try {
                        ChannelLiveProto.GetChannelLiveDetailRsp rsp = ChannelLiveProto.GetChannelLiveDetailRsp.parseFrom(rspData.getData());
                        MyLog.w(STAG, "getChannelLive response : \n" + rsp.toString());
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
