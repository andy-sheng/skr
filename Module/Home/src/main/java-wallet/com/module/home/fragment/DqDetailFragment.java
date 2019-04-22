package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.common.base.BaseFragment;
import com.common.view.titlebar.CommonTitleBar;
import com.module.home.R;
import com.module.home.adapter.WalletRecordAdapter;
import com.module.home.inter.IWalletView;
import com.module.home.model.WalletRecordModel;
import com.module.home.model.WithDrawInfoModel;
import com.module.home.presenter.WalletRecordPresenter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

public class DqDetailFragment extends BaseFragment implements IWalletView {
    WalletRecordAdapter mWalletRecordAdapter;

    WalletRecordPresenter mWalletRecordPresenter;

    RecyclerView mRecyclerView;
    CommonTitleBar mTitlebar;
    SmartRefreshLayout mRefreshLayout;

    int offset = 0; //偏移量
    int DEFAULT_COUNT = 10; //每次拉去的数量

    @Override
    public int initView() {
        return R.layout.dq_detail_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mWalletRecordAdapter = new WalletRecordAdapter();
        mRecyclerView.setAdapter(mWalletRecordAdapter);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
//                mWalletRecordPresenter.getWalletIncrRecords(offset, DEFAULT_COUNT);
                mWalletRecordPresenter.getAllWalletRecords(offset, DEFAULT_COUNT);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });

        mWalletRecordPresenter = new WalletRecordPresenter(this);
        addPresent(mWalletRecordPresenter);
        mWalletRecordPresenter.getAllWalletRecords(offset, DEFAULT_COUNT);
    }

    @Override
    public void onGetBalanceSucess(String availableBalance, String lockedBalance) {

    }

    @Override
    public void onGetIncrRecords(int offset, List<WalletRecordModel> list) {
        this.offset = offset;
        if (list == null || list.size() <= 0) {
            mRefreshLayout.setEnableLoadMore(false);
            return;
        }
        mRefreshLayout.finishLoadMore();
        mWalletRecordAdapter.insertListLast(list);
        mWalletRecordAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetAllRecords(int offset, List<WalletRecordModel> list) {
        this.offset = offset;
        if (list == null || list.size() <= 0) {
            mRefreshLayout.setEnableLoadMore(false);
            mRefreshLayout.finishLoadMore();
            return;
        }
        mRefreshLayout.finishLoadMore();
        mWalletRecordAdapter.insertListLast(list);
        mWalletRecordAdapter.notifyDataSetChanged();
    }

    @Override
    public void showWithDrawInfo(WithDrawInfoModel withDrawInfoModel) {

    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
