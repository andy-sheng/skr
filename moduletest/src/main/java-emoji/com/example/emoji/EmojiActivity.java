package com.example.emoji;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.module.RouterConstants;
import com.common.utils.FragmentUtils;
import com.common.utils.U;

@Route(path = RouterConstants.ACTIVITY_EMOJI)
public class EmojiActivity extends BaseActivity {


    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getFragmentUtils().addFragment(FragmentUtils
                .newAddParamsBuilder(EmojiActivity.this, EmojiFragment.class)
                .setAddToBackStack(false)
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

    boolean resizeLayoutSelfWhenKeybordShow = false;

    @Override
    public boolean resizeLayoutSelfWhenKeybordShow() {
        return resizeLayoutSelfWhenKeybordShow;
    }
}
