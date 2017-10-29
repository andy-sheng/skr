package com.wali.live.watchsdk.personinfo.presenter;

import android.os.Bundle;
import android.os.SystemClock;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.relation.RelationApi;
import com.mi.live.data.event.GetUserInfoAndUnpdateConversationEvent;
import com.mi.live.data.manager.UserInfoManager;
import com.mi.live.data.user.User;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.dao.RelationDaoAdapter;
import com.wali.live.proto.RankProto;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.eventbus.FollowStatEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by wangmengjie on 17-8-24.
 */
public class FloatInfoPresenter extends BaseRxPresenter<IFloatInfoView> {

    public static final String EXTRA_IN_UUID = "uuid";
    public static final String EXTRA_IN_OWNER_UUID = "owner_uuid";
    public static final String EXTRA_IN_LIVE_ENTER_TIME = "live_enter_time";
    public static final String EXTRA_IN_ROOM_ID = "room_id";
    public static final String EXTRA_IN_LIVE_URL = "live_url";

    //从其他页面bundle传递过来的
    private long mUserUid;
    private long mOwnerUid;
    private long mEnterTime;
    private String mRoomId;
    private String mLiveUrl;

    private IFloatInfoView mView;

    private User mUser;
    private RankProto.RankUser mTopOneUser;

    private Subscription mGetUserInfoSubscription;
    private Subscription mFollowOrUnFollowSubscription;

    @Override
    protected String getTAG() {
        return "FloatInfoPresenter";
    }

    public FloatInfoPresenter(IFloatInfoView view, Bundle bundle) {
        super(view);
        initData(bundle);
    }

    public void initData(Bundle bundle) {
        if (bundle != null) {
            mUserUid = bundle.getLong(FloatInfoPresenter.EXTRA_IN_UUID);
            mOwnerUid = bundle.getLong(FloatInfoPresenter.EXTRA_IN_OWNER_UUID);
            mEnterTime = bundle.getLong(FloatInfoPresenter.EXTRA_IN_LIVE_ENTER_TIME);
            mRoomId = bundle.getString(FloatInfoPresenter.EXTRA_IN_ROOM_ID);
            mLiveUrl = bundle.getString(FloatInfoPresenter.EXTRA_IN_LIVE_URL);
        }
        if (mGetUserInfoSubscription != null && !mGetUserInfoSubscription.isUnsubscribed()) {
            mGetUserInfoSubscription.unsubscribe();
        }
        mGetUserInfoSubscription = Observable
                .create(new Observable.OnSubscribe<User>() {
                    @Override
                    public void call(Subscriber<? super User> subscriber) {
                        if (mUserUid >= 0) {
                            mUser = UserInfoManager.getUserInfoByUuid(mUserUid, false);
                            List<RankProto.RankUser> rankUsers =
                                    RelationApi.getTicketListResponse(mUserUid, 1, 0);
                            if (rankUsers != null && !rankUsers.isEmpty()) {
                                mTopOneUser = rankUsers.get(0);
                            }
                        }
                        if (mUser != null) {
                            AvatarUtils.updateMyFollowAvatarTimeStamp(mUser.getUid(), mUser.getAvatar());
                            EventBus.getDefault().post(new GetUserInfoAndUnpdateConversationEvent(
                                    mUser.getUid(),
                                    mUser.isBlock(),
                                    0,
                                    mUser.getCertificationType(),
                                    mUser.getNickname()));
                            subscriber.onNext(mUser);
                            subscriber.onCompleted();
                        } else {
                            subscriber.onError(new Throwable("user == null"));
                        }

                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<User>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        MyLog.d(TAG, "onNext");
                        if (mUser == null) {
                            MyLog.w(TAG, "user == null");
                            return;
                        }
                        mView.refreshAllViews(mUser, mTopOneUser);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public long getOwnerUid() {
        return mOwnerUid;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public void handleFollow() {
        if (mUserUid == mOwnerUid && mOwnerUid != UserAccountManager.getInstance().getUuidAsLong()) {
            EventBus.getDefault().post(new FollowStatEvent(StatisticsKey.KEY_FLOATING_NAME_FOLLOW));
        }
        if (mUser == null) {
            return;
        }
        if (!mUser.isFocused()) {
            followOrUnFollow();
        } else {
            mView.popUnFollowDialog();
        }
    }

    public void followOrUnFollow() {
        if (mFollowOrUnFollowSubscription != null && !mFollowOrUnFollowSubscription.isUnsubscribed()) {
            mFollowOrUnFollowSubscription.unsubscribe();
        }
        mFollowOrUnFollowSubscription = Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        if (mUser == null) {
                            subscriber.onError(new Throwable("user == null"));
                        }
                        Boolean result;
                        if (!mUser.isFocused()) {   //关注
                            MyLog.d(TAG, "follow");
                            result = RelationApi.follow2(
                                    UserAccountManager.getInstance().getUuidAsLong(),
                                    mUser.getUid(),
                                    mUser.getUid() == mOwnerUid ? mRoomId : null)
                                    >= RelationApi.FOLLOW_STATE_SUCCESS;
                            if (result) {
                                mUser.setIsFocused(true);
                                mUser.setFansNum(mUser.getFansNum() + 1);
                                StatisticsAlmightyWorker.getsInstance().recordDelay(
                                        StatisticsKey.AC_APP,
                                        StatisticsKey.KEY,
                                        StatisticsKey.KEY_LIVE_ROOM_FLOAT_FOLLOW_BUTTON + mUser.getUid());
                                RelationDaoAdapter.getInstance().insertRelation(mUser.getRelation());

                                //多久关注主播点
                                if (mUserUid == mOwnerUid && mOwnerUid != 0 && mEnterTime > 0) {
                                    StatisticsAlmightyWorker.getsInstance().recordDelay(
                                            StatisticsKey.STATISTICS_FOLLOW_ANCHOR_AC,
                                            StatisticsKey.KEY,
                                            StatisticsKey.STATISTICS_FOLLOW_ANCHOR_CARD_KEY,
                                            StatisticsKey.STATISTICS_FOLLOW_ANCHOR_USERID,
                                            String.valueOf(mOwnerUid),
                                            StatisticsKey.STATISTICS_FOLLOW_ANCHOR_LIVEID,
                                            mRoomId,
                                            StatisticsKey.STATISTICS_FOLLOW_ANCHOR_DURATION,
                                            String.valueOf(SystemClock.elapsedRealtime() - mEnterTime));
                                }
                            }
                        } else {  //取消关注
                            MyLog.d(TAG, "un follow");
                            result = RelationApi.unFollow(
                                    UserAccountManager.getInstance().getUuidAsLong(),
                                    mUser.getUid());
                            if (result) {
                                mUser.setIsFocused(false);
                                mUser.setFansNum(mUser.getFansNum() - 1);
                                RelationDaoAdapter.getInstance().deleteRelation(mUser.getUid());
                            }
                        }
                        subscriber.onNext(result);
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<Boolean>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        if (result) {
                            mView.refreshUserInfo();
                        } else {
                            if (RelationApi.sErrorCode == ErrorCode.CODE_RELATION_BLACK) {
                                ToastUtils.showToast(GlobalData.app(), GlobalData.app().getString(R.string.setting_black_follow_hint));
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }
}
