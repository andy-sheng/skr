package com.module.home;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.callback.Callback;
import com.common.core.account.UserAccountManager;
import com.common.core.login.LoginActivity;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.permission.SkrPhoneStatePermission;
import com.common.core.permission.SkrSdcardPermission;
import com.common.core.scheme.SchemeSdkActivity;
import com.common.core.scheme.event.InviteRelationCardSchemeEvent;
import com.common.core.scheme.event.JumpHomeDoubleChatPageEvent;
import com.common.core.scheme.event.JumpHomeFromSchemeEvent;
import com.common.core.upgrade.UpgradeManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.flutter.boost.FlutterBoostController;
import com.common.log.MyLog;
import com.common.notification.event.GrabInviteNotifyEvent;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.ActivityUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExLinearLayout;
import com.common.view.ex.ExTextView;
import com.common.view.viewpager.NestViewPager;
import com.component.busilib.event.GameTabRefreshEvent;
import com.component.busilib.event.PostsPublishSucessEvent;
import com.component.busilib.event.PostsWatchTabRefreshEvent;
import com.component.busilib.manager.WeakRedDotManager;
import com.component.lyrics.utils.SongResUtils;
import com.engine.EngineConfigFromServer;
import com.module.ModuleServiceManager;
import com.module.RouterConstants;
import com.module.home.dialogmanager.HomeDialogManager;
import com.module.home.event.SkipGuideHomepageEvent;
import com.module.home.fragment.PersonFragment6;
import com.module.home.game.GameFragment3;
import com.module.home.persenter.CheckInPresenter;
import com.module.home.persenter.EnterHomeDialogPresenter;
import com.module.home.persenter.HomeCorePresenter;
import com.component.notification.presenter.NotifyCorePresenter;
import com.module.home.persenter.RedPkgPresenter;
import com.module.home.persenter.VipReceiveCoinPresenter;
import com.module.home.view.IHomeActivity;
import com.module.mall.RelationCardUtils;
import com.module.msg.IMsgService;
import com.module.playways.IPlaywaysModeService;
import com.orhanobut.dialogplus.DialogPlus;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import useroperate.OperateFriendActivity;
import useroperate.inter.AbsRelationOperate;

@Route(path = RouterConstants.ACTIVITY_HOME)
public class HomeActivity extends BaseActivity implements IHomeActivity, WeakRedDotManager.WeakRedDotListener {

    public final String TAG = "HomeActivity";

    ConstraintLayout mMainActContainer;
    ExLinearLayout mBottomContainer;
    RelativeLayout mGameArea;
    ExTextView mGameBtn;
    RelativeLayout mPartyArea;
    ExTextView mPartyBtn;
    RelativeLayout mMessageArea;
    ExTextView mMessageBtn;
    ExTextView mUnreadNumTv;
    ExImageView mMessageRedDot;
    RelativeLayout mPersonArea;
    ExTextView mPersonInfoBtn;
    ExImageView mPersonInfoRedDot;
    RelativeLayout mPostsArea;
    ExTextView mPostBtn;
    NestViewPager mMainVp;

    IMsgService mMsgService;
    HomeCorePresenter mHomePresenter;
    NotifyCorePresenter mNotifyCorePresenter;
    RedPkgPresenter mRedPkgPresenter;
    EnterHomeDialogPresenter mEnterHomeDialogPresenter;
    CheckInPresenter mCheckInPresenter;
    VipReceiveCoinPresenter mVipReceiveCoinPresenter;

    Handler mUiHandler = new Handler(Looper.getMainLooper());

    String mPendingSchemeUri; //想要跳转的scheme，但因为没登录被挂起了
    boolean mFromCreate = false;

    int lastFollowRedDotValue;
    int postRedDotValue;
    int spFollowRedDotValue;
    int giftRedDotValue;

    RelationCardUtils mRelationCardUtils;

    SkrSdcardPermission mSkrSdcardPermission = new SkrSdcardPermission();
    SkrPhoneStatePermission mSkrPhoneStatePermission = new SkrPhoneStatePermission();

    //SkrLocationPermission mSkrLocationPermission = new SkrLocationPermission();

    HomeDialogManager mHomeDialogManager = new HomeDialogManager();

