package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.titlebar.CommonTitleBar;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.home.R;
import com.module.home.WalletServerApi;
import com.module.home.adapter.DqRecordAdapter;
import com.module.home.adapter.WalletRecordAdapter;
import com.module.home.inter.IWalletView;
import com.module.home.loadsir.DqEmptyCallBack;
import com.module.home.model.DqRecordModel;
import com.module.home.model.WalletRecordModel;
import com.module.home.model.WithDrawInfoModel;
import com.module.home.presenter.WalletRecordPresenter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

public class DqDetailFragment extends BaseFragment {
    DqRecordAdapter mWalletRecordAdapter;

    RecyclerView mRecyclerView;
    CommonTitleBar mTitlebar;
    SmartRefreshLayout mRefreshLayout;
    ExImageView mIvBg;

    LoadService mLoadService;

    int offset = 0; //偏移量
    int DEFAULT_COUNT = 10; //每次拉去的数量

    WalletServerApi mWalletServerApi;

    ArrayList<DqRecordModel> mDqRecordModelArrayList = new ArrayList<>();

    @Override
    public int initView() {
        return R.layout.dq_detail_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mIvBg = (ExImageView) mRootView.findViewById(R.id.iv_bg);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mWalletRecordAdapter = new DqRecordAdapter();
        mRecyclerView.setAdapter(mWalletRecordAdapter);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getDqList();
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

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new DqEmptyCallBack())
                .build();

        mLoadService = mLoadSir.register(mRefreshLayout, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                getDqList();
            }
        });

        mWalletServerApi = ApiManager.getInstance().createService(WalletServerApi.class);
        getDqList();
    }

    private void getDqList() {
        ApiMethods.subscribe(mWalletServerApi.getDqList(offset, DEFAULT_COUNT), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<DqRecordModel> dqRecordModelList = JSON.parseArray(result.getData().getString("list"), DqRecordModel.class);
                    if (dqRecordModelList == null || dqRecordModelList.size() == 0) {
                        mRefreshLayout.finishLoadMore();
                        mRefreshLayout.setEnableLoadMore(false);
                        if (mDqRecordModelArrayList.size() == 0) {
                            mLoadService.showCallback(DqEmptyCallBack.class);
                        }
                        return;
                    }

                    mLoadService.showSuccess();
                    mDqRecordModelArrayList.addAll(dqRecordModelList);
                    offset = JSON.parseObject(result.getData().getString("offset"), Integer.class);
                    mWalletRecordAdapter.setDataList(mDqRecordModelArrayList);
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
