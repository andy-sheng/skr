package com.module.rankingmode.song.holder;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.rankingmode.R;
import com.module.rankingmode.song.model.SongModel;

public class SongInfoHolder extends RecyclerView.ViewHolder {

    SongModel mSongModel;
    int position;

    SimpleDraweeView mSongCoverIv;
    ExTextView mSongNameTv;
    ExTextView mSongOwnerTv;
    ExTextView mSongSelectTv;

    public SongInfoHolder(View itemView, RecyclerOnItemClickListener recyclerOnItemClickListener) {
        super(itemView);

        mSongCoverIv = (SimpleDraweeView) itemView.findViewById(R.id.song_cover_iv);
        mSongNameTv = (ExTextView) itemView.findViewById(R.id.song_name_tv);
        mSongOwnerTv = (ExTextView) itemView.findViewById(R.id.song_owner_tv);
        mSongSelectTv = (ExTextView) itemView.findViewById(R.id.song_select_tv);

        mSongSelectTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recyclerOnItemClickListener != null) {
                    recyclerOnItemClickListener.onItemClicked(itemView, position, mSongModel);
                }
            }
        });
    }

    public void bind(int position, SongModel songModel) {
        this.position = position;
        this.mSongModel = songModel;

        mSongNameTv.setText(mSongModel.getItemName());
        mSongOwnerTv.setText(mSongModel.getOwner());
        FrescoWorker.loadImage(mSongCoverIv,ImageFactory.newHttpImage(mSongModel.getCover())
                .setCornerRadius(U.getDisplayUtils().dip2px(4)).setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColor(Color.parseColor("#0C2275")).build());
    }

}
