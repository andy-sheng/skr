package com.module.playways.room.song.holder;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.room.song.adapter.SongSelectAdapter;
import com.module.playways.room.song.model.SongModel;
import com.module.playways.R;

public class SongInfoHolder extends RecyclerView.ViewHolder {

    SongModel mSongModel;
    int position;
    int mode;

    SimpleDraweeView mSongCoverIv;
    ExTextView mSongNameTv;
    ExTextView mSongOwnerTv;
    ExTextView mSongSelectTv;

    public SongInfoHolder(View itemView, int mode, RecyclerOnItemClickListener recyclerOnItemClickListener) {
        super(itemView);
        this.mode = mode;

        mSongCoverIv = (SimpleDraweeView) itemView.findViewById(R.id.song_cover_iv);
        mSongNameTv = (ExTextView) itemView.findViewById(R.id.song_name_tv);
        mSongOwnerTv = (ExTextView) itemView.findViewById(R.id.song_owner_tv);
        mSongSelectTv = (ExTextView) itemView.findViewById(R.id.song_select_tv);

        if (mode == SongSelectAdapter.GRAB_MODE) {
            mSongNameTv.setTextColor(Color.parseColor("#B2B6D6"));
            mSongOwnerTv.setTextColor(Color.parseColor("#99B2B6D6"));
            mSongSelectTv.setTextColor(Color.parseColor("#2E3041"));
            mSongSelectTv.setText("添加");
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setStroke(U.getDisplayUtils().dip2px(2), Color.parseColor("#202239"));
            drawable.setCornerRadius(U.getDisplayUtils().dip2px(8));
            drawable.setColor(Color.parseColor("#EDA734"));
            mSongSelectTv.setBackground(drawable);
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


        mSongNameTv.setText(mSongModel.getItemName());
        mSongOwnerTv.setText(mSongModel.getOwner());
        int strokeColor = mode == SongSelectAdapter.GRAB_MODE ? Color.parseColor("#202239") : Color.parseColor("#0C2275");
        if (!TextUtils.isEmpty(mSongModel.getCover())) {
            FrescoWorker.loadImage(mSongCoverIv, ImageFactory.newPathImage(mSongModel.getCover())
                    .setCornerRadius(U.getDisplayUtils().dip2px(4))
                    .setBorderWidth(U.getDisplayUtils().dip2px(2))
                    .setBorderColor(strokeColor)
                    //压缩一把
                    .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_160.getW()).build())
                    .build()
            );
        } else {
            FrescoWorker.loadImage(mSongCoverIv, ImageFactory.newResImage(R.drawable.xuanzegequ_wufengmian)
                    .setCornerRadius(U.getDisplayUtils().dip2px(4)).setBorderWidth(U.getDisplayUtils().dip2px(2))
                    .setBorderColor(strokeColor)
                    .build());
        }

    }

}
