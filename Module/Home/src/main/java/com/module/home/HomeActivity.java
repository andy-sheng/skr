package com.module.home;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.account.UserAccountManager;
import com.common.core.permission.SkrSdcardPermission;
import com.common.core.upgrade.UpgradeManager;
import com.common.log.MyLog;
import com.common.utils.ActivityUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.viewpager.NestViewPager;
import com.component.busilib.manager.WeakRedDotManager;
import com.module.ModuleServiceManager;
import com.module.RouterConstants;
import com.module.home.dialogmanager.HomeDialogManager;
import com.module.home.fragment.PersonFragment;
import com.module.home.game.GameFragment2;
import com.module.home.fragment.PkInfoFragment;
import com.module.home.persenter.CheckInPresenter;
import com.module.home.persenter.HomeCorePresenter;
import com.module.home.persenter.NotifyCorePresenter;
import com.module.home.persenter.RedPkgPresenter;
import com.module.home.view.IHomeActivity;
import com.module.msg.IMsgService;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@Route(path = RouterConstants.ACTIVITY_HOME)
public class HomeActivity extends BaseActivity implements IHomeActivity, WeakRedDotManager.WeakRedDotListener {

    public final static String TAG = "HomeActivity";

    RelativeLayout mMainActContainer;
    LinearLayout mBottomContainer;
    RelativeLayout mGameArea;
    ExTextView mGameBtn;
    RelativeLayout mMessageArea;
    ExTextView mMessageBtn;
    ExTextView mUnreadNumTv;
    RelativeLayout mPersonArea;
    ExTextView mPersonInfoBtn;
    ExImageView mPersonInfoRedDot;
    RelativeLayout mRankArea;
    ExTextView mRankBtn;
    NestViewPager mMainVp;
    IMsgService mMsgService;
    HomeCorePresenter mHomePresenter;
    NotifyCorePresenter mNotifyCorePresenter;
    RedPkgPresenter mRedPkgPresenter;
    CheckInPresenter mCheckInPresenter;

    String mPengingSchemeUri; //想要跳转的scheme，但因为没登录被挂起了
    boolean mFromCreate = false;

    int mFansRedDotValue = 0;
    int mFriendRedDotValue = 0;

    SkrSdcardPermission mSkrSdcardPermission = new SkrSdcardPermission();

    //SkrLocationPermission mSkrLocationPermission = new SkrLocationPermission();

    HomeDialogManager mHomeDialogManager = new HomeDialogManager();

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
        mGameBtn = findViewById(R.id.game_btn);
        mRankArea = (RelativeLayout) findViewById(R.id.rank_area);
        mRankBtn = findViewById(R.id.rank_btn);
        mMessageArea = (RelativeLayout) findViewById(R.id.message_area);
        mMessageBtn = findViewById(R.id.message_btn);
        mUnreadNumTv = (ExTextView) findViewById(R.id.unread_num_tv);
        mPersonArea = (RelativeLayout) findViewById(R.id.person_area);
        mPersonInfoBtn = findViewById(R.id.person_info_btn);
        mPersonInfoRedDot = (ExImageView) findViewById(R.id.person_info_red_dot);
        mMainVp = (NestViewPager) findViewById(R.id.main_vp);

