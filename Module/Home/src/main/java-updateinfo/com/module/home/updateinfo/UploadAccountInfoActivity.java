package com.module.home.updateinfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.myinfo.MyUserInfoManager;
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

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.upload_account_info_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(BUNDLE_IS_UPLOAD, true);

        if (TextUtils.isEmpty(MyUserInfoManager.getInstance().getUploadUser().getUserNickname()) || TextUtils.isEmpty(MyUserInfoManager.getInstance().getUploadUser().getAvatar())) {
            U.getFragmentUtils().addFragment(FragmentUtils
                    .newAddParamsBuilder(this, UploadAccountInfoFragment.class)
                    .setBundle(bundle)
                    .setAddToBackStack(false)
                    .setHasAnimation(true)
                    .build());
        } else if (MyUserInfoManager.getInstance().getUploadUser().getSex() == 0) {
            U.getFragmentUtils().addFragment(FragmentUtils
                    .newAddParamsBuilder(this, EditInfoSexFragment.class)
                    .setBundle(bundle)
                    .setAddToBackStack(false)
                    .setHasAnimation(true)
                    .build());
        } else if (TextUtils.isEmpty(MyUserInfoManager.getInstance().getUploadUser().getBirthday())) {
            U.getFragmentUtils().addFragment(FragmentUtils
                    .newAddParamsBuilder(this, EditInfoAgeFragment.class)
                    .setBundle(bundle)
                    .setAddToBackStack(false)
                    .setHasAnimation(true)
                    .build());
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

    @Override
    public boolean onBackPressedForActivity() {
        return true;
    }
}
