package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.adapter.WalletRecordAdapter;
import com.module.home.adapter.WithDrawHistoryAdapter;
import com.module.home.inter.IWalletView;
import com.module.home.inter.IWithDrawHistoryView;
import com.module.home.model.WithDrawHistoryModel;
import com.module.home.presenter.WalletRecordPresenter;
import com.module.home.presenter.WithDrawHistoryPresenter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

public class WithDrawHistoryFragment extends BaseFragment implements IWithDrawHistoryView {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mRecyclerView;
    WithDrawHistoryAdapter mWalletRecordAdapter;
    WithDrawHistoryPresenter mWithDrawHistoryPresenter;

    @Override
    public int initView() {
        return R.layout.withdraw_history_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);

        initPresenter();

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                mWithDrawHistoryPresenter.getMoreWithDrawHistory();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                //U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                U.getFragmentUtils().popFragment(WithDrawHistoryFragment.this);
            }
        });

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);
    }

    private void initPresenter() {
        mWithDrawHistoryPresenter = new WithDrawHistoryPresenter(this);
        addPresent(mWithDrawHistoryPresenter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mWalletRecordAdapter = new WithDrawHistoryAdapter();
        mRecyclerView.setAdapter(mWalletRecordAdapter);

        mWithDrawHistoryPresenter.getMoreWithDrawHistory();
    }

    @Override
    public void update(List<WithDrawHistoryModel> withDrawHistoryModelList) {
        mWalletRecordAdapter.setDataList(withDrawHistoryModelList);
        mRefreshLayout.finishRefresh();
    }

    @Override
    public void hasMore(boolean hasMore) {
        mRefreshLayout.setEnableLoadMore(hasMore);
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
