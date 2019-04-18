package com.imagebrowse.big;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.common.base.BaseActivity;
import com.common.base.R;

/**
 * 包裹看大图的Activity
 */
public class BigImageBrowseActivity extends BaseActivity {
    static Loader sLoader;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        BigImageBrowseFragment.open(false, this, sLoader);
        sLoader = null;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out_center);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    public static void open(Loader loader, FragmentActivity activity) {
        sLoader = loader;
        Intent intent = new Intent(activity, BigImageBrowseActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in_center, 0);
    }

    @Override
    public boolean canSlide() {
        return false;
    }
}