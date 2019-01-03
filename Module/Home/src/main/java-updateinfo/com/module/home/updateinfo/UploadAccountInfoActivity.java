package com.module.home.updateinfo;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.updateinfo.fragment.EditInfoAgeFragment;
import com.module.home.updateinfo.fragment.EditInfoSexFragment;
import com.module.home.updateinfo.fragment.UploadAccountInfoFragment;


@Route(path = RouterConstants.ACTIVITY_UPLOAD)
public class UploadAccountInfoActivity extends BaseActivity {

    public static final String BUNDLE_IS_UPLOAD = "bundle_is_upload";

    int mJumpToFoot = 1; // 跳到第几步

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.upload_account_info_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (getIntent() != null) {
            mJumpToFoot = getIntent().getIntExtra("jump_to_foot", 1);
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(BUNDLE_IS_UPLOAD, true);
        switch (mJumpToFoot) {
            case 1:
                U.getFragmentUtils().addFragment(FragmentUtils
                        .newAddParamsBuilder(this, UploadAccountInfoFragment.class)
                        .setBundle(bundle)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
                break;
            case 2:
                U.getFragmentUtils().addFragment(FragmentUtils
                        .newAddParamsBuilder(this, EditInfoSexFragment.class)
                        .setBundle(bundle)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
                break;
            case 3:
                U.getFragmentUtils().addFragment(FragmentUtils
                        .newAddParamsBuilder(this, EditInfoAgeFragment.class)
                        .setBundle(bundle)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
                break;
            default:
                break;
        }

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
