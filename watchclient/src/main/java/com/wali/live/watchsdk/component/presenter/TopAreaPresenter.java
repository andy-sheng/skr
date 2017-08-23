package com.wali.live.watchsdk.component.presenter;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;

import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.relation.RelationApi;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.query.model.ViewerModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.mi.live.data.user.User;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.dao.RelationDaoAdapter;
import com.wali.live.event.UserActionEvent;
import com.wali.live.proto.RelationProto;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.view.TopAreaView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.BaseSdkController.MSG_ON_LINK_MIC_START;
import static com.wali.live.component.BaseSdkController.MSG_ON_LINK_MIC_STOP;

/**
 * Created by wangmengjie on 2017/08/03.
 *
 * @module 顶部view表现
 */
public class TopAreaPresenter extends BaseSdkRxPresenter<TopAreaView.IView>
        implements TopAreaView.IPresenter {
    private static final String TAG = "TopAreaPresenter";

    private RoomBaseDataModel mRoomDataModel;
    private boolean mIsLive;

    private Subscription mFollowSubscription;
    private Subscription mRefViewersSubscription;

    //刷新观众头像
    private Handler mRefViewersHandler = new Handler();

    @Override
    protected String getTAG() {
        return TAG;
    }

    public TopAreaPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel roomData,
            boolean isLive) {
        super(controller);
        mRoomDataModel = roomData;
        mIsLive = isLive;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_LINK_MIC_START);
        registerAction(MSG_ON_LINK_MIC_STOP);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (mView != null) {
            mView.cancelAnimator();
        }
    }

    @Override
    public void getAnchorInfo() {
        UserActionEvent.post(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, mRoomDataModel.getUid(), null);
    }

    @Override
    public void followAnchor() {
        MyLog.d(TAG, "follow anchor");
        if (mFollowSubscription != null &&
                !mFollowSubscription.isUnsubscribed() ||
                !AccountAuthManager.triggerActionNeedAccount(mView.getRealView().getContext())) {
            return;
        }
        mFollowSubscription = RelationApi.follow(UserAccountManager.getInstance().getUuidAsLong(),
                mRoomDataModel.getUid(), mRoomDataModel.getRoomId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RelationProto.FollowResponse>() {
                    @Override
                    public void call(RelationProto.FollowResponse followResponse) {
                        MyLog.d(TAG, "followResultCode = " + followResponse.getCode());
                        mView.onFollowResult(followResponse.getCode());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mView.onFollowResult(-1);
                    }
                });
    }

    @Override
    public void getTicketDetail() {
        UserActionEvent.post(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_TICKET,
                mRoomDataModel.getUid(), mRoomDataModel.getTicket(), mRoomDataModel.getRoomId());
    }

    @Override
    public void syncData() {
        mView.updateTicketAndViewerCount(mRoomDataModel.getTicket(), mRoomDataModel.getViewerCnt());
        mView.updateAnchorInfo(mRoomDataModel.getUid(), mRoomDataModel.getAvatarTs(),
                mRoomDataModel.getCertificationType(), mRoomDataModel.getLevel(), mRoomDataModel.getNickName());
        mView.showManager(mIsLive);
        mView.initViewers(mRoomDataModel.getViewersList());
        initFollowAndLink();
    }

    private void initFollowAndLink() {
        if (mView == null) {
            return;
        }
        if (!mRoomDataModel.isFocused() && mRoomDataModel.getUid() != UserAccountManager.getInstance().getUuidAsLong()) {
            mView.showFollowBtn(true, false);
        } else {
            mView.showFollowBtn(false, false);
        }
        //TODO 连麦情况下的准备工作
    }

    @Override
    public void postAvatarEvent(int eventTypeRequestLookMoreViewer, int itemCount) {
        if (mRoomDataModel.getViewerCnt() > itemCount) {
            UserActionEvent.post(eventTypeRequestLookMoreViewer, mRoomDataModel, null);
        }
    }

    @Override
    public void reset() {
        mView.cancelAnimator();
        mRefViewersHandler.removeCallbacks(mRefreshViewersRun);
        mView.onLinkMicStopped();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoomDataChangeEvent event) {
        if (mView == null) {
            return;
        }
        if (event.source != mRoomDataModel || mView.getRealView().getVisibility() == View.GONE) {
            return;
        }
        switch (event.type) {
            case RoomDataChangeEvent.TYPE_CHANGE_USER_INFO_COMPLETE: {
                mRoomDataModel = event.source;
                syncData();
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_TICKET:
            case RoomDataChangeEvent.TYPE_CHANGE_VIEWER_COUNT: {
                mRoomDataModel = event.source;
                mView.updateTicketAndViewerCount(mRoomDataModel.getTicket(), mRoomDataModel.getViewerCnt());
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_VIEWERS: {
                mRoomDataModel = event.source;
                dealViewers();
            }
            break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FollowOrUnfollowEvent event) {
        if (null != event && mRoomDataModel != null && mRoomDataModel.getUser() != null && mRoomDataModel.getUser().getUid() == event.uuid) {
            final User user = mRoomDataModel.getUser();
            if (user != null && user.getUid() == event.uuid) {
                boolean needUpdateDb = false;

                if (event.eventType == FollowOrUnfollowEvent.EVENT_TYPE_FOLLOW) {
                    user.setIsFocused(true);
                    mView.showFollowBtn(false, true);
                    needUpdateDb = true;
                } else if (event.eventType == FollowOrUnfollowEvent.EVENT_TYPE_UNFOLLOW) {
                    user.setIsFocused(false);
                    mView.showFollowBtn(true, true);
                    needUpdateDb = true;
                } else {
                    MyLog.e(TAG, "type error");
                }
                MyLog.d(TAG, "needUpdateDb=" + needUpdateDb);
                if (needUpdateDb) {
                    // 其后台线程
                    Observable.just(null)
                            .map(new Func1<Object, Object>() {
                                @Override
                                public Object call(Object o) {
                                    return RelationDaoAdapter.getInstance().insertRelation(user.getRelation());
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .subscribe();
                }
            }
        }
    }

    private long mLastUpdateTime = 0;

    private void dealViewers() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastUpdateTime < 3000) {
            mRefViewersHandler.removeCallbacks(mRefreshViewersRun);
            mRefViewersHandler.postDelayed(mRefreshViewersRun, 3000);
        } else {
            mRefViewersHandler.removeCallbacks(mRefreshViewersRun);
            mRefViewersHandler.post(mRefreshViewersRun);
        }

    }

    private Runnable mRefreshViewersRun = new Runnable() {
        @Override
        public void run() {
            ArrayList<ViewerModel> temp = new ArrayList<>();
            temp.addAll(mRoomDataModel.getViewersList());
            mLastUpdateTime = System.currentTimeMillis();
            if (temp.isEmpty()) {
                mView.updateViewers(temp);
            } else {
                if (mRefViewersSubscription != null && !mRefViewersSubscription.isUnsubscribed()) {
                    mRefViewersSubscription.unsubscribe();
                }
                mRefViewersSubscription = Observable.just(temp)
                        .observeOn(Schedulers.computation())
                        .map(new Func1<List<ViewerModel>, List<ViewerModel>>() {
                            @Override
                            public List<ViewerModel> call(List<ViewerModel> temp) {
                                Collections.sort(temp, new Comparator<ViewerModel>() {
                                    @Override
                                    public int compare(ViewerModel lhs, ViewerModel rhs) {
                                        return rhs.getLevel() - lhs.getLevel();
                                    }
                                });
                                return temp;
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(TopAreaPresenter.this.<List<ViewerModel>>bindUntilEvent(PresenterEvent.DESTROY))
                        .subscribe(new Action1<List<ViewerModel>>() {
                            @Override
                            public void call(List<ViewerModel> viewerModels) {
                                mView.updateViewers(viewerModels);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                MyLog.e(TAG, throwable);
                            }
                        });
            }
        }
    };

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_ON_LINK_MIC_START: {
                if (params != null) {
                    mView.onLinkMicStarted((long) params.getItem(0), (long) params.getItem(1));
                }
                break;
            }
            case MSG_ON_LINK_MIC_STOP: {
                mView.onLinkMicStopped();
                break;
            }
            default:
                break;
        }
        return false;
    }

}
