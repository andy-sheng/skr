package com.component.busilib.friends;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.log.MyLog;
import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;

/**
 * 横向好友在线适配器
 */
public class FriendRoomHorizontalAdapter extends DiffAdapter<RecommendModel, FriendRoomHorizontalViewHolder> {

    RecyclerOnItemClickListener<RecommendModel> mOnItemClickListener;

    public FriendRoomHorizontalAdapter(RecyclerOnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @NonNull
    @Override
    public FriendRoomHorizontalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_room_horiz_item_layout, parent, false);
        FriendRoomHorizontalViewHolder itemHolder = new FriendRoomHorizontalViewHolder(view);
        itemHolder.setOnItemClickListener(mOnItemClickListener);
        return itemHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRoomHorizontalViewHolder holder, int position) {
        RecommendModel friendRoomModel = mDataList.get(position);
        holder.bindData(friendRoomModel, position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

}

