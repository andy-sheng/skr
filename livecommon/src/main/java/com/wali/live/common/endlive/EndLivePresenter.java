package com.wali.live.common.endlive;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.query.LiveRoomQuery;
import com.mi.live.data.relation.RelationApi;
import com.wali.live.proto.RelationProto;
import com.wali.live.proto.RoomRecommend;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by jiyangli on 16-7-6.
 */
public abstract class EndLivePresenter {
    protected IUserEndLiveView mEndLiveView;
    protected IEndLiveModel mEndLiveModel;

    public EndLivePresenter(IUserEndLiveView view, Bundle bundle) {
        mEndLiveView = view;
        mEndLiveModel = new EndLiveBean();
        mEndLiveModel.initData(bundle);
    }

    /**
     * 获取主播ID
     *
     * @return
     */

    public long getOwnerId() {
        return mEndLiveModel.getOwnerId();
    }

    /**
     * 获取主播认证
     *
     * @return
     */
    public int getOwnerCertType() {
        return mEndLiveModel.getOwnerCertType();
    }

    /**
     * 获取时间戳
     *
     * @return
     */
    public long getAvatarTs() {
        return mEndLiveModel.getAvaTarTs();
    }

    /**
     * 获取主播名称
     *
     * @return
     */
    public String getNickName() {
        return mEndLiveModel.getNickName();
    }

    /**
     * 获取观看人数
     *
     * @return
     */
    public int getViewerCount() {
        return mEndLiveModel.getViewerCount();
    }

    /**
     * 是否已关注
     *
     * @return
     */
    public boolean isFocused() {
        return mEndLiveModel.isFocused();
    }

    public void popFragment() {
        mEndLiveView.popFragment();
    }

    public abstract void startWatchActivity(Activity activity, RoomRecommend.RecommendRoom roomData, int position);

    public boolean isMyReplay() {
        return UserAccountManager.getInstance().getUuidAsLong() == mEndLiveModel.getOwnerId();
    }

    /**
     * 关注主播
     */
    public void followAvatar() {
        RelationApi.follow(UserAccountManager.getInstance().getUuidAsLong(), mEndLiveModel.getOwnerId(), mEndLiveModel.getRoomId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mEndLiveView.<RelationProto.FollowResponse>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RelationProto.FollowResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(RelationProto.FollowResponse result) {
                        mEndLiveView.followResult(result.getCode() == 0);
                    }
                });
    }

    /**
     * 获取推荐房间列表
     */
    public void getRoomRecommendList() {
        LiveRoomQuery.getRoomRecommendList(mEndLiveModel.getRoomId(), mEndLiveModel.getOwnerId(), UserAccountManager.getInstance().getUuidAsLong())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<RoomRecommend.RecommendRoom>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<RoomRecommend.RecommendRoom> result) {
                        mEndLiveView.getRoomListResult(result);
                    }
                });
    }

    public void sendShowCommend(long id1, long id2, long id3, long id4) {

        String key = String.format(StatisticsKey.KEY_END_LIVE_SHOW, mEndLiveModel.getUuid(), mEndLiveModel.getOwnerId(), id1, id2, id3, id4);
        if (TextUtils.isEmpty(key)) {
            return;
        }

        StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, key, 1);
    }

    public void sendFollowCommend() {

        String key = String.format(StatisticsKey.KEY_END_LIVE_FOLLOW, mEndLiveModel.getUuid(), mEndLiveModel.getOwnerId());
        if (TextUtils.isEmpty(key)) {
            return;
        }

        //构造一个StatisticsItem, 并上传
        StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, key, 1);
    }

    public void sendChatCommend() {

        String key = String.format(StatisticsKey.KEY_END_LIVE_CHAT, mEndLiveModel.getUuid(), mEndLiveModel.getOwnerId());
        if (TextUtils.isEmpty(key)) {
            return;
        }

        StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, key, 1);
    }

    public void sendCloseCommend() {

        String key = String.format(StatisticsKey.KEY_END_LIVE_CLOSE, mEndLiveModel.getUuid(), mEndLiveModel.getOwnerId());
        if (TextUtils.isEmpty(key)) {
            return;
        }

        StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, key, 1);
    }

    public void sendRecommendCommend(int position, long id1, long zuid) {

        String key = String.format(StatisticsKey.KEY_END_LIVE_RECOMMEND, mEndLiveModel.getUuid(), position, id1, zuid);
        if (TextUtils.isEmpty(key)) {
            return;
        }

        StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, key, 1);
    }

    public void sendClickAvatarCommend(long zuid) {

        String key = String.format(StatisticsKey.KEY_END_LIVE_AVATAR, zuid);
        if (TextUtils.isEmpty(key)) {
            return;
        }

        StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, key, 1);
    }

    public void sendHomePageCommend() {

        String key = String.format(StatisticsKey.KEY_END_LIVE_HOMEPAGE, mEndLiveModel.getZuid());
        if (TextUtils.isEmpty(key)) {
            return;
        }

        StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, key, 1);
    }
}
