package com.module.home;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;

import com.common.core.permission.SkrSdcardPermission;

import com.common.core.scheme.SchemeSdkActivity;

import com.common.core.upgrade.UpgradeManager;
import com.common.core.account.UserAccountManager;
import com.common.core.login.interceptor.JudgeLoginInterceptor;
import com.common.log.MyLog;
import com.common.utils.ActivityUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;

import com.common.view.ex.ExTextView;
import com.common.view.viewpager.NestViewPager;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.ModuleServiceManager;
import com.module.RouterConstants;
import com.module.home.fragment.GameFragment;
import com.module.home.fragment.PersonFragment;
import com.module.home.persenter.HomeCorePresenter;
import com.module.home.persenter.RedPkgPresenter;
import com.module.home.setting.fragment.SettingFragment;
import com.module.home.view.GetRedPkgCashView;
import com.module.home.view.IHomeActivity;
import com.module.home.view.IRedPkgView;
import com.module.msg.IMsgService;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnBackPressListener;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;


@Route(path = RouterConstants.ACTIVITY_HOME)
public class HomeActivity extends BaseActivity implements IHomeActivity, IRedPkgView {

    public final static String TAG = "HomeActivity";

    RelativeLayout mMainActContainer;
    LinearLayout mBottomContainer;
    RelativeLayout mGameArea;
    ExImageView mGameBtn;
    RelativeLayout mMessageArea;
    ExImageView mMessageBtn;
    ExTextView mUnreadNumTv;
    RelativeLayout mPersonArea;
    ExImageView mPersonInfoBtn;
    ExImageView mPersonInfoRedDot;
    NestViewPager mMainVp;
    DialogPlus mRedPkgView;

    IMsgService mMsgService;
    HomeCorePresenter mHomePresenter;
    RedPkgPresenter mRedPkgPresenter;
    String mPengingSchemeUri; //想要跳转的scheme，但因为没登录被挂起了
    boolean mFromCreate = false;

    SkrSdcardPermission mSkrSdcardPermission = new SkrSdcardPermission();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        boolean needFinish = false;
        for (Activity activity : U.getActivityUtils().getActivityList()) {
            if (activity instanceof HomeActivity) {
                MyLog.w(TAG, "已经有HomeActivity在堆栈中，取消当前的");
                needFinish = true;
                break;
            }
        }
        super.onCreate(savedInstanceState);
        if (needFinish) {
            finish();
        }
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.home_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        MyLog.w(TAG, "HomeActivity initData" + ", version is " + U.getAppInfoUtils().getVersionCode());
        mMainActContainer = (RelativeLayout) findViewById(R.id.main_act_container);
        mBottomContainer = (LinearLayout) findViewById(R.id.bottom_container);
        mGameArea = (RelativeLayout) findViewById(R.id.game_area);
        mGameBtn = (ExImageView) findViewById(R.id.game_btn);
        mMessageArea = (RelativeLayout) findViewById(R.id.message_area);
        mMessageBtn = (ExImageView) findViewById(R.id.message_btn);
        mUnreadNumTv = (ExTextView) findViewById(R.id.unread_num_tv);
        mPersonArea = (RelativeLayout) findViewById(R.id.person_area);
        mPersonInfoBtn = (ExImageView) findViewById(R.id.person_info_btn);
        mPersonInfoRedDot = (ExImageView) findViewById(R.id.person_info_red_dot);
        mMainVp = (NestViewPager) findViewById(R.id.main_vp);

