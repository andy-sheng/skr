package com.wali.live.watchsdk.watch.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.fragment.BaseFragment;
import com.wali.live.watchsdk.R;

public class WatchNormalFragment extends BaseFragment {
    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.watchsdk_layout, container, false);
    }

    @Override
    protected void bindView() {

    }


}
