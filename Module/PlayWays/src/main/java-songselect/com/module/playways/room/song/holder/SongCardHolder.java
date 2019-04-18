package com.module.playways.room.song.holder;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.common.utils.U;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.room.song.model.SongCardModel;
import com.module.rank.R;
import com.module.playways.room.song.adapter.SongSelectAdapter;

import static com.umeng.socialize.utils.ContextUtil.getContext;

public class SongCardHolder extends RecyclerView.ViewHolder {

    SongCardRecycleView mSongListView;
    SongSelectAdapter mSongSelectAdapter;

    public SongCardHolder(View itemView, RecyclerOnItemClickListener onItemClickListener, int defaultCount) {
        super(itemView);
        mSongListView = itemView.findViewById(R.id.song_list_recycle);
        ViewGroup.LayoutParams lp = mSongListView.getLayoutParams();
        lp.height = defaultCount * U.getDisplayUtils().dip2px(72) + U.getDisplayUtils().dip2px(24);
        mSongListView.setLayoutParams(lp);
        mSongListView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (mSongSelectAdapter == null) {
            mSongSelectAdapter = new SongSelectAdapter(onItemClickListener);
        }
        mSongListView.setAdapter(mSongSelectAdapter);
    }

    public void bind(int position, SongCardModel songCardModel) {
        if (songCardModel != null) {
            mSongSelectAdapter.setDataList(songCardModel.getList());
        }
    }
}
