package com.zq.relation.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoModel;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.person.fragment.OtherPersonFragment;
import com.zq.relation.adapter.RelationAdapter;
import com.zq.toast.CommonToastView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static com.common.core.userinfo.event.RelationChangeEvent.FOLLOW_TYPE;
import static com.common.core.userinfo.event.RelationChangeEvent.UNFOLLOW_TYPE;

public class RelationView extends RelativeLayout {

    public final static String TAG = "RelationView";

    private int mMode = UserInfoManager.RELATION_FRIENDS;
    private int mOffset = 0; // 偏移量
    private int DEFAULT_COUNT = 30; // 每次拉去最大值
    private boolean hasMore = true; // 是否还有数据

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

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        mRecyclerView = (RecyclerView) this.findViewById(R.id.recycler_view);
        mRefreshLayout = (SmartRefreshLayout) this.findViewById(R.id.refreshLayout);

        mRelationAdapter = new RelationAdapter(mode, new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                UserInfoModel userInfoModel = (UserInfoModel) model;
                if (view.getId() == R.id.content) {
                    // 跳到他人的个人主页
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(OtherPersonFragment.BUNDLE_USER_MODEL, userInfoModel);
                    U.getFragmentUtils().addFragment(FragmentUtils
                            .newAddParamsBuilder((BaseActivity) getContext(), OtherPersonFragment.class)
                            .setBundle(bundle)
                            .setAddToBackStack(true)
                            .setHasAnimation(true)
                            .build());
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
        if (!hasMore) {
            U.getToastUtil().showShort("没有更多数据了");
            return;
        }
        UserInfoManager.getInstance().getRelationList(mode, offset, limit, new UserInfoManager.ResponseCallBack<ApiResult>() {
            @Override
            public void onServerSucess(ApiResult result) {
                mOffset = result.getData().getIntValue("offset");
                List<UserInfoModel> userInfoModels = JSON.parseArray(result.getData().getString("users"), UserInfoModel.class);
                if (userInfoModels != null && userInfoModels.size() != 0) {
                    mRelationAdapter.addData(userInfoModels);
                    mRelationAdapter.notifyDataSetChanged();
                    hasMore = true;
                } else {
                    hasMore = false;
                }
            }

            @Override
            public void onServerFailed() {

            }
        });
    }

    public void destroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        MyLog.d(TAG, "RelationChangeEvent" + " event type = " + event.type + " isFriend = " + event.isFriend + " old = " + event.userInfoModel.isFriend());
        UserInfoModel userInfoModel = null;
        try {
            userInfoModel = (UserInfoModel) event.userInfoModel.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        if (mMode == UserInfoManager.RELATION_FRIENDS) {
            if (event.type == FOLLOW_TYPE) {
                if (event.isFriend) {
                    userInfoModel.setFriend(true);
                    mRelationAdapter.getData().add(0, userInfoModel);
                    mRelationAdapter.notifyDataSetChanged();
                }
            } else if (event.type == UNFOLLOW_TYPE) {
                if (event.userInfoModel.isFriend()) {
                    UserInfoModel delModel = null;
                    for (UserInfoModel model : mRelationAdapter.getData()) {
                        if (model.getUserId() == event.userInfoModel.getUserId()) {
                            delModel = model;
                            break;
                        }
                    }
                    if (delModel != null) {
                        mRelationAdapter.getData().remove(delModel);
                        mRelationAdapter.notifyDataSetChanged();
                    }
                }
            }
        } else if (mMode == UserInfoManager.RELATION_FANS) {
            if (event.type == FOLLOW_TYPE) {
                if (event.isFriend) {
                    UserInfoModel delModel = null;
                    for (UserInfoModel model : mRelationAdapter.getData()) {
                        if (model.getUserId() == event.userInfoModel.getUserId()) {
                            delModel = model;
                            break;
                        }
                    }
                    if (delModel != null) {
                        mRelationAdapter.getData().remove(delModel);
                    }

                    userInfoModel.setFriend(true);
                    mRelationAdapter.getData().add(0, userInfoModel);
                    mRelationAdapter.notifyDataSetChanged();
                }
            } else if (event.type == UNFOLLOW_TYPE) {
                // 粉丝页面收到取关
                if (event.userInfoModel.isFriend()) {
                    UserInfoModel delModel = null;
                    for (UserInfoModel model : mRelationAdapter.getData()) {
                        if (model.getUserId() == event.userInfoModel.getUserId()) {
                            delModel = model;
                            break;
                        }
                    }
                    if (delModel != null) {
                        mRelationAdapter.getData().remove(delModel);
                    }

                    userInfoModel.setFriend(false);
                    mRelationAdapter.getData().add(0, userInfoModel);
                    mRelationAdapter.notifyDataSetChanged();
                }
            }
        } else if (mMode == UserInfoManager.RELATION_FOLLOW) {
            if (event.type == FOLLOW_TYPE) {
                if (event.isFriend) {
                    userInfoModel.setFriend(true);
                    mRelationAdapter.getData().add(0, userInfoModel);
                    mRelationAdapter.notifyDataSetChanged();
                } else {
                    userInfoModel.setFriend(false);
                    mRelationAdapter.getData().add(0, userInfoModel);
                    mRelationAdapter.notifyDataSetChanged();
                }
            } else if (event.type == UNFOLLOW_TYPE) {
                UserInfoModel delModel = null;
                for (UserInfoModel model : mRelationAdapter.getData()) {
                    if (model.getUserId() == event.userInfoModel.getUserId()) {
                        delModel = model;
                        break;
                    }
                }
                if (delModel != null) {
                    mRelationAdapter.getData().remove(delModel);
                    mRelationAdapter.notifyDataSetChanged();
                }

            }
        }
    }

}
