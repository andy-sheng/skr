package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.titlebar.CommonTitleBar;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.home.R;
import com.module.home.adapter.WalletRecordAdapter;
import com.module.home.inter.IWalletView;
import com.module.home.loadsir.BalanceEmptyCallBack;
import com.module.home.model.WalletRecordModel;
import com.module.home.model.WithDrawInfoModel;
import com.module.home.presenter.WalletRecordPresenter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

public class CashDetailFragment extends BaseFragment implements IWalletView {
    WalletRecordAdapter mWalletRecordAdapter;

    WalletRecordPresenter mWalletRecordPresenter;

    RecyclerView mRecyclerView;
    CommonTitleBar mTitlebar;
    SmartRefreshLayout mRefreshLayout;
    ExImageView mIvBg;

    LoadService mLoadService;

    int offset = 0; //偏移量
    int DEFAULT_COUNT = 50; //每次拉去的数量

    @Override
    public int initView() {
        return R.layout.cash_detail_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) getRootView().findViewById(R.id.titlebar);
        mRefreshLayout = (SmartRefreshLayout) getRootView().findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) getRootView().findViewById(R.id.recycler_view);
        mIvBg = (ExImageView) getRootView().findViewById(R.id.iv_bg);

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

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                finish();
            }
        });

        mTitlebar.getRightTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), WithDrawHistoryFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
            }
        });

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new BalanceEmptyCallBack())
                .build();

        mLoadService = mLoadSir.register(mRefreshLayout, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                mWalletRecordPresenter.getAllWalletRecords(offset, DEFAULT_COUNT);
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
            if(mWalletRecordAdapter.getItemCount() == 0){
                mLoadService.showCallback(BalanceEmptyCallBack.class);
            }
            return;
        }

        mLoadService.showSuccess();
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
            if(mWalletRecordAdapter.getItemCount() == 0){
                mLoadService.showCallback(BalanceEmptyCallBack.class);
            }
            return;
        }

        mLoadService.showSuccess();
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
