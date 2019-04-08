package com.imagebrowse.big;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.common.base.BaseActivity;

/**
 * 包裹看大图的Activity
 */
public class BigImageBrowseActivity extends BaseActivity {
    static BigImageBrowseFragment.Loader sLoader;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        BigImageBrowseFragment.open(false, this,sLoader);
        sLoader = null;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    public static void open(BigImageBrowseFragment.Loader loader, FragmentActivity activity) {
        sLoader = loader;
        Intent intent = new Intent(activity, BigImageBrowseActivity.class);
        activity.startActivity(intent);
    }
}