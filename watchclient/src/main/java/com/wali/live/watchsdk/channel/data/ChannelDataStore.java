package com.wali.live.watchsdk.channel.data;

import android.support.annotation.IntDef;

import com.base.log.MyLog;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.HotChannelProto;
import com.wali.live.proto.LiveShowProto;
import com.wali.live.watchsdk.channel.list.request.ChannelListRequest;
import com.wali.live.watchsdk.channel.request.GetChannelRequest;
import com.wali.live.watchsdk.channel.request.GetRecListRequest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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


    @IntDef({GAME_WATCH_CHANNEL_TYPE_RECOMMEND, GAME_WATCH_CHANNEL_TYPE_FOCUS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RecType {}
    public static final int GAME_WATCH_CHANNEL_TYPE_RECOMMEND = 1; // 推荐类型，根据当前观看的游戏直播推荐
    public static final int GAME_WATCH_CHANNEL_TYPE_FOCUS = 2; // 推荐类型，我的关注

    @IntDef({GAME_WATCH_CHANNEL_FROM_PORTRAIT, GAME_WATCH_CHANNEL_FROM_LANDSCAPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ReqFrom {}
    public static final int GAME_WATCH_CHANNEL_FROM_PORTRAIT = 1; //请求来源，竖屏
    public static final int GAME_WATCH_CHANNEL_FROM_LANDSCAPE = 2; //请求来源，游戏全屏
    /**
     * 新版游戏直播间内
     * 根据当前观看的直播，关注等信息，获取推荐的直播列表
     * @param viewerId　观众Id
     * @param anchorId  主播Id
     * @param packageName　游戏包名
     * @param gameId　游戏Id
     * @param recType 推荐类型，决定内容，1=根据当前观看的游戏直播推荐，2=我的关注
     * @param reqFrom 请求来源，决定样式，1=竖屏-更多直播，2=游戏全屏-更多直播
     * @return
     */
    public Observable<HotChannelProto.GetRecListRsp> getRecListObservable(final long viewerId, final long anchorId, final String packageName, final long gameId, @RecType final int recType, @ReqFrom final int reqFrom) {
        return Observable.create(new Observable.OnSubscribe<HotChannelProto.GetRecListRsp>() {
                                     @Override
                                     public void call(Subscriber<? super HotChannelProto.GetRecListRsp> subscriber) {
                                         HotChannelProto.GetRecListRsp rsp = new GetRecListRequest(viewerId, anchorId, packageName, gameId, recType, reqFrom).syncRsp();
                                         if (rsp == null) {
                                             subscriber.onError(new Exception("GetRecListRsp is null"));
                                         } else if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                                             subscriber.onError(new Exception(String.format("GetRecListRsp retCode = %d", rsp.getRetCode())));
                                         } else {
                                             subscriber.onNext(rsp);
                                             subscriber.onCompleted();
                                         }
                                     }
                                 }
        );
    }
}
