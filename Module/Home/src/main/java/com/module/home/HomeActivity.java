package com.module.home;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;

import com.common.core.upgrade.UpgradeManager;
import com.common.core.account.UserAccountManager;
import com.common.core.login.interceptor.JudgeLoginInterceptor;
import com.common.log.MyLog;
import com.common.utils.ActivityUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;

import com.common.view.ex.ExTextView;
import com.common.view.viewpager.NestViewPager;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.ModuleServiceManager;
import com.module.RouterConstants;
import com.module.home.fragment.GameFragment;
import com.module.home.fragment.PersonFragment;
import com.module.home.persenter.HomeCorePresenter;
import com.module.home.view.IHomeActivity;
import com.module.msg.IMsgService;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;


@Route(path = RouterConstants.ACTIVITY_HOME)
public class HomeActivity extends BaseActivity implements IHomeActivity {

    public final static String TAG = "HomeActivity";

    LinearLayout mBottomContainer;
    ExImageView mGameBtn;
    RelativeLayout mMessageArea;
    ExImageView mMessageBtn;
    ExTextView mUnreadNumTv;
    ExImageView mPersonInfoBtn;
    ImageView mPersonInfoRedDot;
    NestViewPager mMainVp;
    IMsgService mMsgService;
    HomeCorePresenter mHomePresenter;
    String mPengingSchemeUri; //想要跳转的scheme，但因为没登录被挂起了
    boolean mFromCreate = false;

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
        mBottomContainer = (LinearLayout) findViewById(R.id.bottom_container);
        mGameBtn = (ExImageView) findViewById(R.id.game_btn);
        mMessageArea = (RelativeLayout) findViewById(R.id.message_area);
        mMessageBtn = (ExImageView) findViewById(R.id.message_btn);
        mUnreadNumTv = (ExTextView) findViewById(R.id.unread_num_tv);
        mPersonInfoBtn = (ExImageView) findViewById(R.id.person_info_btn);
        mPersonInfoRedDot = findViewById(R.id.person_info_red_dot);

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
        mGameBtn.setSelected(true);

        mHomePresenter = new HomeCorePresenter(this);

        mHomePresenter.checkUserInfo("HomeActivity onCreate");

        RxView.clicks(mGameBtn)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.trans_tab);
                        mMainVp.setCurrentItem(0, false);
                        mGameBtn.setSelected(true);
                        mMessageBtn.setImageResource(R.drawable.ic_chat_normal);
                        mPersonInfoBtn.setSelected(false);
                    }
                });

        RxView.clicks(mMessageArea)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.trans_tab);
                        mMainVp.setCurrentItem(1, false);
                        mGameBtn.setSelected(false);
                        mMessageBtn.setImageResource(R.drawable.ic_chat_selected);
                        mPersonInfoBtn.setSelected(false);
                    }
                });

        RxView.clicks(mPersonInfoBtn)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.trans_tab);
                        mMainVp.setCurrentItem(2, false);
                        mGameBtn.setSelected(false);
                        mMessageBtn.setImageResource(R.drawable.ic_chat_normal);
                        mPersonInfoBtn.setSelected(true);
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
        if (mFromCreate) {
            mHomePresenter.checkPermiss(this);
        }
        mFromCreate = false;
        UpgradeManager.getInstance().checkUpdate1();
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
        if (foreOrBackgroundChange.foreground) {
            // 后台到前台了
            mHomePresenter.checkPermiss(this);
        }
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
        mGameBtn.setSelected(true);
        mMessageBtn.setImageResource(R.drawable.ic_chat_normal);
        mPersonInfoBtn.setSelected(false);
    }


}
