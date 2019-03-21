package com.module.playways.grab.songselect.friends;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rank.R;

public class FriendRoomVerticalAdapter extends DiffAdapter<FriendRoomModel, FriendRoomVerticalViewHolder> {

    RecyclerOnItemClickListener<FriendRoomModel> mOnItemClickListener;

    public FriendRoomVerticalAdapter(RecyclerOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @NonNull
    @Override
    public FriendRoomVerticalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_room_verit_item_layout, parent, false);
        FriendRoomVerticalViewHolder itemHolder = new FriendRoomVerticalViewHolder(view);
        itemHolder.setOnItemClickListener(mOnItemClickListener);
        return itemHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRoomVerticalViewHolder holder, int position) {
        FriendRoomModel friendRoomModel = mDataList.get(position);
        holder.bindData(friendRoomModel, position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }
}
