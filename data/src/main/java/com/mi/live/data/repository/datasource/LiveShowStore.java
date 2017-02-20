package com.mi.live.data.repository.datasource;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.LiveShowProto;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by chengsimin on 16/6/29.
 */
public class LiveShowStore {
    final static String TAG = LiveShowStore.class.getSimpleName();
    /**
     * 获取某个频道的直播列表
     */
    public Observable<LiveShowProto.GetTopicLiveRsp> getLiveListOfTopic(final String topic, final int pageCount) {
        return Observable.create(new Observable.OnSubscribe<LiveShowProto.GetTopicLiveRsp>() {
            @Override
            public void call(Subscriber<? super LiveShowProto.GetTopicLiveRsp> subscriber) {
                LiveShowProto.GetTopicLiveReq request = LiveShowProto.GetTopicLiveReq.newBuilder()
                        .setTopic(topic)
                        .setLiveType(2) //类型，默认0:直播,1:回放,2:直播和回放
                        .setPageNum(pageCount)//页码,默认1
                        .build();

                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_LIST_TOPIC);
                data.setData(request.toByteArray());
                MyLog.v(TAG, "getLiveListOfTopic request : \n" + request.toString());

                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
                if (null != rspData) {
                    try {
                        LiveShowProto.GetTopicLiveRsp rsp = LiveShowProto.GetTopicLiveRsp.parseFrom(rspData.getData());
                        MyLog.v(TAG, "getLiveListOfTopic response : \n" + rsp.toString());
                        subscriber.onNext(rsp);
                    } catch (InvalidProtocolBufferException e) {
                        MyLog.e(e);
                        subscriber.onError(e);
                    }
                }
                subscriber.onCompleted();
            }
        });
    }
}
