package com.wali.live.modulewatch.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.TextureView;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.account.UserAccountManager;
import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.player.VideoPlayerAdapter;
import com.common.utils.PermissionUtil;
import com.common.utils.U;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.wali.live.modulewatch.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

@Route(path = "/watch/WatchSdkAcitivity")
public class WatchSdkAcitivity extends BaseActivity {

    TextureView mVideoView;
    VideoPlayerAdapter mVideoPlayerAdapter = new VideoPlayerAdapter();
    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.watch_main_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mVideoView = findViewById(R.id.video_view);
        mVideoPlayerAdapter.setTextureView(mVideoView);
        mVideoPlayerAdapter.setVideoPath("sss");
        mVideoPlayerAdapter.play();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

}
