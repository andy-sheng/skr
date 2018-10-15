package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.view.ViewGroup;

import com.base.global.GlobalData;
import com.wali.live.watchsdk.R;


/**
 * Created by yaojian on 16-7-16.
 *
 * @module 频道
 * @description 占位item
 */
public class PlaceHolder extends BaseHolder {
    public View mRootView;

    public PlaceHolder(View view) {
        super(view);
    }

    @Override
    protected void initView() {
        mRootView = $(R.id.root_view);
    }

    @Override
    protected void bindView() {
        int height = GlobalData.app().getResources().getDimensionPixelSize(R.dimen.view_dimen_110);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        mRootView.setLayoutParams(layoutParams);
    }
}
