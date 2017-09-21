package com.wali.live.watchsdk.longtext.holder;

import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.longtext.model.interior.item.VideoFeedItemModel;
import com.wali.live.watchsdk.longtext.view.HolderVideoView;

/**
 * Created by lan on 2017/9/20.
 */
public class VideoFeedItemHolder extends BaseFeedItemHolder<VideoFeedItemModel> {
    private ViewGroup mVideoContainer;
    private HolderVideoView mVideoView;

    private BaseImageView mCoverIv;
    private ImageView mPlayBtn;

    private TextView mDescTv;

    public VideoFeedItemHolder(View view) {
        super(view);
    }

    @Override
    protected void initView() {
        mVideoContainer = $(R.id.video_container);
        mVideoView = $(R.id.video_view);

        mCoverIv = $(R.id.cover_iv);
        mPlayBtn = $(R.id.play_btn);

        mDescTv = $(R.id.desc_tv);
    }

    @Override
    protected void bindView() {
        int height;
        int width = DisplayUtils.getScreenWidth() - (DisplayUtils.dip2px(13.33f) << 1);
        if (mViewModel.getWidth() > 0 && mViewModel.getHeight() > 0) {
            if (mViewModel.getHeight() >= mViewModel.getWidth()) {
                height = width;
            } else {
                height = width * mViewModel.getHeight() / mViewModel.getWidth();
            }
        } else {
            height = (DisplayUtils.getScreenWidth() * 9) >> 4;
        }

        ViewGroup.LayoutParams lp = mVideoContainer.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(width, height);
        } else {
            lp.width = width;
            lp.height = height;
        }
        mVideoContainer.setLayoutParams(lp);

        if (!TextUtils.isEmpty(mViewModel.getCoverUrl())) {
            mCoverIv.setVisibility(View.VISIBLE);
            FrescoWorker.loadImage(mCoverIv,
                    ImageFactory.newHttpImage(mViewModel.getCoverUrl())
                            .setLoadingDrawable(new ColorDrawable(itemView.getResources().getColor(R.color.color_f2f2f2)))
                            .setWidth(width)
                            .setHeight(height)
                            .build());
        } else {
            mCoverIv.setVisibility(View.GONE);
        }

        bindText(mDescTv, mViewModel.getDesc());

        if (!TextUtils.isEmpty(mViewModel.getUrl())) {
            mPlayBtn.setVisibility(View.VISIBLE);
            mPlayBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mVideoView.play(mViewModel.getUrl());
                    mPlayBtn.setVisibility(View.GONE);
                    mCoverIv.setVisibility(View.GONE);
                }
            });

            mVideoView.setHolderCallback(new HolderVideoView.HolderVideoCallback() {
                @Override
                public void process(Integer state) {
                    if (state == HolderVideoView.PLAYER_STOP) {
                        if (!TextUtils.isEmpty(mViewModel.getCoverUrl())) {
                            mCoverIv.setVisibility(View.VISIBLE);
                        }
                        mPlayBtn.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            mPlayBtn.setVisibility(View.GONE);
        }
    }
}
