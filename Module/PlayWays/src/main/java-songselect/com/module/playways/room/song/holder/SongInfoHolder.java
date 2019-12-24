package com.module.playways.room.song.holder;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.room.song.adapter.SongSelectAdapter;
import com.module.playways.room.song.model.SongModel;
import com.module.playways.R;

public class SongInfoHolder extends RecyclerView.ViewHolder {

    SongModel mSongModel;
    int position;

    ExTextView mSongNameTv;
    ExTextView mSongSelectTv;
    TextView mSongDesc;
    View mDivider;

    public SongInfoHolder(View itemView, SongSelectAdapter.Listener recyclerOnItemClickListener, String text) {
        super(itemView);

        mSongNameTv = itemView.findViewById(R.id.song_name_tv);
        mSongSelectTv = itemView.findViewById(R.id.song_select_tv);
        mSongDesc = itemView.findViewById(R.id.song_desc);
        mDivider = itemView.findViewById(R.id.divider);

        mSongSelectTv.setText(text);
        mSongSelectTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (recyclerOnItemClickListener != null) {
                    recyclerOnItemClickListener.onClickSelect(position, mSongModel);
                }
            }
        });

        mSongNameTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (recyclerOnItemClickListener != null) {
                    recyclerOnItemClickListener.onClickSongName(position, mSongModel);
                }
            }
        });

        mSongDesc.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (recyclerOnItemClickListener != null) {
                    recyclerOnItemClickListener.onClickSongName(position, mSongModel);
                }
            }
        });
    }

    public void setTextColorWhite(boolean colorWhite) {
        if (colorWhite) {
            mSongNameTv.setTextColor(U.getColor(R.color.white_trans_80));
            mSongDesc.setTextColor(U.getColor(R.color.white_trans_50));
        } else {
            mSongNameTv.setTextColor(U.getColor(R.color.black_trans_80));
            mSongDesc.setTextColor(U.getColor(R.color.black_trans_50));
        }
    }

    public void bind(int position, SongModel songModel, int size) {
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
    }

}
