package com.example.paginate;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
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
import com.wali.live.moduletest.H;
import com.wali.live.moduletest.R;
import com.wali.live.moduletest.TestViewHolder;

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
        personList.addAll(genData(0, 2));
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerPersonAdapter = new RecyclerPersonAdapter(personList);
        mRecyclerView.setAdapter(mRecyclerPersonAdapter);

        // 自定义的 rv 动画
        mRecyclerView.setItemAnimator(new LandingAnimator());
        setupPanigate();


        RecyclerView opListView = mRootView.findViewById(R.id.op_list_view);
        List<H> opDataList = new ArrayList<>();
        opListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        opListView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                TextView tv = new TextView(parent.getContext());
                tv.setGravity(Gravity.CENTER);
                tv.setPadding(10,10,10,10);
                TestViewHolder testHolder = new TestViewHolder(tv);
                return testHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                if (holder instanceof TestViewHolder) {
                    TestViewHolder testHolder = (TestViewHolder) holder;
                    testHolder.bindData(opDataList.get(position));
                }
            }

            @Override
            public int getItemCount() {
                return opDataList.size();
            }
        });

        opDataList.add(new H("0移到3，局部更新", new Runnable() {
            @Override
            public void run() {
                List<Person> l = new ArrayList<>();
                l.addAll(mRecyclerPersonAdapter.getDataList());
//                l.remove(0);
                Person a = l.remove(0);
                l.add(3,a);
                mRecyclerPersonAdapter.setDataList(l);
            }
        }));

        opDataList.add(new H("移除0 和 4，局部更新", new Runnable() {
            @Override
            public void run() {
                List<Person> l = new ArrayList<>();
                l.addAll(mRecyclerPersonAdapter.getDataList());
                l.remove(0);
                l.remove(4);
                mRecyclerPersonAdapter.setDataList(l);
            }
        }));

        opDataList.add(new H("1增加2条，局部更新", new Runnable() {
            @Override
            public void run() {
                List<Person> l = new ArrayList<>();
                l.addAll(mRecyclerPersonAdapter.getDataList());
                l.addAll(1,genData(0,2));
                mRecyclerPersonAdapter.setDataList(l);
            }
        }));


        opDataList.add(new H("更新某条数据", new Runnable() {
            @Override
            public void run() {
                Person person = (Person) mRecyclerPersonAdapter.getDataList().get(0);
                person.setName("吃什么");
                mRecyclerPersonAdapter.update(person);
            }
        }));
    }

    List<Person> genData(int idBegin, int num) {
        List<Person> list = new ArrayList<>();
        for (int i = idBegin; i < idBegin + num; i++, mIndex++) {
            list.add(new Person(i, "fistName" + mIndex, mIndex));
        }
        return list;
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
                        personList.addAll(genData(0, 20));
                        mRecyclerPersonAdapter.insertListLast(personList);
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
