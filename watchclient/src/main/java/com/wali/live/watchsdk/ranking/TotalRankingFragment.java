package com.wali.live.watchsdk.ranking;

import android.os.Bundle;
import android.view.View;

import com.base.log.MyLog;
import com.wali.live.proto.RankProto;
import com.wali.live.utils.relation.RelationUtils;
import com.wali.live.watchsdk.ranking.adapter.RankRecyclerViewAdapter;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @module 总榜星票排行榜页面
 */
public class TotalRankingFragment extends BaseRankingFragment {
    public TotalRankingFragment() {
        super();
        mIsFollowSysRotateForViewPagerFragment = true;
    }

    protected void initData(Bundle bundle) {
        super.initData(bundle);
        mTicketNum = bundle.getInt(EXTRA_TICKET_NUM);

        mFragmentType = RankRecyclerViewAdapter.TOTAL_RANK;
    }

    @Override
    protected void loadMoreData(final long id, String mLiveId, final int pageCount, final int offset) {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            return;
        }
        preLoadData();

        mSubscription = Observable
                .create(new Observable.OnSubscribe<List<RankProto.RankUser>>() {
                    @Override
                    public void call(Subscriber<? super List<RankProto.RankUser>> subscriber) {
                        subscriber.onNext(RelationUtils.getTicketListResponse(id, pageCount, offset));
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(this.<List<RankProto.RankUser>>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<RankProto.RankUser>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "getTicketListResponse error", e);
                    }

                    @Override
                    public void onNext(List<RankProto.RankUser> result) {
                        mLoadingView.setVisibility(View.GONE);
                        mResultList.addAll(result);
                        updateView(result);
                    }
                });
    }
}
