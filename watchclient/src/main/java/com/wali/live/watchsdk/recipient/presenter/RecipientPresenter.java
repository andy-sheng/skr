package com.wali.live.watchsdk.recipient.presenter;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.mvp.IRxView;
import com.base.pinyin.PinyinUtils;
import com.base.utils.CommonUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.data.UserListData;
import com.mi.live.data.manager.LiveRoomCharacterManager;
import com.wali.live.utils.relation.RelationUtils;

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

/**
 * Created by zhangyuehuan on 2017/11/6.
 *
 * @module 选人的数据拉取
 */
public class RecipientPresenter extends BaseRxPresenter<RecipientPresenter.IView> {
    private final String TAG = "RecipientPresenter";
    public static final int ITEM_TYPE_FOLLOWING = 0;   //关注的item
    public static final int ITEM_TYPE_MANAGER = 1;     //管理员

    private Subscription mSubscription;
    private Subscription mSearchSubscription;
    private int mItemNormalType;
    private int mOffset = 0;
    private int mTotal = 0;
    private List<Object> mDataList = new ArrayList<>();

    private RecipientPresenter(IView view) {
        super(view);
    }

    public RecipientPresenter(int itemNormalType, IView view) {
        this(view);
        mItemNormalType = itemNormalType;
    }

    public void loadDataFromServer(final long uuid, final boolean bothWay) {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        MyLog.w(TAG, "loadDataFromServer mOffset=" + mOffset);
        mSubscription = Observable.just("")
                .observeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<List<Object>>>() {
                    @Override
                    public Observable<List<Object>> call(String item) {

                        switch (mItemNormalType) {
                            case ITEM_TYPE_FOLLOWING: {
                                List<Object> dataList = new ArrayList<>();
                                mTotal = RelationUtils.loadFollowingData(uuid,
                                        RelationUtils.LOADING_FOLLOWING_PAGE_COUNT, mOffset,
                                        dataList, bothWay);
                                dataList = sortData(dataList);
                                return Observable.just(dataList);
                            }
                            case ITEM_TYPE_MANAGER: {
                                List<Object> dataList = new ArrayList<>();
                                mTotal = RelationUtils.loadFollowingData(uuid,
                                        RelationUtils.LOADING_FOLLOWING_PAGE_COUNT, mOffset,
                                        dataList, false);
                                dataList = sortData(dataList);
                                return Observable.just(dataList);
                            }
                        }
                        return Observable.just(null);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mView.<List<Object>>bindLifecycle())
                .subscribe(new Action1<List<Object>>() {
                    @Override
                    public void call(List<Object> dataList) {
                        if (dataList != null) {
                            mOffset = dataList.size();
                            if (mItemNormalType == ITEM_TYPE_MANAGER) {
                                List<Object> tempList = new ArrayList<>(dataList);
                                for (Object obj : tempList) {
                                    UserListData item = (UserListData) obj;
                                    if (LiveRoomCharacterManager.getInstance().isManager(item.userId)) {
                                        dataList.remove(obj);
                                    }
                                }
                            }
                            mDataList.addAll(dataList);
                            if (mView != null) {
                                mView.notifyDataSetChanged(mDataList);
                            }
                            if (mOffset < mTotal) {
                                loadDataFromServer(UserAccountManager.getInstance()
                                        .getUuidAsLong(), bothWay);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "loadDataFromServer failed=" + throwable);
                    }
                });
    }

    private List<Object> sortData(List<Object> list) {
        List<UserListData> mDataListTemp = new ArrayList<>();
        for (Object object : list) {
            mDataListTemp.add((UserListData) object);
        }
        // 按照字母排序的
        Collections.sort(mDataListTemp, new Comparator<UserListData>() {
            @Override
            public int compare(UserListData lb, UserListData rb) {
                return CommonUtils.sortName(lb.userNickname, rb.userNickname);

            }
        });
        list.clear();
        list.addAll(mDataListTemp);
        return list;
    }

    public void doSearch(final String key) {
        if (TextUtils.isEmpty(key)) {
            if (mView != null) {
                mView.notifyDataSetChanged(mDataList);
            }
            return;
        }
        MyLog.w(TAG, "doSearch key=" + key);
        if (mDataList != null && mDataList.size() > 0) {
            if (mSearchSubscription != null && !mSearchSubscription.isUnsubscribed()) {
                mSearchSubscription.unsubscribe();
            }
            mSearchSubscription = Observable.just("")
                    .observeOn(Schedulers.io())
                    .flatMap(new Func1<String, Observable<List<Object>>>() {
                        @Override
                        public Observable<List<Object>> call(String item) {
                            List<Object> searchDataSource = new ArrayList<>();
                            for (Object o : mDataList) {
                                UserListData u = (UserListData) o;
                                if ((u.userId + "").contains(key) || u.userNickname.contains(key)
                                        || PinyinUtils.hanziToPinyin(u.userNickname).contains(key)) {
                                    searchDataSource.add(o);
                                }
                            }
                            return Observable.just(searchDataSource);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(mView.<List<Object>>bindLifecycle())
                    .subscribe(new Action1<List<Object>>() {
                        @Override
                        public void call(List<Object> searchResultList) {
                            if (mView != null) {
                                mView.notifyDataSetChanged(searchResultList);
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            MyLog.e(TAG, "doSearch failed=" + throwable);
                        }
                    });
        }
    }

    public interface IView extends IRxView {
        /**
         * 通知加载数据
         */
        void notifyDataSetChanged(List<Object> list);
    }
}
