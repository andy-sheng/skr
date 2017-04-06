package com.wali.live.livesdk.live.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.manager.LiveRoomCharacterManager;
import com.mi.live.data.manager.UserInfoManager;
import com.wali.live.livesdk.live.api.TitleListRequest;
import com.wali.live.livesdk.live.presenter.cache.TitleCache;
import com.wali.live.livesdk.live.presenter.view.IRoomPrepareView;
import com.wali.live.livesdk.live.presenter.viewmodel.TitleViewModel;
import com.wali.live.proto.Live2Proto;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 17/4/5.
 */
public class RoomPreparePresenter extends BaseRxPresenter<IRoomPrepareView> {
    private TitleViewModel mCacheModel;
    private int mSource;

    private Subscription mTitleSubscription;

    public RoomPreparePresenter(IRoomPrepareView view) {
        mView = view;
    }

    public RoomPreparePresenter(IRoomPrepareView view, int source) {
        mView = view;
        mSource = source;
    }

    public void setSource(int source) {
        mSource = source;
    }

    /**
     * 异步加载管理员
     */
    public void loadManager() {
        Observable
                .create(new Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {
                        UserInfoManager.getMyManagerList(UserAccountManager.getInstance().getUuidAsLong());
                        int managerCount = LiveRoomCharacterManager.getInstance().getManagerCount();
                        subscriber.onNext(managerCount);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mView.<Integer>bindLifecycle())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        int result = integer == null ? 0 : integer;
                        mView.setManagerCount(result);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public void loadTitle() {
        if (mTitleSubscription != null && !mTitleSubscription.isUnsubscribed()) {
            return;
        }
        mTitleSubscription = Observable
                .create(new Observable.OnSubscribe<TitleViewModel>() {
                    @Override
                    public void call(Subscriber<? super TitleViewModel> subscriber) {
                        TitleViewModel titleModel = TitleCache.getInstance().getTitleModel(mSource);
                        if (titleModel == null) {
                            Live2Proto.GetTitleListRsp rsp = new TitleListRequest(mSource).syncRsp();
                            if (rsp == null) {
                                subscriber.onError(new Exception("GetTitleListRsp is null"));
                                return;
                            } else if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                                subscriber.onError(new Exception(String.format("GetTitleListRsp retCode = %d", rsp.getRetCode())));
                                return;
                            }

                            for (Live2Proto.TitleInfo protoInfo : rsp.getTitleInfoList()) {
                                if (protoInfo.getSource() == mSource) {
                                    try {
                                        titleModel = new TitleViewModel(protoInfo);
                                    } catch (Exception e) {
                                        MyLog.e(TAG, e);
                                    }
                                    break;
                                }
                            }
                        }
                        if (titleModel == null) {
                            subscriber.onError(new Exception("loadTitle titleModel is null"));
                            return;
                        }

                        subscriber.onNext(titleModel);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mView.<TitleViewModel>bindLifecycle())
                .subscribe(new Action1<TitleViewModel>() {
                    @Override
                    public void call(TitleViewModel model) {
                        mCacheModel = model;
                        TitleCache.getInstance().setTitleModel(mCacheModel);
                        mView.fillTitle(mCacheModel.nextTitle());
                        mView.updateControlTitleArea(true);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                        mView.updateControlTitleArea(false);
                    }
                });
    }

    public void changeTitle() {
        if (mCacheModel == null) {
            loadTitle();
            return;
        }
        mView.fillTitle(mCacheModel.nextTitle());
    }
}
