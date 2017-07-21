package com.wali.live.watchsdk.channel.sublist.data;

import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.HotChannelProto.GetRecommendSublistRsp;
import com.wali.live.watchsdk.channel.sublist.presenter.SubChannelParam;
import com.wali.live.watchsdk.channel.sublist.request.GetSubListRequest;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 二级页面的数据加载类
 */
public class SubChannelDataStore {
    public static final String TAG = SubChannelDataStore.class.getSimpleName();

    public Observable<GetRecommendSublistRsp> getHotSubListObservable(final SubChannelParam param, final int gender) {
        return Observable.create(
                new Observable.OnSubscribe<GetRecommendSublistRsp>() {
                    @Override
                    public void call(Subscriber<? super GetRecommendSublistRsp> subscriber) {
                        GetRecommendSublistRsp rsp = new GetSubListRequest(param, gender).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("GetSubListRsp is null"));
                        } else if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            subscriber.onError(new Exception(String.format("GetSubListRsp retCode = %d", rsp.getRetCode())));
                        } else {
                            subscriber.onNext(rsp);
                            subscriber.onCompleted();
                        }
                    }
                }
        );
    }
}
