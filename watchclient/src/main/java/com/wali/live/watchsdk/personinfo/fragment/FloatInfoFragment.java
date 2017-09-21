package com.wali.live.watchsdk.personinfo.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.activity.BaseRotateSdkActivity;
import com.base.dialog.MyAlertDialog;
import com.base.event.SdkEventClass;
import com.base.fragment.BaseFragment;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.event.LiveRoomManagerEvent;
import com.mi.live.data.manager.LiveRoomCharacterManager;
import com.mi.live.data.user.User;
import com.wali.live.manager.WatchRoomCharactorManager;
import com.wali.live.proto.RankProto;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.editinfo.EditInfoActivity;
import com.wali.live.watchsdk.eventbus.DismissFloatPersonInfoEvent;
import com.wali.live.watchsdk.personinfo.presenter.FloatInfoPresenter;
import com.wali.live.watchsdk.personinfo.presenter.ForbidManagePresenter;
import com.wali.live.watchsdk.personinfo.presenter.IFloatInfoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observable;

/**
 * Created by wangmengjie on 17-8-23.
 */
public class FloatInfoFragment extends RxFragment
        implements View.OnClickListener, ForbidManagePresenter.IForbidManageView, IFloatInfoView {

    private FloatInfoPresenter mPresenter;
    private User mUser;
    private RankProto.RankUser mTopOneUser;
    private FloatInfoClickListener mFloatInfoClickListener;
    private ForbidManagePresenter mForbidManagePresenter;

    //views
    private SimpleDraweeView mMainAvatar;       //主头像
    private ImageView mEditIcon;
    private ImageView mWeiboVerifyConnerImage;  //微博认证的角标
    private SimpleDraweeView mTopOneAvatar;     //排行第一的头像
    private SimpleDraweeView mTopOneRelationView;//联系图标
    private ImageView mTopOneConner;        //排行第一的角标

    private TextView mNicknameTV;       //昵称
    private TextView mIdTv;     //显示ID
    private TextView mSignTv;       //显示签名
    private View mVerifyZone;       //认证信息
    private TextView mVerifyLine1Tv;     //显示第一行认证信息
    private TextView mSentDiamondTv;      //显示送出的钻石
    private TextView mLevelTv;      //显示级别
    private ImageView mGenderIv;       //性别

    private TextView mLiveTicketCountTv;    //显示星票数量
    private TextView mFollowCountTv;        //显示关注数
    private TextView mFansCountTv;      //显示粉丝数

    private RelativeLayout mFollowButton;        //关注按钮
    private TextView mFollowButtonTv;
    private RelativeLayout mForbidSpeak;
    private TextView mForbidSpeakTv;

    @Override
    public int getRequestCode() {
        return GlobalData.getRequestCode();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        MyLog.d(TAG, "createView");
        return inflater.inflate(R.layout.float_info_layout, container, false);
    }

    @Override
    protected void bindView() {
        MyLog.d(TAG, "bindView");

        $click($(R.id.out_view), this);
        $click($(R.id.close_btn), this);

        //被查看者头像
        {
            mMainAvatar = $(R.id.top_main_avatar);
            mEditIcon = $(R.id.edit_icon);
            mWeiboVerifyConnerImage = $(R.id.weibo_verify_conner);

            $click(mMainAvatar, this);
            $click(mEditIcon, this);
        }

        //星票贡献者相关
        {
            mTopOneAvatar = $(R.id.top_one_avatar);
            mTopOneRelationView = $(R.id.top_one_relation_avatar);
            mTopOneConner = $(R.id.top_one_conner);

            $click(mTopOneAvatar, this);
        }

        //昵称区域
        {
            mNicknameTV = $(R.id.my_nick);
            mLevelTv = $(R.id.level_tv);
            mVerifyZone = $(R.id.verify_zone);
            mVerifyLine1Tv = $(R.id.verify_line1_tv);
            mGenderIv = $(R.id.gender_iv);
        }

        //直播号码、签名、送出钻石
        {
            mIdTv = $(R.id.my_id_tv);
            mSignTv = $(R.id.my_singature_tv);
            mSentDiamondTv = $(R.id.hint_sent_count_tv);
        }

        //星票、观众、粉丝
        {
            mLiveTicketCountTv = $(R.id.live_ticket_tv);
            mFollowCountTv = $(R.id.follow_count_tv);
            mFansCountTv = $(R.id.fans_count_tv);
        }

        //关注、禁言
        {
            mFollowButton = $(R.id.follow_button);
            mFollowButtonTv = $(R.id.follow_button_tv);
            mForbidSpeak = $(R.id.forbid_speak);
            mForbidSpeakTv = $(R.id.forbid_speak_tv);

            $click(mFollowButton, this);
            $click(mForbidSpeak, this);
        }

        initPresenter();
    }

    private void initView() {
        MyLog.d(TAG, "initView");
        if (mUser == null) {
            return;
        }
        AvatarUtils.loadAvatarByUidTs(mMainAvatar, mUser.getUid(), 0, true);

        long ownerUid = mPresenter.getOwnerUid();
        //编辑资料
        if (mUser.getUid() != 0 &&
                mUser.getUid() != ownerUid &&
                mUser.getUid() == UserAccountManager.getInstance().getUuidAsLong()) {
            mEditIcon.setVisibility(View.VISIBLE);
            $click(mEditIcon, this);
        } else {
            mEditIcon.setVisibility(View.GONE);
            $click(mEditIcon, null);
        }
        //关注和禁言
        if (mUser.getUid() == UserAccountManager.getInstance().getUuidAsLong()) {
            mFollowButton.setVisibility(View.GONE);
        } else {

            if (getActivity() instanceof ForbidManagePresenter.IForbidManageProvider) {
                //判断是否有禁言权限和踢人权限
                if (ownerUid == UserAccountManager.getInstance().getUuidAsLong()) {
                    changeForbidSpeakBtnStatus(
                            LiveRoomCharacterManager.getInstance().isBanSpeaker(mUser.getUid()));
                    mForbidSpeak.setVisibility(View.VISIBLE);
                } else if (mUser.getUid() != ownerUid &&
                        WatchRoomCharactorManager.getInstance().hasManagerPower(ownerUid)) {
                    changeForbidSpeakBtnStatus(
                            WatchRoomCharactorManager.getInstance().isBanSpeaker(mUser.getUid()));
                    mForbidSpeak.setVisibility(View.VISIBLE);
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

    private void initPresenter() {
        mPresenter = new FloatInfoPresenter(this, getArguments());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.d(TAG, "onCreate");
        EventBus.getDefault().register(this);
        setupForbidManagePresenter();
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        MyLog.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        KeyboardUtils.hideKeyboardImmediately(getActivity());
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onOrientation(true);
        } else {
            onOrientation(false);
        }
    }

    @Override
    protected String getTAG() {
        return "FloatInfoFragment";
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
    public void popUnFollowDialog() {
        MyLog.d(TAG, "pop UnFollowDialog");
        final MyAlertDialog.Builder builder = new MyAlertDialog.Builder(getActivity());
        builder.setMessage(R.string.unfollow_dialog_title);
        //取消关注
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mPresenter.followOrUnFollow();
            }
        });
        //不取消关注
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        MyAlertDialog dialog = builder.setAutoDismiss(false).setCancelable(true).create();
        dialog.show();
    }

    @Override
    public void refreshAllViews(User user, RankProto.RankUser topOneUser) {
        MyLog.d(TAG, "refreshAllViews");
        mUser = user;
        mTopOneUser = topOneUser;
        initView();
        refreshAvatar();
        refreshUserInfo();
    }

    private void refreshAvatar() {
        MyLog.d(TAG, "refreshAvatar");
        if (mUser == null) {
            if (mUser.getUid() > 0) {
                mWeiboVerifyConnerImage.setVisibility(View.GONE);
            }
        } else {
            AvatarUtils.loadAvatarByUidTs(mMainAvatar, mUser.getUid(), mUser.getAvatar(), true);
            if (mUser.getCertificationType() == 0 && !mUser.isRedName()) {
                mWeiboVerifyConnerImage.setVisibility(View.GONE);
            } else {
                if (mUser.isRedName()) {
                    mWeiboVerifyConnerImage.setVisibility(View.GONE);
                } else {
                    mWeiboVerifyConnerImage.setVisibility(View.VISIBLE);
                    mWeiboVerifyConnerImage.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(mUser.getCertificationType()));
                }
            }
        }
        if (mTopOneUser != null) {
            AvatarUtils.loadAvatarByUidTs(mTopOneAvatar, mTopOneUser.getUuid(), 0, true);
            mTopOneAvatar.setVisibility(View.VISIBLE);
            mTopOneConner.setVisibility(View.VISIBLE);
            mTopOneRelationView.setVisibility(View.VISIBLE);
        } else {
            mTopOneAvatar.setVisibility(View.GONE);
            mTopOneConner.setVisibility(View.GONE);
            mTopOneRelationView.setVisibility(View.GONE);
        }
    }

    @Override
    public void refreshUserInfo() {
        MyLog.d(TAG, "refreshUserInfo");
        if (null == getActivity() || getActivity().isFinishing()) {
            return;
        }
        if (mUser == null) {
            if (mUser.getUid() > 0 && getActivity() != null) {
                mIdTv.setText(getActivity().getResources().getString(R.string.default_id_hint) + mUser.getUid());
            }
            return;
        }

        refreshInfoZone();

        //显示送出的钻石
        int sentDiamondCount = mUser.getSendDiamondNum() < 0 ? 0 : mUser.getSendDiamondNum();
        int sentVirtualDiamondCount = mUser.getSentVirtualDiamondNum() < 0 ?
                0 : mUser.getSentVirtualDiamondNum();
        int totalSentDiamondCount = sentDiamondCount + sentVirtualDiamondCount;
        mSentDiamondTv.setText(getResources().getQuantityString(R.plurals.sent_diamond_text,
                totalSentDiamondCount, totalSentDiamondCount));

        //显示星票数
        int liveTicketsNumber = mUser.getLiveTicketNum() < 0 ? 0 : mUser.getLiveTicketNum();
        MyLog.v(TAG + " handleMsgFreshUserInfo liveTicketsNumber == " + liveTicketsNumber);
        mLiveTicketCountTv.setText(String.valueOf(liveTicketsNumber));

        //显示关注数
        int followNum = mUser.getFollowNum() < 0 ? 0 : mUser.getFollowNum();
        mFollowCountTv.setText(String.valueOf(followNum));

        //显示粉丝数
        int fansCount = mUser.getFansNum() < 0 ? 0 : mUser.getFansNum();
        mFansCountTv.setText(String.valueOf(fansCount));

        if (mUser.getUid() != UserAccountManager.getInstance().getUuidAsLong()) {
            //刷新关注按钮
            if (mUser.isFocused()) {
                if (!mUser.isBothwayFollowing()) {
                    mFollowButtonTv.setText(R.string.already_followed);
                } else {
                    mFollowButtonTv.setText(R.string.follow_both);
                }
                mFollowButtonTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } else {
                mFollowButtonTv.setText(R.string.add_follow);
                mFollowButtonTv.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.live_card_add_attention, 0, 0, 0);
            }
        }
    }

    public void refreshInfoZone() {
        MyLog.d(TAG, "refreshInfoZone");
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
            mIdTv.setText(GlobalData.app().getResources().getString(R.string.default_id_hint) +
                    String.valueOf(mUser.getUid()));
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
        int level = mUser.getLevel() <= 1 ? 1 : mUser.getLevel();
        GetConfigManager.LevelItem levelItem = ItemDataFormatUtils.getLevelItem(level);
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
    public void onClick(View v) {
        if (getActivity() == null) {
            return;
        }
        if (v.getId() == R.id.close_btn || v.getId() == R.id.out_view) {
            onBackPressed();
            return;
        } else if (!AccountAuthManager.triggerActionNeedAccount(getActivity())) {
            return;
        }
        if (v.getId() == R.id.top_main_avatar) {
            onClickMainAvatar();
        } else if (v.getId() == R.id.edit_icon) {
            onClickEditIcon();
        } else if (v.getId() == R.id.top_one_avatar) {
            onClickTopOneAvatar();
        } else if (v.getId() == R.id.follow_button) {
            onClickFollowButton();
        } else if (v.getId() == R.id.forbid_speak) {
            onClickForbidSpeak();
        }
    }

    private void onClickMainAvatar() {
        MyLog.d(TAG, "click mainAvatar");
        onBackPressed();
        onClickEditIcon();
        if (mFloatInfoClickListener != null) {
            mFloatInfoClickListener.onClickMainAvatar(mUser);
        }
    }

    private void onClickEditIcon() {
        if (mEditIcon.getVisibility() == View.VISIBLE) {
            EditInfoActivity.open(getActivity());
        }
    }

    private void onClickTopOneAvatar() {
        MyLog.d(TAG, "click topOneAvatar");
        onBackPressed();
        StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_USERINFO_CARD_NO1, 1);
        if (mFloatInfoClickListener != null) {
            mFloatInfoClickListener.onClickTopOne(mUser);
        }
    }

    private void onClickFollowButton() {
        MyLog.d(TAG, "click followButton");
        StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_USERINFO_CARD_FOLLOW, 1);
        mPresenter.handleFollow();
    }

    private void onClickForbidSpeak() {
        MyLog.d(TAG, "click forbid speak");
        if (mForbidManagePresenter != null) {
            if (mForbidSpeak.isSelected()) {
                mForbidManagePresenter.cancelForbidSpeak(
                        mPresenter.getRoomId(),
                        mPresenter.getOwnerUid(),
                        UserAccountManager.getInstance().getUuidAsLong(),
                        mUser.getUid());
            } else {
                mForbidManagePresenter.forbidSpeak(
                        mPresenter.getRoomId(),
                        mPresenter.getOwnerUid(),
                        UserAccountManager.getInstance().getUuidAsLong(),
                        mUser);
            }
        }
    }

    public void setFloatPersonInfoClickListener(FloatInfoClickListener listener) {
        if (listener != null) {
            mFloatInfoClickListener = listener;
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

    @Override
    public void onKickViewerDone(long targetId, int errCode) {
    }

    @Override
    public void onBlockViewer(long targetId, int errCode) {
    }

    @Override
    public void onOrientation(boolean isLandscape) {
        if (mRootView == null) {
            return;
        }
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)
                mRootView.findViewById(R.id.count_hint).getLayoutParams();
        lp.topMargin = DisplayUtils.dip2px(isLandscape ? 10 : 24);

        RelativeLayout.LayoutParams lpLine = (RelativeLayout.LayoutParams)
                mRootView.findViewById(R.id.splite_line).getLayoutParams();
        lpLine.topMargin = DisplayUtils.dip2px(isLandscape ? 10f : 14f);

        RelativeLayout.LayoutParams lpFollow = (RelativeLayout.LayoutParams)
                mRootView.findViewById(R.id.admin_area).getLayoutParams();
        lpFollow.topMargin = DisplayUtils.dip2px(isLandscape ? 10f : 14f);

        RelativeLayout.LayoutParams lpBottom = (RelativeLayout.LayoutParams)
                mRootView.findViewById(R.id.user_info_zone).getLayoutParams();
        lpBottom.topMargin = DisplayUtils.dip2px(isLandscape ? 95f : 97.3f);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FollowOrUnfollowEvent event) {
        if (event != null && mUser != null) {
            mUser.setIsBothwayFollowing(event.isBothFollow);
            refreshUserInfo();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LiveRoomManagerEvent event) {
        MyLog.d(TAG, "receive LiveRoomManagerEvent");
        if (event != null) {
            refreshUserInfo();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SdkEventClass.OrientEvent event) {
        MyLog.d(TAG, "receive orientation event");
        if (event.orientation == BaseRotateSdkActivity.ORIENTATION_DEFAULT) {
            return;
        } else if (event.orientation == BaseRotateSdkActivity.ORIENTATION_LANDSCAPE_NORMAL ||
                event.orientation == BaseRotateSdkActivity.ORIENTATION_LANDSCAPE_REVERSED) {
            onOrientation(true);
        } else if (event.orientation == BaseRotateSdkActivity.ORIENTATION_PORTRAIT_NORMAL ||
                event.orientation == BaseRotateSdkActivity.ORIENTATION_PORTRAIT_REVERSED) {
            onOrientation(false);
        }
    }

    @Override
    public <T> Observable.Transformer<T, T> bindLifecycle() {
        return bindUntilEvent();
    }

    public static FloatInfoFragment openFragment(
            BaseActivity activity,
            long fromUuid,
            long ownerUuid,
            String roomId,
            String url,
            FloatInfoFragment.FloatInfoClickListener listener) {
        return openFragment(activity, fromUuid, ownerUuid, roomId, url, listener, -1);
    }


    public static FloatInfoFragment openFragment(
            BaseActivity activity,
            long fromUuid,
            long ownerUuid,
            String roomId,
            String url,
            FloatInfoFragment.FloatInfoClickListener listener,
            long enterTime) {
        Bundle bundle = new Bundle();
        bundle.putLong(FloatInfoPresenter.EXTRA_IN_UUID, fromUuid);
        bundle.putLong(FloatInfoPresenter.EXTRA_IN_OWNER_UUID, ownerUuid);
        bundle.putString(FloatInfoPresenter.EXTRA_IN_ROOM_ID, roomId);
        bundle.putString(FloatInfoPresenter.EXTRA_IN_LIVE_URL, url);
        bundle.putLong(FloatInfoPresenter.EXTRA_IN_LIVE_ENTER_TIME, enterTime);
        bundle.putString(EXTRA_SCREEN_ORIENTATION, BaseFragment.PARAM_FOLLOW_SYS);

        FloatInfoFragment fragment = (FloatInfoFragment) FragmentNaviUtils
                .addFragmentWithZoomInOutAnimation(activity, R.id.main_act_container,
                        FloatInfoFragment.class, bundle, true, true, true);
        fragment.setFloatPersonInfoClickListener(listener);
        return fragment;
    }

    public interface FloatInfoClickListener {
        /**
         * 点击排行第一
         */
        void onClickTopOne(User user);

        /**
         * 点击主头像
         */
        void onClickMainAvatar(User user);
    }
}