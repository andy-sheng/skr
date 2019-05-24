package com.zq.person.holder;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

    int mPosition;
    boolean mIsPlay;
    ProducationModel mModel;

    ExRelativeLayout mCoverArea;
    SimpleDraweeView mCoverIv;
    ExImageView mPlayBackIv;
    ExTextView mSongNameTv;
    ExTextView mSongOwnerTv;
    RelativeLayout mShareArea;
    RelativeLayout mDeleArea;
    RelativeLayout mPlayNumArea;
    TextView mPlayNumTv;

    public ProducationHolder(View itemView, final ProducationAdapter.Listener listener, boolean mIsSelf) {
        super(itemView);
        itemView.setTag(this);
        mCoverArea = (ExRelativeLayout) itemView.findViewById(R.id.cover_area);
        mCoverIv = (SimpleDraweeView) itemView.findViewById(R.id.cover_iv);
        mPlayBackIv = (ExImageView) itemView.findViewById(R.id.play_back_iv);
        mSongNameTv = (ExTextView) itemView.findViewById(R.id.song_name_tv);
        mSongOwnerTv = (ExTextView) itemView.findViewById(R.id.song_owner_tv);
        mShareArea = (RelativeLayout) itemView.findViewById(R.id.share_area);
        mDeleArea = (RelativeLayout) itemView.findViewById(R.id.dele_area);
        mPlayNumArea = (RelativeLayout) itemView.findViewById(R.id.play_num_area);
        mPlayNumTv = (TextView) itemView.findViewById(R.id.play_num_tv);

        if (mIsSelf) {
            mDeleArea.setVisibility(View.VISIBLE);
        } else {
            mDeleArea.setVisibility(View.GONE);
        }

        mShareArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (listener != null) {
                    listener.onClickShare(mPosition, mModel);
                }
            }
        });

        mDeleArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (listener != null) {
                    listener.onClickDele(mPosition, mModel);
                }
            }
        });

        mPlayBackIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mIsPlay) {
                    if (listener != null) {
                        listener.onClickPlayBtn(v, !mIsPlay, mPosition, mModel);
                    }
                } else {
                    if (listener != null) {
                        listener.onClickPlayBtn(v, !mIsPlay, mPosition, mModel);
                    }
                }
            }
        });
    }

    public void bindData(int position, ProducationModel model, boolean isPlay) {
        MyLog.d(TAG, "bindData" + " position=" + position + " model=" + model + " isPlay=" + isPlay);
        this.mPosition = position;
        this.mModel = model;
        this.mIsPlay = isPlay;

        if (model != null) {
            mSongNameTv.setText("" + model.getName());
            mSongOwnerTv.setText(MyUserInfoManager.getInstance().getNickName());
            mPlayNumTv.setText("" + model.getPlayCnt() + "次播放");
        }

        mSongOwnerTv.setText(model.getNickName());
        if (!TextUtils.isEmpty(model.getCover())) {
            FrescoWorker.loadImage(mCoverIv,
                    ImageFactory.newPathImage(model.getCover())
                            .setCornerRadius(U.getDisplayUtils().dip2px(7))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setBorderColor(U.getColor(R.color.white))
                            .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_160.getW()).build())
                            .build());
        } else {
            FrescoWorker.loadImage(mCoverIv,
                    ImageFactory.newResImage(R.drawable.xuanzegequ_wufengmian)
                            .setCornerRadius(U.getDisplayUtils().dip2px(7))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setBorderColor(U.getColor(R.color.white))
                            .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_160.getW()).build())
                            .build());
        }
        setPlayBtn(isPlay);
    }

    public void setPlayBtn(boolean play) {
        mIsPlay = play;
        if (play) {
            mPlayBackIv.setBackgroundResource(R.drawable.grab_works_pause);
        } else {
            mPlayBackIv.setBackgroundResource(R.drawable.grab_works_play);
        }
    }

    public void setPlaycnt(int playCnt) {
        mPlayNumTv.setText("" + playCnt + "次播放");
    }
}
