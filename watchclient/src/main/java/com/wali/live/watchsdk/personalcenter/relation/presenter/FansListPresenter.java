package com.wali.live.watchsdk.personalcenter.relation.presenter;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;

import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.data.UserListData;
import com.trello.rxlifecycle.FragmentEvent;
import com.wali.live.proto.RelationProto;
import com.wali.live.utils.relation.RelationUtils;
import com.wali.live.watchsdk.personalcenter.relation.contact.FansListContact;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zhujianning on 18-6-21.
 */

public class FansListPresenter extends RxLifeCyclePresenter implements FansListContact.Ipresenter {
    private static final String TAG = "FansListPresenter";
    private static final int PAGE_SIZE = 20;

    private FansListContact.Iview mIview;

    private boolean mHasMore;
    private Subscription mSubscribe;

    public FansListPresenter(FansListContact.Iview iview) {
        this.mIview = iview;
    }

    @Override
    public void loadFansList(long uuid, final int offset) {
        if (mSubscribe != null && !mSubscribe.isUnsubscribed()) {
            return;
        }

        mSubscribe = RelationUtils.getFollowerListResponse(uuid, PAGE_SIZE, offset)
                .flatMap(new Func1<RelationProto.FollowerListResponse, Observable<List<UserListData>>>() {
                    @Override
                    public Observable<List<UserListData>> call(RelationProto.FollowerListResponse response) {
                        if (response != null && response.getCode() == ErrorCode.CODE_SUCCESS) {
                            MyLog.v(TAG + " FollowerListResponse total:" + response.getTotal());
                            int total = response.getTotal();
                            List<UserListData> list = UserListData.parseList(response);
                            if (total > 0) {
                                mHasMore = offset < total;
                            } else {
                                mHasMore = list.size() == PAGE_SIZE;
                            }
                            return Observable.just(list);
                        } else {
                            return Observable.error(new Exception("get data failed"));
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(this.<List<UserListData>>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<UserListData>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(List<UserListData> dataList) {
                        mIview.loadFansListSuccess(dataList);
                    }
                });
    }

    public boolean isHasMore() {
        return mHasMore;
    }

    @Override
    public void destroy() {
        super.destroy();
        if(mSubscribe != null && !mSubscribe.isUnsubscribed()) {
            mSubscribe.unsubscribe();
        }
    }
}
