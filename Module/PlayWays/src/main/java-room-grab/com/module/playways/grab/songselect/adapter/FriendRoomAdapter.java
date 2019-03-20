package com.module.playways.grab.songselect.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.grab.songselect.model.FriendRoomModel;
import com.module.playways.grab.songselect.viewholder.FriendRoomViewHolder;
import com.module.rank.R;

public class FriendRoomAdapter extends DiffAdapter<FriendRoomModel, FriendRoomViewHolder> {

    RecyclerOnItemClickListener<FriendRoomModel> mOnItemClickListener;

    public FriendRoomAdapter(RecyclerOnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @NonNull
    @Override
    public FriendRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_room_view_holder_layout, parent, false);
        FriendRoomViewHolder itemHolder = new FriendRoomViewHolder(view);
        return itemHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRoomViewHolder holder, int position) {
        FriendRoomModel friendRoomModel = mDataList.get(position);
        holder.bindData(friendRoomModel, position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

}
