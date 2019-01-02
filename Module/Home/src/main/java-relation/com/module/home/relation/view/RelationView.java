package com.module.home.relation.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoModel;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.rxretrofit.ApiResult;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.home.R;
import com.module.home.relation.adapter.RelationAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static com.common.core.userinfo.event.RelationChangeEvent.FOLLOW_TYPE;
import static com.common.core.userinfo.event.RelationChangeEvent.UNFOLLOW_TYPE;

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

        mRelationAdapter = new RelationAdapter(mode, new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                UserInfoModel userInfoModel = (UserInfoModel) model;
                if (view.getId() == R.id.content) {

                } else if (view.getId() == R.id.follow_tv) {
                    // 关注和好友都是有关系的人
                    if (mMode == UserInfoManager.RELATION_FANS) {
                        if (userInfoModel.isFriend()) {
                            UserInfoManager.getInstance().mateRelation(userInfoModel, UserInfoManager.RA_UNBUILD);
                        } else {
                            UserInfoManager.getInstance().mateRelation(userInfoModel, UserInfoManager.RA_BUILD);
                        }
                    } else {
                        UserInfoManager.getInstance().mateRelation(userInfoModel, UserInfoManager.RA_UNBUILD);
                    }

                }
            }
        });
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
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        if (event.type == FOLLOW_TYPE) {
            // 粉丝,好友和关注都有影响
            if (event.isFriend) {
                // 变成好友
                if (mMode == UserInfoManager.RELATION_FRIENDS) {
                    // 必定未包含，新增好友
                    event.userInfoModel.setFriend(true);
                    mRelationAdapter.getData().add(0, event.userInfoModel);
                    mRelationAdapter.notifyDataSetChanged();
                } else if (mMode == UserInfoManager.RELATION_FANS) {
                    // 必定已包含，更新视图
                    mRelationAdapter.getData().remove(event.userInfoModel);

                    event.userInfoModel.setFriend(true);
                    mRelationAdapter.getData().add(0, event.userInfoModel);
                    mRelationAdapter.notifyDataSetChanged();
                } else if (mMode == UserInfoManager.RELATION_FOLLOW) {
                    // 必定未包含，新增关注
                    event.userInfoModel.setFriend(true);
                    mRelationAdapter.getData().add(0, event.userInfoModel);
                    mRelationAdapter.notifyDataSetChanged();
                }
            } else {
                // 只有关注关系
                if (mMode == UserInfoManager.RELATION_FRIENDS) {
                    if (event.userInfoModel.isFriend()) {
                        // 之前是好友，删除视图
                        mRelationAdapter.getData().remove(event.userInfoModel);
                        mRelationAdapter.notifyDataSetChanged();
                    } else {
                        // 之前非好友，donothing
                    }
                } else if (mMode == UserInfoManager.RELATION_FANS) {
                    if (event.userInfoModel.isFriend()) {
                        // 之前是好友，更新视图
                        mRelationAdapter.getData().remove(event.userInfoModel);

                        event.userInfoModel.setFriend(false);
                        mRelationAdapter.getData().add(0, event.userInfoModel);
                        mRelationAdapter.notifyDataSetChanged();
                    } else {
                        // 之前非好友，donothing
                    }
                } else if (mMode == UserInfoManager.RELATION_FOLLOW) {
                    if (event.userInfoModel.isFriend()) {
                        // 之前是好友，更新视图
                        mRelationAdapter.getData().remove(event.userInfoModel);

                        event.userInfoModel.setFriend(false);
                        mRelationAdapter.getData().add(0, event.userInfoModel);
                        mRelationAdapter.notifyDataSetChanged();
                    } else {
                        // 之前非好友，新增关注
                        event.userInfoModel.setFriend(false);
                        mRelationAdapter.getData().add(0, event.userInfoModel);
                        mRelationAdapter.notifyDataSetChanged();
                    }
                }
            }
        } else if (event.type == UNFOLLOW_TYPE) {
            // 取消关注
            if (mMode == UserInfoManager.RELATION_FRIENDS) {
                // 删除视图
                mRelationAdapter.getData().remove(event.userInfoModel);
                mRelationAdapter.notifyDataSetChanged();
            } else if (mMode == UserInfoManager.RELATION_FANS) {
                if (event.userInfoModel.isFriend()) {
                    // 之前是好友，从互关变为未关注
                    mRelationAdapter.getData().remove(event.userInfoModel);

                    event.userInfoModel.setFriend(false);
                    mRelationAdapter.getData().add(0, event.userInfoModel);
                    mRelationAdapter.notifyDataSetChanged();
                } else {
                    // 之前非好友，无影响
                }
            } else if (mMode == UserInfoManager.RELATION_FOLLOW) {
                // 之前非好友，删除该视图
                // 之前是好友，删除该视图
                mRelationAdapter.getData().remove(event.userInfoModel);
                mRelationAdapter.notifyDataSetChanged();

            }
        }
    }

}
