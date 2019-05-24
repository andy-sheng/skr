package com.module.home.game.viewholder;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.friends.FriendRoomHorizontalAdapter;
import com.component.busilib.friends.GrabSongApi;
import com.component.busilib.friends.RecommendModel;
import com.module.home.R;
import com.module.home.game.adapter.GameAdapter;
import com.module.home.game.listener.EndlessRecycleOnScollListener;
import com.module.home.game.model.RecommendRoomModel;

import java.util.List;

public class RecommendRoomViewHolder extends RecyclerView.ViewHolder {

    public final static String TAG = "RecommendRoomViewHolder";

    BaseFragment mBaseFragment;

    ExRelativeLayout mRecyclerArea;
    RecyclerView mFriendsRecycle;
    ExImageView mMoreFriends;

    RecommendRoomModel mRecommendRoomModel;
    FriendRoomHorizontalAdapter mFriendRoomAdapter;
    GameAdapter.GameAdapterListener mListener;

    public RecommendRoomViewHolder(View itemView, BaseFragment baseFragment, GameAdapter.GameAdapterListener listener) {
        super(itemView);

        this.mBaseFragment = baseFragment;
        this.mListener = listener;

        mRecyclerArea = (ExRelativeLayout) itemView.findViewById(R.id.recycler_area);
        mFriendsRecycle = (RecyclerView) itemView.findViewById(R.id.friends_recycle);
        mMoreFriends = (ExImageView) itemView.findViewById(R.id.more_friends);

        mFriendsRecycle.setFocusableInTouchMode(false);
        mFriendsRecycle.setLayoutManager(new LinearLayoutManager(baseFragment.getContext(), LinearLayoutManager.HORIZONTAL, false));
        mFriendRoomAdapter = new FriendRoomHorizontalAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                if (model != null) {
                    RecommendModel friendRoomModel = (RecommendModel) model;
                    if (mListener != null) {
                        mListener.enterRoom(friendRoomModel);
                    }
                } else {
                    MyLog.w(TAG, "onItemClicked model = null");
                }
            }
        });
        mFriendsRecycle.addOnScrollListener(new EndlessRecycleOnScollListener() {
            @Override
            public void onLoadMore() {
                MyLog.d(TAG, "onLoadMore");
                refreshData();
            }
        });

        mMoreFriends.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.moreRoom();
                }
            }
        });

        mFriendsRecycle.setAdapter(mFriendRoomAdapter);

    }

    public void bindData(RecommendRoomModel recommendRoomModel) {
        this.mRecommendRoomModel = recommendRoomModel;
        mFriendRoomAdapter.setDataList(mRecommendRoomModel.getRoomModels());
    }

    private void refreshData() {
        GrabSongApi mGrabSongApi = ApiManager.getInstance().createService(GrabSongApi.class);
        ApiMethods.subscribe(mGrabSongApi.getFirstPageRecommendRoomList(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    List<RecommendModel> list = JSON.parseArray(obj.getData().getString("rooms"), RecommendModel.class);
                    refreshData(list);
                }
            }
        }, mBaseFragment);
    }

    private void refreshData(List<RecommendModel> list) {
        if (list != null && list.size() > 0) {
            this.mRecommendRoomModel.getRoomModels().clear();
            this.mRecommendRoomModel.getRoomModels().addAll(list);
            mFriendRoomAdapter.setDataList(mRecommendRoomModel.getRoomModels());
        }
    }
}
