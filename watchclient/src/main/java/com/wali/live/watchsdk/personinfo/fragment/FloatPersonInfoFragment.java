package com.wali.live.watchsdk.personinfo.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.activity.RxActivity;
import com.base.dialog.DialogUtils;
import com.base.dialog.MyAlertDialog;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.base.BaseRotateSdkActivity;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.event.GetUserInfoAndUnpdateConversationEvent;
import com.mi.live.data.event.LiveRoomManagerEvent;
import com.wali.live.event.SdkEventClass;
import com.mi.live.data.manager.LiveRoomCharactorManager;
import com.mi.live.data.manager.UserInfoManager;
import com.mi.live.data.relation.RelationApi;
import com.mi.live.data.user.User;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.manager.WatchRoomCharactorManager;
import com.wali.live.proto.Rank;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.utils.AsyncTaskUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataCommonFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.eventbus.DismissFloatPersonInfoEvent;
import com.wali.live.watchsdk.eventbus.FollowStatEvent;
import com.wali.live.watchsdk.personinfo.presenter.ForbidManagePresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by yaojian on 16-3-21.
 *
 * @module 个人资料悬浮框
 */
public class FloatPersonInfoFragment extends BaseFragment implements View.OnClickListener, ForbidManagePresenter.IForbidManageView {
    private static final String TAG = FloatPersonInfoFragment.class.getSimpleName();

    private static final String REDNAME_URL = "http://live.mi.com/lang/cn/qa/detail.html?qid=4_4";
    public static final int REQUEST_CODE = GlobalData.getRequestCode();
    public static final String EXTRA_IN_UUID = "uuid";
    public static final String EXTRA_IN_OWNER_UUID = "owner_uuid";
    public static final String EXTRA_IN_ROOM_ID = "room_id";
    public static final String EXTRA_IN_LIVE_URL = "live_url";
    public static final String EXTRA_IN_LIVE_ENTER_TIME = "live_enter_time";

    private final static int MSG_FRESH_ALL_VIEWS = 200;     //刷新所有的View
    private final static int MSG_FRESH_USER_INFO_VIEWS = 201;     //刷新用户信息的view
    private final static int MSG_FRESH_MAIN_AVATAR = 202;       //刷新主头像

    //datas begins **********************************************
    private long mUserUuidFromBundle = 0;       //从别的页面传进来的被查看者的uuid
    private long mOwnerUuidFromBundle = 0;        //房间所有者uuid
    private String mRoomIdFromBundle;        //房间id
    private String mLiveUrlFromBundle;        //房间流url
    private long mEnterTimeFromBundle;        //仅限于直播 进入只播房间时间 用于统计用户多久关注主播

    private User mUser = null;          //被查看者的User Info

    private boolean mGetUserInfoTaskRunning = false;        //指示GetUserInfoAndFreshUiTask是否正在进行
    private boolean mFollowOrUnFollowTaskRunning = false;      //指示关注取消关注的线程是否正在执行
    private boolean mSetManagerTaskRunning = false;             //用于指示SetManagerTask是否正在执行
    private boolean mBanSpeakTaskRunning = false;               //用于指示禁言

    private final MainHandler mMainHandler = new MainHandler(this);         //main thread

    public final static int MAX_FOLLOW_BUTTON_CLICK_TIMES = 6;       //关注按钮的最多点击次数
    private int mFollowButtonClickTimes = 0;       //关注按钮的点击次数

    private FloatPersonInfoClickListener mFloatPersonInfoClickListener = null;      //点击事件
    private Rank.RankUser mTopOneUser = null;           //排名第一的用户
    private boolean mFragAnimationEnd;
    //datas ends ************************************************

    //Views begins *********************************************
    private ImageView mCloseBtn;        //顶部的按钮

    private SimpleDraweeView mMainAvatar;      //主头像
    private ImageView mWeiboVerifyConnerImage;      //微博认证的角标
    private TextView mRednameTv;              //红名tv

    private TextView mNicknameTV;       //昵称
    private TextView mIdTv;     //显示ID
    private TextView mSignTv;       //显示签名
    private View mVerifyZone;       //认证信息
    private TextView mVerifyLine1Tv;     //显示第一行认证信息
    private TextView mSentDiamondTv;      //显示送出的钻石
    //private TextView mSentVirtualDiamondTv;       //显示送出的虚拟钻
    private TextView mLevelTv;      //显示级别
    private ImageView mGenderIv;       //性别

    private TextView mLiveTicketCountTv;    //显示星票数量
    private TextView mFollowCountTv;        //显示关注数
    private TextView mFansCountTv;      //显示粉丝数

    private RelativeLayout mFollowButton;        //关注按钮

    //底部按钮
    private View mBottomButtonZone;         //底部的按钮区域
    private TextView mHomePageTv;
    private TextView mPrivateMessageTv;
    private TextView mMoreTv;

    private SimpleDraweeView mTopOneAvatar;     //排行第一的头像
    private SimpleDraweeView mTopOneRelationView;//联系图标
    private ImageView mTopOneConner;        //排行第一的角标
    //Views ends ***********************************************

    // inner class begins ***************************************

    private ForbidManagePresenter mForbidManagePresenter;

    RelativeLayout mForbidSpeak;

    TextView mKickViewerBtn;

    TextView mFollowButtonTv;

    TextView mForbidSpeakTv;

    /**
     * 获取用户信息并刷新UI的task
     */
    private final static class GetUserInfoAndFreshUiTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<FloatPersonInfoFragment> reference = null;

