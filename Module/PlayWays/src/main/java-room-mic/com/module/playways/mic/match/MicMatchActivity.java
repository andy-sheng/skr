package com.module.playways.mic.match;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.R;
import com.module.playways.mic.match.fragment.MicMatchFragment;

@Route(path = RouterConstants.ACTIVITY_MIC_MATCH)
public class MicMatchActivity extends BaseActivity {

    /**
     * 存起该房间一些状态信息
     */
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.grab_match_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        for (Activity activity : U.getActivityUtils().getActivityList()) {
            if (U.getActivityUtils().isHomeActivity(activity)) {
                continue;
            }
            if (activity == this) {
                continue;
            }

            activity.finish();
        }
//        PrepareData prepareData = (PrepareData) getIntent().getSerializableExtra("prepare_data");
//        if (prepareData == null) {
//            MyLog.e("GrabMatchActivity", "initData prepareData is null");
//            finish();
//            return;
//        }

        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, MicMatchFragment.class)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .build());
        U.getStatusBarUtil().setTransparentBar(this, false);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void destroy() {
        super.destroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    @Override
    public boolean resizeLayoutSelfWhenKeybordShow() {
        return true;
    }
}
