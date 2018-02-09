package com.wali.live.watchsdk.contest;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.activity.BaseSdkActivity;
import com.base.fragment.FragmentListener;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.date.DateTimeUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.event.UserInfoEvent;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.mi.live.data.user.User;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.contest.cache.ContestGlobalCache;
import com.wali.live.watchsdk.contest.fragment.MyContestInfoFragment;
import com.wali.live.watchsdk.contest.manager.ContestDownloadManager;
import com.wali.live.watchsdk.contest.model.ContestNoticeModel;
import com.wali.live.watchsdk.contest.presenter.ContestAdvertisingPresenter;
import com.wali.live.watchsdk.contest.presenter.ContestInvitePresenter;
import com.wali.live.watchsdk.contest.presenter.ContestPreparePresenter;
import com.wali.live.watchsdk.contest.presenter.IContestAdvertisingView;
import com.wali.live.watchsdk.contest.presenter.IContestInviteView;
import com.wali.live.watchsdk.contest.presenter.IContestPrepareView;
import com.wali.live.watchsdk.contest.rank.ContestRankActivity;
import com.wali.live.watchsdk.contest.util.FormatUtils;
import com.wali.live.watchsdk.contest.view.AdvertisingView;
import com.wali.live.watchsdk.contest.view.ContestRevivalInputView;
import com.wali.live.watchsdk.contest.view.ContestRevivalRuleView;
import com.wali.live.watchsdk.contest.view.ContestSpecialInputView;
import com.wali.live.watchsdk.webview.WebViewActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by lan on 2018/1/10.
 */
