package com.wali.live.watchsdk.personalcenter.relation.presenter;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.base.utils.CommonUtils;
import com.mi.live.data.data.UserListData;
import com.trello.rxlifecycle.FragmentEvent;
import com.wali.live.utils.relation.RelationUtils;
import com.wali.live.watchsdk.personalcenter.relation.contact.FollowListContact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zhujianning on 18-6-21.
 */

public class FollowListPresenter extends RxLifeCyclePresenter implements FollowListContact.Ipresenter{
    private static final String TAG = "FollowListPresenter";

    private FollowListContact.Iview mIview;
    private Subscription mSubscribe;

    public FollowListPresenter(FollowListContact.Iview iview) {
        this.mIview = iview;
    }

    @Override
    public void loadFollowList(final long uid) {
        mSubscribe = Observable.create(new Observable.OnSubscribe<List<UserListData>>() {
            @Override
            public void call(Subscriber<? super List<UserListData>> subscriber) {
                List<Object> dataList = new ArrayList<>();
                List<UserListData> resultList = new ArrayList<>();
                int total = RelationUtils.loadFollowingData(uid, RelationUtils.LOADING_FOLLOWING_PAGE_COUNT, 0, dataList, false);
                if (total > 0) {
                    resultList = sortFollowingData(dataList);
                } else {
                    subscriber.onError(new Exception("no data"));
                }
                subscriber.onNext(resultList);
                subscriber.onCompleted();
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
                        mIview.loadFollowListSuccess(dataList);
                    }
                });
    }

    private List<UserListData> sortFollowingData(List<Object> list) {

        List<UserListData> resultList = new ArrayList<UserListData>();
        for (Object object : list) {
            if (object instanceof UserListData) {
                resultList.add((UserListData) object);
            }
        }

        //jdk 7sort有可能报错，
        //加上这句
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        // 按照字母排序的
        Collections.sort(resultList, new Comparator<UserListData>() {
            @Override
            public int compare(UserListData lb, UserListData rb) {
                return CommonUtils.sortName(lb.userNickname, rb.userNickname);

            }
        });
        return resultList;
    }

    @Override
    public void destroy() {
        super.destroy();
        if(mSubscribe != null && !mSubscribe.isUnsubscribed()) {
            mSubscribe.unsubscribe();
        }
    }
}