        public GetUserInfoAndFreshUiTask(FloatPersonInfoFragment fragment) {
            if (fragment != null) {
                reference = new WeakReference<FloatPersonInfoFragment>(fragment);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (reference != null && reference.get() != null) {
                FloatPersonInfoFragment fragment = reference.get();
                if (fragment != null) {
                    fragment.mGetUserInfoTaskRunning = true;

                    if (fragment.mUserUuidFromBundle >= 0) {
                        fragment.mUser = UserInfoManager.getUserInfoByUuid(fragment.mUserUuidFromBundle, false);
                    }

                    if (fragment.mUserUuidFromBundle >= 0) {
                        List<Rank.RankUser> rankUsers = RelationApi.getTicketListResponse(fragment.mUserUuidFromBundle, 1, 0);
                        if (rankUsers != null && !rankUsers.isEmpty()) {
                            fragment.mTopOneUser = rankUsers.get(0);
                        }
                    }
                    List<Long> whiteList = GetConfigManager.getInstance().getSixinSystemServiceNumWhiteList();
                    if (fragment.mUser != null) {
                        AvatarUtils.updateMyFollowAvatarTimeStamp(fragment.mUser.getUid(), fragment.mUser.getAvatar());
                        EventBus.getDefault().post(new GetUserInfoAndUnpdateConversationEvent(fragment.mUser.getUid(), fragment.mUser.isBlock(), 0, fragment.mUser.getCertificationType(), fragment.mUser.getNickname()));
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (reference != null && reference.get() != null) {
                FloatPersonInfoFragment fragment = reference.get();
                if (fragment != null) {
                    fragment.mGetUserInfoTaskRunning = false;
                    if (fragment.mFragAnimationEnd) {
                        fragment.mMainHandler.sendEmptyMessage(MSG_FRESH_ALL_VIEWS);
                    } else {
                        fragment.refreshInfoZone();
                    }
                }
            }
            super.onPostExecute(aVoid);
        }
    }

    /**
     * 关注或者取消关注的task
     */
    private static class FollowOrUnFollowUserTask extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<FloatPersonInfoFragment> reference = null;

        public FollowOrUnFollowUserTask(FloatPersonInfoFragment fragment) {
            if (fragment != null) {
                reference = new WeakReference<FloatPersonInfoFragment>(fragment);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (reference != null && reference.get() != null) {
                FloatPersonInfoFragment fragment = reference.get();
                if (fragment != null) {
                    fragment.mFollowOrUnFollowTaskRunning = true;

                    if (fragment.mUser == null) {
                        return false;
                    }
                    Boolean result = false;
                    if (!fragment.mUser.isFocused()) {   //关注
                        result = RelationApi.follow2(UserAccountManager.getInstance().getUuidAsLong(), fragment.mUser.getUid(), fragment.mUser.getUid() == fragment.getOwnerID() ? fragment.getLiveID() : null) >= RelationApi.FOLLOW_STATE_SUCCESS;
                        if (result) {
                            fragment.mUser.setIsFocused(true);
                            StatisticsAlmightyWorker.getsInstance().recordDelay(StatisticsKey.AC_APP,
                                    StatisticsKey.KEY, StatisticsKey.KEY_LIVE_ROOM_FLOAT_FOLLOW_BUTTON + fragment.mUser.getUid());
                            //sdk不用数据库
                            //RelationDaoAdapter.getInstance().insertRelation(fragment.mUser.getRelation());

                            //多久关注主播点
                            if (fragment.mUserUuidFromBundle == fragment.mOwnerUuidFromBundle && fragment.mOwnerUuidFromBundle != 0 && fragment.mEnterTimeFromBundle > 0) {
                                StatisticsAlmightyWorker.getsInstance().recordDelay(StatisticsKey.STATISTICS_FOLLOW_ANCHOR_AC,
                                        StatisticsKey.KEY, StatisticsKey.STATISTICS_FOLLOW_ANCHOR_CARD_KEY,
                                        StatisticsKey.STATISTICS_FOLLOW_ANCHOR_USERID, String.valueOf(fragment.mOwnerUuidFromBundle),
                                        StatisticsKey.STATISTICS_FOLLOW_ANCHOR_LIVEID, fragment.mRoomIdFromBundle,
                                        StatisticsKey.STATISTICS_FOLLOW_ANCHOR_DURATION, String.valueOf(SystemClock.elapsedRealtime() - fragment.mEnterTimeFromBundle));
                            }
                        }
                    } else {  //取消关注
                        result = RelationApi.unFollow(UserAccountManager.getInstance().getUuidAsLong(), fragment.mUser.getUid());
                        if (result) {
                            fragment.mUser.setIsFocused(false);
                            //sdk 不存数据库
                            //RelationDaoAdapter.getInstance().deleteRelation(fragment.mUser.getUid());
                        }
                    }
                    return result;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (reference != null && reference.get() != null) {
                FloatPersonInfoFragment fragment = reference.get();
                if (fragment != null) {
                    fragment.mFollowOrUnFollowTaskRunning = false;
                    if (result) {
                        //刷新Info UI
                        fragment.mMainHandler.sendEmptyMessage(MSG_FRESH_USER_INFO_VIEWS);
                    } else {
                        if (RelationApi.errorCode == RelationApi.ERROR_CODE_BLACK) {
                            ToastUtils.showToast(GlobalData.app(), GlobalData.app().getString(R.string.setting_black_follow_hint));
                        }
                    }
                }
            }
        }
    }


    /**
     * main thread
     */
    private final static class MainHandler extends Handler {
        private WeakReference<FloatPersonInfoFragment> reference = null;

        public MainHandler(FloatPersonInfoFragment fragment) {
            if (fragment != null) {
                reference = new WeakReference<>(fragment);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            if (reference != null && reference.get() != null) {
                FloatPersonInfoFragment fragment = reference.get();
                if (fragment != null) {
                    switch (msg.what) {
                        case MSG_FRESH_ALL_VIEWS:
                            fragment.handleMsgFreshAllViews();
                            break;
                        case MSG_FRESH_USER_INFO_VIEWS:
                            fragment.handleMsgFreshUserInfo();
                            break;
                        case MSG_FRESH_MAIN_AVATAR:
                            fragment.handleMsgFreshMainAvatar();
                            break;
                    }
                }
            }
        }
    }

    // inner class ends ******************************************

    public long getOwnerID() {
        return mOwnerUuidFromBundle;
    }

    public long getTargetID() {
        return mUser != null ? mUser.getUid() : 0;
    }

    public String getLiveID() {
        return mRoomIdFromBundle;
    }

    public String getLiveUrl() {
        return mLiveUrlFromBundle;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setupForbidManagePresenter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(R.layout.float_person_info_fragment, container, false);
        //初始化信息
        Bundle bundle = getArguments();
        if (bundle != null) {
            mUserUuidFromBundle = bundle.getLong(FloatPersonInfoFragment.EXTRA_IN_UUID, 0);
            mOwnerUuidFromBundle = bundle.getLong(FloatPersonInfoFragment.EXTRA_IN_OWNER_UUID, 0);
            mRoomIdFromBundle = bundle.getString(FloatPersonInfoFragment.EXTRA_IN_ROOM_ID, "");
            mLiveUrlFromBundle = bundle.getString(FloatPersonInfoFragment.EXTRA_IN_LIVE_URL, "");
            mEnterTimeFromBundle = bundle.getLong(FloatPersonInfoFragment.EXTRA_IN_LIVE_ENTER_TIME, -1);
        }
        //获取user
        if (!mGetUserInfoTaskRunning) {
            AsyncTaskUtils.exeNetWorkTask(new GetUserInfoAndFreshUiTask(this));
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            initViewLandscape();
        } else {
            initViewPortrait();
        }
    }

    private void initViewLandscape() {
        if (mRootView == null) {
            return;
        }
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mRootView.findViewById(R.id.count_hint).getLayoutParams();
        lp.topMargin = DisplayUtils.dip2px(10);

        RelativeLayout.LayoutParams lpLine = (RelativeLayout.LayoutParams) mRootView.findViewById(R.id.splite_line).getLayoutParams();
        lpLine.topMargin = DisplayUtils.dip2px(10f);

        RelativeLayout.LayoutParams lpFollow = (RelativeLayout.LayoutParams) mRootView.findViewById(R.id.admin_area).getLayoutParams();
        lpFollow.topMargin = DisplayUtils.dip2px(10f);

        RelativeLayout.LayoutParams lpBottom = (RelativeLayout.LayoutParams) mRootView.findViewById(R.id.user_info_zone).getLayoutParams();
        lpBottom.topMargin = DisplayUtils.dip2px(95f);
    }

    private void initViewPortrait() {
        if (mRootView == null) {
            return;
        }
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mRootView.findViewById(R.id.count_hint).getLayoutParams();
        lp.topMargin = DisplayUtils.dip2px(24);

        RelativeLayout.LayoutParams lpLine = (RelativeLayout.LayoutParams) mRootView.findViewById(R.id.splite_line).getLayoutParams();
        lpLine.topMargin = DisplayUtils.dip2px(14f);

        RelativeLayout.LayoutParams lpFollow = (RelativeLayout.LayoutParams) mRootView.findViewById(R.id.admin_area).getLayoutParams();
        lpFollow.topMargin = DisplayUtils.dip2px(14f);

        RelativeLayout.LayoutParams lpBottom = (RelativeLayout.LayoutParams) mRootView.findViewById(R.id.user_info_zone).getLayoutParams();
        lpBottom.topMargin = DisplayUtils.dip2px(97.3f);
    }

    @Override
    protected void bindView() {
        mRootView.findViewById(R.id.out_view).setOnClickListener(this);
        mRootView.findViewById(R.id.out_view).setTag(TAG_OUT_VIEW);
        mRootView.findViewById(R.id.float_main_view).setOnClickListener(this);

        mCloseBtn = (ImageView) mRootView.findViewById(R.id.close_btn);
        mCloseBtn.setOnClickListener(this);
        mCloseBtn.setTag(TAG_CLOSE_BTN);

        mMainAvatar = (SimpleDraweeView) mRootView.findViewById(R.id.top_main_avatar);
        mMainAvatar.setOnClickListener(this);
        mMainAvatar.setTag(TAG_TOP_MAIN_AVATAR);
        AvatarUtils.loadAvatarByUidTs(mMainAvatar, mUserUuidFromBundle, 0, true);
        mTopOneAvatar = (SimpleDraweeView) mRootView.findViewById(R.id.top_one_avatar);
        mTopOneAvatar.setOnClickListener(this);
        mTopOneAvatar.setTag(TAG_TOP_ONE_AVATAR);
        mTopOneRelationView = (SimpleDraweeView) mRootView.findViewById(R.id.top_one_relation_avatar);
        mTopOneConner = (ImageView) mRootView.findViewById(R.id.top_one_conner);
        mWeiboVerifyConnerImage = (ImageView) mRootView.findViewById(R.id.weibo_verify_conner);
        mRednameTv = (TextView) mRootView.findViewById(R.id.redname_tv);
        mNicknameTV = (TextView) mRootView.findViewById(R.id.my_nick);
        mIdTv = (TextView) mRootView.findViewById(R.id.my_id_tv);
        mSignTv = (TextView) mRootView.findViewById(R.id.my_singature_tv);
        mLevelTv = (TextView) mRootView.findViewById(R.id.level_tv);
        mVerifyZone = mRootView.findViewById(R.id.verify_zone);

        mVerifyLine1Tv = (TextView) mRootView.findViewById(R.id.verify_line1_tv);
        mGenderIv = (ImageView) mRootView.findViewById(R.id.gender_iv);
        mSentDiamondTv = (TextView) mRootView.findViewById(R.id.hint_sent_count_tv);
        //mSentVirtualDiamondTv = (TextView) mRootView.findViewById(R.id.hint_sent_virtual_count);
        mLiveTicketCountTv = (TextView) mRootView.findViewById(R.id.live_ticket_tv);
        mFollowCountTv = (TextView) mRootView.findViewById(R.id.follow_count_tv);
        mFansCountTv = (TextView) mRootView.findViewById(R.id.fans_count_tv);

        mFollowButton = (RelativeLayout) mRootView.findViewById(R.id.follow_button);
        mFollowButton.setOnClickListener(this);
        mFollowButton.setTag(TAG_FOLLOW_BUTTON);

        mFollowButtonTv = (TextView) mRootView.findViewById(R.id.follow_button_tv);

        mBottomButtonZone = mRootView.findViewById(R.id.bottom_button_zone);
        mHomePageTv = (TextView) mRootView.findViewById(R.id.homepage_tv);
        mHomePageTv.setOnClickListener(this);
        mHomePageTv.setTag(TAG_HOMEPAGE_TV);
        mPrivateMessageTv = (TextView) mRootView.findViewById(R.id.private_message_tv);
        mPrivateMessageTv.setOnClickListener(this);
        mPrivateMessageTv.setTag(TAG_PRIVATE_MESSAGE_TV);

        mMoreTv = (TextView) mRootView.findViewById(R.id.more_tv);
        mMoreTv.setOnClickListener(this);
        mMoreTv.setTag(TAG_MORE_TV);

        mForbidSpeak = (RelativeLayout) mRootView.findViewById(R.id.forbid_speak);
        mForbidSpeak.setOnClickListener(this);
        mForbidSpeak.setTag(TAG_FORBID_SPEAK);

        mForbidSpeakTv = (TextView) mRootView.findViewById(R.id.forbid_speak_tv);

        mKickViewerBtn = (TextView) mRootView.findViewById(R.id.kick_viewer_btn);
        mKickViewerBtn.setOnClickListener(this);
        mKickViewerBtn.setTag(TAG_KICK_VIEWER_BTN);

        mRednameTv.setOnClickListener(this);
        mRednameTv.setTag(TAG_REDNAME_TV);

        //如果是自己则隐藏底部的按钮,默认都是visible的
        if (mUserUuidFromBundle == UserAccountManager.getInstance().getUuidAsLong()) {
            mBottomButtonZone.setVisibility(View.GONE);
            mFollowButton.setVisibility(View.GONE);
        } else {
            if (getActivity() instanceof ForbidManagePresenter.IForbidManageProvider) {
                //判断是否有禁言权限和踢人权限
                if (mOwnerUuidFromBundle == UserAccountManager.getInstance().getUuidAsLong()) {
                    //判断禁言按钮的状态
                    changeForbidSpeakBtnStatus(LiveRoomCharactorManager.getInstance().isBanSpeaker(mUserUuidFromBundle));
                    mForbidSpeak.setVisibility(View.VISIBLE);
                    if (LiveRoomCharactorManager.getInstance().haveKickPermission()) {
                        mKickViewerBtn.setVisibility(View.VISIBLE);
                    }
                } else if (mUserUuidFromBundle != mOwnerUuidFromBundle) {
                    if (WatchRoomCharactorManager.getInstance().hasManagerPower(getOwnerID())) {
                        //判断禁言按钮的状态
                        changeForbidSpeakBtnStatus(WatchRoomCharactorManager.getInstance().isBanSpeaker(mUserUuidFromBundle));
                        mForbidSpeak.setVisibility(View.VISIBLE);
                    }
                    if (WatchRoomCharactorManager.getInstance().haveKickPermission(mOwnerUuidFromBundle, UserAccountManager.getInstance().getUuidAsLong())) {
                        mKickViewerBtn.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        if (mForbidSpeak.getVisibility() == View.GONE) {
            ViewGroup.LayoutParams params = mFollowButton.getLayoutParams();
            params.width = com.base.global.GlobalData.app().getResources().getDimensionPixelSize(R.dimen.view_dimen_700);
        } else {
            ViewGroup.LayoutParams params = mFollowButton.getLayoutParams();
            params.width = com.base.global.GlobalData.app().getResources().getDimensionPixelSize(R.dimen.view_dimen_340);
        }

    }

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }


    public final int TAG_TOP_MAIN_AVATAR = 1001;
    public final int TAG_TOP_ONE_AVATAR = 1002;
    public final int TAG_OUT_VIEW = 1003;
    public final int TAG_CLOSE_BTN = 1004;
    public final int TAG_HOMEPAGE_TV = 1005;
    public final int TAG_PRIVATE_MESSAGE_TV = 1006;
    public final int TAG_MORE_TV = 1007;
    public final int TAG_FOLLOW_BUTTON = 1008;
    public final int TAG_FORBID_SPEAK = 1009;
    public final int TAG_KICK_VIEWER_BTN = 1010;
    public final int TAG_REDNAME_TV = 1011;

    @Override
    public void onClick(View v) {
        if (v.getTag() == null || getActivity() == null) {
            return;
        }
        if ((int)v.getTag() == TAG_OUT_VIEW || (int)v.getTag() == TAG_CLOSE_BTN) {
            finish();
            return;
        } else if (AccountAuthManager.triggerActionNeedAccount(getActivity())) {
            switch ((int) v.getTag()) {
                case TAG_TOP_MAIN_AVATAR:
                    onClickMainAvatar();
                    break;
                case TAG_TOP_ONE_AVATAR:
                    onClickTopOneAvatar();
                    break;
                case TAG_HOMEPAGE_TV:     //点击底部第一个按钮
                    onClickHomePageBtn();
                    break;
                case TAG_PRIVATE_MESSAGE_TV:     //点击底部第二个按钮
                    onClickPrivateMessageBtn();
                    break;
                case TAG_MORE_TV:     //点击底部第三个按钮
                    onClickMoreBtn();
                    break;
                case TAG_FOLLOW_BUTTON:
                    onClickFollowButton();
                    break;
                case TAG_FORBID_SPEAK:
                    if (mForbidManagePresenter != null) {
                        if (v.isSelected()) {
                            mForbidManagePresenter.cancelForbidSpeak(getLiveID(), getOwnerID(),
                                    UserAccountManager.getInstance().getUuidAsLong(), getTargetID());
                        } else {
                            mForbidManagePresenter.forbidSpeak(getLiveID(), getOwnerID(),
                                    UserAccountManager.getInstance().getUuidAsLong(), mUser);
                        }
                    }
                    break;
                case TAG_KICK_VIEWER_BTN:
                    DialogUtils.showNormalDialog(getActivity(), 0, R.string.kick_viewer_confirm_tips, R.string.kick_confirm_btn, R.string.cancle_operating, new DialogUtils.IDialogCallback() {
                        @Override
                        public void process(DialogInterface dialogInterface, int i) {
                            if (mForbidManagePresenter != null) {
                                mForbidManagePresenter.kickViewer(getLiveID(), getOwnerID(), UserAccountManager.getInstance().getUuidAsLong(), getTargetID());
                            }
                        }
                    }, null);
                    break;
                case TAG_REDNAME_TV:
//                TODO 打开注释
//                WebViewActivity.openUrlWithBrowserIntent(REDNAME_URL, getActivity());
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 点击关注按钮
     */
    private void onClickFollowButton() {
        if (null == getActivity()) {
            return;
        }
        //打点
        StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_USERINFO_CARD_FOLLOW, 1);

        //点击了房主卡片的关注按钮
        if (mUserUuidFromBundle == mOwnerUuidFromBundle && mOwnerUuidFromBundle != UserAccountManager.getInstance().getUuidAsLong()) {
            EventBus.getDefault().post(new FollowStatEvent(StatisticsKey.KEY_FLOATING_NAME_FOLLOW));
        }

        if (mFollowOrUnFollowTaskRunning) {
            ToastUtils.showToast(getActivity(), R.string.doing_now);
        } else {
            if (mFollowButtonClickTimes >= MAX_FOLLOW_BUTTON_CLICK_TIMES) {
                ToastUtils.showToast(getActivity(), R.string.click_follow_button_too_many_time);
            } else {
                ++mFollowButtonClickTimes;

                if (getActivity() != null && mUser != null) {
                    if (!mUser.isFocused()) { //没有关注, 则直接关注
                        //起一个线程
                        AsyncTaskUtils.exeNetWorkTask(new FollowOrUnFollowUserTask(FloatPersonInfoFragment.this));
                    } else { //已关注, 则弹出对话框提示取消关注

                        { //弹出对话框提示取消关注
                            final MyAlertDialog.Builder builder = new MyAlertDialog.Builder(getActivity());
                            builder.setMessage(R.string.unfollow_dialog_title);
                            //继续关注
                            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            //取消关注
                            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    if (!mFollowOrUnFollowTaskRunning) {
                                        //起一个线程
                                        AsyncTaskUtils.exeNetWorkTask(new FollowOrUnFollowUserTask(FloatPersonInfoFragment.this));
                                    }
                                }
                            });
                            if (getActivity() != null) {
                                int color = getActivity().getResources().getColor(R.color.color_e5aa1e);
                                builder.setPositiveButtonTextColor(color);
                            }
                            MyAlertDialog dialog = builder.setAutoDismiss(false).setCancelable(true).create();
                            dialog.show();
                        }

                    }
                }
            }
        }
    }

    public void setFloatPersonInfoClickListener(FloatPersonInfoClickListener listener) {
        if (listener != null) {
            mFloatPersonInfoClickListener = listener;
        }
    }

    /*
    * 点击更多按钮
    * */
    private void onClickMoreBtn() {
        showMoreDialog();
    }

    private void showMoreDialog() {
        if (null == getActivity()) {
            return;
        }
        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(getActivity());
        String reportText = GlobalData.app().getResources().getString(R.string.report);
        String cancelText = GlobalData.app().getResources().getString(R.string.cancel);
        String[] items = null;
        if (mOwnerUuidFromBundle == UserAccountManager.getInstance().getUuidAsLong()) {//主播端
//            String forbiddenText;
//            boolean setForbidden;
//            if (LiveRoomCharactorManager.getInstance().isBanSpeaker(mUserUuidFromBundle)) {   //已经禁言
//                forbiddenText = getResources().getString(R.string.unforbid_speak);
//                setForbidden = false;
//            } else {
//                forbiddenText = getResources().getString(R.string.forbid_speak);
//                setForbidden = true;
//            }
            String managerText;
            final boolean setManager;
            if (LiveRoomCharactorManager.getInstance().isManager(mUserUuidFromBundle)) {//查看的用户是管理员
                managerText = getResources().getString(R.string.cancel_manager);
                setManager = false;
            } else {
                managerText = getResources().getString(R.string.set_manager);
                setManager = true;
            }
            items = new String[]{managerText, reportText, cancelText};
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
//                    case 0:
//                        onClickForbiddenBtn(setForbidden);
//                        break;
                        case 0:
                            onClickManageBtn(setManager);
                            break;
                        case 1:
                            onClickReportBtn();
                            break;
                    }
                }
            });
        } else if (null != mUser &&
                WatchRoomCharactorManager.getInstance().hasManagerPower(getOwnerID()) &&
                mUser.getUid() != mOwnerUuidFromBundle) {//管理员　查看的非主播, 则有禁言按钮  注：看了改之前主播和非主播都有举报功能
//            String forbiddenText;
//            boolean setForbidden;
//            if (WatchRoomCharactorManager.getInstance().isBanSpeaker(mUserUuidFromBundle)) {   //已经禁言
//                forbiddenText = getResources().getString(R.string.unforbid_speak);
//                setForbidden = false;
//            } else {
//                forbiddenText = getResources().getString(R.string.forbid_speak);
//                setForbidden = true;
//            }
            items = new String[]{reportText, cancelText};
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
//                    case 0:
//                        onClickForbiddenBtn(setForbidden);
//                        break;
                        case 0:
                            onClickReportBtn();
                            break;
                    }
                }
            });
        } else {
            items = new String[]{reportText, cancelText};
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            onClickReportBtn();
                            break;
                    }
                }
            });
        }
        builder.create().show();
    }

    /**
     * 点击关闭按钮
     */
    private void finish() {
        onBackPressed();
    }

    /**
     * 点击排行第一的头像
     */
    private void onClickMainAvatar() {
        onBackPressed();
        //打点
        if (mFloatPersonInfoClickListener != null) {
            mFloatPersonInfoClickListener.onClickMainAvatar(mUser);
        }
    }

    /**
     * 点击排行第一的头像
     */
    private void onClickTopOneAvatar() {
        onBackPressed();
        //打点
        StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_USERINFO_CARD_NO1, 1);
        if (mFloatPersonInfoClickListener != null) {
            mFloatPersonInfoClickListener.onClickTopOne(mUser);
        }
    }

    /*
    * 点击主页按钮操作
    * */
    private void onClickHomePageBtn() {
        //打点
        StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_USERINFO_CARD_HOME, 1);
        onBackPressed();

        if (mFloatPersonInfoClickListener != null) {
            mFloatPersonInfoClickListener.onClickHomepage(mUser);
        }
    }

    /*
    * 点击私信按钮操作
    * */
    private void onClickPrivateMessageBtn() {
        //打点
        StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_USERINOF_CARD_MESS, 1);
        onBackPressed();
        if (mFloatPersonInfoClickListener != null) {
            mFloatPersonInfoClickListener.onClickSixin(mUser);
        }
    }

    /*
    * 点击举报按钮操作
    * */
    private void onClickReportBtn() {
//        TODO
//        //打点
//        if (null != getActivity()) {
//            StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_USERINFO_CARD_REPORT, 1);
//
//            /*ReportAlertDialog reportAlertDialog = new ReportAlertDialog(getActivity(), mUserUuidFromBundle, MLPreferenceUtils.PREF_KEY_REPORT_ITEM_DATA, getLiveID(), getLiveUrl(), ReportAlertDialog.EXT_ROOM);
//            reportAlertDialog.create().show();
//            onBackPressed();*/
//            String type;
//            if (mUserUuidFromBundle == mOwnerUuidFromBundle) {
//                type = ReportFragment.EXT_ANCHOR;
//            } else {
//                type = ReportFragment.EXT_USER;
//            }
//            ReportFragment.openFragment((BaseActivity) getActivity(), mUserUuidFromBundle, getLiveID(), getLiveUrl(), ReportFragment.LOCATION_ROOM, type);
//        }
    }

    /*
    * 点击管理员按钮操作
    * */
    private void onClickManageBtn(boolean isSetManager) {
        if (getActivity() == null) {
            return;
        }
        if (isSetManager) {//设置为管理员
            //打点
            StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_USERINFO_CARD_MANAGER, 1);

            if (!mSetManagerTaskRunning) {
                mSetManagerTaskRunning = true;
                if (LiveRoomCharactorManager.getInstance().getManagerCount() < LiveRoomCharactorManager.MANAGER_CNT) {
                    LiveRoomCharactorManager.getInstance().setManagerRxTask((RxActivity) getActivity(), mUser, getLiveID(), getOwnerID(), true);
                } else {
                    ToastUtils.showToast(getActivity(), getString(R.string.manager_max_err, LiveRoomCharactorManager.MANAGER_CNT));
                    mSetManagerTaskRunning = false;
                }
            }
        } else {//取消管理员
            if (!mSetManagerTaskRunning) {
                mSetManagerTaskRunning = true;
                LiveRoomCharactorManager.getInstance().setManagerRxTask((RxActivity) getActivity(), mUser, getLiveID(), getOwnerID(), false);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LiveRoomManagerEvent event) {
        if (event != null) {
            mMainHandler.sendEmptyMessage(MSG_FRESH_USER_INFO_VIEWS);
            mSetManagerTaskRunning = false;
        }
    }

    /**
     * 处理MSG_FRESH_ALL_VIEWS, 刷新所有的Views
     */
    private void handleMsgFreshAllViews() {

        //刷新头像
        mMainHandler.sendEmptyMessage(MSG_FRESH_MAIN_AVATAR);

        //刷新信息
        mMainHandler.sendEmptyMessage(MSG_FRESH_USER_INFO_VIEWS);

    }

    /**
     * 处理MSG_FRESH_MAIN_AVATAR, 刷新头像
     */
    private void handleMsgFreshMainAvatar() {

        if (mUser == null) {    //没有拉取到用户信息
            if (mUserUuidFromBundle > 0) {
                mWeiboVerifyConnerImage.setVisibility(View.GONE);

                if (mTopOneUser != null) {
                    AvatarUtils.loadAvatarByUidTs(mTopOneAvatar, mTopOneUser.getUuid(), 0, true);
                    mTopOneAvatar.setVisibility(View.VISIBLE);
                    mTopOneConner.setVisibility(View.VISIBLE);
                    mTopOneRelationView.setVisibility(View.VISIBLE);
                } else {
                    mTopOneAvatar.setVisibility(View.GONE);
                    mTopOneRelationView.setVisibility(View.GONE);
                    mTopOneConner.setVisibility(View.GONE);
                }

            }
            return;
        } else {
            //显示认证信息

            if (mUser.getCertificationType() == 0 && !mUser.isRedName()) {     //没有认证信息
                mWeiboVerifyConnerImage.setVisibility(View.GONE);
                mRednameTv.setVisibility(View.GONE);
            } else {
                if (mUser.isRedName()) {
                    mWeiboVerifyConnerImage.setVisibility(View.GONE);
                    mRednameTv.setVisibility(View.VISIBLE);
                } else {
                    mRednameTv.setVisibility(View.GONE);
                    mWeiboVerifyConnerImage.setVisibility(View.VISIBLE);
                    mWeiboVerifyConnerImage.setImageDrawable(ItemDataCommonFormatUtils.getCertificationImgSource(mUser.getCertificationType()));
                }
            }

            AvatarUtils.loadAvatarByUidTs(mMainAvatar, mUser.getUid(), mUser.getAvatar(), true);


            if (mTopOneUser != null) {
                AvatarUtils.loadAvatarByUidTs(mTopOneAvatar, mTopOneUser.getUuid(), 0, true);
                mTopOneAvatar.setVisibility(View.VISIBLE);
                mTopOneConner.setVisibility(View.VISIBLE);
                mTopOneRelationView.setVisibility(View.VISIBLE);
            } else {
                mTopOneAvatar.setVisibility(View.GONE);
                mTopOneRelationView.setVisibility(View.GONE);
                mTopOneConner.setVisibility(View.GONE);
            }

        }
    }

    /**
     * 处理MSG_FRESH_USER_INFO_VIEWS, 刷新用户信息相关的UI
     */
    private void handleMsgFreshUserInfo() {
        if (null == getActivity() || getActivity().isFinishing()) {
            //ADD by jinbin，保护可能偶现的崩溃
            return;
        }

        if (mUser == null) {
            if (mUserUuidFromBundle > 0 && getActivity() != null) {
                mIdTv.setText(getActivity().getResources().getString(R.string.default_id_hint) + String.valueOf(mUserUuidFromBundle));
            }

            return;
        }

        refreshInfoZone();

        //显示送出的钻石
        int sentDiamondCount = mUser.getSendDiamondNum();
        if (sentDiamondCount < 0) {
            sentDiamondCount = 0;
        }

        int sentVirtualDiamondCount = mUser.getSentVirtualDiamondNum();
        if (sentVirtualDiamondCount < 0) {
            sentVirtualDiamondCount = 0;
        }
        int totalSentDiamondCount = sentDiamondCount + sentVirtualDiamondCount;
        mSentDiamondTv.setText(getResources().getQuantityString(R.plurals.sent_diamond_text, totalSentDiamondCount, totalSentDiamondCount));
        //mSentVirtualDiamondTv.setText(String.valueOf(sentVirtualDiamondCount));

        //显示星票数
        int liveTicketsNumber = mUser.getLiveTicketNum();
        MyLog.v(TAG + " handleMsgFreshUserInfo liveTicketsNumber == " + liveTicketsNumber);
        if (liveTicketsNumber < 0) {
            liveTicketsNumber = 0;
        }
        mLiveTicketCountTv.setText(String.valueOf(liveTicketsNumber));

        //显示关注数
        int followNum = mUser.getFollowNum();
        if (followNum < 0) {
            followNum = 0;
        }
        mFollowCountTv.setText(String.valueOf(followNum));

        int fansCount = mUser.getFansNum();
        if (fansCount < 0) {
            fansCount = 0;
        }
        mFansCountTv.setText(String.valueOf(fansCount));

        if (mUser.getUid() != UserAccountManager.getInstance().getUuidAsLong()) {
            //刷新关注按钮
            if (mUser.isFocused()) {
                mFollowButton.setEnabled(false);
                if (!mUser.isBothwayFollowing()) {
                    mFollowButtonTv.setText(R.string.already_followed);
                } else {
                    mFollowButtonTv.setText(R.string.follow_both);
                }
                mFollowButtonTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } else {
                mFollowButton.setEnabled(true);
                mFollowButtonTv.setText(R.string.add_follow);
                mFollowButtonTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.live_card_add_attention, 0, 0, 0);
            }
        }
    }

    public void refreshInfoZone() {
        if (mUser == null) {
            return;
        }
        //显示姓名
        if (TextUtils.isEmpty(mUser.getNickname())) {
            mNicknameTV.setText(String.valueOf(mUser.getUid()));
        } else {
            mNicknameTV.setText(mUser.getNickname());
        }

        //显示ID
        if (GlobalData.app() != null) {
            mIdTv.setText(GlobalData.app().getResources().getString(R.string.default_id_hint) + String.valueOf(mUser.getUid()));
        }

        //显示认证信息
        if (mUser.getCertificationType() == 0 || TextUtils.isEmpty(mUser.getCertification())) {
            mVerifyZone.setVisibility(View.GONE);
        } else {
            String verifyText = mUser.getCertification();
            if (mUser.getCertificationType() == 1) {  //新浪微博
                verifyText = GlobalData.app().getResources().getString(R.string.verify_sina) + verifyText;
            } else if (mUser.getCertificationType() == 3) { //推荐认证
                verifyText = GlobalData.app().getResources().getString(R.string.verify_recommand) + verifyText;
            } else if (mUser.getCertificationType() == 2) {    //官方账号
                verifyText = GlobalData.app().getResources().getString(R.string.verify_offical) + verifyText;
            } else if (mUser.getCertificationType() == 4) {    //黑金认证
                verifyText = GlobalData.app().getResources().getString(R.string.verify_xiaomi) + verifyText;
            }
            mVerifyLine1Tv.setVisibility(View.VISIBLE);
            mVerifyLine1Tv.setText(verifyText);
        }

        //显示签名
        if (TextUtils.isEmpty(mUser.getSign())) {
            mSignTv.setText(GlobalData.app().getResources().getString(R.string.default_signature));
        } else {
            mSignTv.setText(mUser.getSign());
        }

        //显示等级
        int level = mUser.getLevel();
        if (level <= 1) {
            level = 1;
        }
        GetConfigManager.LevelItem levelItem = ItemDataCommonFormatUtils.getLevelItem(level);
        mLevelTv.setText(String.valueOf(level));
        mLevelTv.setBackgroundDrawable(levelItem.drawableBG);
        mLevelTv.setCompoundDrawables(levelItem.drawableLevel, null, null, null);

        //显示性别
        if (mUser.getGender() == 1) {
            mGenderIv.setVisibility(View.VISIBLE);
            mGenderIv.setBackgroundResource(R.drawable.all_man);
        } else if (mUser.getGender() == 2) {
            mGenderIv.setVisibility(View.VISIBLE);
            mGenderIv.setBackgroundResource(R.drawable.all_women);
        } else {
            mGenderIv.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (!isDetached()) {
            FragmentNaviUtils.popFragment(getActivity());
            FragmentNaviUtils.removeFragment(getActivity(), this);
            EventBus.getDefault().post(new DismissFloatPersonInfoEvent());
            return true;
        }
        return super.onBackPressed();
    }

    public void onEvent(FollowOrUnfollowEvent event) {
        if (event != null && mUser != null && mMainHandler != null) {
            if (event.isBothFollow) {
                mUser.setIsBothwayFollowing(true);
            } else {
                mUser.setIsBothwayFollowing(false);
            }
            mMainHandler.sendEmptyMessage(MSG_FRESH_ALL_VIEWS);
        }
    }

    public static FloatPersonInfoFragment openFragment(BaseActivity activity, long fromUuid, long ownerUuid, String roomId, String url, FloatPersonInfoClickListener listener) {
        return openFragment(activity, fromUuid, ownerUuid, roomId, url, listener, -1);
    }

    public static FloatPersonInfoFragment openFragment(BaseActivity activity, long fromUuid, long ownerUuid, String roomId, String url, FloatPersonInfoClickListener listener, long enterTime) {
        Bundle bundle = new Bundle();
        bundle.putLong(FloatPersonInfoFragment.EXTRA_IN_UUID, fromUuid);
        bundle.putLong(FloatPersonInfoFragment.EXTRA_IN_OWNER_UUID, ownerUuid);
        bundle.putString(FloatPersonInfoFragment.EXTRA_IN_ROOM_ID, roomId);
        bundle.putString(FloatPersonInfoFragment.EXTRA_IN_LIVE_URL, url);
        bundle.putLong(FloatPersonInfoFragment.EXTRA_IN_LIVE_ENTER_TIME, enterTime);
        bundle.putString(EXTRA_SCREEN_ORIENTATION, BaseFragment.PARAM_FOLLOW_SYS);

        FloatPersonInfoFragment fragment = (FloatPersonInfoFragment) FragmentNaviUtils.addFragmentWithZoomInOutAnimation(activity, R.id.main_act_container, FloatPersonInfoFragment.class, bundle, true, true, true);
        fragment.setFloatPersonInfoClickListener(listener);
        return fragment;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation animation = super.onCreateAnimation(transit, enter, nextAnim);
        if (null == animation && nextAnim != 0 && enter) {
            animation = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        }
        if (null != animation) {
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mFragAnimationEnd = true;
                    mMainHandler.sendEmptyMessage(MSG_FRESH_ALL_VIEWS);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        return animation;
    }

    private void setupForbidManagePresenter() {
        Activity activity = getActivity();
        if (activity != null && activity instanceof ForbidManagePresenter.IForbidManageProvider) {
            mForbidManagePresenter = ((ForbidManagePresenter.IForbidManageProvider) activity).provideForbidManagePresenter();
            if (mForbidManagePresenter != null) {
                mForbidManagePresenter.setForbidManageView(this);
            }
        }
    }

    @Override
    public void onForbidSpeakDone(User user, int errCode) {
        MyLog.w(TAG, "onForbidSpeakDone targetId=" + user.getUid() + ", errCode=" + errCode);
        if (errCode == ForbidManagePresenter.ERR_CODE_SUCCESS) {
            changeForbidSpeakBtnStatus(true);
        }
    }

    @Override
    public void onCancelForbidSpeakDone(long targetId, int errCode) {
        MyLog.w(TAG, "onCancelForbidSpeakDone targetId=" + targetId + ", errCode=" + errCode);
        if (errCode == ForbidManagePresenter.ERR_CODE_SUCCESS) {
            changeForbidSpeakBtnStatus(false);
        }
    }

    /**
     * 更改禁言按钮的状态
     *
     * @param isForbid
     */
    public void changeForbidSpeakBtnStatus(boolean isForbid) {
        mForbidSpeak.setSelected(isForbid);
        if (isForbid) {
            mForbidSpeakTv.setText(getString(R.string.cancel_banspeaker));
            mForbidSpeakTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            mForbidSpeakTv.setText(getString(R.string.forbid_speak));
            mForbidSpeakTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.live_forbid_speak, 0, 0, 0);
        }
    }

    @Override
    public void onkickViewerDone(long targetId, int errCode) {
        if (mUser != null && targetId == mUser.getUid()) {
            if (errCode == ErrorCode.CODE_SUCCESS) {
                ToastUtils.showToast(R.string.kick_viewer_success);
            } else if (errCode == ErrorCode.CODE_SERVER_RESPONSE_ERROR_CODE_NOT_HAVE_PERMISSION_TO_KICK) {
                ToastUtils.showToast(R.string.not_permission_to_kick);
            } else if (errCode == ErrorCode.CODE_SERVER_RESPONSE_ERROR_CODE_NOT_HAVE_PERMISSION_TO_KICK_VIEWER) {
                ToastUtils.showToast(R.string.not_permission_kick_this_viewer);
            } else {
                ToastUtils.showToast(R.string.kick_viewer_fail);
            }
        }
    }

    @Override
    public void onBlockViewer(long targetId, int errCode) {

    }

    /**
     * 定义点击事件的回调
     */
    public interface FloatPersonInfoClickListener {
        /**
         * 点击个人主页
         */
        void onClickHomepage(User user);

        /**
         * 点击排行第一
         */
        void onClickTopOne(User user);

        /**
         * 点击主头像
         */
        void onClickMainAvatar(User user);

        /**
         * 点击私信
         */
        void onClickSixin(User user);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SdkEventClass.OrientEvent event) {
        if (event.orientation == BaseRotateSdkActivity.ORIENTATION_DEFAULT) {
            return;
        } else if (event.orientation == BaseRotateSdkActivity.ORIENTATION_LANDSCAPE_NORMAL || event.orientation == BaseRotateSdkActivity.ORIENTATION_LANDSCAPE_REVERSED) {
            initViewLandscape();
        } else if (event.orientation == BaseRotateSdkActivity.ORIENTATION_PORTRAIT_NORMAL || event.orientation == BaseRotateSdkActivity.ORIENTATION_PORTRAIT_REVERSED) {
            initViewPortrait();
        }
    }
}
