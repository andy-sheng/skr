package com.wali.live.modulewatch.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.log.MyLog;
import com.common.player.VideoPlayerAdapter;
import com.common.utils.U;
import com.wali.live.modulewatch.R;

@Route(path = "/watch/WatchSdkAcitivity")
public class WatchSdkAcitivity extends BaseActivity {

    TextureView mTextureView;
    SurfaceView mSurfaceView;

    VideoPlayerAdapter mVideoPlayerAdapter = new VideoPlayerAdapter();

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.watch_main_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
//        mSurfaceView = new MySurfaceView(this);
//        mVideoPlayerAdapter.setSurfaceView(mSurfaceView);
//        setContentView(mSurfaceView);

//        mTextureView = new TextureView(this);
//        mVideoPlayerAdapter.setTextureView(mTextureView);
//        setContentView(mTextureView);

//        mSurfaceView = findViewById(R.id.video_view2);
//        mVideoPlayerAdapter.setSurfaceView(mSurfaceView);


        mTextureView = findViewById(R.id.video_view);
        mVideoPlayerAdapter.setTextureView(mTextureView);

        mVideoPlayerAdapter.setVideoPath("http://playback.ks.zb.mi.com/record/live/101743_1531094545/hls/101743_1531094545.m3u8?playui=1");
        mVideoPlayerAdapter.play();
//
//        mTextureView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                U.getToastUtil().showToast("sssss");
//
//                mVideoPlayerAdapter.setVideoPath("sss");
//                mVideoPlayerAdapter.play();
//            }
//        });

    }

    @Override
    protected void destroy() {
        super.destroy();
        mVideoPlayerAdapter.destroy();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback{
        private SurfaceHolder holder;

        public MySurfaceView(Context context){
            super(context);
            holder = this.getHolder(); //获取holder对象
//            holder.addCallback(this); // 添加surface回调函数
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            MyLog.d(TAG,"surfaceChanged" + " holder=" + holder + " format=" + format + " width=" + width + " height=" + height);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            MyLog.d(TAG,"surfaceCreated" + " holder=" + holder);

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            MyLog.d(TAG,"surfaceDestroyed" + " holder=" + holder);

        }
    }

}
