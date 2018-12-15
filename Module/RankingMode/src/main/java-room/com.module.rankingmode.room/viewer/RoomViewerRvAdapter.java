package com.module.rankingmode.room.viewer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rankingmode.R;
import com.module.rankingmode.room.viewer.RoomViewerHolder;
import com.module.rankingmode.room.viewer.RoomViewerModel;

public class RoomViewerRvAdapter extends DiffAdapter<RoomViewerModel, RecyclerView.ViewHolder> {

    RecyclerOnItemClickListener mRecyclerOnItemClickListener;

    public RoomViewerRvAdapter(RecyclerOnItemClickListener onItemClickListener) {
        mRecyclerOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_top_viewer_item, parent, false);
        RoomViewerHolder viewHolder = new RoomViewerHolder(view);
        viewHolder.setListener(mRecyclerOnItemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RoomViewerHolder) {
            RoomViewerModel roomViewerModel = mDataList.get(position);
            ((RoomViewerHolder) holder).bind(position, roomViewerModel);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }


}
