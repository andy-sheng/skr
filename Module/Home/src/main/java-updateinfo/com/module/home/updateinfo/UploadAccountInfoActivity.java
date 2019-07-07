package com.module.home.updateinfo;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.updateinfo.fragment.UploadAccountInfoFragment;


@Route(path = RouterConstants.ACTIVITY_UPLOAD)
public class UploadAccountInfoActivity extends BaseActivity {

    public final static String TAG = "UploadAccountInfoActivity";

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.upload_account_info_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (MyUserInfoManager.getInstance().isNeedCompleteInfo()) {
            U.getFragmentUtils().addFragment(FragmentUtils
                    .newAddParamsBuilder(this, UploadAccountInfoFragment.class)
                    .setAddToBackStack(false)
                    .setHasAnimation(true)
                    .build());
        }else{
            finish();
        }

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);
    }

    @Override
    protected void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    @Override
    public boolean onBackPressedForActivity() {
        return true;
    }

    @Override
    public boolean resizeLayoutSelfWhenKeybordShow() {
        // 自己处理有键盘时的整体布局
        return true;
    }
}
