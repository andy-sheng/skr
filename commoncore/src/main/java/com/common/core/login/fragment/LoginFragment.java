package com.common.core.login.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.R;
import com.common.core.account.UserAccountManager;
import com.common.core.share.ShareManager;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

public class LoginFragment extends BaseFragment {

    RelativeLayout mMainActContainer;
    ExTextView mLogoTv;
    ExTextView mWeixinLoginTv;
    ExTextView mPhoneLoginTv;
    ExTextView mWeiboLoginTv;
    ProgressBar mProgressBar;

    TextView mTvUserAgree;

    volatile boolean isWaitOss = false;

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
        mTvUserAgree = (TextView)mRootView.findViewById(R.id.tv_user_agree);
        mTvUserAgree.setText(Html.fromHtml("<u>"+"《用户协议》"+"</u>"));
        mProgressBar = (ProgressBar)mRootView.findViewById(R.id.progress_bar);

        RxView.clicks(mPhoneLoginTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .filter(new Predicate<Object>() {
                    @Override
                    public boolean test(Object o) {
                        return !isWaitOss;
                    }
                })
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), LoginByPhoneFragment.class)
                                .setNotifyHideFragment(LoginFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
                    }
                });

        RxView.clicks(mWeixinLoginTv)
                .filter(new Predicate<Object>() {
                    @Override
                    public boolean test(Object o) {
                        return !isWaitOss;
                    }
                })
                .subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                showLoginingBar(true);
                UMShareAPI.get(getContext()).getPlatformInfo(getActivity(), SHARE_MEDIA.WEIXIN, authListener);
            }
        });

        RxView.clicks(mTvUserAgree)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .filter(new Predicate<Object>() {
                    @Override
                    public boolean test(Object o) {
                        return !isWaitOss;
                    }
                })
                .subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                        .withString("url", "https://api.inframe.mobi/user-agreement.html")
                        .greenChannel().navigation();
            }
        });
    }


    UMAuthListener authListener = new UMAuthListener() {
        @Override
        public void onStart(SHARE_MEDIA platform) {
        }

        @Override
        public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
            showLoginingBar(false);
            if(platform == SHARE_MEDIA.WEIXIN){
                Toast.makeText(getContext(), "微信授权成功", Toast.LENGTH_LONG).show();
                String accessToken = data.get("accessToken");
                String openid = data.get("openid");
                loginWithWX(accessToken, openid);
            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, int action, Throwable t) {
            showLoginingBar(false);
            Toast.makeText(getContext(), "微信授权失败：" + t.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancel(SHARE_MEDIA platform, int action) {
            showLoginingBar(false);
        }
    };

    private void showLoginingBar(boolean show){
        isWaitOss = show;
        mProgressBar.setVisibility(isWaitOss ? View.VISIBLE : View.GONE);
    }

    private void loginWithWX(String accessToken, String openId) {
        UserAccountManager.getInstance().loginByWX(accessToken, openId);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
