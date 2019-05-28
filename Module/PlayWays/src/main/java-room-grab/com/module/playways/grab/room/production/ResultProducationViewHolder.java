package com.module.playways.grab.room.production;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

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
import com.component.busilib.SkrConfig;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.R;
import com.module.playways.grab.room.model.WorksUploadModel;

public class ResultProducationViewHolder extends RecyclerView.ViewHolder {

    public final static String TAG = "ResultProducationViewHolder";

    WorksUploadModel mWonderfulMomentModel;

    int mPosition;
    boolean mIsPlay;

    ExImageView mBlightTipsIv;
    ExRelativeLayout mCoverArea;
    SimpleDraweeView mCoverIv;
    ExImageView mPlayBackIv;
    RelativeLayout mSaveShareArea;
    ExImageView mSaveShareIv;
    ExTextView mSaveShareTv;
    ExTextView mSongNameTv;
    ExTextView mSongOwnerTv;

    ResultProducationAdapter.Listener mListener;

    public ResultProducationViewHolder(View itemView, ResultProducationAdapter.Listener listener) {
        super(itemView);
        this.mListener = listener;
        itemView.setTag(this);
        mBlightTipsIv = (ExImageView) itemView.findViewById(R.id.blight_tips_iv);
        mCoverArea = (ExRelativeLayout) itemView.findViewById(R.id.cover_area);
        mCoverIv = (SimpleDraweeView) itemView.findViewById(R.id.cover_iv);
        mPlayBackIv = (ExImageView) itemView.findViewById(R.id.play_back_iv);
        mSaveShareArea = (RelativeLayout) itemView.findViewById(R.id.save_share_area);
        mSaveShareIv = (ExImageView) itemView.findViewById(R.id.save_share_iv);
        mSaveShareTv = (ExTextView) itemView.findViewById(R.id.save_share_tv);
        mSongNameTv = (ExTextView) itemView.findViewById(R.id.song_name_tv);
        mSongOwnerTv = (ExTextView) itemView.findViewById(R.id.song_owner_tv);

        mPlayBackIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mIsPlay) {
                    if (mListener != null) {
                        mListener.onClickPlayBtn(v, !mIsPlay, mPosition, mWonderfulMomentModel);
                    }

                } else {
                    if (mListener != null) {
                        mListener.onClickPlayBtn(v, !mIsPlay, mPosition, mWonderfulMomentModel);
                    }
                }
            }
        });

        mSaveShareArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickSaveAndShare(mPosition, mWonderfulMomentModel);
                }
            }
        });
    }

    public void bindData(int position, WorksUploadModel momentModel, boolean isPlay) {
        this.mPosition = position;
        this.mWonderfulMomentModel = momentModel;
        this.mIsPlay = isPlay;

        if (mWonderfulMomentModel != null && !TextUtils.isEmpty(mWonderfulMomentModel.getSongModel().getCover())) {
            mSongNameTv.setText(mWonderfulMomentModel.getSongModel().getDisplaySongName());
            if (mWonderfulMomentModel.isBlight()) {
                mBlightTipsIv.setVisibility(View.VISIBLE);
            } else {
                mBlightTipsIv.setVisibility(View.GONE);
            }
            if (SkrConfig.getInstance().worksShareOpen()) {
                if (momentModel.getWorksID() > 0) {
                    mSaveShareIv.setBackgroundResource(R.drawable.grab_works_share_icon);
                    mSaveShareTv.setText("分享");
                } else {
                    mSaveShareTv.setText("保存分享");
                    mSaveShareIv.setBackgroundResource(R.drawable.grab_works_save_share);
                }
            } else {
                if (momentModel.getWorksID() > 0) {
                    mSaveShareIv.setBackgroundResource(R.drawable.grab_works_has_saved);
                    mSaveShareTv.setText("已保存");
                } else {
                    mSaveShareTv.setText("保存");
                    mSaveShareIv.setBackgroundResource(R.drawable.grab_works_save_share);
                }
            }

        } else {
            MyLog.w(TAG, "bindData" + " position=" + position + " momentModel=" + momentModel + " isPlay=" + isPlay);
        }

        mSongOwnerTv.setText(MyUserInfoManager.getInstance().getNickName());
        FrescoWorker.loadImage(mCoverIv,
                ImageFactory.newPathImage(MyUserInfoManager.getInstance().getAvatar())
                        .setCornerRadius(U.getDisplayUtils().dip2px(7))
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(U.getColor(R.color.white))
                        .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_160.getW()).build())
                        .build());

        setPlayBtn(isPlay);
    }

    public void setPlayBtn(boolean b) {
        mIsPlay = b;
        if (mIsPlay) {
            mPlayBackIv.setBackgroundResource(R.drawable.grab_works_pause);
        } else {
            mPlayBackIv.setBackgroundResource(R.drawable.grab_works_play);
        }
    }
}
