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
import com.component.busilib.GrabJoinRoomFailEvent;
import com.component.busilib.R;
import com.module.RouterConstants;
import com.module.playways.IPlaywaysModeService;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class GrabFriendsRoomFragment extends BaseFragment {

    ExImageView mIvBack;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mContentRv;
    ClassicsHeader mClassicsHeader;

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
        mClassicsHeader = (ClassicsHeader) mRootView.findViewById(R.id.classics_header);

        mSkrAudioPermission = new SkrAudioPermission();

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableLoadMore(false);
        mRefreshLayout.setRefreshHeader(mClassicsHeader);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                loadData();
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
                                IPlaywaysModeService playWaysService = (IPlaywaysModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
                                if (playWaysService != null) {
                                    playWaysService.tryGoGrabRoom(roomID, 0);
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

        loadData();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabJoinRoomFailEvent event) {
        if (event.getType() == GrabJoinRoomFailEvent.TYPE_DISSOLVE_ROOM) {
            // 解散了
            mFriendRoomVeritAdapter.deleteRoomModel(event.getRoomID());
        } else if (event.getType() == GrabJoinRoomFailEvent.TYPE_FULL_ROOM) {
            // 房间满了
            mFriendRoomVeritAdapter.updateFullRoom(event.getRoomID());
        }
    }

    private void loadData() {
        GrabSongApi grabSongApi = ApiManager.getInstance().createService(GrabSongApi.class);
        ApiMethods.subscribe(grabSongApi.getRecommendRoomList(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    List<RecommendModel> list = JSON.parseArray(obj.getData().getString("rooms"), RecommendModel.class);
                    refreshView(list);
                }
            }
        }, this);
    }

    /**
     * 刷新数据
     *
     * @param list
     */
    private void refreshView(List<RecommendModel> list) {
        mRefreshLayout.finishRefresh();
        if (list != null && list.size() > 0) {
            mFriendRoomVeritAdapter.getDataList().clear();
            mFriendRoomVeritAdapter.getDataList().addAll(list);
            mFriendRoomVeritAdapter.notifyDataSetChanged();
        }
    }
}
