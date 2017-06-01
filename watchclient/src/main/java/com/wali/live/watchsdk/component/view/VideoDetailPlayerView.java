package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.base.image.fresco.BaseImageView;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.engine.player.widget.VideoPlayerTextureView;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.video.widget.player.DetailSeekBar;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.watch.presenter.VideoPlayerPresenterEx;

/**
 * Created by zyh on 2017/05/31.
 *
 * @module [TODO add module]
 */
public class VideoDetailPlayerView extends RelativeLayout
        implements IComponentView<VideoDetailPlayerView.IPresenter, VideoDetailPlayerView.IView>, View.OnClickListener {
    private static final String TAG = "VideoDetailPlayerView";

    private RoomBaseDataModel mMyRoomData;
    @Nullable
    protected IPresenter mPresenter;
    private VideoPlayerPresenterEx mVideoPlayerPresenterEx;

    private VideoPlayerTextureView mVideoPlayerView;
    private DetailSeekBar mDetailSeekBar;
    private ImageView mLoadingIv;
    private ImageButton mPlayBtn;
    private BaseImageView mCoverIv;

    public VideoDetailPlayerView(Context context) {
        this(context, null);
    }

    public VideoDetailPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoDetailPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public void setMyRoomData(RoomBaseDataModel myRoomData) {
        mMyRoomData = myRoomData;
        if (mMyRoomData != null) {
            AvatarUtils.loadAvatarByUid(mCoverIv, mMyRoomData.getUid(), false);
            startPlayer();
        }
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.float_video_view, this);
        {
            mLoadingIv = $(R.id.loading_iv);
            mDetailSeekBar = $(R.id.detail_seek_bar);
            mCoverIv = $(R.id.cover_iv);
            mPlayBtn = $(R.id.play_button);
        }
        {
            mVideoPlayerView = $(R.id.video_player_texture_view);
            mVideoPlayerPresenterEx = new VideoPlayerPresenterEx(this.getContext(), mVideoPlayerView, mDetailSeekBar, null, false);
            mVideoPlayerPresenterEx.setSeekBarHideDelay(4000);
            mVideoPlayerPresenterEx.setSeekBarFullScreenBtnVisible(false);
            $click(mVideoPlayerView, this);
        }
    }

    private void startPlayer() {
        if (mVideoPlayerPresenterEx != null) {
            if (!mVideoPlayerPresenterEx.isActivate()) {
                mVideoPlayerPresenterEx.play(mMyRoomData.getVideoUrl());//, mVideoContainer, false, VideoPlayerTextureView.TRANS_MODE_CENTER_INSIDE, true, true);
                mVideoPlayerPresenterEx.setTransMode(VideoPlayerTextureView.TRANS_MODE_CENTER_INSIDE);
            }
        }
    }

    private void stopPlayer() {
        if (mVideoPlayerPresenterEx != null) {
            mVideoPlayerPresenterEx.destroy();
        }
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) VideoDetailPlayerView.this;
            }
        }
        return new ComponentView();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.play_btn) {

        } else if (i == R.id.full_screen_btn) {

        } else if (i == R.id.video_player_texture_view) {
            mVideoPlayerPresenterEx.onSeekBarContainerClick();
        }
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy {
    }
}