    private DialogPlus mWaitingDialogPlus;   // 新版本上新的提示

    public static void open(Activity activity) {
        Intent intent = new Intent(activity, HomeActivity.class);
        activity.startActivity(intent);
    }

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
        MyLog.w(TAG, "HomeActivity initData" + " version=" + U.getAppInfoUtils().getVersionCode());
        MyLog.w(TAG, "HomeActivity initData" + " intent=" + getIntent());
        if (getIntent() != null) {
            MyLog.w(TAG, "HomeActivity initData" + " intent.getData()=" + getIntent().getData());
        }
        U.getStatusBarUtil().setTransparentBar(this, false);
        mMainActContainer = findViewById(R.id.main_act_container);
        mBottomContainer = findViewById(R.id.bottom_container);
        mGameArea = findViewById(R.id.game_area);
        mGameBtn = findViewById(R.id.game_btn);
        mPartyArea = findViewById(R.id.party_area);
        mPartyBtn = findViewById(R.id.party_btn);
        mPostsArea = findViewById(R.id.posts_area);
        mPostBtn = findViewById(R.id.post_btn);
        mMessageArea = findViewById(R.id.message_area);
        mMessageBtn = findViewById(R.id.message_btn);
        mUnreadNumTv = findViewById(R.id.unread_num_tv);
        mMessageRedDot = findViewById(R.id.message_red_dot);
        mPersonArea = findViewById(R.id.person_area);
        mPersonInfoBtn = findViewById(R.id.person_info_btn);
        mPersonInfoRedDot = findViewById(R.id.person_info_red_dot);
        mMainVp = findViewById(R.id.main_vp);


        mMsgService = ModuleServiceManager.getInstance().getMsgService();
        mMainVp.setViewPagerCanScroll(false);
        mMainVp.setOffscreenPageLimit(4);
        checkIfFromSchema();
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                MyLog.d(TAG, "getItem" + " position=" + position);
                if (position == 0) {
                    return new GameFragment3();
                } else if (position == 1) {
                    return ((IPlaywaysModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation()).getPartyGameFragment();
                } else if (position == 2) {
                    return (Fragment) mMsgService.getMessageFragment();
                } else if (position == 3) {
                    return ModuleServiceManager.getInstance().getPostsService().getFragment();
                } else if (position == 4) {
                    return new PersonFragment6();
                }
                return null;
            }

