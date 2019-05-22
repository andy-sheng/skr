package com.zq.person.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.log.MyLog;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zq.person.adapter.ProducationAdapter;
import com.zq.person.model.ProducationModel;

public class ProducationHolder extends RecyclerView.ViewHolder {

    public final static String TAG = "ProducationHolder";

    int position;
    boolean isPlay;
    ProducationModel model;

    ExRelativeLayout mCoverArea;
    SimpleDraweeView mCoverIv;
    ExImageView mPlayBackIv;
    ExTextView mSongNameTv;
    ExTextView mSongOwnerTv;
    RelativeLayout mShareArea;
    RelativeLayout mDeleArea;
    RelativeLayout mPlayNumArea;
    TextView mPlayNumTv;

    public ProducationHolder(View itemView, final ProducationAdapter.Listener listener, boolean mHasDeleted) {
        super(itemView);

        mCoverArea = (ExRelativeLayout) itemView.findViewById(R.id.cover_area);
        mCoverIv = (SimpleDraweeView) itemView.findViewById(R.id.cover_iv);
        mPlayBackIv = (ExImageView) itemView.findViewById(R.id.play_back_iv);
        mSongNameTv = (ExTextView) itemView.findViewById(R.id.song_name_tv);
        mSongOwnerTv = (ExTextView) itemView.findViewById(R.id.song_owner_tv);
        mShareArea = (RelativeLayout) itemView.findViewById(R.id.share_area);
        mDeleArea = (RelativeLayout) itemView.findViewById(R.id.dele_area);
        mPlayNumArea = (RelativeLayout) itemView.findViewById(R.id.play_num_area);
        mPlayNumTv = (TextView) itemView.findViewById(R.id.play_num_tv);

        if (mHasDeleted) {
            mDeleArea.setVisibility(View.VISIBLE);
        } else {
            mDeleArea.setVisibility(View.GONE);
        }

        mShareArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (listener != null) {
                    listener.onClickShare(position, model);
                }
            }
        });

        mDeleArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (listener != null) {
                    listener.onClickDele(position, model);
                }
            }
        });

        mPlayBackIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (isPlay) {
                    if (listener != null) {
                        listener.onClickPause(position, model);
                    }
                } else {
                    if (listener != null) {
                        listener.onClickPlay(position, model);
                    }
                }
            }
        });
    }

    public void bindData(int position, ProducationModel model, boolean isPlay) {
        MyLog.d(TAG, "bindData" + " position=" + position + " model=" + model + " isPlay=" + isPlay);
        this.position = position;
        this.model = model;
        this.isPlay = isPlay;

        if (model != null) {
            mSongNameTv.setText("" + model.getName());
            mSongOwnerTv.setText(MyUserInfoManager.getInstance().getNickName());
            mPlayNumTv.setText("" + model.getPlayCnt() + "次播放");
        }

        mSongOwnerTv.setText(MyUserInfoManager.getInstance().getNickName());
        FrescoWorker.loadImage(mCoverIv,
                ImageFactory.newPathImage(MyUserInfoManager.getInstance().getAvatar())
                        .setCornerRadius(U.getDisplayUtils().dip2px(7))
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(U.getColor(R.color.white))
                        .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_160.getW()).build())
                        .build());


        if (isPlay) {
            mPlayBackIv.setBackgroundResource(R.drawable.grab_works_pause);
        } else {
            mPlayBackIv.setBackgroundResource(R.drawable.grab_works_play);
        }

    }

}
