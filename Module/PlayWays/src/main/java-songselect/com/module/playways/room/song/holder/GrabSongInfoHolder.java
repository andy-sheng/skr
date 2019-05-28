package com.module.playways.room.song.holder;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.log.MyLog;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.R;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Common.StandPlayType;

public class GrabSongInfoHolder extends RecyclerView.ViewHolder {
    SongModel mSongModel;
    int position;

//    SimpleDraweeView mSongCoverIv;
    ExTextView mSongSelectTv;
    ExTextView mSongNameTv;
    ExTextView mChorusSongTag;
    ExTextView mPkSongTag;
//    ExTextView mSongOwnerTv;

    public GrabSongInfoHolder(View itemView, RecyclerOnItemClickListener recyclerOnItemClickListener) {
        super(itemView);

//        mSongCoverIv = (SimpleDraweeView) itemView.findViewById(R.id.song_cover_iv);
        mSongSelectTv = (ExTextView) itemView.findViewById(R.id.song_select_tv);
        mSongNameTv = (ExTextView) itemView.findViewById(R.id.song_name_tv);
        mChorusSongTag = (ExTextView) itemView.findViewById(R.id.chorus_song_tag);
        mPkSongTag = (ExTextView) itemView.findViewById(R.id.pk_song_tag);
//        mSongOwnerTv = (ExTextView) itemView.findViewById(R.id.song_owner_tv);

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
//        mSongOwnerTv.setText(mSongModel.getOwner());
        int strokeColor = Color.parseColor("#0C2275");
//        if (!TextUtils.isEmpty(mSongModel.getCover())) {
//            FrescoWorker.loadImage(mSongCoverIv, ImageFactory.newPathImage(mSongModel.getCover())
//                    .setCornerRadius(U.getDisplayUtils().dip2px(4))
//                    .setBorderWidth(U.getDisplayUtils().dip2px(2))
//                    .setBorderColor(strokeColor)
//                    //压缩一把
//                    .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_160.getW()).build())
//                    .build()
//            );
//        } else {
//            FrescoWorker.loadImage(mSongCoverIv, ImageFactory.newResImage(R.drawable.xuanzegequ_wufengmian)
//                    .setCornerRadius(U.getDisplayUtils().dip2px(4)).setBorderWidth(U.getDisplayUtils().dip2px(2))
//                    .setBorderColor(strokeColor)
//                    .build());
//        }

        if (mSongModel.getPlayType() == StandPlayType.PT_SPK_TYPE.getValue()) {
            mSongNameTv.setPadding(0, 0, U.getDisplayUtils().dip2px(125), 0);
            mPkSongTag.setVisibility(View.VISIBLE);
            mChorusSongTag.setVisibility(View.GONE);
        } else if (mSongModel.getPlayType() == StandPlayType.PT_CHO_TYPE.getValue()) {
            mSongNameTv.setPadding(0, 0, U.getDisplayUtils().dip2px(125), 0);
            mPkSongTag.setVisibility(View.GONE);
            mChorusSongTag.setVisibility(View.VISIBLE);
        } else {
            mSongNameTv.setPadding(0, 0, U.getDisplayUtils().dip2px(80), 0);
            mPkSongTag.setVisibility(View.GONE);
            mChorusSongTag.setVisibility(View.GONE);
        }
    }
}
