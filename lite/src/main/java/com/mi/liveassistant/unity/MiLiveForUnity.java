package com.mi.liveassistant.unity;

import android.app.Activity;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.engine.player.widget.VideoPlayerPresenter;
import com.mi.liveassistant.engine.player.widget.VideoPlayerView;
import com.mi.liveassistant.room.presenter.streamer.PullStreamerPresenter;
import com.xiaomi.player.Player;

/**
 * Created by yangli on 2017/4/22.
 *
 * @module Unity直播辅助类
 */
public class MiLiveForUnity {
    private static final String TAG = "MiLiveForUnity";

    protected Activity mActivity;

    protected VideoPlayerView mSurfaceView;

    protected VideoPlayerPresenter mVideoPlayerPresenter;
    protected PullStreamerPresenter mStreamerPresenter;

    public MiLiveForUnity(Activity activity) {
        MyLog.w(TAG, "MiLiveForUnity");
        mActivity = activity;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "MiLiveForUnity addContentView");
                mStreamerPresenter = new PullStreamerPresenter();

                LayoutInflater inflater = mActivity.getLayoutInflater();
                Resources resources = mActivity.getResources();
                String pkgName = mActivity.getPackageName();

                int id = resources.getIdentifier("layout_for_unity", "layout", pkgName);
                View view = inflater.inflate(id, null);

                for (int i = 0; i < ((ViewGroup) view).getChildCount(); ++i) {
                    View subView = ((ViewGroup) view).getChildAt(i);
                    if (subView instanceof VideoPlayerView) {
                        mSurfaceView = (VideoPlayerView) subView;
                        break;
                    }
                }
                mSurfaceView.setZOrderMediaOverlay(true);
//                mSurfaceView.setZOrderOnTop(true);

                mVideoPlayerPresenter = mSurfaceView.getVideoPlayerPresenter();
                mVideoPlayerPresenter.setRealTime(true);
                mVideoPlayerPresenter.setVideoStreamBufferTime(2);

                FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                mActivity.addContentView(view, param);

                mStreamerPresenter.setStreamer(mVideoPlayerPresenter);
            }
        });
    }

    public void startLive(final @NonNull String videoUrl) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "startLive videoUrl=" + videoUrl);
                mStreamerPresenter.setOriginalStreamUrl(videoUrl);
                mStreamerPresenter.startLive();
                ViewGroup.LayoutParams layoutParams = mSurfaceView.getLayoutParams();
                mVideoPlayerPresenter.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFit,
                        layoutParams.width, layoutParams.height);
                MyLog.w(TAG, "startLive done");
            }
        });
    }

    public void stopLive() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStreamerPresenter.stopLive();
                MyLog.w(TAG, "stopLive done");
            }
        });
    }

    public void destroy() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStreamerPresenter.destroy();
                mStreamerPresenter = null;
                mVideoPlayerPresenter.destroy();
                mVideoPlayerPresenter = null;
            }
        });
    }
}
