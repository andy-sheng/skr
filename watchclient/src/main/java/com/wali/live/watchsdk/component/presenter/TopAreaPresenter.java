package com.wali.live.watchsdk.component.presenter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;

import com.base.activity.BaseSdkActivity;
import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.relation.RelationApi;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.push.model.BarrageMsgExt;
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
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.view.TopAreaView;
import com.wali.live.watchsdk.fans.FansPagerFragment;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.request.GetGroupDetailRequest;

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

    private RoomBaseDataModel mMyRoomData;
    private boolean mIsLive;
    private FansGroupDetailModel mFansGroupDetailModel;

    private Subscription mFollowSubscription;
    private Subscription mRefViewersSubscription;
    protected Subscription mSubscription;

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
        mMyRoomData = roomData;
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
        UserActionEvent.post(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, mMyRoomData.getUid(), null);
    }

    /***
     *  TODO 获取粉丝团信息 -- 多处调用,后续优化
     */
    private void getGroupDetailFromServer() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        mSubscription = Observable.just(0)
                .map(new Func1<Object, FansGroupDetailModel>() {
                    @Override
                    public FansGroupDetailModel call(Object object) {
                        if (mMyRoomData == null || mMyRoomData.getUid() <= 0) {
                            MyLog.e(TAG, "getGroupDetail null");
                            return null;
                        }
                        VFansProto.GroupDetailRsp rsp = new GetGroupDetailRequest(mMyRoomData.getUid()).syncRsp();
                        MyLog.v(TAG, "getGroupDetailFromServer rsp=" + rsp);
                        if (rsp != null && rsp.getErrCode() == ErrorCode.CODE_SUCCESS) {
                            return new FansGroupDetailModel(rsp);
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FansGroupDetailModel>() {
                    @Override
                    public void call(FansGroupDetailModel groupDetailModel) {
                        if (groupDetailModel != null) {
                            mFansGroupDetailModel = groupDetailModel;
                            mView.setFansGroupModel(groupDetailModel);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "getGroupDetail failed=" + throwable);
                    }
                });
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
                mMyRoomData.getUid(), mMyRoomData.getRoomId())
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
                mMyRoomData.getUid(), mMyRoomData.getTicket(), mMyRoomData.getRoomId());
    }

    @Override
    public void showFansFragment() {
        //TODO 粉丝团的信息获取暂时没有，ui写完再加
        final Context context = mView.getRealView().getContext();
        if (context instanceof BaseSdkActivity) {
            FansPagerFragment.openFragment((BaseSdkActivity) context, mMyRoomData.getNickName(),
                    mMyRoomData.getUid(), mMyRoomData.getRoomId(), 5);
        }
    }

    @Override
    public void syncData() {
        MyLog.d(TAG, "syncData");
        if (mFansGroupDetailModel == null || mFansGroupDetailModel.getZuid() != mMyRoomData.getUid()) {
            //这里加条件限制是因为观看端RoomDataChangeEvent.TYPE_CHANGE_USER_INFO_COMPLETE这个事件会多次触发
            getGroupDetailFromServer();
        }
        mView.setWaterMarkView(mMyRoomData);
        mView.updateTicketAndViewerCount(mMyRoomData.getTicket(),mMyRoomData.getInitTicket(), mMyRoomData.getViewerCnt());
        mView.updateAnchorInfo(mMyRoomData.getUid(), mMyRoomData.getAvatarTs(),
                mMyRoomData.getCertificationType(), mMyRoomData.getLevel(), mMyRoomData.getNickName());
        mView.showManager(mIsLive);
        mView.initViewers(mMyRoomData.getViewersList());
        initFollowAndLink();
    }

    private void initFollowAndLink() {
        if (mView == null) {
            return;
        }
        if (!mMyRoomData.isFocused() && mMyRoomData.getUid() != UserAccountManager.getInstance().getUuidAsLong()) {
            mView.showFollowBtn(true, false);
        } else {
            mView.showFollowBtn(false, false);
        }
        //TODO 连麦情况下的准备工作
    }

    @Override
    public void postAvatarEvent(int eventTypeRequestLookMoreViewer, int itemCount) {
        if (mMyRoomData.getViewerCnt() > itemCount) {
            UserActionEvent.post(eventTypeRequestLookMoreViewer, mMyRoomData, null);
        }
    }

    @Override
    public void reset() {
        mView.cancelAnimator();
        mRefViewersHandler.removeCallbacks(mRefreshViewersRun);
        mView.onLinkMicStopped();
        mFansGroupDetailModel = null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoomDataChangeEvent event) {
        if (mView == null) {
            return;
        }
        if (event.source != mMyRoomData || mView.getRealView().getVisibility() == View.GONE) {
            return;
        }
        switch (event.type) {
            case RoomDataChangeEvent.TYPE_CHANGE_USER_INFO_COMPLETE: {
                mMyRoomData = event.source;
                syncData();
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_TICKET:
            case RoomDataChangeEvent.TYPE_CHANGE_VIEWER_COUNT: {
                mMyRoomData = event.source;
                mView.updateTicketAndViewerCount(mMyRoomData.getTicket(),mMyRoomData.getInitTicket(), mMyRoomData.getViewerCnt());
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_VIEWERS: {
                mMyRoomData = event.source;
                dealViewers();
            }
            break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FollowOrUnfollowEvent event) {
        if (null != event && mMyRoomData != null && mMyRoomData.getUser() != null && mMyRoomData.getUser().getUid() == event.uuid) {
            final User user = mMyRoomData.getUser();
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
            temp.addAll(mMyRoomData.getViewersList());
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
                BarrageMsgExt.MicBeginInfo micBeginInfo = params.getItem(0);
                if (micBeginInfo != null && micBeginInfo.isMicNormal()) {
                    mView.onLinkMicStarted(micBeginInfo.micuid, 0);
                }
                break;
            }
            case MSG_ON_LINK_MIC_STOP: {
                BarrageMsgExt.MicEndInfo micEndInfo = params.getItem(0);
                if (micEndInfo != null && micEndInfo.isMicNormal()) {
                    mView.onLinkMicStopped();
                }
                break;
            }
            default:
                break;
        }
        return false;
    }

}
