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
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
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

    long mLastUpdateTime = 0;    //上次请求成功的时间

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
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                loadData(mOffset, DEFAULT_COUNT, true, false);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                loadData(0, DEFAULT_COUNT, false, true);
            }
        });

        mFriendAdapter = new FriendAdapter(new RecyclerOnItemClickListener<FriendStatusModel>() {
            @Override
            public void onItemClicked(View view, int position, FriendStatusModel model) {
                ModuleServiceManager.getInstance().getMsgService().startPrivateChat(getContext(),
                        String.valueOf(model.getUserID()), model.getNickname(), true);
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
                loadData(0, DEFAULT_COUNT, false, false);
            }
        });

        loadData(0, DEFAULT_COUNT, false, false);
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        loadData(0, DEFAULT_COUNT, false);
    }

    /**
     * @param offset 偏移量
     * @param limit  个数限制
     * @param isFlag 是否立即请求
     */
    private void loadData(int offset, int limit, boolean isFlag) {
        long now = System.currentTimeMillis();
        if (!isFlag) {
            // 距离上次拉去已经超过30秒了
            if ((now - mLastUpdateTime) < 30 * 1000) {
                return;
            }
        }

        loadData(offset, limit, false, false);
    }

    /**
     * @param offset     偏移量
     * @param limit      个数限制
     * @param isLoadMore 是否是加载更多
     * @param isRefresh  是否下拉刷新
     */
    private void loadData(int offset, int limit, boolean isLoadMore, boolean isRefresh) {
        UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        ApiMethods.subscribe(userInfoServerApi.getFriendStatusList(offset, limit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mLastUpdateTime = System.currentTimeMillis();
                    List<FriendStatusModel> list = JSON.parseArray(result.getData().getString("contacts"), FriendStatusModel.class);
                    int newOffset = result.getData().getIntValue("offset");
                    refreshView(list, newOffset, isLoadMore, isRefresh);

                    if (list != null && list.size() > 0) {
                        updateDBAndCache(list);
                    }
                } else {
                    if (isLoadMore) mRefreshLayout.finishLoadMore();
                    if (isRefresh) mRefreshLayout.finishRefresh();
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                if (isLoadMore) mRefreshLayout.finishLoadMore();
                if (isRefresh) mRefreshLayout.finishRefresh();
                // 可以加上请求出错
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

    private void refreshView(List<FriendStatusModel> list, int newOffset, boolean isLoadMore, boolean isRefresh) {
        this.mOffset = newOffset;
        if (isLoadMore) mRefreshLayout.finishLoadMore();
        if (isRefresh) mRefreshLayout.finishRefresh();

        if (!isLoadMore) {
            mFriendAdapter.getDataList().clear();
        }

        if (list != null && list.size() != 0) {
            hasMore = true;
            mRefreshLayout.setEnableLoadMore(true);
            mLoadService.showSuccess();
            mFriendAdapter.getDataList().addAll(list);
            mFriendAdapter.notifyDataSetChanged();
        } else {
            hasMore = false;
            mRefreshLayout.setEnableLoadMore(false);

            if (mFriendAdapter.getDataList() == null || mFriendAdapter.getDataList().size() == 0) {
                // 数据为空
                mLoadService.showCallback(FriendsEmptyCallback.class);
            }
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean isInViewPager() {
        return true;
    }
}
