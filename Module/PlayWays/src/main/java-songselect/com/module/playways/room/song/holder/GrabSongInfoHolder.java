package com.module.playways.room.song.holder;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.R;
import com.module.playways.room.song.adapter.SongSelectAdapter;
import com.module.playways.room.song.model.SongModel;
import com.component.live.proto.Common.StandPlayType;

public class GrabSongInfoHolder extends RecyclerView.ViewHolder {
    SongModel mSongModel;
    int position;

    ExTextView mSongSelectTv;
    ExTextView mSongNameTv;
    ExTextView mSongTag;
    TextView mSongDesc;

    public GrabSongInfoHolder(View itemView, RecyclerOnItemClickListener recyclerOnItemClickListener, boolean canAdd) {
        super(itemView);

        mSongSelectTv = itemView.findViewById(R.id.song_select_tv);
        mSongNameTv = itemView.findViewById(R.id.song_name_tv);
        mSongTag = itemView.findViewById(R.id.song_tag);
        mSongDesc = itemView.findViewById(R.id.song_desc);

        if (canAdd) {
            mSongSelectTv.setText("点歌");
        } else {
            mSongSelectTv.setText("想唱");
        }
        mSongSelectTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (recyclerOnItemClickListener != null) {
                    recyclerOnItemClickListener.onItemClicked(itemView, position, mSongModel);
                }
            }
        });
    }

    public void bind(int position, SongModel songModel) {
        this.position = position;
        this.mSongModel = songModel;

        mSongNameTv.setText(mSongModel.getDisplaySongName());
        if (TextUtils.isEmpty(songModel.getSongDesc())) {
            mSongDesc.setVisibility(View.GONE);
        } else {
            mSongDesc.setVisibility(View.VISIBLE);
            mSongDesc.setText(mSongModel.getSongDesc());
        }

        if (mSongModel.getPlayType() == StandPlayType.PT_SPK_TYPE.getValue()) {
            mSongTag.setBackground(SongSelectAdapter.pk);
            mSongTag.setText("PK");
        } else if (mSongModel.getPlayType() == StandPlayType.PT_CHO_TYPE.getValue()) {
            mSongTag.setBackground(SongSelectAdapter.togather);
            mSongTag.setText("合唱");
        } else if (mSongModel.getPlayType() == StandPlayType.PT_MINI_GAME_TYPE.getValue()) {
            mSongTag.setBackground(SongSelectAdapter.game);
            mSongTag.setText("双人游戏");
        } else {
            mSongTag.setVisibility(View.GONE);
        }
    }
}
