package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.IWalletView;
import com.module.home.R;
import com.module.home.adapter.WalletRecordAdapter;
import com.module.home.model.WalletRecordModel;
import com.module.home.presenter.WalletRecordPresenter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

public class WalletFragment extends BaseFragment implements IWalletView {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    TextView mBalanceTv;
    TextView mWithdrawTv;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mRecyclerView;

    WalletRecordAdapter mWalletRecordAdapter;

    WalletRecordPresenter mWalletRecordPresenter;

    int offset = 0; //偏移量
    int DEFAULT_COUNT = 10; //每次拉去的数量

    float balance = 0; //可用余额

    @Override
    public int initView() {
        return R.layout.wallet_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mBalanceTv = (TextView) mRootView.findViewById(R.id.balance_tv);
        mWithdrawTv = (TextView) mRootView.findViewById(R.id.withdraw_tv);
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
                mWalletRecordPresenter.getWalletIncrRecords(offset, DEFAULT_COUNT);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(WalletFragment.this);
            }
        });

        mTitlebar.getRightTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getToastUtil().showShort("暂无提现记录");
            }
        });

        mWithdrawTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (balance < 10) {
                    U.getToastUtil().showShort("满10元才能提现哦～");
                } else {
                    U.getToastUtil().showShort("提现功能下版本开放\n" +
                            "如有疑问，请添加微信号“skrer1”进行咨询");
                }

            }
        });

    }

    private void initPresenter() {
        mWalletRecordPresenter = new WalletRecordPresenter(this);
        addPresent(mWalletRecordPresenter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mWalletRecordAdapter = new WalletRecordAdapter();
        mRecyclerView.setAdapter(mWalletRecordAdapter);

        mWalletRecordPresenter.getBalance();
        mWalletRecordPresenter.getWalletIncrRecords(offset, DEFAULT_COUNT);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void onGetBalanceSucess(String availableBalance, String lockedBalance) {
        if (!TextUtils.isEmpty(availableBalance)) {
            balance = Float.parseFloat(availableBalance);
        }
        SpannableStringBuilder stringBuilder = new SpanUtils()
                .append(availableBalance).setFontSize(50, true)
                .append("元").setFontSize(14, true)
                .create();
        mBalanceTv.setText(stringBuilder);
    }

    @Override
    public void onGetIncrRecords(int offset, List<WalletRecordModel> list) {
        this.offset = offset;
        if (list == null || list.size() <= 0) {
            mRefreshLayout.finishLoadMoreWithNoMoreData();
            return;
        }
        mRefreshLayout.finishLoadMore();
        mWalletRecordAdapter.insertListLast(list);
        mWalletRecordAdapter.notifyDataSetChanged();
    }
}
