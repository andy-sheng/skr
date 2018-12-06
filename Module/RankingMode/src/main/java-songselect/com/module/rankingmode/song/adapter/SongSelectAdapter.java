package com.module.rankingmode.song.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.DiffAdapter;
import com.module.rankingmode.R;
import com.module.rankingmode.song.holder.SongInfoHolder;
import com.module.rankingmode.song.model.SongModel;

public class SongSelectAdapter extends DiffAdapter<SongModel,RecyclerView.ViewHolder> {
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_view_holder_item,parent,false);
        SongInfoHolder viewHolder = new SongInfoHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof SongInfoHolder){
            SongInfoHolder songInfoHolder = (SongInfoHolder) holder;
            SongModel songModel = mDataList.get(position);
            songInfoHolder.bind(position,songModel);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

}
