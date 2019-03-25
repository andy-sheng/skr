package com.module.msg.friend;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoLocalApi;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.cache.BuddyCache;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.ModuleServiceManager;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.relation.callback.FriendsEmptyCallback;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import io.rong.imkit.R;

public class FriendFragment extends BaseFragment {

    SmartRefreshLayout mRefreshLayout;
    RecyclerView mContentRv;

    FriendAdapter mFriendAdapter;

    LoadService mLoadService;

    private int mOffset = 0; // 偏移量
    private int DEFAULT_COUNT = 15; // 每次拉去最大值
    private boolean hasMore = true; // 是否还有数据

    @Override
    public int initView() {
        return R.layout.friend_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mContentRv = (RecyclerView) mRootView.findViewById(R.id.content_rv);

        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                loadData(mOffset, DEFAULT_COUNT, true);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                loadData(0, DEFAULT_COUNT, false);
            }
        });

        mFriendAdapter = new FriendAdapter(new RecyclerOnItemClickListener<FriendStatusModel>() {
            @Override
            public void onItemClicked(View view, int position, FriendStatusModel model) {
                ModuleServiceManager.getInstance().getMsgService().startPrivateChat(getContext(),
                        String.valueOf(model.getUserID()), model.getNickname());
            }
        });
        mContentRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mContentRv.setAdapter(mFriendAdapter);

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new FriendsEmptyCallback())
                .build();
        mLoadService = mLoadSir.register(mRefreshLayout, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                loadData(mOffset, DEFAULT_COUNT, false);
            }
        });

        loadData(mOffset, DEFAULT_COUNT, false);
    }

    private void loadData(int offset, int limit, boolean isLoadMore) {
        UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        ApiMethods.subscribe(userInfoServerApi.getFriendStatusList(offset, limit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<FriendStatusModel> list = JSON.parseArray(result.getData().getString("contacts"), FriendStatusModel.class);
                    int newOffset = result.getData().getIntValue("offset");
                    refreshView(list, newOffset, isLoadMore);

                    if (list != null && list.size() > 0) {
                        updateDBAndCache(list);
                    }
                }
            }
        }, this);
    }

    private void updateDBAndCache(List<FriendStatusModel> list) {
        List<UserInfoModel> userInfoModels = new ArrayList<>();
        for (FriendStatusModel friendStatusModel : list) {
            userInfoModels.add(friendStatusModel.toUserInfoModel());
        }
        Observable.create(new ObservableOnSubscribe<List<UserInfoModel>>() {
            @Override
            public void subscribe(ObservableEmitter<List<UserInfoModel>> emitter) throws Exception {
                if (userInfoModels != null && userInfoModels.size() > 0) {
                    UserInfoLocalApi.insertOrUpdate(userInfoModels);

                    List<BuddyCache.BuddyCacheEntry> buddyCacheEntries = new ArrayList<>();
                    for (UserInfoModel userInfoModel : userInfoModels) {
                        buddyCacheEntries.add(new BuddyCache.BuddyCacheEntry(userInfoModel));
                    }
                    BuddyCache.getInstance().putBuddyList(buddyCacheEntries);
                }
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();


    }

    private void refreshView(List<FriendStatusModel> list, int newOffset, boolean isLoadMore) {
        this.mOffset = newOffset;
        if (list != null && list.size() != 0) {
            if (!isLoadMore) {
                mFriendAdapter.getDataList().clear();
            }
            mRefreshLayout.finishRefresh();
            mRefreshLayout.finishLoadMore();
            mLoadService.showSuccess();
            mFriendAdapter.getDataList().addAll(list);
            mFriendAdapter.notifyDataSetChanged();
            hasMore = true;
        } else {
            hasMore = false;
            mRefreshLayout.finishRefresh();
            mRefreshLayout.finishLoadMoreWithNoMoreData();
            if (mOffset == 0) {
                mLoadService.showCallback(FriendsEmptyCallback.class);
            }
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
