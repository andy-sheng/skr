package com.zq.relation.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.component.busilib.R;
import com.module.RouterConstants;
import com.zq.relation.fragment.RelationFragment;

@Route(path = RouterConstants.ACTIVITY_RELATION)
public class RelationActivity extends BaseActivity {
    public static final String FROM_PAGE_KEY = "from_page_key";
    public static final String FRIEND_NUM_KEY = "friend_num_key";
    public static final String FANS_NUM_KEY = "fans_num_key";
    public static final String FOLLOW_NUM_KEY = "follow_num_key";

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.empty_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, RelationFragment.class)
                        .setBundle(bundle)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .build());
    }

    @Override
    public boolean resizeLayoutSelfWhenKeybordShow() {
        return true;
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
