package com.wali.live.modulewatch.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.account.UserAccountManager;
import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.PermissionUtil;
import com.common.utils.U;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.wali.live.modulewatch.R;

import java.util.List;

@Route(path = "/watch/WatchSdkAcitivity")
public class WatchSdkAcitivity extends BaseActivity {
    private BaseImageView mLoginBtn;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.watch_main_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mLoginBtn = (BaseImageView) findViewById(R.id.login_btn);

        U.getPermissionUtils().requestExternalStorage(new PermissionUtil.RequestPermission() {
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
        }, new RxPermissions(U.getActivityUtils().getTopActivity()));

        FrescoWorker.loadImage(mLoginBtn,
                ImageFactory.newHttpImage("http://yifeng.studio/assets/imgSite/avatar.png")
                        .build());

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                U.getActivityUtils().showSnackbar("测试",true);
                if (!UserAccountManager.getInstance().hasAccount()) {
                    //跳到LoginActivity,要用ARouter跳
                    ARouter.getInstance().build("/core/login").navigation();
                }
            }
        });


    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
