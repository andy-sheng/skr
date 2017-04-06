package com.wali.live.channel.data;

import com.mi.live.data.api.ErrorCode;
import com.wali.live.channel.request.GetChannelRequest;
import com.wali.live.proto.HotChannelProto;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道的数据加载类：推荐频道
 */
public class ChannelDataStore {
    public static final String TAG = ChannelDataStore.class.getSimpleName();

    public Observable<HotChannelProto.GetRecommendListRsp> getHotChannelObservable(final long channelId) {
        return Observable.create(new Observable.OnSubscribe<HotChannelProto.GetRecommendListRsp>() {
                                     @Override
                                     public void call(Subscriber<? super HotChannelProto.GetRecommendListRsp> subscriber) {
                                         HotChannelProto.GetRecommendListRsp rsp = new GetChannelRequest(channelId).syncRsp();
                                         if (rsp == null) {
                                             subscriber.onError(new Exception("GetRecommendListRsp is null"));
                                         } else if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                                             subscriber.onError(new Exception(String.format("GetRecommendListRsp retCode = %d", rsp.getRetCode())));
                                         } else {
                                             subscriber.onNext(rsp);
                                             subscriber.onCompleted();
                                         }
                                     }
                                 }
        );
    }
}
