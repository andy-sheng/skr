package com.module.home.game.viewholder;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.friends.SpecialModel;
import com.module.home.R;
import com.module.home.game.adapter.GameAdapter;
import com.module.home.game.adapter.GrabSelectAdapter;
import com.module.home.game.model.QuickJoinRoomModel;

public class QuickRoomViewHolder extends RecyclerView.ViewHolder {

    public final static String TAG = "QuickRoomViewHolder";

    RecyclerView mFriendsRecycle;

    GrabSelectAdapter mGrabSelectAdapter;

    GameAdapter.GameAdapterListener mListener;

    public QuickRoomViewHolder(View itemView, BaseFragment baseFragment, GameAdapter.GameAdapterListener listener) {
        super(itemView);
        this.mListener = listener;

        mFriendsRecycle = (RecyclerView) itemView.findViewById(R.id.friends_recycle);
        mFriendsRecycle.setFocusableInTouchMode(false);
        mFriendsRecycle.setLayoutManager(new GridLayoutManager(baseFragment.getContext(), 2));
        mGrabSelectAdapter = new GrabSelectAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                if (model != null && model instanceof SpecialModel) {
                    SpecialModel specialModel = (SpecialModel) model;
                    if (mListener != null) {
                        mListener.selectSpecial(specialModel);
                    }
                }
            }
        });

        mFriendsRecycle.setAdapter(mGrabSelectAdapter);
    }

    public void bindData(QuickJoinRoomModel quickJoinRoomModel) {
        mGrabSelectAdapter.setDataList(quickJoinRoomModel.getModelList());
    }

}
