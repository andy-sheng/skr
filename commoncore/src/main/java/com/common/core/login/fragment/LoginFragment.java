package com.common.core.login.fragment;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.R;
import com.common.core.account.UserAccountManager;
import com.common.core.permission.SkrBasePermission;
import com.common.core.permission.SkrPhoneStatePermission;
import com.common.core.share.ShareManager;
import com.common.log.MyLog;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.module.RouterConstants;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.Map;

public class LoginFragment extends BaseFragment {

    public static final int QQ_MODE = 2;
    public static final int WX_MODE = 3;

    public static final int MSG_HIDE_PORGRESS_BAR = 1;
    public static final int MIN_HEIGHT = U.getDisplayUtils().dip2px(100);

    RelativeLayout mMainActContainer;
    ImageView mPicture;
    RelativeLayout mContainer;


    LinearLayout mTvUserAgree;
    ExImageView mWeixinLoginTv;
    ExImageView mPhoneLoginTv;
    ExImageView mQqLoginTv;

    LinearLayout mDengluArea;
    TextView mLogoText;
    ProgressBar mProgressBar;

    volatile boolean mIsWaitOss = false;

    ObjectAnimator mAnimator;

    ViewTreeObserver mObserver;
    boolean isMeasured = false;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_HIDE_PORGRESS_BAR) {
                showLoginingBar(false);
            }
        }
    };

    SkrBasePermission mSkrPermission = new SkrPhoneStatePermission();

    ViewTreeObserver.OnPreDrawListener mOnDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            if (!isMeasured) {
                isMeasured = true;
                int height = mContainer.getMeasuredHeight();
                if (height < MIN_HEIGHT) {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mContainer.getLayoutParams();
                    layoutParams.height = MIN_HEIGHT;
                    mContainer.setLayoutParams(layoutParams);
                    mDengluArea.setTranslationY(height - MIN_HEIGHT);
                    mLogoText.setTranslationY((height - MIN_HEIGHT) / 2);
                    mPicture.setTranslationY(height - MIN_HEIGHT);
                }
            }
            return true;
        }
    };

    @Override
    public int initView() {
        return R.layout.core_login_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        ShareManager.init();

        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mPicture = (ImageView) mRootView.findViewById(R.id.picture);
        mContainer = (RelativeLayout) mRootView.findViewById(R.id.container);

        mDengluArea = (LinearLayout) mRootView.findViewById(R.id.denglu_area);
        mWeixinLoginTv = (ExImageView) mRootView.findViewById(R.id.weixin_login_tv);
        mPhoneLoginTv = (ExImageView) mRootView.findViewById(R.id.phone_login_tv);
        mQqLoginTv = (ExImageView) mRootView.findViewById(R.id.qq_login_tv);
        mLogoText = (TextView) mRootView.findViewById(R.id.logo_text);
        mTvUserAgree = (LinearLayout) mRootView.findViewById(R.id.tv_user_agree);
        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.progress_bar);

        mObserver = mContainer.getViewTreeObserver();
        mObserver.addOnPreDrawListener(mOnDrawListener);

        mPhoneLoginTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mIsWaitOss) {
                    return;
                }
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), LoginByPhoneFragment.class)
                        .setNotifyHideFragment(LoginFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
            }
        });

        mWeixinLoginTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mIsWaitOss) {
                    return;
                }
                if (!UMShareAPI.get(getContext()).isInstall(getActivity(), SHARE_MEDIA.WEIXIN)) {
                    U.getToastUtil().showShort("你没有安装微信");
                    return;
                }
                if (U.getChannelUtils().getChannel().startsWith("MI_SHOP_mimusic")) {
                    // 小米商店渠道，需要获取读取imei权限
                    mSkrPermission.ensurePermission(new Runnable() {
                        @Override
                        public void run() {
                            showLoginingBar(true);
                            UMShareAPI.get(getContext()).doOauthVerify(getActivity(), SHARE_MEDIA.WEIXIN, mAuthListener);
                        }
                    }, true);
                } else {
                    showLoginingBar(true);
                    UMShareAPI.get(getContext()).doOauthVerify(getActivity(), SHARE_MEDIA.WEIXIN, mAuthListener);
                }
            }
        });

        mQqLoginTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mIsWaitOss) {
                    return;
                }
                if (!UMShareAPI.get(getContext()).isInstall(getActivity(), SHARE_MEDIA.QQ)) {
                    U.getToastUtil().showShort("你没有安装QQ");
                    return;
                }
                if (U.getChannelUtils().getChannel().startsWith("MI_SHOP_mimusic")) {
                    mSkrPermission.ensurePermission(new Runnable() {
                        @Override
                        public void run() {
                            showLoginingBar(true);
                            UMShareAPI.get(getContext()).doOauthVerify(getActivity(), SHARE_MEDIA.QQ, mAuthListener);
                        }
                    }, true);
                } else {
                    showLoginingBar(true);
                    UMShareAPI.get(getContext()).doOauthVerify(getActivity(), SHARE_MEDIA.QQ, mAuthListener);
                }

            }
        });

        mTvUserAgree.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mIsWaitOss) {
                    return;
                }
                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                        .withString("url", "https://api.inframe.mobi/user-agreement.html")
                        .greenChannel().navigation();
            }
        });

        StatisticsAdapter.recordCountEvent("login", "expose", null);
        if (U.getPreferenceUtils().getSettingBoolean("newinstall", true)) {
            U.getPreferenceUtils().setSettingBoolean("newinstall", false);
            StatisticsAdapter.recordCountEvent("signup", "expose", null);
        }
    }


    UMAuthListener mAuthListener = new UMAuthListener() {
        @Override
        public void onStart(SHARE_MEDIA platform) {
        }

        @Override
        public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
            MyLog.d(TAG,"onComplete" + " platform=" + platform + " action=" + action + " data=" + data);
            showLoginingBar(false);
            if (platform == SHARE_MEDIA.WEIXIN) {
                Toast.makeText(getContext(), "微信授权成功", Toast.LENGTH_LONG).show();
                String accessToken = data.get("access_token");
                String openid = data.get("openid");
                loginWithThirdPard(WX_MODE, accessToken, openid);
            } else if (platform == SHARE_MEDIA.QQ) {
                Toast.makeText(getContext(), "QQ授权成功", Toast.LENGTH_LONG).show();
                String accessToken = data.get("accessToken");
                String openid = data.get("openid");
                loginWithThirdPard(QQ_MODE, accessToken, openid);
            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, int action, Throwable t) {
            MyLog.d(TAG,"onError" + " platform=" + platform + " action=" + action + " t=" + t);
            showLoginingBar(false);
            if (platform == SHARE_MEDIA.WEIXIN) {
                Toast.makeText(getContext(), "微信授权失败：" + t.getMessage(), Toast.LENGTH_LONG).show();
            } else if (platform == SHARE_MEDIA.QQ) {
                Toast.makeText(getContext(), "QQ授权失败：" + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA platform, int action) {
            showLoginingBar(false);
        }
    };

    private void showLoginingBar(boolean show) {
        mUiHandler.removeMessages(MSG_HIDE_PORGRESS_BAR);
        if (show) {
            mUiHandler.sendEmptyMessageDelayed(MSG_HIDE_PORGRESS_BAR, 4000);
        }
        mIsWaitOss = show;
        mProgressBar.setVisibility(mIsWaitOss ? View.VISIBLE : View.GONE);
    }

    private void loginWithThirdPard(int mode, String accessToken, String openId) {
        UserAccountManager.getInstance().loginByThirdPart(mode, accessToken, openId);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSkrPermission.onBackFromPexrmisionManagerMaybe();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        if (mObserver != null && mObserver.isAlive() && mOnDrawListener != null) {
            mObserver.removeOnPreDrawListener(mOnDrawListener);
        }
        if (mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null);
        }
    }
}