public class ContestPrepareActivity extends BaseSdkActivity implements View.OnClickListener,
        IContestPrepareView, IContestInviteView, IContestAdvertisingView {
    private static final String RULE_PAGE_URL = "http://activity.zb.mi.com/egg/index?id=52";

    private static final String EXTRA_ZUID = "extra_zuid";

    private ImageView mBackBtn;
    private ImageView mShareBtn;

    private ViewGroup mLiveStatusArea;
    private TextView mLiveBonusTv;

    private ViewGroup mLiveStatusBtn;
    private TextView mLiveStatusTv;
    private ImageView mLiveStatusIv;
    private AnimationDrawable mLiveStatusDrawable;

    private ImageView mTitleIv;
    private ViewGroup mStatusArea;
    private TextView mBonusTv;
    private TextView mTimeTv;

    private BaseImageView mAvatarIv;

    private ViewGroup mContentArea;
    private ViewGroup mMyBonusArea;
    private TextView mMyBonusTv;

    private ViewGroup mMyRankArea;
    private TextView mMyRankTv;

    private TextView mInputBtn;
    private TextView mInputShareBtn;

    private TextView mRevivalCountTv;
    private TextView mInviteBtn;

    private TextView mSpecialCodeTv;
    private TextView mRuleTv;

    private AdvertisingView mAdvertisingView;

    private ContestRevivalInputView mRevivalInputView;
    private ContestSpecialInputView mSpecialInputView;

    private ContestRevivalRuleView mRevivalRuleView;

    private ContestPreparePresenter mPreparePresenter;
    private ContestInvitePresenter mInvitePresenter;

    private ContestAdvertisingPresenter mAdvertisingPresenter;
    private boolean mHasGetActInfo = false;
    private ContestDownloadManager mDownloadManager;

    private ContestNoticeModel mNoticeModel;
    private User mMySelf;

    private long mZuid;

    private boolean mIsInterval = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isMIUIV6()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        setContentView(R.layout.contest_prepare_layout);

        initData();
        initView();
        initPresenter();
        AccountAuthManager.triggerActionNeedAccount(this);
    }


    private void initData() {
        mMySelf = MyUserInfoManager.getInstance().getUser();

        Intent data = getIntent();
        if (data == null) {
            return;
        }
        mZuid = data.getLongExtra(EXTRA_ZUID, 0);
    }

    private void initView() {
        mBackBtn = $(R.id.back_btn);
        mBackBtn.setOnClickListener(this);

        mShareBtn = $(R.id.share_btn);
        mShareBtn.setOnClickListener(this);

        mLiveStatusArea = $(R.id.live_status_area);
        mLiveBonusTv = $(R.id.live_bonus_tv);

        mLiveStatusBtn = $(R.id.live_status_btn);
        mLiveStatusBtn.setOnClickListener(this);

        mLiveStatusTv = $(R.id.live_status_tv);
        mLiveStatusIv = $(R.id.live_status_iv);
        mLiveStatusDrawable = (AnimationDrawable) mLiveStatusIv.getBackground();

        mTitleIv = $(R.id.title_iv);
        mStatusArea = $(R.id.status_area);
        mBonusTv = $(R.id.bonus_tv);
        mTimeTv = $(R.id.time_tv);

        mAvatarIv = $(R.id.avatar_iv);
        mAvatarIv.setOnClickListener(this);
        updateAvatarView();

        mContentArea = $(R.id.content_area);
        mMyBonusArea = $(R.id.my_bonus_area);
        mMyBonusArea.setOnClickListener(this);
        mMyBonusTv = $(R.id.my_bonus_tv);

        mMyRankArea = $(R.id.my_rank_area);
        mMyRankArea.setOnClickListener(this);
        mMyRankTv = $(R.id.my_rank_tv);

        mInputBtn = $(R.id.input_btn);
        mInputBtn.setOnClickListener(this);
        mInputShareBtn = $(R.id.input_share_btn);
        mInputShareBtn.setOnClickListener(this);

        mRevivalCountTv = $(R.id.revival_count_tv);

        mInviteBtn = $(R.id.invite_tv);
        mInviteBtn.setOnClickListener(this);

        mRuleTv = $(R.id.rule_tv);
        mRuleTv.setOnClickListener(this);

        mSpecialCodeTv = $(R.id.special_code_tv);
        mSpecialCodeTv.setOnClickListener(this);

        mRevivalInputView = $(R.id.revival_input_view);
        mRevivalInputView.setInputListener(new ContestRevivalInputView.RevivalInputListener() {
            @Override
            public void setRevivalCode(String revivalCode) {
                mInvitePresenter.setInviteCode(revivalCode);
            }
        });
        mSpecialInputView = $(R.id.special_input_view);
        mSpecialInputView.setInputListener(new ContestSpecialInputView.SpecialInputListener() {
            @Override
            public void useSpecialCode(String code) {
                mInvitePresenter.useSpecialCode(code);
            }
        });

        mRevivalRuleView = $(R.id.revival_rule_view);
    }

    private void initPresenter() {
        mPreparePresenter = new ContestPreparePresenter(this, mZuid);
        mInvitePresenter = new ContestInvitePresenter(this);
        mInvitePresenter.getInviteCode();
        mAdvertisingPresenter = new ContestAdvertisingPresenter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPreparePresenter.getContestNotice();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        MyLog.w(TAG, "onNewIntent");
    }

    @Override
    public void getRevivalActSuccess() {
        MyLog.d(TAG, "getRevivalActSuccess");
        if (!mAdvertisingPresenter.hasCardAct()) {
            return;
        }
        mAdvertisingView = $(R.id.advertising_area);
        mAdvertisingView.setVisibility(View.VISIBLE);
        adjustToAdvertisingView();

        mDownloadManager = new ContestDownloadManager(mAdvertisingView, this);
        mAdvertisingView.init(mAdvertisingPresenter, mDownloadManager, mNoticeModel.getContestID());
    }

    //Invoked when has adv
    private void adjustToAdvertisingView() {
        MyLog.d(TAG, "adjustViewLayout");
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mTitleIv.getLayoutParams();
        lp.height = DisplayUtils.dip2px(64f);

        lp = (RelativeLayout.LayoutParams) mContentArea.getLayoutParams();
        lp.height = DisplayUtils.dip2px(280.66f);
        mContentArea.setLayoutParams(lp);
    }

    @Override
    public void getRevivalActFailed() {
        MyLog.d(TAG, "getRevivalAct failed");
    }

    @Override
    public void addRevivalCardActSuccess(int revivalNum) {
        MyLog.d(TAG, "addRevivalCardActSuccess num=" + revivalNum);
        ToastUtils.showToast(R.string.contest_act_add_revival_success);
        mRevivalCountTv.setText(String.valueOf(revivalNum));
    }

    @Override
    public void addRevivalCardActFailed(int errCode) {
        MyLog.d(TAG, "addRevivalCardActFailed retcode=" + errCode);
        if (errCode == ErrorCode.CODE_CONTEST_ACT_REENTER) {
            ToastUtils.showToast(R.string.contest_act_reenter);
        } else if (errCode == ErrorCode.CODE_CONTEST_ACT_NOT_EXIST) {
            ToastUtils.showToast(R.string.contest_act_not_exist);
        } else if (errCode == ErrorCode.CODE_CONTEST_ACT_ERROR) {
            ToastUtils.showToast(R.string.contest_act_add_revival_error);
        }
    }

    @Override
    public void setContestNotice(ContestNoticeModel model) {
        if (model == null) {
            return;
        }
        mNoticeModel = model;
        updateView();
        if (!mHasGetActInfo) {
            mAdvertisingPresenter.getRevivalAct(mNoticeModel.getContestID());
            mHasGetActInfo = true;
        }
        if (mNoticeModel.getStatus() == ContestNoticeModel.STATUS_GOING) {
            mPreparePresenter.cancelIntervalUpdate();
            mIsInterval = false;
        } else {
            if (!mIsInterval) {
                mIsInterval = true;

                long delayTime = mNoticeModel.getDelayTime();
                if (delayTime < 0) {
                    delayTime = 0;
                }
                mPreparePresenter.startIntervalUpdate(delayTime);
            }
        }
    }

    private void updateView() {
        updateStatusView();
        updateMyView();
    }

    private void updateMyView() {
        float totalIncome = mNoticeModel.getTotalIncome();
        mMyBonusTv.setText(FormatUtils.formatMoney(totalIncome));
        int rank = mNoticeModel.getRank();
        mMyRankTv.setText(FormatUtils.formatRank(rank));
    }

    private void updateAvatarView() {
        AvatarUtils.loadAvatarByUidTs(mAvatarIv, mMySelf.getUid(), mMySelf.getAvatar(), true);
    }

    private void updateInputView(boolean canUseCode) {
        if (canUseCode) {
            mInputBtn.setVisibility(View.VISIBLE);
            mInputBtn.setEnabled(true);

            mInputShareBtn.setVisibility(View.GONE);
            return;
        }
        mInputBtn.setVisibility(View.GONE);
        mInputShareBtn.setVisibility(View.VISIBLE);
    }

    private void updateStatusView() {
        int status = mNoticeModel.getStatus();
        if (status == ContestNoticeModel.STATUS_NORMAL) {
            mLiveStatusArea.setVisibility(View.GONE);

            mTitleIv.setVisibility(View.VISIBLE);
            mStatusArea.setVisibility(View.VISIBLE);
            mTimeTv.setText(DateTimeUtils.formatContestTimeString(this, mNoticeModel.getStartTime()));
            mBonusTv.setText(String.valueOf((long) mNoticeModel.getBonus()));
        } else if (status == ContestNoticeModel.STATUS_COMING) {
            mLiveStatusArea.setVisibility(View.VISIBLE);
            mLiveStatusTv.setText(R.string.contest_prepare_enter_live);
            if (!mLiveStatusDrawable.isRunning()) {
                mLiveStatusDrawable.start();
            }
            mLiveBonusTv.setText(String.valueOf((long) mNoticeModel.getBonus()));

            mTitleIv.setVisibility(View.GONE);
            mStatusArea.setVisibility(View.GONE);
        } else if (status == ContestNoticeModel.STATUS_GOING) {
            mLiveStatusArea.setVisibility(View.VISIBLE);
            mLiveStatusTv.setText(R.string.contest_prepare_begin_answer);
            if (!mLiveStatusDrawable.isRunning()) {
                mLiveStatusDrawable.start();
            }
            mLiveBonusTv.setText(String.valueOf((long) mNoticeModel.getBonus()));

            mTitleIv.setVisibility(View.GONE);
            mStatusArea.setVisibility(View.GONE);
        }
    }

    @Override
    public void getInviteCodeSuccess(String code) {
        mRevivalRuleView.updateCode();
        mRevivalCountTv.setText(String.valueOf(ContestGlobalCache.getRevivalNum()));
        updateInputView(ContestGlobalCache.canUseCode());
    }

    @Override
    public void setInviteCodeSuccess(int revivalNum) {
        ToastUtils.showToast(R.string.contest_prepare_revival_added);
        mRevivalCountTv.setText(String.valueOf(revivalNum));

        mRevivalInputView.hide();
        updateInputView(false);
    }

    @Override
    public void setInviteCodeFailure(int errCode) {
        MyLog.w(TAG, "set invite code failure, code=" + errCode);
        if (errCode == ErrorCode.CODE_CONTEST_INVITE_INVALID) {
            ToastUtils.showToast(R.string.contest_prepare_invite_invalid);
            mRevivalInputView.reset();
        } else if (errCode == ErrorCode.CODE_CONTEST_INVITE_PARAM_INVALID) {
            ToastUtils.showToast(R.string.contest_prepare_invite_invalid);
            mRevivalInputView.reset();
        } else if (errCode == ErrorCode.CODE_CONTEST_INVITE_MYSELF) {
            ToastUtils.showToast(R.string.contest_prepare_invite_myself);
            mRevivalInputView.reset();
        } else if (errCode == ErrorCode.CODE_CONTEST_INVITE_UUID_INVALID) {
            ToastUtils.showToast(R.string.contest_prepare_invite_uuid_invalid);
        } else {
            ToastUtils.showToast(R.string.contest_prepare_invite_server_error);
        }
    }

    @Override
    public void useSpecialCodeSuccess(int revivalNum) {
        MyLog.w(TAG, "use special code success, revivalNum=" + revivalNum);
        ToastUtils.showToast(R.string.use_special_invitenum_success);
        mRevivalCountTv.setText(String.valueOf(revivalNum));
        mSpecialInputView.reset();
    }

    @Override
    public void useSpecialCodeFailure(int errCode) {
        MyLog.w(TAG, "use special code failure, errCode=" + errCode);
        if (errCode == ErrorCode.CODE_CONTEST_INVITE_INVALID) {
            ToastUtils.showToast(R.string.contest_prepare_invite_invalid);
            mSpecialInputView.reset();
        } else if (errCode == ErrorCode.CODE_CONTEST_INVITE_PARAM_INVALID) {
            ToastUtils.showToast(R.string.contest_prepare_invite_invalid);
            mSpecialInputView.reset();
        } else if (errCode == ErrorCode.CODE_CONTEST_INVITE_MYSELF) {
            ToastUtils.showToast(R.string.contest_prepare_invite_myself);
            mSpecialInputView.reset();
        } else if (errCode == ErrorCode.CODE_CONTEST_INVITE_UUID_INVALID) {
            ToastUtils.showToast(R.string.contest_prepare_invite_uuid_invalid);
        } else if (errCode == ErrorCode.CODE_CONTEST_INVITE_CODE_USED) {
            ToastUtils.showToast(R.string.contest_prepare_invite_code_used);
            mSpecialInputView.reset();
        } else {
            ToastUtils.showToast(R.string.contest_prepare_invite_server_error);
        }
    }

    private void showRevivalInputView() {
        mRevivalInputView.show();
    }

    private void showSpecialInputView() {
        mSpecialInputView.show();
    }

    private void showRevivalRuleView() {
        mRevivalRuleView.show();
    }

    @Override
    public boolean isKeyboardResize() {
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_btn) {
            finish();
            return;
        }

        if (!AccountAuthManager.triggerActionNeedAccount(this)) {
            return;
        }

        if (id == R.id.live_status_btn) {
            if (CommonUtils.isFastDoubleClick()) {
                return;
            }
            ContestWatchActivity.open(this, mNoticeModel.getZuid(), mNoticeModel.getLiveId(), mNoticeModel.getVideoUrl());
        } else if (id == R.id.my_rank_area) {
            ContestRankActivity.open(this);
        } else if (id == R.id.input_btn) {
            showRevivalInputView();
        } else if (id == R.id.invite_tv || id == R.id.share_btn || id == R.id.input_share_btn) {
            showRevivalRuleView();
        } else if (id == R.id.my_bonus_area) {
            enterMyContestInfo();
        } else if (id == R.id.rule_tv) {
            enterRulePage();
        } else if (id == R.id.special_code_tv) {
            showSpecialInputView();
        }
    }

    private void enterMyContestInfo() {
        MyContestInfoFragment.openFragment(this);
    }

    private void enterRulePage() {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, RULE_PAGE_URL);
        intent.putExtra(WebViewActivity.EXTRA_IS_CONTEST, true);
        startActivity(intent);
    }

    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            //退出栈弹出
            String fName = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
            if (!TextUtils.isEmpty(fName)) {
                Fragment fragment = fm.findFragmentByTag(fName);
                if (null != fragment && fragment instanceof FragmentListener) {
                    //特定的返回事件处理
                    if (((FragmentListener) fragment).onBackPressed()) {
                        return;
                    }
                }
                try {
                    FragmentNaviUtils.popFragmentFromStack(this);
                } catch (Exception e) {
                    MyLog.e(e);
                }
                return;
            }
        }
        if (mRevivalRuleView.isShown()) {
            mRevivalRuleView.hide();
            return;
        }
        if (mRevivalInputView.isShown()) {
            mRevivalInputView.hide();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRevivalRuleView.destroy();
        mRevivalInputView.destroy();
        if (mLiveStatusDrawable.isRunning()) {
            mLiveStatusDrawable.stop();
        }
        if(mDownloadManager!=null){
            mDownloadManager.destroy();
        }
        mSpecialInputView.destroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MiLinkEvent.StatusLogined event) {
        MyLog.w(TAG, "receive event : statusLogin");
        if (event != null) {
            if (mInvitePresenter != null) {
                mInvitePresenter.getInviteCode();
            }
            if (mPreparePresenter != null) {
                mPreparePresenter.getContestNotice();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UserInfoEvent userInfoEvent) {
        MyLog.w(TAG, "userInfoEvent");
        if (mMySelf != null && mMySelf.getUid() != MyUserInfoManager.getInstance().getUuid()) {
            mMySelf = MyUserInfoManager.getInstance().getUser();
            updateAvatarView();
        }
    }

    public static void open(BaseActivity activity, long zuid) {
        Intent intent = new Intent(activity, ContestPrepareActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_ZUID, zuid);
        activity.startActivity(intent);
    }

}
