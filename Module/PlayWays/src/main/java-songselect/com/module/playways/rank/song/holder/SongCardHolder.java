package com.module.playways.rank.song.holder;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.rank.song.model.SongCardModel;
import com.module.rank.R;
import com.module.playways.rank.song.adapter.SongSelectAdapter;

import static com.umeng.socialize.utils.ContextUtil.getContext;

public class SongCardHolder extends RecyclerView.ViewHolder {

    RecyclerView mSongListView;
    SongSelectAdapter mSongSelectAdapter;

    public SongCardHolder(View itemView, RecyclerOnItemClickListener onItemClickListener) {
        super(itemView);
        mSongListView = itemView.findViewById(R.id.song_list_recycle);
        mSongListView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (mSongSelectAdapter == null) {
            mSongSelectAdapter = new SongSelectAdapter(onItemClickListener);
        }
        mSongListView.setAdapter(mSongSelectAdapter);
    }

    public void bind(int position, SongCardModel songCardModel) {
        mSongSelectAdapter.setDataList(songCardModel.getList());
    }
}
