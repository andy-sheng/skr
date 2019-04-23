package com.component.busilib.friends;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.permission.SkrAudioPermission;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.module.RouterConstants;
import com.module.playways.IPlaywaysModeService;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

public class GrabFriendsRoomFragment extends BaseFragment {

    ExImageView mIvBack;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mContentRv;

    int offset = 0;          //偏移量
    int DEFAULT_COUNT = 50;  // 每次拉去列表数目

    FriendRoomVerticalAdapter mFriendRoomVeritAdapter;

    SkrAudioPermission mSkrAudioPermission;

    @Override
    public int initView() {
        return R.layout.grab_friends_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mContentRv = (RecyclerView) mRootView.findViewById(R.id.content_rv);

        mSkrAudioPermission = new SkrAudioPermission();

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                loadData(offset, DEFAULT_COUNT, true);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });
        mContentRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        mFriendRoomVeritAdapter = new FriendRoomVerticalAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                if (model != null && model instanceof RecommendModel) {
                    RecommendModel friendRoomModel = (RecommendModel) model;
                    if (friendRoomModel != null && friendRoomModel.getRoomInfo() != null) {
                        final int roomID = friendRoomModel.getRoomInfo().getRoomID();
                        mSkrAudioPermission.ensurePermission(new Runnable() {
                            @Override
                            public void run() {
                                IPlaywaysModeService iRankingModeService = (IPlaywaysModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
                                if (iRankingModeService != null) {
                                    iRankingModeService.tryGoGrabRoom(roomID);
                                }
                            }
                        }, true);
                    } else {
                        MyLog.w(TAG, "friendRoomModel == null or friendRoomModel.getRoomInfo() == null");
                    }
                } else {
                    MyLog.w(TAG, "onItemClicked" + " view=" + view + " position=" + position + " model=" + model);
                }
            }
        });
        mContentRv.setAdapter(mFriendRoomVeritAdapter);

        loadData(0, DEFAULT_COUNT, false);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    private void loadData(int offset, int count, final boolean isLoadMore) {
        GrabSongApi grabSongApi = ApiManager.getInstance().createService(GrabSongApi.class);
        ApiMethods.subscribe(grabSongApi.getRecommendRoomList(offset, count), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    List<RecommendModel> list = JSON.parseArray(obj.getData().getString("rooms"), RecommendModel.class);
                    int offset = obj.getData().getIntValue("offset");
                    int totalNum = obj.getData().getIntValue("cnt");
                    refreshView(list, offset, isLoadMore);
                }
            }
        }, this);
    }

    private void refreshView(List<RecommendModel> list, int offset, boolean isLoadMore) {
        this.offset = offset;
        mRefreshLayout.finishLoadMore();
        if (!isLoadMore) {
            mFriendRoomVeritAdapter.getDataList().clear();
        }

        if (list != null && list.size() > 0) {
            mRefreshLayout.setEnableLoadMore(true);
            mFriendRoomVeritAdapter.getDataList().addAll(list);
            mFriendRoomVeritAdapter.notifyDataSetChanged();
        } else {
            // TODO: 2019/4/15  用mFriendRoomVeritAdapter去判断是否空和没有更多
            mRefreshLayout.setEnableLoadMore(false);
        }
    }
}
