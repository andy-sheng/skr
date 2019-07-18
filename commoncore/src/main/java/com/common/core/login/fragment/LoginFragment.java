package com.common.core.login.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.callback.Callback;
import com.common.core.R;
import com.common.core.account.UserAccountManager;
import com.common.core.login.LoginActivity;
import com.common.core.permission.SkrBasePermission;
import com.common.core.permission.SkrPhoneStatePermission;
import com.common.core.permission.SkrSdcardPermission;
import com.common.core.share.ShareManager;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.module.RouterConstants;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends BaseFragment implements Callback {

    public static final int QQ_MODE = 2;
    public static final int WX_MODE = 3;

    public static final int MSG_HIDE_PORGRESS_BAR = 1;
    public static final int MIN_HEIGHT = U.getDisplayUtils().dip2px(140);

    RelativeLayout mMainActContainer;
    ImageView mPicture;

    LinearLayout mTvUserAgree;
    ExImageView mWeixinLoginTv;
    ExImageView mPhoneLoginTv;
    ExImageView mQqLoginTv;

    LinearLayout mDengluArea;
    TextView mLogoText;
    ProgressBar mProgressBar;

    volatile boolean mIsWaitOss = false;

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

    SkrSdcardPermission mSkrSdcardPermission = new SkrSdcardPermission() {
        @Override
        public void onRequestPermissionFailureWithAskNeverAgain1(Activity activity, boolean goSettingIfRefuse) {
            super.onRequestPermissionFailureWithAskNeverAgain1(activity, goSettingIfRefuse);
            StatisticsAdapter.recordCountEvent("signup", "sdcard_refuse", null);
        }

        @Override
        public void onRequestPermissionFailure1(Activity activity, boolean goSettingIfRefuse) {
            // 点击拒绝但不是不再询问 不弹去设置的弹窗
            StatisticsAdapter.recordCountEvent("signup", "sdcard_refuse", null);
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

        mDengluArea = (LinearLayout) mRootView.findViewById(R.id.denglu_area);
        mWeixinLoginTv = (ExImageView) mRootView.findViewById(R.id.weixin_login_tv);
        mPhoneLoginTv = (ExImageView) mRootView.findViewById(R.id.phone_login_tv);
        mQqLoginTv = (ExImageView) mRootView.findViewById(R.id.qq_login_tv);
        mLogoText = (TextView) mRootView.findViewById(R.id.logo_text);
        mTvUserAgree = (LinearLayout) mRootView.findViewById(R.id.tv_user_agree);
        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.progress_bar);

        mPhoneLoginTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mIsWaitOss) {
                    return;
                }
                final HashMap map = new HashMap();
                map.put("type", "Phone");
                StatisticsAdapter.recordCountEvent("signup", "click", map);
                mSkrSdcardPermission.ensurePermission(getActivity(), new Runnable() {
                    @Override
                    public void run() {
                        StatisticsAdapter.recordCountEvent("signup", "sdcard_agree", map);
                        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), LoginByPhoneFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
                    }
                }, true);
            }
        });

        mWeixinLoginTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mIsWaitOss) {
                    return;
                }
                final HashMap map = new HashMap();
                map.put("type", "WeiXin");
                StatisticsAdapter.recordCountEvent("signup", "click", map);
                if (!UMShareAPI.get(U.app()).isInstall(getActivity(), SHARE_MEDIA.WEIXIN)) {
                    StatisticsAdapter.recordCountEvent("signup", "noinstall", map);
                    U.getToastUtil().showShort("你没有安装微信");
                    return;
                }
                if (U.getChannelUtils().getChannel().startsWith("MI_SHOP_mimusic")) {
                    // 小米商店渠道，需要获取读取imei权限
                    mSkrSdcardPermission.ensurePermission(getActivity(), new Runnable() {
                        @Override
                        public void run() {
                            mSkrPermission.ensurePermission(new Runnable() {
                                @Override
                                public void run() {
                                    showLoginingBar(true);
                                    UMShareAPI.get(U.app()).getPlatformInfo(getActivity(), SHARE_MEDIA.WEIXIN, mAuthListener);
                                }
                            }, true);
                        }
                    }, true);
                } else {
                    mSkrSdcardPermission.ensurePermission(getActivity(), new Runnable() {
                        @Override
                        public void run() {
                            StatisticsAdapter.recordCountEvent("signup", "sdcard_agree", map);
                            showLoginingBar(true);
                            UMShareAPI.get(U.app()).getPlatformInfo(getActivity(), SHARE_MEDIA.WEIXIN, mAuthListener);
                        }
                    }, true);
                }
            }
        });

        mQqLoginTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mIsWaitOss) {
                    return;
                }
                final HashMap map = new HashMap();
                map.put("type", "QQ");
                StatisticsAdapter.recordCountEvent("signup", "click", map);
                if (!UMShareAPI.get(U.app()).isInstall(getActivity(), SHARE_MEDIA.QQ)) {
                    StatisticsAdapter.recordCountEvent("signup", "noinstall", map);
                    U.getToastUtil().showShort("你没有安装QQ");
                    return;
                }
                if (U.getChannelUtils().getChannel().startsWith("MI_SHOP_mimusic")) {
                    // 小米商店渠道，需要获取读取imei权限
                    mSkrSdcardPermission.ensurePermission(getActivity(), new Runnable() {
                        @Override
                        public void run() {
                            mSkrPermission.ensurePermission(new Runnable() {
                                @Override
                                public void run() {
                                    showLoginingBar(true);
                                    UMShareAPI.get(U.app()).getPlatformInfo(getActivity(), SHARE_MEDIA.QQ, mAuthListener);
                                }
                            }, true);
                        }
                    }, true);
                } else {
                    mSkrSdcardPermission.ensurePermission(getActivity(), new Runnable() {
                        @Override
                        public void run() {
                            StatisticsAdapter.recordCountEvent("signup", "sdcard_agree", map);
                            showLoginingBar(true);
                            UMShareAPI.get(U.app()).getPlatformInfo(getActivity(), SHARE_MEDIA.QQ, mAuthListener);
                        }
                    }, true);
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
    }

    UMAuthListener mAuthListener = new UMAuthListener() {
        @Override
        public void onStart(SHARE_MEDIA platform) {
            final HashMap map = new HashMap();
            map.put("type", platform.toString());
            StatisticsAdapter.recordCountEvent("signup", "shouquan_begin", map);
        }

        @Override
        public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
            MyLog.d(TAG, "onComplete" + " platform=" + platform + " action=" + action + " data=" + data);
            showLoginingBar(false);
            final HashMap map = new HashMap();
            map.put("type", platform.toString());
            StatisticsAdapter.recordCountEvent("signup", "shouquan_success", map);
            if (platform == SHARE_MEDIA.WEIXIN) {
                U.getToastUtil().showLong("微信授权成功");
                String accessToken = data.get("access_token");
                String openid = data.get("openid");
                loginWithThirdPard(WX_MODE, accessToken, openid);
            } else if (platform == SHARE_MEDIA.QQ) {
                U.getToastUtil().showLong("QQ授权成功");
                String accessToken = data.get("accessToken");
                String openid = data.get("openid");
                loginWithThirdPard(QQ_MODE, accessToken, openid);
            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, int action, Throwable t) {
            MyLog.d(TAG, "onError" + " platform=" + platform + " action=" + action + " t=" + t);
            showLoginingBar(false);
            final HashMap map = new HashMap();
            map.put("type", platform.toString());
            StatisticsAdapter.recordCountEvent("signup", "shouquan_failed", map);
            if (platform == SHARE_MEDIA.WEIXIN) {
                U.getToastUtil().showLong("微信授权失败：" + t.getMessage());
            } else if (platform == SHARE_MEDIA.QQ) {
                U.getToastUtil().showLong("QQ授权失败：" + t.getMessage());
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
        UserAccountManager.getInstance().loginByThirdPart(mode, accessToken, openId, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSkrPermission.onBackFromPermisionManagerMaybe(getActivity());
        mSkrSdcardPermission.onBackFromPermisionManagerMaybe(getActivity());
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        StatisticsAdapter.recordCountEvent("signup", "expose", null, true);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onCallback(int r, Object obj) {
        if (r == 1) {
            final ApiResult apiResult = (ApiResult) obj;
            Activity activity = getActivity();
            if(activity instanceof LoginActivity){
                ((LoginActivity)activity).onLoginResult(1,apiResult);
            }
        }
    }
}
