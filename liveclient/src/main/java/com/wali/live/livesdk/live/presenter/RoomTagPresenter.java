package com.wali.live.livesdk.live.presenter;

import com.base.activity.RxActivity;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.network.Network;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.api.RoomTagRequest;
import com.wali.live.livesdk.live.viewmodel.RoomTag;
import com.wali.live.proto.Live2Proto;
import com.wali.live.proto.Live2Proto.GetRoomTagRsp;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 16/12/16.
 */
public class RoomTagPresenter {
    private static final String TAG = RoomTagPresenter.class.getSimpleName();

    private Subscription mSubscription;
    private RxActivity mRxActivity;

    private IRoomTagView mView;
    private List<RoomTag> mRoomTags;

    public RoomTagPresenter(RxActivity rxActivity, IRoomTagView view) {
        mRxActivity = rxActivity;
        mView = view;
    }

    public void prepare(final int type) {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        mSubscription = Observable
                .create(new Observable.OnSubscribe<GetRoomTagRsp>() {
                    @Override
                    public void call(Subscriber<? super GetRoomTagRsp> subscriber) {
                        GetRoomTagRsp rsp = new RoomTagRequest(type).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("GetRoomTagRsp is null"));
                        } else if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            subscriber.onError(new Exception(String.format("GetRoomTagRsp retCode = %d", rsp.getRetCode())));
                        } else {
                            subscriber.onNext(rsp);
                            subscriber.onCompleted();
                        }
                    }
                }).map(new Func1<GetRoomTagRsp, List<RoomTag>>() {
                    @Override
                    public List<RoomTag> call(GetRoomTagRsp getRoomTagRsp) {
                        List<RoomTag> result = new ArrayList();
                        for (Live2Proto.TagInfo tagInfo : getRoomTagRsp.getTagInfosList()) {
                            result.add(new RoomTag(tagInfo));
                        }
                        return result;
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mRxActivity.<List<RoomTag>>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<RoomTag>>() {
                    @Override
                    public void call(List<RoomTag> roomTags) {
                        MyLog.d(TAG, "GetRoomTagRsp call");
                        if (roomTags != null && roomTags.size() != 0) {
                            mRoomTags = roomTags;
                        } else {
                            mView.hideTag();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "GetRoomTagRsp failed throwable", throwable);
                    }
                });
    }

    public void start(final int type) {
        if (mRoomTags != null && mRoomTags.size() != 0) {
            mView.showTagList(mRoomTags, type);
            return;
        }
        //网络判断
        if (!Network.hasNetwork((GlobalData.app()))) {
            ToastUtils.showToast(GlobalData.app(), R.string.network_unavailable);
            return;
        }
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        mSubscription = Observable
                .create(new Observable.OnSubscribe<GetRoomTagRsp>() {
                    @Override
                    public void call(Subscriber<? super GetRoomTagRsp> subscriber) {
                        GetRoomTagRsp rsp = new RoomTagRequest(type).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("GetRoomTagRsp is null"));
                        } else if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            subscriber.onError(new Exception(String.format("GetRoomTagRsp retCode = %d", rsp.getRetCode())));
                        } else {
                            subscriber.onNext(rsp);
                            subscriber.onCompleted();
                        }
                    }
                })
                .map(new Func1<GetRoomTagRsp, List<RoomTag>>() {
                    @Override
                    public List<RoomTag> call(GetRoomTagRsp getRoomTagRsp) {
                        List<RoomTag> result = new ArrayList();
                        for (Live2Proto.TagInfo tagInfo : getRoomTagRsp.getTagInfosList()) {
                            result.add(new RoomTag(tagInfo));
                        }
                        return result;
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mRxActivity.<List<RoomTag>>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<RoomTag>>() {
                    @Override
                    public void call(List<RoomTag> roomTags) {
                        MyLog.d(TAG, "GetRoomTagRsp onNext");
                        if (roomTags != null && roomTags.size() != 0) {
                            mRoomTags = roomTags;
                            mView.showTagList(mRoomTags, type);
                        } else {
                            mView.hideTag();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.d(TAG, "GetRoomTagRsp onError=" + throwable);
                        mView.hideTag();
                    }
                });
    }

    public void stop() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }
}
