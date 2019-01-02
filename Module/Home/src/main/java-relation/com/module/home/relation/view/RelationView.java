package com.module.home.relation.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoModel;
import com.common.rxretrofit.ApiResult;
import com.module.home.R;
import com.module.home.relation.adapter.RelationAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

public class RelationView extends RelativeLayout {

    private int mMode = UserInfoManager.RELATION_FRIENDS;
    private int mOffset = 0; // 偏移量
    private int DEFAULT_COUNT = 30; // 每次拉去最大值

    RecyclerView mRecyclerView;
    SmartRefreshLayout mRefreshLayout;

    RelationAdapter mRelationAdapter;

    public RelationView(Context context, int mode) {
        super(context);
        init(context, mode);
    }

    private void init(Context context, int mode) {
        inflate(context, R.layout.relation_view, this);
        this.mMode = mode;

        mRecyclerView = (RecyclerView) this.findViewById(R.id.recycler_view);
        mRefreshLayout = (SmartRefreshLayout) this.findViewById(R.id.refreshLayout);

        mRelationAdapter = new RelationAdapter(mode);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRelationAdapter);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                loadData(mMode, mOffset, DEFAULT_COUNT);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });

        loadData(mMode, mOffset, DEFAULT_COUNT);
    }

    public void loadData(int mode, int offset, int limit) {
        UserInfoManager.getInstance().getRelationList(mode, offset, limit, new UserInfoManager.ResponseCallBack<ApiResult>() {
            @Override
            public void onServerSucess(ApiResult result) {
                mOffset = result.getData().getIntValue("offset");
                List<UserInfoModel> userInfoModels = JSON.parseArray(result.getData().getString("users"), UserInfoModel.class);
                if (userInfoModels != null && userInfoModels.size() != 0) {
                    mRelationAdapter.addData(userInfoModels);
                    mRelationAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onServerFailed() {

            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
