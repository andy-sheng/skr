package com.module.playways.grab.songselect.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.grab.songselect.model.FriendRoomModel;
import com.module.playways.grab.songselect.viewholder.FriendRoomHorizViewHolder;
import com.module.rank.R;

/**
 * 横向好友在线适配器
 */
public class FriendRoomHorizAdapter extends DiffAdapter<FriendRoomModel, FriendRoomHorizViewHolder> {

    RecyclerOnItemClickListener<FriendRoomModel> mOnItemClickListener;

    public FriendRoomHorizAdapter(RecyclerOnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @NonNull
    @Override
    public FriendRoomHorizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_room_horiz_item_layout, parent, false);
        FriendRoomHorizViewHolder itemHolder = new FriendRoomHorizViewHolder(view);
        itemHolder.setOnItemClickListener(mOnItemClickListener);
        return itemHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRoomHorizViewHolder holder, int position) {
        FriendRoomModel friendRoomModel = mDataList.get(position);
        holder.bindData(friendRoomModel, position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

}
