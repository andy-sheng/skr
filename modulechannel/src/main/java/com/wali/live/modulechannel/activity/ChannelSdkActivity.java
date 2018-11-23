package com.wali.live.modulechannel.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.RouterConstants;
import com.common.core.login.interceptor.JumpInterceptor;
import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.PermissionUtils;
import com.common.utils.U;
import com.wali.live.modulechannel.R;

import java.util.List;

@Route(path = RouterConstants.ACTIVITY_CHANNEL_SDK, extras = JudgeLoginInterceptor.NO_NEED_LOGIN)
public class ChannelSdkActivity extends BaseActivity {
    private BaseImageView mLoginBtn;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.channel_main_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mLoginBtn = (BaseImageView) findViewById(R.id.login_btn);
        FrescoWorker.loadImage(mLoginBtn,
                ImageFactory.newHttpImage("http://yifeng.studio/assets/imgSite/avatar.png")
                        .build());

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!U.getPermissionUtils().checkExternalStorage(ChannelSdkActivity.this)) {
                    U.getPermissionUtils().requestExternalStorage(new PermissionUtils.RequestPermission() {
                        @Override
                        public void onRequestPermissionSuccess() {
                            MyLog.d(TAG, "onRequestPermissionSuccess");
                        }

                        @Override
                        public void onRequestPermissionFailure(List<String> permissions) {
                            MyLog.d(TAG, "onRequestPermissionFailure" + " permissions=" + permissions);
                        }

                        @Override
                        public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                            MyLog.d(TAG, "onRequestPermissionFailureWithAskNeverAgain" + " permissions=" + permissions);
                        }
                    }, ChannelSdkActivity.this);
                }

                U.getActivityUtils().showSnackbar("测试", true);
                //跳到LoginActivity,要用ARouter跳
                ARouter.getInstance().build("/watch/WatchSdkAcitivity").navigation(ChannelSdkActivity.this, new NavigationCallback() {
                    @Override
                    public void onFound(Postcard postcard) {
                        MyLog.d(TAG, "onFound" + " postcard=" + postcard);
                    }

                    @Override
                    public void onLost(Postcard postcard) {
                        MyLog.d(TAG, "onLost" + " postcard=" + postcard);
                    }

                    @Override
                    public void onArrival(Postcard postcard) {
                        MyLog.d(TAG, "onArrival" + " postcard=" + postcard);
                    }

                    @Override
                    public void onInterrupt(Postcard postcard) {
                        MyLog.d(TAG, "onInterrupt" + " postcard=" + postcard);
                    }
                });
            }
        });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
