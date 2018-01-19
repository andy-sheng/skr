package com.wali.live.watchsdk.contest.presenter;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.query.ContestRoomQuery;
import com.mi.live.data.query.model.EnterRoomInfo;
import com.wali.live.proto.LiveProto;
import com.wali.live.proto.LiveSummitProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.contest.model.AwardUser;
import com.wali.live.watchsdk.contest.request.GetContestAwardListRequest;
import com.wali.live.watchsdk.contest.request.GetContestViewerInfoRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by liuyanyan on 2018/1/13.
 */
public class ContestWatchPresenter extends BaseRxPresenter<IContestWatchView> {
    public static final int INTERVAL = 5_000; //5s拉一次

    private Subscription mPullSubscription;

    public ContestWatchPresenter(IContestWatchView view) {
        super(view);
    }

    public void enterLiveToServer(final long anchorId, final String roomId) {
        MyLog.w(TAG, "enterLiveToServer");
        ContestRoomQuery.enterContestRoom(MyUserInfoManager.getInstance().getUuid(), anchorId, roomId, null)
                .subscribeOn(Schedulers.io())
                .compose(mView.<EnterRoomInfo>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<EnterRoomInfo>() {
                    @Override
                    public void call(EnterRoomInfo enterRoomInfo) {
                        MyLog.w(TAG, "enterLiveToServer onNext");
                        mView.processEnterLive(enterRoomInfo);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, "enterLiveToServer onError");
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public void pullRoomInfo(final long anchorId, final String roomId) {
        MyLog.w(TAG, "pullRoomInfo");
        ContestRoomQuery.roomInfo(MyUserInfoManager.getInstance().getUuid(), anchorId, roomId)
                .subscribeOn(Schedulers.io())
                .compose(mView.<LiveProto.RoomInfoRsp>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveProto.RoomInfoRsp>() {
                    @Override
                    public void call(LiveProto.RoomInfoRsp roomInfoRsp) {
                        MyLog.w(TAG, "pullRoomInfo onNext");
                        mView.processRoomInfo(roomInfoRsp);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, "pullRoomInfo onError");
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public void startTimerForViewers(final long zuid, final String roomId) {
        MyLog.w(TAG, "startTimerForViewers zuid=" + zuid + " roomId=" + roomId);
        Observable.interval(0, INTERVAL, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .compose(mView.<Long>bindUntilEvent())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        startInterval(zuid, roomId);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, throwable);
                    }
                });

    }

    private void startInterval(final long zuid, final String roomId) {
        MyLog.w(TAG, "startInterval zuid=" + zuid + " roomId=" + roomId);
        if (mPullSubscription != null && !mPullSubscription.isUnsubscribed()) {
            mPullSubscription.unsubscribe();
        }
        mPullSubscription = Observable.just(0).map(new Func1<Integer, LiveSummitProto.GetContestViewerInfoRsp>() {
            @Override
            public LiveSummitProto.GetContestViewerInfoRsp call(Integer integer) {
                return new GetContestViewerInfoRequest(zuid, roomId).syncRsp();
            }
        }).subscribeOn(Schedulers.io())
                .compose(mView.<LiveSummitProto.GetContestViewerInfoRsp>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveSummitProto.GetContestViewerInfoRsp>() {
                    @Override
                    public void call(LiveSummitProto.GetContestViewerInfoRsp rsp) {
                        if (rsp != null && rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                            MyLog.w(TAG, " cnt=" + rsp.getViewerNum());
                            mView.processViewerNum(rsp.getViewerNum());
                        } else {
                            MyLog.e(TAG, "rsp = null");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, throwable);
                    }
                });
    }

    public void leaveLiveToServer(final long anchorId, final String roomId) {
        MyLog.w(TAG, "leaveLiveToServer");
        ContestRoomQuery.leaveContestRoom(anchorId, roomId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        MyLog.w(TAG, "leaveLiveToServer onNext");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "leaveLiveToServer", throwable);
                    }
                });
    }

    public void getAwardList(final String contestId, final String liveId) {
        Observable
                .create(new Observable.OnSubscribe<List<AwardUser>>() {
                    @Override
                    public void call(Subscriber<? super List<AwardUser>> subscriber) {
                        LiveSummitProto.GetContestAwardListRsp rsp = new GetContestAwardListRequest(contestId, liveId).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("getAwardList rsp is null"));
                        } else if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            ToastUtils.showToast(GlobalData.app().getResources().getString(R.string.commit_answer_error_code, rsp.getRetCode()));
                            subscriber.onError(new Exception(String.format("getAwardList retCode = %d", rsp.getRetCode())));
                        } else {
                            if (rsp.getUserInfosList() != null) {
                                List<AwardUser> awardUsers = new ArrayList<>();
                                for (LiveSummitProto.UserInfo userInfo : rsp.getUserInfosList()) {
                                    awardUsers.add(new AwardUser(userInfo.getUuid(), userInfo.getNickname(), userInfo.getAvatar()));
                                }
                                subscriber.onNext(awardUsers);
                                subscriber.onCompleted();
                            } else {
                                subscriber.onError(new Exception("getAwardList  extraInfo= null"));
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<List<AwardUser>>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<AwardUser>>() {
                    @Override
                    public void call(List<AwardUser> awardUsers) {
                        MyLog.w(TAG, "getAwardList onNext");
                        if (mView != null) {
                            mView.showAwardListView(awardUsers);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, "getAwardList onError=" + throwable.getMessage());
                    }
                });

    }

}
