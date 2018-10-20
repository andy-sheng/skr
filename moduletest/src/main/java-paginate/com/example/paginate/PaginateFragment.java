package com.example.paginate;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.utils.U;
import com.example.paginate.adapter.RecyclerPersonAdapter;
import com.example.paginate.data.Person;
import com.paginate.Paginate;
import com.paginate.recycler.LoadingListItemCreator;
import com.wali.live.moduletest.R;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class PaginateFragment extends BaseFragment {

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerPersonAdapter mRecyclerPersonAdapter;

    private Paginate mPaginate;

    Handler mUiHanlder = new Handler();

    int mIndex = 0;

    @Override
    public int initView() {
        return R.layout.test_paginate_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        List<Person> personList = new ArrayList<>();
        for (int i = 0; i < 2; i++, mIndex++) {
            personList.add(new Person("a" + mIndex, "b" + mIndex, mIndex));
        }
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerPersonAdapter = new RecyclerPersonAdapter(personList);
        mRecyclerView.setAdapter(mRecyclerPersonAdapter);

        mRecyclerView.setItemAnimator(new LandingAnimator());
        setupPanigate();
    }

    private void setupPanigate() {
        if (mPaginate != null) {
            mPaginate.unbind();
        }

        mPaginate = Paginate.with(mRecyclerView, new Paginate.Callbacks() {
            boolean loading = false;

            @Override
            public void onLoadMore() {
                loading = true;
                mUiHanlder.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        List<Person> personList = new ArrayList<>();
                        for (int i = 0; i < 20; i++, mIndex++) {
                            personList.add(new Person("a" + mIndex, "b" + mIndex, mIndex));
                        }
                        mRecyclerPersonAdapter.add(personList);
                        loading = false;
                    }
                }, 2000);
            }

            @Override
            public boolean isLoading() {
                return loading;
            }

            @Override
            public boolean hasLoadedAllItems() {
                return mIndex >= 80;
            }
        })
                .setLoadingTriggerThreshold(2)
                .addLoadingListItem(true)
                .setLoadingListItemCreator(new LoadingListItemCreator() {
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        TextView textView = new TextView(parent.getContext());
                        return new VH(textView);
                    }

                    @Override
                    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                        MyLog.d(TAG, "onBindViewHolder" + " holder=" + holder + " position=" + position);
                        if (holder instanceof VH) {
                            VH vh = (VH) holder;
                            ((TextView) vh.itemView).setText("加载中");
                        }
                    }
                })
//                .setLoadingListItemSpanSizeLookup(new LoadingListItemSpanLookup() {
//                    @Override
//                    public int getSpanSize() {
//                        return GRID_SPAN;
//                    }
//                })
                .build();

    }

    @Override
    protected boolean onBackPressed() {
        U.getFragmentUtils().popFragment(getActivity());
        return true;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    static class VH extends RecyclerView.ViewHolder {

        public VH(View itemView) {
            super(itemView);
        }
    }
}
