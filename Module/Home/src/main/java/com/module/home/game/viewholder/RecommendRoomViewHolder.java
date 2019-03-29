package com.module.home.game.viewholder;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.log.MyLog;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.friends.FriendRoomHorizontalAdapter;
import com.component.busilib.friends.FriendRoomModel;
import com.module.home.R;
import com.module.home.game.adapter.GameAdapter;
import com.module.home.game.model.RecommendRoomModel;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

public class RecommendRoomViewHolder extends RecyclerView.ViewHolder {

    public final static String TAG = "RecommendRoomViewHolder";

    ExTextView mFriendsTv;
    ExTextView mMoreTv;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mFriendsRecycle;

    FriendRoomHorizontalAdapter mFriendRoomAdapter;
    GameAdapter.GameAdapterListener mListener;

    public RecommendRoomViewHolder(View itemView, Context context, GameAdapter.GameAdapterListener listener) {
        super(itemView);

        this.mListener = listener;

        mFriendsTv = (ExTextView) itemView.findViewById(R.id.friends_tv);
        mMoreTv = (ExTextView) itemView.findViewById(R.id.more_tv);
        mRefreshLayout = (SmartRefreshLayout) itemView.findViewById(R.id.refreshLayout);
        mFriendsRecycle = (RecyclerView) itemView.findViewById(R.id.friends_recycle);

        mFriendsRecycle.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        mFriendRoomAdapter = new FriendRoomHorizontalAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                if (model != null) {
                    FriendRoomModel friendRoomModel = (FriendRoomModel) model;
                    if (mListener != null) {
                        mListener.enterRoom(friendRoomModel);
                    }
                } else {
                    MyLog.w(TAG, "onItemClicked model = null");
                }
            }
        });

        mMoreTv.setOnClickListener(new DebounceViewClickListener() {
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
        mFriendRoomAdapter.setDataList(recommendRoomModel.getRoomModels());
    }
}
