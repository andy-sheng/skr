package com.common.core.login.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.R;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;

public class LoginFragment extends BaseFragment {

    RelativeLayout mMainActContainer;
    ExTextView mLogoTv;
    ExTextView mWeixinLoginTv;
    ExTextView mPhoneLoginTv;
    ExTextView mWeiboLoginTv;

    @Override
    public int initView() {
        return R.layout.core_login_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mLogoTv = (ExTextView) mRootView.findViewById(R.id.logo_tv);
        mWeixinLoginTv = (ExTextView) mRootView.findViewById(R.id.weixin_login_tv);
        mPhoneLoginTv = (ExTextView) mRootView.findViewById(R.id.phone_login_tv);
        mWeiboLoginTv = (ExTextView) mRootView.findViewById(R.id.weibo_login_tv);


        mPhoneLoginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (U.getCommonUtils().isFastDoubleClick()) {
                    return;
                }

                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), LoginByPhoneFragment.class)
                        .setNotifyHideFragment(LoginFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
            }
        });

    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
