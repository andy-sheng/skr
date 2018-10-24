package com.wali.live.modulewatch.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.core.RouterConstants;
import com.common.core.login.interceptor.JumpInterceptor;
import com.wali.live.modulewatch.R;
import com.wali.live.modulewatch.base.BaseSdkActivity;
import com.wali.live.modulewatch.fragemnt.BaseWatchFragment;
import com.wali.live.modulewatch.fragemnt.WatchGameFragment;
import com.wali.live.modulewatch.fragemnt.WatchNormalFragment;
import com.wali.live.modulewatch.live.LiveManager;

@Route(path = RouterConstants.ACTIVITY_WATCH, extras = JumpInterceptor.NO_NEED_LOGIN)
public class WatchSdkAcitivity extends BaseSdkActivity {

    private BaseWatchFragment mBaseWatchFragment;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.watch_main_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        String tag = null;
        if (mMyRoomData.getLiveType() != LiveManager.TYPE_LIVE_GAME
                && mMyRoomData.getLiveType() != LiveManager.TYPE_LIVE_HUYA) {
            mBaseWatchFragment = new WatchNormalFragment();
            tag = WatchNormalFragment.class.getSimpleName();
        } else {
            mBaseWatchFragment = new WatchGameFragment();
            tag = WatchGameFragment.class.getSimpleName();
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.container, mBaseWatchFragment, tag);
        ft.commitAllowingStateLoss();
    }

    @Override
    protected void destroy() {
        super.destroy();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean canSlide() {
        return false;
    }

}
