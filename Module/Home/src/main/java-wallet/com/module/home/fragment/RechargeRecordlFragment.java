package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.view.titlebar.CommonTitleBar;
import com.module.home.R;
import com.module.home.WalletServerApi;
import com.module.home.adapter.RechargeRecordAdapter;
import com.module.home.model.RechargeRecordModel;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

public class RechargeRecordlFragment extends BaseFragment {
    RechargeRecordAdapter mWalletRecordAdapter;

    RecyclerView mRecyclerView;
    CommonTitleBar mTitlebar;
    SmartRefreshLayout mRefreshLayout;

    int offset = 0; //偏移量
    int DEFAULT_COUNT = 50; //每次拉去的数量

    WalletServerApi mWalletServerApi;

    ArrayList<RechargeRecordModel> mRechargeRecordModels = new ArrayList<>();

    @Override
    public int initView() {
        return R.layout.dq_detail_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mTitlebar.getCenterTextView().setText("充值记录");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mWalletRecordAdapter = new RechargeRecordAdapter();
        mRecyclerView.setAdapter(mWalletRecordAdapter);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getRechargeList();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });

        mWalletServerApi = ApiManager.getInstance().createService(WalletServerApi.class);
        getRechargeList();
    }

    private void getRechargeList() {
        ApiMethods.subscribe(mWalletServerApi.getRechargeList(offset, DEFAULT_COUNT), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<RechargeRecordModel> rechargeRecordModelList = JSON.parseArray(result.getData().getString("list"), RechargeRecordModel.class);
                    if (rechargeRecordModelList == null || rechargeRecordModelList.size() == 0) {
                        mRefreshLayout.finishLoadMore();
                        mRefreshLayout.setEnableLoadMore(false);
                        return;
                    }

                    mRechargeRecordModels.addAll(rechargeRecordModelList);
                    offset = JSON.parseObject(result.getData().getString("offset"), Integer.class);
                    mWalletRecordAdapter.setDataList(mRechargeRecordModels);
                }

                mRefreshLayout.finishLoadMore();
            }

            @Override
            public void onError(Throwable e) {
                mRefreshLayout.finishLoadMore();
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                mRefreshLayout.finishLoadMore();
            }
        }, this);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
