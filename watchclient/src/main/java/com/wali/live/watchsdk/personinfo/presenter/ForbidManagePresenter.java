package com.wali.live.watchsdk.personinfo.presenter;

import android.support.annotation.NonNull;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.base.presenter.Presenter;
import com.mi.live.data.api.BanSpeakerUtils;
import com.mi.live.data.api.relation.RelationApi;
import com.mi.live.data.manager.LiveRoomCharacterManager;
import com.mi.live.data.user.User;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.manager.WatchRoomCharactorManager;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by yangli on 16-7-8.
 */
public class ForbidManagePresenter implements Presenter {
    private static final String TAG = "ForbidManagePresenter";

    public final static int ERR_CODE_SUCCESS = 0;
    public final static int ERR_CODE_FAILED = -1;

    private RxActivity mRxActivity;
    private WeakReference<IForbidManageView> mForbidManageViewRef;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public ForbidManagePresenter(@NonNull RxActivity rxActivity) {
        MyLog.w(TAG, "ForbidManagePresenter()");
        mRxActivity = rxActivity;
    }

    public void setForbidManageView(IForbidManageView forbidManageView) {
        mForbidManageViewRef = new WeakReference<IForbidManageView>(forbidManageView);
    }

    public void forbidSpeak(final String roomId, final long anchorId, final long userId, final User user) {
        if (null == user) {
            return;
        }
        MyLog.w(TAG, "forbidSpeak roomId=" + roomId + ", anchorId=" + anchorId + ", userId=" + userId + ", targetId=" + user.getUid());
        Observable.just(0)
                .map(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return BanSpeakerUtils.banSpeaker(roomId, anchorId, userId, user.getUid());
                    }
                })
                .compose(mRxActivity.<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.from(mExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        if (result) {
                            if (anchorId == userId) {
                                LiveRoomCharacterManager.getInstance().banSpeaker(user, true);
                            } else {
                                WatchRoomCharactorManager.getInstance().banSpeaker(user.getUid(), true);
                            }
                        }
                        IForbidManageView kickManageView;
                        if ((kickManageView = mForbidManageViewRef.get()) != null) {
                            kickManageView.onForbidSpeakDone(user, result ? ERR_CODE_SUCCESS : ERR_CODE_FAILED);
                        }
                        MyLog.d(TAG, "forbidSpeak result=" + result);

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "forbidSpeak exception=" + throwable);
                        IForbidManageView kickManageView;
                        if ((kickManageView = mForbidManageViewRef.get()) != null) {
                            kickManageView.onForbidSpeakDone(user, ERR_CODE_FAILED);
                        }
                    }
                });
    }

    public void cancelForbidSpeak(final String roomId, final long anchorId, final long userId, final long targetId) {
        MyLog.w(TAG, "cancelForbidSpeak roomId=" + roomId + ", anchorId=" + anchorId + ", userId=" + userId + ", targetId=" + targetId);
        Observable.just(0)
                .map(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return BanSpeakerUtils.cancelBanSpeaker(roomId, anchorId, targetId);
                    }
                })
                .compose(mRxActivity.<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.from(mExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        if (result) {
                            if (anchorId == userId) {
                                User user = new User();
                                user.setUid(targetId);
                                LiveRoomCharacterManager.getInstance().banSpeaker(user, false);
                            } else {
                                WatchRoomCharactorManager.getInstance().banSpeaker(targetId, false);
                            }
                        }
                        IForbidManageView kickManageView;
                        if ((kickManageView = mForbidManageViewRef.get()) != null) {
                            kickManageView.onCancelForbidSpeakDone(targetId, result ? ERR_CODE_SUCCESS : ERR_CODE_FAILED);
                        }
                        MyLog.d(TAG, "cancelForbidSpeak result=" + result);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "cancelForbidSpeak exception=" + throwable);
                        IForbidManageView kickManageView;
                        if ((kickManageView = mForbidManageViewRef.get()) != null) {
                            kickManageView.onCancelForbidSpeakDone(targetId, ERR_CODE_FAILED);
                        }
                    }
                });
    }

    /**
     * 踢人
     *
     * @param roomId     房间ID
     * @param anchorId   主播ID
     * @param operatorId 操作人
     * @param kickedId   被踢人ID
     * @return
     */
    public void kickViewer(final String roomId, final long anchorId, final long operatorId, final long kickedId) {
        MyLog.w(TAG, "kickViewer roomId=" + roomId + ", anchorId=" + anchorId + ", operatorId=" + operatorId + ", kickedId=" + kickedId);
        Observable.just(0)
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        return RelationApi.kickViewer(roomId, anchorId, operatorId, kickedId);
                    }
                })
                .compose(mRxActivity.<Integer>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.from(mExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer result) {
                        IForbidManageView kickManageView;
                        if ((kickManageView = mForbidManageViewRef.get()) != null) {
                            kickManageView.onkickViewerDone(kickedId, result);
                        }
                        MyLog.d(TAG, "kickViewer result=" + result);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "kickViewer exception=" + throwable);
                        IForbidManageView kickManageView;
                        if ((kickManageView = mForbidManageViewRef.get()) != null) {
                            kickManageView.onkickViewerDone(kickedId, ERR_CODE_FAILED);
                        }
                    }
                });
    }

    /**
     * 拉黑
     *
     * @param anchorId
     * @param targetId
     */
    public void blockViewer(final long anchorId, final long targetId) {
        MyLog.w(TAG, "block targetId=" + targetId + " anchorId=" + anchorId);
        Observable.just(0)
                .map(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return RelationApi.block(anchorId, targetId);
                    }
                })
                .compose(mRxActivity.<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.from(mExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        IForbidManageView kickManageView;
                        if ((kickManageView = mForbidManageViewRef.get()) != null) {
                            kickManageView.onBlockViewer(targetId, result ? ERR_CODE_SUCCESS : ERR_CODE_FAILED);
                        }
                        MyLog.d(TAG, "block result=" + result);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        IForbidManageView kickManageView;
                        if ((kickManageView = mForbidManageViewRef.get()) != null) {
                            kickManageView.onBlockViewer(targetId, ERR_CODE_FAILED);
                        }
                        MyLog.e(TAG, "block exception=" + throwable);
                    }
                });
    }

    @Override
    public void start() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void destroy() {
        mExecutor.shutdown();
    }

    public interface IForbidManageView {
        void onForbidSpeakDone(User user, int errCode);

        void onCancelForbidSpeakDone(long targetId, int errCode);

        void onkickViewerDone(long targetId, int errCode);

        void onBlockViewer(long targetId, int errCode);
    }

    public interface IForbidManageProvider {
        ForbidManagePresenter provideForbidManagePresenter();
    }
}
