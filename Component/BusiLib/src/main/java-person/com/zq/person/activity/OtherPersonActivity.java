package com.zq.person.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.component.busilib.R;
import com.module.RouterConstants;
import com.zq.person.fragment.OtherPersonFragment4;

@Route(path = RouterConstants.ACTIVITY_OTHER_PERSON)
public class OtherPersonActivity extends BaseActivity {
    public static final String BUNDLE_USER_ID = "bundle_user_id";

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.empty_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, OtherPersonFragment4.class)
                .setBundle(bundle)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .build());
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
    public boolean resizeLayoutSelfWhenKeybordShow() {
        return true;
    }
}
