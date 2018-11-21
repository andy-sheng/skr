package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.RouterConstants;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.module.home.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

public class GameFragment extends BaseFragment {

//    SmartRefreshLayout mSmartRefreshLayout;
    ExTextView mTestBtn;

    @Override
    public int initView() {
        return R.layout.game_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
//        mSmartRefreshLayout = mRootView.findViewById(R.id.smart_refresh_layout);
        mTestBtn = mRootView.findViewById(R.id.test_btn);
        mTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(U.getCommonUtils().isFastDoubleClick()){
                    return;
                }
                ARouter.getInstance().build(RouterConstants.ACTIVITY_RANKINGMODE).greenChannel().navigation();
            }
        });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