            @Override
            public int getCount() {
                if (mMsgService == null) {
                    return 4;
                } else {
                    return 5;
                }
            }
        };

        mMainVp.setAdapter(fragmentPagerAdapter);

        mHomePresenter = new HomeCorePresenter(this, this);
        if (!UserAccountManager.INSTANCE.hasAccount()) {
            mMainActContainer.setVisibility(View.GONE);
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMainActContainer.setVisibility(View.VISIBLE);
                }
            }, 3000);
            // 没有账号 跳到登陆页面
            LoginActivity.open(this);
        }

        mCheckInPresenter = new CheckInPresenter(this);

        addPresent(mCheckInPresenter);

        mGameArea.setOnClickListener(new DebounceViewClickListener(100) {
            @Override
            public void clickValid(View v) {
                if (mMainVp.getCurrentItem() == 0) {
                    EventBus.getDefault().post(new GameTabRefreshEvent());
                }
                mMainVp.setCurrentItem(0, false);
                selectTab(0);
            }
        });

        if (MyLog.isDebugLogOpen()) {
            mGameArea.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    MyLog.w(TAG, "  mGameArea.setOnLongClickListener");
                    mUiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            HashMap map = new HashMap();
                            map.put("key1", 2);
                            FlutterBoostController.INSTANCE.openFlutterPage(HomeActivity.this, RouterConstants.FLUTTER_PAGE_RELAY_RESULT, null, 0);
                        }
                    }, 5000);
                    return false;
                }
            });
        }

        mPartyArea.setOnClickListener(new DebounceViewClickListener(100) {
            @Override
            public void clickValid(View v) {
                StatisticsAdapter.recordCountEvent("party", "tab_expose", null);
                mMainVp.setCurrentItem(1, false);
                selectTab(1);
            }
        });

        mMessageArea.setOnClickListener(new DebounceViewClickListener(100) {
            @Override
            public void clickValid(View v) {
//                StandFullStar standFullStar = new StandFullStar("asdjlajsadadadald");
//                EventBus.getDefault().post(new EStandFullStarEvent(standFullStar));
                mMainVp.setCurrentItem(2, false);
                selectTab(2);

                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_SP_FOLLOW, 1);
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_FOLLOW_RED_ROD_TYPE, 1);
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_POSTS_COMMENT_LIKE_TYPE, 1);
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_GIFT_TYPE, 1);
            }
        });

        mPostsArea.setOnClickListener(new DebounceViewClickListener(100) {
            @Override
            public void clickValid(View v) {
                if (mMainVp.getCurrentItem() == 3) {
                    EventBus.getDefault().post(new PostsWatchTabRefreshEvent());
                }
                mMainVp.setCurrentItem(3, false);
                selectTab(3);
            }
        });

        mPersonArea.setOnClickListener(new DebounceViewClickListener(100) {
            @Override
            public void clickValid(View v) {
                mMainVp.setCurrentItem(4, false);
                selectTab(4);
            }
        });

        mRedPkgPresenter = new RedPkgPresenter(this);

        addPresent(mRedPkgPresenter);

        mNotifyCorePresenter = new NotifyCorePresenter();

        addPresent(mNotifyCorePresenter);

        mVipReceiveCoinPresenter = new VipReceiveCoinPresenter(this);

        addPresent(mVipReceiveCoinPresenter);

        mEnterHomeDialogPresenter = new EnterHomeDialogPresenter(this);
        addPresent(mEnterHomeDialogPresenter);

        mMainVp.setCurrentItem(0, false);

        selectTab(0);
        mHomeDialogManager.register();
        mFromCreate = true;

        WeakRedDotManager.getInstance().addListener(this);
        spFollowRedDotValue = U.getPreferenceUtils().getSettingInt(WeakRedDotManager.SP_KEY_NEW_MESSAGE_SP_FOLLOW, 0);
        lastFollowRedDotValue = U.getPreferenceUtils().getSettingInt(WeakRedDotManager.SP_KEY_NEW_MESSAGE_FOLLOW, 0);
        postRedDotValue = U.getPreferenceUtils().getSettingInt(WeakRedDotManager.SP_KEY_POSTS_COMMENT_LIKE, 0);
        giftRedDotValue = U.getPreferenceUtils().getSettingInt(WeakRedDotManager.SP_KEY_NEW_MESSAGE_GIFT, 0);
        refreshMessageRedDot();

        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                /**
                 * 清除启动页
                 */
                Window window = getWindow();
                if (window != null) {
                    window.setBackgroundDrawable(null);
                }
            }
        }, 5000);

        tryGoConversationList(getIntent());

        if (U.getChannelUtils().getChannel().startsWith("FEED")) {
            mPostBtn.callOnClick();
        }

        showNewFunctionDialog();

        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                MyLog.i(TAG, "queueIdle");
                // 触发提前获取引擎配置
                EngineConfigFromServer.getDefault();
