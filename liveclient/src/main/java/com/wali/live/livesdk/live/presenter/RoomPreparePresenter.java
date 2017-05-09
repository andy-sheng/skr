package com.wali.live.livesdk.live.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.utils.rx.RxRetryAssist;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.manager.LiveRoomCharacterManager;
import com.mi.live.data.manager.UserInfoManager;
import com.wali.live.livesdk.live.api.TitleListRequest;
import com.wali.live.livesdk.live.presenter.cache.TitleCache;
import com.wali.live.livesdk.live.presenter.view.IRoomPrepareView;
import com.wali.live.livesdk.live.presenter.viewmodel.TitleViewModel;
import com.wali.live.proto.Live2Proto;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveProto;
import com.wali.live.watchsdk.component.presenter.WidgetPresenter;

import java.util.List;

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
                .compose(mView.<Integer>bindLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
                .compose(mView.<TitleViewModel>bindLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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

    public void loadDailyTask() {
        WidgetPresenter.getRoomAttachment("", UserAccountManager.getInstance().getUuidAsLong(), 0)
                .compose(mView.<LiveProto.GetRoomAttachmentRsp>bindLifecycle())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RxRetryAssist(3, 5, true)) // 重试3次，间隔5秒
                .subscribe(new Action1<LiveProto.GetRoomAttachmentRsp>() {
                    @Override
                    public void call(LiveProto.GetRoomAttachmentRsp getRoomAttachmentRsp) {
                        if (!com.base.utils.Constants.isGooglePlayBuild && !com.base.utils.Constants.isIndiaBuild) {
                            int liveType = getSourceLiveType();
                            if (liveType != LiveManager.TYPE_LIVE_PRIVATE && liveType != LiveManager.TYPE_LIVE_TOKEN) {
                                LiveCommonProto.NewWidgetInfo newWidgetInfo = getRoomAttachmentRsp.getNewWidgetInfo();
                                if (newWidgetInfo != null) {
                                    MyLog.v(TAG + "  NewWidgetInfo " + newWidgetInfo.getWidgetItemCount());
                                    List<LiveCommonProto.NewWidgetItem> newWidgetItems = newWidgetInfo.getWidgetItemList();
                                    if (newWidgetItems != null && newWidgetItems.size() > 0) {
                                        for (int i = 0; i < newWidgetItems.size(); i++) {
                                            LiveCommonProto.NewWidgetItem info = newWidgetItems.get(i);
                                            switch (info.getPosition()) {
                                                case 0://左上角
                                                    List<LiveCommonProto.NewWidgetUnit> data = info.getWidgetUintList();
                                                    //第一张图片
                                                    if (data != null && data.size() > 0) {
                                                        LiveCommonProto.NewWidgetUnit unit = data.get(0);
                                                        mView.setDailyTaskUnit(unit);
                                                    }
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG + throwable.getMessage());
                    }
                });
    }

    private int getSourceLiveType() {
        switch (mSource) {
            case TitleViewModel.SOURCE_NORMAL:
                return LiveManager.TYPE_LIVE_PUBLIC;
            case TitleViewModel.SOURCE_GAME:
                return LiveManager.TYPE_LIVE_GAME;
            default:
                return LiveManager.TYPE_LIVE_PUBLIC;
        }
    }
}