        mMsgService = ModuleServiceManager.getInstance().getMsgService();
        mMainVp.setViewPagerCanScroll(false);
        checkIfFromSchema();

        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                MyLog.d(TAG, "getItem" + " position=" + position);
                if (position == 0) {
                    return new GameFragment();
                } else if (position == 1) {
                    if (mMsgService == null) {
                        return new PersonFragment();
                    } else {
                        return (Fragment) mMsgService.getMessageFragment();
                    }
                } else if (position == 2) {
                    return new PersonFragment();
                }
                return null;
            }

            @Override
            public int getCount() {
                if (mMsgService == null) {
                    return 2;
                } else {
                    return 3;
                }
            }
        };

        mMainVp.setAdapter(fragmentPagerAdapter);

        mHomePresenter = new HomeCorePresenter(this);

        mHomePresenter.checkUserInfo("HomeActivity onCreate");

        mGameArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getSoundUtils().play(TAG, R.raw.trans_tab);
                mMainVp.setCurrentItem(0, false);
                mGameBtn.setImageResource(R.drawable.ic_home_selected);
                mMessageBtn.setImageResource(R.drawable.ic_chat_normal);
                mPersonInfoBtn.setImageResource(R.drawable.ic_me_normal);
            }
        });

        mMessageArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getSoundUtils().play(TAG, R.raw.trans_tab);
                mMainVp.setCurrentItem(1, false);
                mGameBtn.setImageResource(R.drawable.ic_home_normal);
                mMessageBtn.setImageResource(R.drawable.ic_chat_selected);
                mPersonInfoBtn.setImageResource(R.drawable.ic_me_normal);
            }
        });

        mPersonArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getSoundUtils().play(TAG, R.raw.trans_tab);
                mMainVp.setCurrentItem(2, false);
                mGameBtn.setImageResource(R.drawable.ic_home_normal);
                mMessageBtn.setImageResource(R.drawable.ic_chat_normal);
                mPersonInfoBtn.setImageResource(R.drawable.ic_me_selected);
            }
        });

        mRedPkgPresenter = new RedPkgPresenter(this);
        addPresent(mRedPkgPresenter);

        RxView.clicks(mGameArea)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.trans_tab);
                        mMainVp.setCurrentItem(0, false);
                        mGameBtn.setImageResource(R.drawable.ic_home_selected);
                        mMessageBtn.setImageResource(R.drawable.ic_chat_normal);
                        mPersonInfoBtn.setImageResource(R.drawable.ic_me_normal);
                    }
                });

        RxView.clicks(mMessageArea)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.trans_tab);
                        mMainVp.setCurrentItem(1, false);
                        mGameBtn.setImageResource(R.drawable.ic_home_normal);
                        mMessageBtn.setImageResource(R.drawable.ic_chat_selected);
                        mPersonInfoBtn.setImageResource(R.drawable.ic_me_normal);
                    }
                });

        RxView.clicks(mPersonArea)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.trans_tab);
                        mMainVp.setCurrentItem(2, false);
                        mGameBtn.setImageResource(R.drawable.ic_home_normal);
                        mMessageBtn.setImageResource(R.drawable.ic_chat_normal);
                        mPersonInfoBtn.setImageResource(R.drawable.ic_me_selected);
                    }
                });
        mMainVp.setCurrentItem(0, false);
        mFromCreate = true;
        U.getSoundUtils().preLoad(TAG, R.raw.trans_tab);
    }

    private void checkIfFromSchema() {
        if (getIntent() != null) {
            String scheme = getIntent().getStringExtra("from_scheme");
            if (!TextUtils.isEmpty(scheme)) {
                if (UserAccountManager.getInstance().hasAccount()) {
                    goSchemeActivity(scheme);
                } else {
                    MyLog.d(TAG, "挂起scheme mPengingSchemeUri:" + mPengingSchemeUri);
                    mPengingSchemeUri = scheme;
                }
            }
        }
    }

    @Override
    public void showGetCashView(float cash, String schema) {
        if (mRedPkgView != null) {
            mRedPkgView.dismiss();
        }

        GetRedPkgCashView getRedPkgCashView = new GetRedPkgCashView(this);
        TextView tvCash = getRedPkgCashView.findViewById(R.id.tv_cash);
        tvCash.setText("" + cash);

        mRedPkgView = DialogPlus.newDialog(this)
                .setContentHolder(new ViewHolder(getRedPkgCashView))
                .setGravity(Gravity.CENTER)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .setCancelable(true)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                        if (view.getId() == R.id.tv_ruzhang) {
                            ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                                    .withString("uri", schema)
                                    .navigation();
                        }

                        mRedPkgView.dismiss();
                    }
                })
                .create();

        mRedPkgView.show();
    }

    @Override
    public void tryJumpSchemeIfNeed() {
        // 账号登录等信息都成功了，看看有没有悬而未全的scheme
        if (!TextUtils.isEmpty(mPengingSchemeUri)) {
            goSchemeActivity(mPengingSchemeUri);
        }
    }

    @Override
    public void showUnReadNum(int unReadNum) {
        if (unReadNum == 0) {
            mUnreadNumTv.setVisibility(View.GONE);
        } else {
            mUnreadNumTv.setVisibility(View.VISIBLE);
            if (unReadNum > 99) {
                mUnreadNumTv.setText("99+");
            } else {
                mUnreadNumTv.setText("" + unReadNum);
            }
        }
    }

    private void goSchemeActivity(String scheme) {
        MyLog.d(TAG, "goSchemeActivity" + " scheme=" + scheme);
        ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                .withString("uri", scheme)
                .navigation();
        mPengingSchemeUri = null;
    }

    @Override
    public void updatePersonIconRedDot() {
        if (UpgradeManager.getInstance().needShowRedDotTips()) {
            mPersonInfoRedDot.setVisibility(View.VISIBLE);
        } else {
            mPersonInfoRedDot.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!mSkrSdcardPermission.onBackFromPermisionManagerMaybe()) {
            if (mFromCreate) {
                mSkrSdcardPermission.ensurePermission(null, true);
            }
        }
        mFromCreate = false;
        UpgradeManager.getInstance().checkUpdate1();
        mRedPkgPresenter.checkRedPkg();
    }

    @Override
    protected void destroy() {
        super.destroy();
        if (mHomePresenter != null) {
            mHomePresenter.destroy();
        }
        U.getSoundUtils().release(TAG);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ActivityUtils.ForeOrBackgroundChange foreOrBackgroundChange) {
//        if (foreOrBackgroundChange.foreground) {
//            // 后台到前台了
//            mHomePresenter.checkPermiss(this);
//        }
    }

    @Override
    public boolean resizeLayoutSelfWhenKeybordShow() {
        return true;
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public boolean onBackPressedForActivity() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            moveTaskToBack(true);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onLogoff() {
        mMainVp.setCurrentItem(1, false);
        mGameBtn.setImageResource(R.drawable.ic_home_selected);
        mMessageBtn.setImageResource(R.drawable.ic_chat_normal);
        mPersonInfoBtn.setImageResource(R.drawable.ic_me_normal);
    }


}
