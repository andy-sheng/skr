package com.respicker.preview.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.base.R;
import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.player.VideoPlayerAdapter;
import com.common.utils.U;
import com.respicker.model.VideoItem;

public class VideoPreviewView extends RelativeLayout {

    BaseImageView mPreviewIv;
    ImageView mPlayBtn;
    VideoItem mVideoItem;
    TextureView mPlayerView;
    VideoPlayerAdapter mVideoPlayerAdapter;

    public VideoPreviewView(Context context) {
        super(context);
        init();
    }

    public VideoPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {
        inflate(getContext(), R.layout.video_preivew_view_layout, this);
        mPreviewIv = (BaseImageView) this.findViewById(R.id.preview_iv);
        mPlayBtn = (ImageView) this.findViewById(R.id.play_btn);
        mPlayBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                U.getToastUtil().showShort("播放");
                mVideoPlayerAdapter.play();
            }
        });
        mPlayerView = this.findViewById(R.id.player_view);

        mVideoPlayerAdapter = new VideoPlayerAdapter();
        mVideoPlayerAdapter.setTextureView(mPlayerView);

    }

    public void bind(VideoItem videoItem) {
        mVideoItem = videoItem;
        FrescoWorker.loadImage(mPreviewIv, ImageFactory.newLocalImage(videoItem.getPath())
                .build());
        mVideoPlayerAdapter.setVideoPath(mVideoItem.getPath());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mVideoPlayerAdapter != null) {
            mVideoPlayerAdapter.destroy();
        }
    }
}
