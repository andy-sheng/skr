package com.common.core.login.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.common.base.BaseFragment;
import com.common.core.R;
import com.common.core.share.ShareManager;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.Map;

import io.reactivex.functions.Consumer;

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
        ShareManager.init();
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

        RxView.clicks(mWeixinLoginTv).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                UMShareAPI.get(getContext()).getPlatformInfo(getActivity(), SHARE_MEDIA.WEIXIN, authListener);
            }
        });
    }


    UMAuthListener authListener = new UMAuthListener() {
        @Override
        public void onStart(SHARE_MEDIA platform) {
        }

        @Override
        public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
            Toast.makeText(getContext(), "成功了", Toast.LENGTH_LONG).show();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                MyLog.e("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, int action, Throwable t) {
            Toast.makeText(getContext(), "失败：" + t.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancel(SHARE_MEDIA platform, int action) {

        }
    };

    @Override
    public boolean useEventBus() {
        return false;
    }
}
