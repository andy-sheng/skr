package com.wali.live.watchsdk.ranking;

import android.view.View;

import com.base.log.MyLog;
import com.wali.live.proto.RankProto;
import com.wali.live.proto.RankProto.RankUser;
import com.wali.live.utils.relation.RelationUtils;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @module 本场星票排行榜页面
 */
public class CurrentRankingFragment extends BaseRankingFragment {
    private static final String TAG = CurrentRankingFragment.class.getSimpleName();

    public CurrentRankingFragment() {
    }

    public CurrentRankingFragment(int mTicketNum, int mStartTicket, long mUuid, String mLiveId) {
        super(mTicketNum, mStartTicket, mUuid, mLiveId);
        mIsFollowSysRotateForViewPagerFragment = true;
    }

    @Override
    protected void loadMoreData(long id, final String liveId, final int pageCount, final int offset) {
        if (mIsLoading) {
            return;
        }
        preLoadData();

        Observable
                .create(new Observable.OnSubscribe<List<RankProto.RankUser>>() {
                    @Override
                    public void call(Subscriber<? super List<RankUser>> subscriber) {
                        mIsLoading = true;
                        subscriber.onNext(RelationUtils.getRankRoomList(liveId, pageCount, offset));
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(this.<List<RankUser>>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<RankUser>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "getTicketListResponse error");
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<RankUser> result) {
                        mIsLoading = false;
                        mLoadingView.setVisibility(View.GONE);
                        mResultList.addAll(result);
                        updateView(result);
                    }
                });


        Observable
                .create(new Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {
                        subscriber.onNext(RelationUtils.getRankRoomTicket(liveId));
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(this.<Integer>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "getTicketListResponse error");
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Integer result) {
                        setRoomTicket(result);
                    }
                });
    }
}
