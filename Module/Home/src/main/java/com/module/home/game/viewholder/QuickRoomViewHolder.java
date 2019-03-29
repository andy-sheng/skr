package com.module.home.game.viewholder;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.log.MyLog;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.home.R;
import com.module.home.game.adapter.GrabSelectAdapter;
import com.module.home.game.model.QuickJoinRoomModel;

public class QuickRoomViewHolder extends RecyclerView.ViewHolder {

    public final static String TAG = "QuickRoomViewHolder";

    ExTextView mQuickEnter;
    RecyclerView mFriendsRecycle;

    GrabSelectAdapter mGrabSelectAdapter;

    public QuickRoomViewHolder(View itemView, Context context) {
        super(itemView);

        mQuickEnter = (ExTextView) itemView.findViewById(R.id.quick_enter);
        mFriendsRecycle = (RecyclerView) itemView.findViewById(R.id.friends_recycle);

        mFriendsRecycle.setLayoutManager(new GridLayoutManager(context, 2));
        mGrabSelectAdapter = new GrabSelectAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                MyLog.d(TAG, "onItemClicked" + " view=" + view + " position=" + position + " model=" + model);
            }
        });

        mFriendsRecycle.setAdapter(mGrabSelectAdapter);
    }

    public void bindData(QuickJoinRoomModel quickJoinRoomModel) {
        mGrabSelectAdapter.setDataList(quickJoinRoomModel.getModelList());
    }

}
