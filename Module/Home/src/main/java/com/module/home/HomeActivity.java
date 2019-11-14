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
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.account.UserAccountManager;
import com.common.core.login.LoginActivity;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.permission.SkrSdcardPermission;
import com.common.core.scheme.SchemeSdkActivity;
import com.common.core.scheme.event.JumpHomeDoubleChatPageEvent;
import com.common.core.scheme.event.JumpHomeFromSchemeEvent;
import com.common.core.upgrade.UpgradeManager;
import com.common.log.MyLog;
import com.common.notification.event.GrabInviteNotifyEvent;
import com.common.utils.ActivityUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.viewpager.NestViewPager;
import com.component.busilib.event.PostsPublishSucessEvent;
import com.component.busilib.event.PostsWatchTabRefreshEvent;
import com.component.busilib.manager.WeakRedDotManager;
import com.module.ModuleServiceManager;
import com.module.RouterConstants;
import com.module.home.dialogmanager.HomeDialogManager;
import com.module.home.event.SkipGuideHomepageEvent;
import com.module.home.fragment.PersonFragment4;
import com.module.home.game.GameFragment3;
import com.module.home.persenter.CheckInPresenter;
import com.module.home.persenter.HomeCorePresenter;
import com.module.home.persenter.NotifyCorePresenter;
import com.module.home.persenter.RedPkgPresenter;
import com.module.home.persenter.VipReceiveCoinPresenter;
import com.module.home.view.IHomeActivity;
import com.module.home.view.INotifyView;
import com.module.msg.IMsgService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@Route(path = RouterConstants.ACTIVITY_HOME)
public class HomeActivity extends BaseActivity implements IHomeActivity, WeakRedDotManager.WeakRedDotListener, INotifyView {

    public final String TAG = "HomeActivity";
    public final static String NOTIFY_CHANNEL_ID = "invite_notify";

    ConstraintLayout mMainActContainer;
    LinearLayout mBottomContainer;
    RelativeLayout mGameArea;
    ExTextView mGameBtn;
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
    CheckInPresenter mCheckInPresenter;
    VipReceiveCoinPresenter mVipReceiveCoinPresenter;

    Handler mUiHandler = new Handler(Looper.getMainLooper());

    String mPengingSchemeUri; //想要跳转的scheme，但因为没登录被挂起了
    boolean mFromCreate = false;

    int lastFollowRedDotValue;
    int postRedDotValue;
    int spFollowRedDotValue;
    int giftRedDotValue;

    NotificationManager mNManager;

    SkrSdcardPermission mSkrSdcardPermission = new SkrSdcardPermission();

    //SkrLocationPermission mSkrLocationPermission = new SkrLocationPermission();

    HomeDialogManager mHomeDialogManager = new HomeDialogManager();

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
        mNManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(NOTIFY_CHANNEL_ID, "notify", NotificationManager.IMPORTANCE_LOW);
            mNManager.createNotificationChannel(mChannel);
        }

        mMsgService = ModuleServiceManager.getInstance().getMsgService();
        mMainVp.setViewPagerCanScroll(false);
        mMainVp.setOffscreenPageLimit(2);
        checkIfFromSchema();
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                MyLog.d(TAG, "getItem" + " position=" + position);
                if (position == 0) {
                    return new GameFragment3();
                } else if (position == 1) {
                    return ModuleServiceManager.getInstance().getPostsService().getFragment();
                } else if (position == 2) {
                    return (Fragment) mMsgService.getMessageFragment();
                } else if (position == 3) {
                    return new PersonFragment4();
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

        mHomePresenter = new

                HomeCorePresenter(this, this);
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
                mMainVp.setCurrentItem(0, false);
                selectTab(0);
            }
        });

        mPostsArea.setOnClickListener(new DebounceViewClickListener(100) {
            @Override
            public void clickValid(View v) {
                if (mMainVp.getCurrentItem() == 1) {
                    EventBus.getDefault().post(new PostsWatchTabRefreshEvent());
                }
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

        mPersonArea.setOnClickListener(new DebounceViewClickListener(100) {
            @Override
            public void clickValid(View v) {
                mMainVp.setCurrentItem(3, false);
                selectTab(3);
            }
        });

        mRedPkgPresenter = new RedPkgPresenter(this);

        addPresent(mRedPkgPresenter);

        mNotifyCorePresenter = new NotifyCorePresenter(this);

        addPresent(mNotifyCorePresenter);

        mVipReceiveCoinPresenter = new VipReceiveCoinPresenter(this);

        addPresent(mVipReceiveCoinPresenter);

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

    }

    @Override
    public void showNotify(GrabInviteNotifyEvent event) {
        Intent it = new Intent(this, SchemeSdkActivity.class);
        it.putExtra("uri", String.format("inframeskr://room/grabjoin?owner=%d&gameId=%d&ask=1", event.mUserInfoModel.getUserId(), event.roomID));
        PendingIntent pit = PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);

        //设置图片,通知标题,发送时间,提示方式等属性
        Notification.Builder mBuilder = new Notification.Builder(this);
        mBuilder.setContentTitle("@" + MyUserInfoManager.INSTANCE.getNickName())                        //标题
                .setContentText("你的好友" + event.mUserInfoModel.getNicknameRemark() + "邀请你玩游戏")      //内容
                .setWhen(System.currentTimeMillis())           //设置通知时间
                .setSmallIcon(R.drawable.app_icon)            //设置小图标
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)    //设置默认的三色灯与振动器
//                .setPriority(Notification.PRIORITY_MAX)      //设置应用的优先级，可以用来修复在小米手机上可能显示在不重要通知中
                .setAutoCancel(true)                           //设置点击后取消Notification
                .setContentIntent(pit);                        //设置PendingIntent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId(NOTIFY_CHANNEL_ID);
        }

        Notification notify1 = mBuilder.build();
        mNManager.notify(1, notify1);
    }

    private void selectTab(int tabSeq) {
        Drawable drawable0 = U.getDrawable(R.drawable.ic_home_normal);
        Drawable drawable1 = U.getDrawable(R.drawable.ic_posts_normal);
        Drawable drawable2 = U.getDrawable(R.drawable.ic_chat_normal);
        Drawable drawable3 = U.getDrawable(R.drawable.ic_me_normal);

        mGameBtn.setSelected(false);
        mPostBtn.setSelected(false);
        mMessageBtn.setSelected(false);
        mPersonInfoBtn.setSelected(false);

        switch (tabSeq) {
            case 0:
                drawable0 = U.getDrawable(R.drawable.ic_home_selected);
                mGameBtn.setSelected(true);
                break;
            case 1:
                drawable1 = U.getDrawable(R.drawable.ic_posts_selected);
                mPostBtn.setSelected(true);
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
        setTabDrawable(mPostBtn, drawable1);
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
                if (UserAccountManager.INSTANCE.hasAccount()) {
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
            mMainVp.setCurrentItem(1);
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
        mFromCreate = false;

        UpgradeManager.getInstance().checkUpdate1();
        mRedPkgPresenter.checkRedPkg();
        mCheckInPresenter.check();
        mVipReceiveCoinPresenter.checkVip();

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
        mMainVp.setCurrentItem(3, false);
        selectTab(3);
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
