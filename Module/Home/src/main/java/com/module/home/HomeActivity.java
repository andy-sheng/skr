package com.module.home;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.permission.SkrSdcardPermission;

import com.common.core.upgrade.UpgradeManager;
import com.common.core.account.UserAccountManager;
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
import com.module.home.fragment.GameFragment;
import com.module.home.fragment.PersonFragment;
import com.module.home.persenter.CheckInPresenter;
import com.module.home.persenter.HomeCorePresenter;
import com.module.home.persenter.NotifyCorePresenter;
import com.module.home.persenter.RedPkgPresenter;
import com.module.home.view.GetRedPkgCashView;
import com.module.home.view.IHomeActivity;
import com.module.home.view.IRedPkgView;
import com.module.msg.IMsgService;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.dialog.event.ShowDialogInHomeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@Route(path = RouterConstants.ACTIVITY_HOME)
public class HomeActivity extends BaseActivity implements IHomeActivity, IRedPkgView, WeakRedDotManager.WeakRedDotListener {

    public final static String TAG = "HomeActivity";

    RelativeLayout mMainActContainer;
    LinearLayout mBottomContainer;
    RelativeLayout mGameArea;
    ExImageView mGameBtn;
    RelativeLayout mMessageArea;
    ExImageView mMessageBtn;
    ExTextView mUnreadNumTv;
    ExImageView mMessageRedDot;
    RelativeLayout mPersonArea;
    ExImageView mPersonInfoBtn;
    ExImageView mPersonInfoRedDot;
    NestViewPager mMainVp;
    DialogPlus mRedPkgView;
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
        mGameBtn = (ExImageView) findViewById(R.id.game_btn);
        mMessageArea = (RelativeLayout) findViewById(R.id.message_area);
        mMessageBtn = (ExImageView) findViewById(R.id.message_btn);
        mUnreadNumTv = (ExTextView) findViewById(R.id.unread_num_tv);
        mMessageRedDot = (ExImageView) findViewById(R.id.message_red_dot);
        mPersonArea = (RelativeLayout) findViewById(R.id.person_area);
        mPersonInfoBtn = (ExImageView) findViewById(R.id.person_info_btn);
        mPersonInfoRedDot = (ExImageView) findViewById(R.id.person_info_red_dot);
        mMainVp = (NestViewPager) findViewById(R.id.main_vp);

        mMsgService = ModuleServiceManager.getInstance().getMsgService();
        mMainVp.setViewPagerCanScroll(false);
        mMainVp.setOffscreenPageLimit(2);
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

        mHomePresenter = new HomeCorePresenter(this, this);
        mHomePresenter.checkUserInfo("HomeActivity onCreate");
        mCheckInPresenter = new CheckInPresenter(this);
        addPresent(mCheckInPresenter);

        mGameArea.setOnClickListener(new DebounceViewClickListener(100) {
            @Override
            public void clickValid(View v) {
                mMainVp.setCurrentItem(0, false);
                mGameBtn.setImageResource(R.drawable.ic_home_selected);
                mMessageBtn.setImageResource(R.drawable.ic_chat_normal);
                mPersonInfoBtn.setImageResource(R.drawable.ic_me_normal);
            }
        });

        mMessageArea.setOnClickListener(new DebounceViewClickListener(100) {
            @Override
            public void clickValid(View v) {
                mMainVp.setCurrentItem(1, false);
                mGameBtn.setImageResource(R.drawable.ic_home_normal);
                mMessageBtn.setImageResource(R.drawable.ic_chat_selected);
                mPersonInfoBtn.setImageResource(R.drawable.ic_me_normal);

                mMessageRedDot.setVisibility(View.GONE);
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.FANS_RED_ROD_TYPE, 2);
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.FRIEND_RED_ROD_TYPE, 2);
            }
        });

        mPersonArea.setOnClickListener(new DebounceViewClickListener(100) {
            @Override
            public void clickValid(View v) {
                mMainVp.setCurrentItem(2, false);
                mGameBtn.setImageResource(R.drawable.ic_home_normal);
                mMessageBtn.setImageResource(R.drawable.ic_chat_normal);
                mPersonInfoBtn.setImageResource(R.drawable.ic_me_selected);
            }
        });

        mRedPkgPresenter = new RedPkgPresenter(this);
        addPresent(mRedPkgPresenter);

        mNotifyCorePresenter = new NotifyCorePresenter();
        addPresent(mNotifyCorePresenter);

        mMainVp.setCurrentItem(0, false);
        mHomeDialogManager.register();
        mFromCreate = true;

        WeakRedDotManager.getInstance().addListener(this);
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
    public void showGetCashView(float cash, String scheme) {
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
                                    .withString("uri", scheme)
                                    .navigation();
                        }

                        mRedPkgView.dismiss();
                    }
                })
                .create();
        EventBus.getDefault().post(new ShowDialogInHomeEvent(mRedPkgView, 30));
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
            mMessageRedDot.setVisibility(View.GONE);
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

        refreshMessageRedDot();
    }

    private void refreshMessageRedDot() {
        if (mFansRedDotValue < 3 && mFriendRedDotValue < 3) {
            mMessageRedDot.setVisibility(View.GONE);
        } else {
            if (mUnreadNumTv.getVisibility() == View.GONE) {
                mMessageRedDot.setVisibility(View.VISIBLE);
            } else {
                mMessageRedDot.setVisibility(View.GONE);
            }
        }
    }
}
