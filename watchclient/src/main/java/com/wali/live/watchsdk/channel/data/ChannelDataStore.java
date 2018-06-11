package com.wali.live.watchsdk.channel.data;

import com.base.log.MyLog;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.HotChannelProto;
import com.wali.live.proto.LiveShowProto;
import com.wali.live.watchsdk.channel.list.request.ChannelListRequest;
import com.wali.live.watchsdk.channel.request.GetChannelRequest;

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

    public Observable<LiveShowProto.GetChannelsRsp> getChannelListObservable(final long fcId) {
        return Observable.create(new Observable.OnSubscribe<LiveShowProto.GetChannelsRsp>() {
                                     @Override
                                     public void call(Subscriber<? super LiveShowProto.GetChannelsRsp> subscriber) {
                                         LiveShowProto.GetChannelsRsp rsp = new ChannelListRequest(fcId).syncRsp();
                                         if (rsp == null) {
                                             subscriber.onError(new Exception("getChannelListObservable is null"));
                                         } else {
                                             MyLog.d(TAG, "getChannelListObservable rsp= " + rsp.toString());
                                             subscriber.onNext(rsp);
                                             subscriber.onCompleted();
                                         }
                                     }
                                 }
        );
    }

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
                                             MyLog.d(TAG, "getHotChannelObservable channelId=" + channelId + " rsp= " + rsp.toString());
                                             subscriber.onNext(rsp);
                                             subscriber.onCompleted();
                                         }
                                     }
                                 }
        );
    }
}
