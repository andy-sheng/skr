package com.wali.live.watchsdk.ranking;

import android.view.View;

import com.base.log.MyLog;
import com.wali.live.proto.RankProto;
import com.wali.live.utils.relation.RelationUtils;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @module 总榜星票排行榜页面
 */
public class TotleRankingFragment extends BaseRankingFragment {
    private static final String TAG = TotleRankingFragment.class.getSimpleName();

    public TotleRankingFragment(int mTicketNum, long mUuid, String mLiveId) {
        super(mTicketNum, mUuid, mLiveId);
        mIsFollowSysRotateForViewPagerFragment = true;
    }

    @Override
    protected void loadMoreData(final long id, String mLiveId, final int pageCount, final int offset) {
        if (mIsLoading) {
            return;
        }
        preLoadData();

        Observable
                .create(new Observable.OnSubscribe<List<RankProto.RankUser>>() {
                    @Override
                    public void call(Subscriber<? super List<RankProto.RankUser>> subscriber) {
                        mIsLoading = true;
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
                        MyLog.e(TAG, "getTicketListResponse error");
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<RankProto.RankUser> result) {
                        mIsLoading = false;
                        mLoadingView.setVisibility(View.GONE);
                        mResultList.addAll(result);
                        updateView(result);
                    }
                });
    }

}
