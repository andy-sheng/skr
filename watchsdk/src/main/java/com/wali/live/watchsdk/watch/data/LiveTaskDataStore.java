package com.wali.live.watchsdk.watch.data;

import com.mi.live.data.api.request.live.EnterLiveRequest;
import com.mi.live.data.api.request.live.LeaveLiveRequest;
import com.wali.live.proto.LiveProto;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by lan on 16/6/28.
 */
public class LiveTaskDataStore {
    public static final String TAG = LiveTaskDataStore.class.getSimpleName();

    public Observable<LiveProto.EnterLiveRsp> enterLiveObservable(final long playerId, final String liveId) {
        return Observable.create(new Observable.OnSubscribe<LiveProto.EnterLiveRsp>() {
            @Override
            public void call(Subscriber<? super LiveProto.EnterLiveRsp> subscriber) {

                LiveProto.EnterLiveRsp rsp = new EnterLiveRequest(playerId, liveId).syncRsp();
                if (rsp == null) {
                    subscriber.onError(new Exception("EnterLiveRsp is null"));
                } else {
                    subscriber.onNext(rsp);
                    subscriber.onCompleted();
                }

            }
        });
    }

    public Observable<LiveProto.LeaveLiveRsp> leaveLiveObservable(final long playerId, final String liveId) {
        return Observable.create(new Observable.OnSubscribe<LiveProto.LeaveLiveRsp>() {
                                     @Override
                                     public void call(Subscriber<? super LiveProto.LeaveLiveRsp> subscriber) {
                                         LiveProto.LeaveLiveRsp rsp = new LeaveLiveRequest(playerId, liveId).syncRsp();
                                         if (rsp == null) {
                                             subscriber.onError(new Exception("LeaveLiveRsp is null"));
                                         } else {
                                             subscriber.onNext(rsp);
                                             subscriber.onCompleted();
                                         }
                                     }
                                 }
        );
    }
}
