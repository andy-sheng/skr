package com.module.playways.room.song.holder;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.room.song.model.SongModel;
import com.module.playways.R;

public class SongInfoHolder extends RecyclerView.ViewHolder {

    SongModel mSongModel;
    int position;

    ExTextView mSongNameTv;
    ExTextView mSongSelectTv;
    TextView mSongDesc;
    View mDivider;

    public SongInfoHolder(View itemView, RecyclerOnItemClickListener recyclerOnItemClickListener) {
        super(itemView);

        mSongNameTv = itemView.findViewById(R.id.song_name_tv);
        mSongSelectTv = itemView.findViewById(R.id.song_select_tv);
        mSongDesc = itemView.findViewById(R.id.song_desc);
        mDivider = itemView.findViewById(R.id.divider);

        mSongSelectTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (recyclerOnItemClickListener != null) {
                    recyclerOnItemClickListener.onItemClicked(itemView, position, mSongModel);
                }
            }
        });
    }

    public void bind(int position, SongModel songModel, int size, String text) {
        this.position = position;
        this.mSongModel = songModel;

        if (position == size - 1) {
            mDivider.setVisibility(View.GONE);
        } else {
            mDivider.setVisibility(View.VISIBLE);
        }
        mSongNameTv.setText(mSongModel.getItemName());
        if (TextUtils.isEmpty(mSongModel.getSongDesc())) {
            mSongDesc.setVisibility(View.GONE);
        } else {
            mSongDesc.setVisibility(View.VISIBLE);
            mSongDesc.setText(mSongModel.getSongDesc());
        }
        mSongSelectTv.setText(text);
    }

}