//                FlutterRoute.Companion.pre();
                IPlaywaysModeService iRankingModeService = (IPlaywaysModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
                if (iRankingModeService != null) {
                    iRankingModeService.idleHandler();
                }
                FlutterBoostController.INSTANCE.init();
                SongResUtils.getRootFile();
                return false;
            }
        });
    }

    private void showNewFunctionDialog() {
        if (U.getChannelUtils().getChannel().startsWith("CHORUS")) {
            return;
        }
//        if(U.getChannelUtils().getChannel().startsWith("CHORUS")){
//            return;
//        }
//        if (!U.getPreferenceUtils().getSettingBoolean(PREF_KEY_PARTY_DIALOG, false)) {
//            if (mWaitingDialogPlus == null) {
//                mWaitingDialogPlus = DialogPlus.newDialog(this)
//                        .setContentHolder(new ViewHolder(R.layout.new_funcation_first_tip_layout))
//                        .setContentBackgroundResource(R.color.transparent)
//                        .setOverlayBackgroundResource(R.color.black_trans_50)
//                        .setExpanded(false)
//                        .setCancelable(true)
//                        .setGravity(Gravity.CENTER)
//                        .create();
//                mWaitingDialogPlus.findViewById(R.id.bg_iv).setOnClickListener(new DebounceViewClickListener() {
//                    @Override
//                    public void clickValid(View v) {
//                        if (mWaitingDialogPlus != null) {
//                            mWaitingDialogPlus.dismiss();
//                        }
//                    }
//                });
//
//            }
//
//            EventBus.getDefault().post(new ShowDialogInHomeEvent(mWaitingDialogPlus, 11));
//            U.getPreferenceUtils().setSettingBoolean(PREF_KEY_PARTY_DIALOG, true);
//        }
    }

    private void selectTab(int tabSeq) {
        Drawable drawable0 = U.getDrawable(R.drawable.ic_home_normal);
        Drawable drawable1 = U.getDrawable(R.drawable.ic_party_normal);
        Drawable drawable2 = U.getDrawable(R.drawable.ic_chat_normal);
        Drawable drawable3 = U.getDrawable(R.drawable.ic_posts_normal);
        Drawable drawable4 = U.getDrawable(R.drawable.ic_me_normal);

        mGameBtn.setSelected(false);
        mPartyBtn.setSelected(false);
        mMessageBtn.setSelected(false);
        mPostBtn.setSelected(false);
        mPersonInfoBtn.setSelected(false);

        switch (tabSeq) {
            case 0:
                drawable0 = U.getDrawable(R.drawable.ic_home_selected);
                mGameBtn.setSelected(true);
                break;
            case 1:
                drawable1 = U.getDrawable(R.drawable.ic_party_selected);
                mPartyBtn.setSelected(true);
                break;
            case 2:
                drawable2 = U.getDrawable(R.drawable.ic_chat_selected);
                mMessageBtn.setSelected(true);
                break;
            case 3:
                drawable3 = U.getDrawable(R.drawable.ic_posts_selected);
                mPostBtn.setSelected(true);
                break;
            case 4:
                drawable4 = U.getDrawable(R.drawable.ic_me_selected);
                mPersonInfoBtn.setSelected(true);
                break;
        }

        setTabDrawable(mGameBtn, drawable0);
        setTabDrawable(mPartyBtn, drawable1);
        setTabDrawable(mMessageBtn, drawable2);
        setTabDrawable(mPostBtn, drawable3);
        setTabDrawable(mPersonInfoBtn, drawable4);
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
                if (UserAccountManager.INSTANCE.hasAccount()) {
                    goSchemeActivity(scheme);
                } else {
                    MyLog.d(TAG, "挂起scheme mPendingSchemeUri:" + mPendingSchemeUri);
                    mPendingSchemeUri = scheme;
                }
            }
        }
    }


    @Override
    public void tryJumpSchemeIfNeed() {
        // 账号登录等信息都成功了，看看有没有悬而未全的scheme
        if (!TextUtils.isEmpty(mPendingSchemeUri)) {
            goSchemeActivity(mPendingSchemeUri);
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
        mPendingSchemeUri = null;
    }

    @Override
    public void updatePersonIconRedDot() {
        // 更新红点
        if (UpgradeManager.getInstance().needShowRedDotTips()) {
            mPersonInfoRedDot.setVisibility(View.VISIBLE);
        } else {
            mPersonInfoRedDot.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        tryGoConversationList(intent);
    }

    private void tryGoConversationList(Intent intent) {
        if (intent != null && intent.getData() != null && "conversationlist".equals(intent.getData().getPath())) {
            mMainVp.setCurrentItem(2);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFromCreate) {
            // 获取地理位置权限
            //mSkrLocationPermission.ensurePermission(null, false);
        }
        if (!mSkrSdcardPermission.onBackFromPermisionManagerMaybe(this)) {
            if (mFromCreate && UserAccountManager.INSTANCE.hasAccount()) {
                mSkrSdcardPermission.ensurePermission(this, null, true);
            }
        }
        if (!mSkrPhoneStatePermission.onBackFromPermisionManagerMaybe(this)) {
            if (mFromCreate && UserAccountManager.INSTANCE.hasAccount()) {
                mSkrPhoneStatePermission.ensurePermission(this, null, false);
            }
        }
        mFromCreate = false;

        UpgradeManager.getInstance().checkUpdate1();
        mRedPkgPresenter.checkRedPkg();
        mCheckInPresenter.check();
        mVipReceiveCoinPresenter.checkVip();
        mEnterHomeDialogPresenter.check();

        if (UserAccountManager.INSTANCE.hasAccount()) {
            mMainActContainer.setVisibility(View.VISIBLE);
        }
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
        if (mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ActivityUtils.ForeOrBackgroundChange foreOrBackgroundChange) {
//        if (foreOrBackgroundChange.foreground) {
//            // 后台到前台了
//            mHomePresenter.checkPermiss(this);
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(InviteRelationCardSchemeEvent inviteRelationCardSchemeEvent) {
        OperateFriendActivity.Companion.open(new OperateFriendActivity.Companion.Builder()
                .setIsEnableFriend(true).setText("邀请").setListener(new AbsRelationOperate.ClickListener() {
                    @Override
                    public void clickRelationBtn(WeakReference<BaseActivity> weakReference, View view, int pos, UserInfoModel userInfoModel, Callback<String> callback) {
                        if (userInfoModel != null) {
                            if (mRelationCardUtils == null) {
                                mRelationCardUtils = new RelationCardUtils();
                            }

                            mRelationCardUtils.checkRelation(userInfoModel, weakReference, inviteRelationCardSchemeEvent.getGoodsID(), inviteRelationCardSchemeEvent.getPacketID());
                        }
                    }
                }));
    }

    /**
     * 跳到主页某个channel
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(JumpHomeFromSchemeEvent event) {
        U.getActivityUtils().goHomeActivity();
        mMainVp.setCurrentItem(event.getChannel(), false);
        selectTab(event.getChannel());
        if (event.getChannel() == 2) {
            WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_SP_FOLLOW, 1);
            WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_FOLLOW_RED_ROD_TYPE, 1);
            WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_POSTS_COMMENT_LIKE_TYPE, 1);
            WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_GIFT_TYPE, 1);
        }
    }

    /**
     * 帖子发布成功跳转
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PostsPublishSucessEvent event) {
        U.getActivityUtils().goHomeActivity();
        mMainVp.setCurrentItem(4, false);
        selectTab(4);
    }

    /**
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(JumpHomeDoubleChatPageEvent event) {
        U.getActivityUtils().goHomeActivity();
        mMainVp.setCurrentItem(0, false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SkipGuideHomepageEvent event) {
        UpgradeManager.getInstance().checkUpdate1();
        mRedPkgPresenter.checkRedPkg();
        mCheckInPresenter.check();
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
                WeakRedDotManager.MESSAGE_FOLLOW_RED_ROD_TYPE
                , WeakRedDotManager.MESSAGE_SP_FOLLOW
                , WeakRedDotManager.MESSAGE_POSTS_COMMENT_LIKE_TYPE
                , WeakRedDotManager.MESSAGE_GIFT_TYPE};
    }

    @Override
    public void onWeakRedDotChange(int type, int value) {
        switch (type) {
            case WeakRedDotManager.MESSAGE_SP_FOLLOW:
                spFollowRedDotValue = value;
                break;
            case WeakRedDotManager.MESSAGE_FOLLOW_RED_ROD_TYPE:
                lastFollowRedDotValue = value;
                break;
            case WeakRedDotManager.MESSAGE_POSTS_COMMENT_LIKE_TYPE:
                postRedDotValue = value;
                break;
            case WeakRedDotManager.MESSAGE_GIFT_TYPE:
                giftRedDotValue = value;
                break;
            default:
                break;
        }

        refreshMessageRedDot();
    }


    private void refreshMessageRedDot() {
        boolean hasMessageRedDot = false;
        if (spFollowRedDotValue >= 2 || lastFollowRedDotValue >= 2 || postRedDotValue >= 2 || giftRedDotValue >= 2) {
            hasMessageRedDot = true;
        }
        if (!hasMessageRedDot) {
            mMessageRedDot.setVisibility(View.GONE);
        } else {
            if (mUnreadNumTv.getVisibility() == View.VISIBLE) {
                mMessageRedDot.setVisibility(View.GONE);
            } else {
                mMessageRedDot.setVisibility(View.VISIBLE);
            }
        }
    }
}
