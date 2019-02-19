package com.module.playways.rank.song.holder;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;

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

        mSongSelectTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (recyclerOnItemClickListener != null) {
                    recyclerOnItemClickListener.onItemClicked(itemView, position, mSongModel);
                }
            }
        });
//        mSongSelectTv.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return false;
//            }
//        });
//        mSongSelectTv.getParent().requestDisallowInterceptTouchEvent(true);
    }

    public void bind(int position, SongModel songModel) {
        this.position = position;
        this.mSongModel = songModel;

        mSongNameTv.setText(mSongModel.getItemName());
        mSongOwnerTv.setText(mSongModel.getOwner());
        FrescoWorker.loadImage(mSongCoverIv, ImageFactory.newHttpImage(mSongModel.getCover())
                .setCornerRadius(U.getDisplayUtils().dip2px(4)).setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColor(Color.parseColor("#0C2275")).build());
    }

}
