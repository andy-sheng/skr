package com.module.playways.rank.song.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.log.MyLog;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.rank.song.holder.SongCardHolder;
import com.module.playways.rank.song.model.SongCardModel;
import com.module.rank.R;

import java.util.ArrayList;
import java.util.List;

public class SongCardsAdapter extends RecyclerView.Adapter {
    public final static String TAG = "SongCardsAdapter";

    RecyclerOnItemClickListener onItemClickListener;
    List<SongCardModel> mDataList = new ArrayList<>();

    public SongCardsAdapter(RecyclerOnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_card_item_view, parent, false);
        SongCardHolder viewHolder = new SongCardHolder(view, onItemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyLog.d(TAG, "onBindViewHolder" + " holder=" + holder + " position=" + position);
        if (holder instanceof SongCardHolder) {
            SongCardHolder songCardHolder = (SongCardHolder) holder;
            SongCardModel songModel = mDataList.get(position);
            songCardHolder.bind(position, songModel);
        }
    }

    public void setmDataList(List<SongCardModel> mDataList) {
        this.mDataList = mDataList;
    }

    // 获取当前adpter中的数据
    public List<SongCardModel> getDatas(){
        return mDataList;
    }

    @Override
    public int getItemCount() {
        if (mDataList == null) {
            return 0;
        }
        return mDataList.size();
    }
}
