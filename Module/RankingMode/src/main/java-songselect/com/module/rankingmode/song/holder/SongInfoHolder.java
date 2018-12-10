package com.module.rankingmode.song.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rankingmode.R;
import com.module.rankingmode.song.model.SongModel;

public class SongInfoHolder extends RecyclerView.ViewHolder {
    ExTextView mSongNameTv;
    SongModel mSongModel;

    RecyclerOnItemClickListener mRecyclerOnItemClickListener;

    public SongInfoHolder(View itemView) {
        super(itemView);
        mSongNameTv = (ExTextView) itemView.findViewById(R.id.song_name_tv);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecyclerOnItemClickListener != null) {
                    mRecyclerOnItemClickListener.onItemClicked(itemView,-1,mSongModel);
                }
            }
        });
    }

    public void bind(int position, SongModel songModel) {
        mSongModel = songModel;
        mSongNameTv.setText(mSongModel.getItemName());
    }

    public void setListener(RecyclerOnItemClickListener recyclerOnItemClickListener) {
        mRecyclerOnItemClickListener = recyclerOnItemClickListener;
    }
}