        mMsgService = ModuleServiceManager.getInstance().getMsgService();
        mMainVp.setViewPagerCanScroll(false);
        mMainVp.setOffscreenPageLimit(3);
        checkIfFromSchema();

        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                MyLog.d(TAG, "getItem" + " position=" + position);
                if (position == 0) {
                    return new GameFragment2();
                } else if (position == 1) {
                    return new PkInfoFragment();
                } else if (position == 2) {
                    if (mMsgService == null) {
                        return new PersonFragment();
                    } else {
                        return (Fragment) mMsgService.getMessageFragment();
                    }
                } else if (position == 3) {
                    return new PersonFragment();
                }
                return null;
            }

            @Override
            public int getCount() {
                if (mMsgService == null) {
                    return 3;
                } else {
                    return 4;
                }
            }
        };

        mMainVp.setAdapter(fragmentPagerAdapter);

        mHomePresenter = new HomeCorePresenter(this, this);
        mHomePresenter.checkUserInfo("HomeActivity onCreate");
        mCheckInPresenter = new CheckInPresenter(this);
        addPresent(mCheckInPresenter);

        mGameArea.setOnClickListener(new DebounceViewClickListener(100) {
            @Override
            public void clickValid(View v) {
                mMainVp.setCurrentItem(0, false);
                selectTab(0);
            }
        });

        mRankArea.setOnClickListener(new DebounceViewClickListener(100) {
            @Override
            public void clickValid(View v) {
                mMainVp.setCurrentItem(1, false);
                selectTab(1);
            }
        });

        mMessageArea.setOnClickListener(new DebounceViewClickListener(100) {
            @Override
            public void clickValid(View v) {
                mMainVp.setCurrentItem(2, false);
                selectTab(2);
            }
        });

        mPersonArea.setOnClickListener(new DebounceViewClickListener(100) {
            @Override
            public void clickValid(View v) {
                mMainVp.setCurrentItem(3, false);
                selectTab(3);

                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.FANS_RED_ROD_TYPE, 1);
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.FRIEND_RED_ROD_TYPE, 1);
            }
        });

        mRedPkgPresenter = new RedPkgPresenter();
        addPresent(mRedPkgPresenter);

        mNotifyCorePresenter = new NotifyCorePresenter();
        addPresent(mNotifyCorePresenter);

        mMainVp.setCurrentItem(0, false);
        selectTab(0);
        mHomeDialogManager.register();
        mFromCreate = true;

        WeakRedDotManager.getInstance().addListener(this);
        mFansRedDotValue = U.getPreferenceUtils().getSettingInt(WeakRedDotManager.SP_KEY_NEW_FANS, 0);
        mFriendRedDotValue = U.getPreferenceUtils().getSettingInt(WeakRedDotManager.SP_KEY_NEW_FRIEND, 0);
        refreshPersonRedDot();
    }

    private void selectTab(int tabSeq) {
        Drawable drawable0 = U.getDrawable(R.drawable.ic_home_normal);
        Drawable drawable1 = U.getDrawable(R.drawable.ic_rank_normal);
        Drawable drawable2 = U.getDrawable(R.drawable.ic_chat_normal);
        Drawable drawable3 = U.getDrawable(R.drawable.ic_me_normal);

        mGameBtn.setSelected(false);
        mRankBtn.setSelected(false);
        mMessageBtn.setSelected(false);
        mPersonInfoBtn.setSelected(false);

        switch (tabSeq) {
            case 0:
                drawable0 = U.getDrawable(R.drawable.ic_home_selected);
                mGameBtn.setSelected(true);
                break;
            case 1:
                drawable1 = U.getDrawable(R.drawable.ic_rank_selected);
                mRankBtn.setSelected(true);
                break;
            case 2:
                drawable2 = U.getDrawable(R.drawable.ic_chat_selected);
                mMessageBtn.setSelected(true);
                break;
            case 3:
                drawable3 = U.getDrawable(R.drawable.ic_me_selected);
                mPersonInfoBtn.setSelected(true);
                break;
        }

        setTabDrawable(mGameBtn, drawable0);
        setTabDrawable(mRankBtn, drawable1);
        setTabDrawable(mMessageBtn, drawable2);
        setTabDrawable(mPersonInfoBtn, drawable3);
    }

    private void setTabDrawable(ExTextView textView, Drawable drawable) {
        drawable.setBounds(new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
        textView.setCompoundDrawables(null, drawable,
                null, null);
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
        refreshPersonRedDot();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mFromCreate) {
            // 获取地理位置权限
            //mSkrLocationPermission.ensurePermission(null, false);
        }
        if (!mSkrSdcardPermission.onBackFromPermisionManagerMaybe()) {
            if (mFromCreate) {
                mSkrSdcardPermission.ensurePermission(null, true);
            }
        }
        mFromCreate = false;
        UpgradeManager.getInstance().checkUpdate1();
        mRedPkgPresenter.checkRedPkg();
        mCheckInPresenter.check();
    }

    @Override
    protected void destroy() {
        super.destroy();
        if (mHomePresenter != null) {
            mHomePresenter.destroy();
        }
        if (mHomeDialogManager != null) {
            mHomeDialogManager.destroy();
        }
        WeakRedDotManager.getInstance().removeListener(this);
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
        mMainVp.setCurrentItem(0, false);
        selectTab(0);
    }

    @Override
    public int[] acceptType() {
        return new int[]{
                WeakRedDotManager.FANS_RED_ROD_TYPE,
                WeakRedDotManager.FRIEND_RED_ROD_TYPE};
    }

    @Override
    public void onWeakRedDotChange(int type, int value) {
        if (type == WeakRedDotManager.FANS_RED_ROD_TYPE) {
            mFansRedDotValue = value;
        } else if (type == WeakRedDotManager.FRIEND_RED_ROD_TYPE) {
            mFriendRedDotValue = value;
        }

        refreshPersonRedDot();
    }

    private void refreshPersonRedDot() {
        // 更新红点
        if (UpgradeManager.getInstance().needShowRedDotTips()) {
            mPersonInfoRedDot.setVisibility(View.VISIBLE);
        } else {
            mPersonInfoRedDot.setVisibility(View.GONE);
        }

        // 关注和粉丝红点
        if (mFansRedDotValue < 2 && mFriendRedDotValue < 2) {
            mPersonInfoRedDot.setVisibility(View.GONE);
        } else {
            mPersonInfoRedDot.setVisibility(View.VISIBLE);
        }
    }
}
