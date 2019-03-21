package com.module.playways.grab.songselect.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.grab.songselect.model.FriendRoomModel;
import com.module.playways.grab.songselect.viewholder.FriendRoomVeritViewHolder;
import com.module.rank.R;

public class FriendRoomVeritAdapter extends DiffAdapter<FriendRoomModel, FriendRoomVeritViewHolder> {

    RecyclerOnItemClickListener<FriendRoomModel> mOnItemClickListener;

    public FriendRoomVeritAdapter(RecyclerOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @NonNull
    @Override
    public FriendRoomVeritViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_room_verit_item_layout, parent, false);
        FriendRoomVeritViewHolder itemHolder = new FriendRoomVeritViewHolder(view);
        itemHolder.setOnItemClickListener(mOnItemClickListener);
        return itemHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRoomVeritViewHolder holder, int position) {
        FriendRoomModel friendRoomModel = mDataList.get(position);
        holder.bindData(friendRoomModel, position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }
}
