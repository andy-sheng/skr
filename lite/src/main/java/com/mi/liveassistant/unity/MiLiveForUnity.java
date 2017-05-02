package com.mi.liveassistant.unity;

import android.app.Activity;
import android.content.res.Resources;
import android.support.annotation.Keep;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.engine.player.widget.VideoPlayerPresenter;
import com.mi.liveassistant.engine.player.widget.VideoPlayerView;

import java.util.ArrayList;

/**
 * Created by yangli on 2017/4/22.
 *
 * @module Unity直播辅助类
 */
@Keep
public class MiLiveForUnity {
    private static final String TAG = "MiLiveForUnity";

    protected Activity mActivity;

    protected VideoPlayerView mSurfaceView;

    protected VideoPlayerPresenter mVideoPlayerPresenter;

    @Keep
    public MiLiveForUnity(Activity activity) {
        MyLog.w(TAG, "MiLiveForUnity");
        mActivity = activity;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "MiLiveForUnity addContentView");

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
                mSurfaceView.setZOrderOnTop(true);

                mVideoPlayerPresenter = mSurfaceView.getVideoPlayerPresenter();
                mVideoPlayerPresenter.setRealTime(true);

                FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                mActivity.addContentView(view, param);

                listView(mActivity.getWindow().getDecorView(), "");
            }
        });
    }

    private void listView(View view, String depth) {
        MyLog.e(TAG, depth + "|-" + view);
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); ++i) {
                listView(((ViewGroup) view).getChildAt(i), depth + "| ");
            }
        }
    }

    @Keep
    public void startLive() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String videoUrl = "http://v2.zb.mi.com/live/3243571_1493702092.flv?playui=0";
                mVideoPlayerPresenter.setVideoPath(videoUrl, "");
                mVideoPlayerPresenter.setIpList(new ArrayList<String>(), new ArrayList<String>());
                mVideoPlayerPresenter.setVideoStreamBufferTime(2);
                MyLog.w(TAG, "startLive done");
            }
        });
    }

    @Keep
    public void stopLive() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVideoPlayerPresenter.pause();
            }
        });
    }

    @Keep
    public void destroy() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVideoPlayerPresenter.destroy();
                mVideoPlayerPresenter = null;
            }
        });
    }
}
