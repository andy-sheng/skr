package com.module.home.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.log.MyLog;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.constans.GameModeType;
import com.module.RouterConstants;
import com.module.home.R;

import com.module.home.persenter.PkInfoPresenter;
import com.module.home.view.IPkInfoView;
import com.module.home.widget.UserInfoTitleView;
import com.module.rank.IRankingModeService;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.level.view.NormalLevelView2;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.module.home.fragment.GameFragment.SHANDIAN_BADGE;
import static com.module.home.fragment.GameFragment.STAR_BADGE;
import static com.module.home.fragment.GameFragment.TOP_BADGE;

public class PkInfoFragment extends BaseFragment implements IPkInfoView {
    public final static String TAG = "PkInfoFragment";

    SmartRefreshLayout mSmartRefreshLayout;
    ExImageView mIvVoiceRoom;
    ExImageView mIvAthleticsPk;
    CommonTitleBar mTitlebar;
    UserInfoTitleView mUserInfoTitle;
    ClassicsHeader mClassicsHeader;
    ExRelativeLayout mMedalLayout;
    NormalLevelView2 mLevelView;
    ExTextView mLevelTv;
    ImageView mPaiweiImg;
    ExTextView mRankNumTv;
    ImageView mSingendImg;
    ExTextView mSingendNumTv;
    RelativeLayout mRankArea;
    ExTextView mRankText;
    ExImageView mRankDiffIv;
    ExImageView mMedalIv;

    int rank = 0;           //当前父段位
    int subRank = 0;        //当前子段位
    int starNum = 0;        //当前星星
    int starLimit = 0;      //当前星星上限
    String levelDesc;

    PkInfoPresenter mPkInfoPresenter;

    @Override
    public int initView() {
        return R.layout.pkinfo_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mSmartRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.smart_refresh_layout);
        mClassicsHeader = (ClassicsHeader) mRootView.findViewById(R.id.classics_header);

        mTitlebar = (CommonTitleBar)mRootView.findViewById(R.id.titlebar);
        mUserInfoTitle = (UserInfoTitleView) mRootView.findViewById(R.id.user_info_title);
        mMedalLayout = (ExRelativeLayout) mRootView.findViewById(R.id.medal_layout);
        mLevelView = (NormalLevelView2) mRootView.findViewById(R.id.level_view);
        mLevelTv = (ExTextView) mRootView.findViewById(R.id.level_tv);
        mPaiweiImg = (ImageView) mRootView.findViewById(R.id.paiwei_img);
        mRankNumTv = (ExTextView) mRootView.findViewById(R.id.rank_num_tv);
        mSingendImg = (ImageView) mRootView.findViewById(R.id.singend_img);
        mSingendNumTv = (ExTextView) mRootView.findViewById(R.id.singend_num_tv);
        mRankArea = (RelativeLayout) mRootView.findViewById(R.id.rank_area);
        mRankText = (ExTextView) mRootView.findViewById(R.id.rank_text);
        mRankDiffIv = (ExImageView) mRootView.findViewById(R.id.rank_diff_iv);
        mIvAthleticsPk = (ExImageView) mRootView.findViewById(R.id.iv_athletics_pk);
        mIvVoiceRoom = (ExImageView) mRootView.findViewById(R.id.iv_voice_room);
        mMedalIv = (ExImageView) mRootView.findViewById(R.id.medal_iv);
        mUserInfoTitle.getTopUserBg().setVisibility(View.GONE);
        mClassicsHeader.setBackground(new DrawableCreator.Builder()
                .setGradientColor(Color.parseColor("#7088FF"), Color.parseColor("#7088FF"))
                .setGradientAngle(0)
                .build());

        if (U.getDeviceUtils().hasNotch(getContext())) {
            mTitlebar.setVisibility(View.VISIBLE);
        } else {
            mTitlebar.setVisibility(View.GONE);
        }

        mSmartRefreshLayout.setEnableRefresh(true);
        mSmartRefreshLayout.setEnableLoadMore(false);
        mSmartRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mSmartRefreshLayout.setEnableOverScrollDrag(true);
        mSmartRefreshLayout.setRefreshHeader(mClassicsHeader);
        mSmartRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                initBaseInfo();
                mPkInfoPresenter.getHomePage(MyUserInfoManager.getInstance().getUid(), true);
            }
        });

        mIvAthleticsPk.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
                        .withInt("key_game_type", GameModeType.GAME_MODE_CLASSIC_RANK)
                        .withBoolean("selectSong", true)
                        .navigation();

                StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_HOME),
                        StatConstants.KEY_RANK_CLICK, null);
            }
        });

        mRankArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                IRankingModeService iRankingModeService = (IRankingModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
                Class<BaseFragment> baseFragment = (Class<BaseFragment>) iRankingModeService.getLeaderboardFragmentClass();
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), baseFragment)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                            }
                        })
                        .build());
            }
        });

        mIvVoiceRoom.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_AUDIOROOM)
                        .withBoolean("selectSong", true)
                        .navigation();
            }
        });

        mPkInfoPresenter = new PkInfoPresenter(this);
        addPresent(mPkInfoPresenter);
        initBaseInfo();
        mPkInfoPresenter.getHomePage(MyUserInfoManager.getInstance().getUid(), true);
    }

    @Override
    public void showUserLevel(List<UserLevelModel> list) {
        mSmartRefreshLayout.finishRefresh();
        // 展示段位信息
        for (UserLevelModel userLevelModel : list) {
            if (userLevelModel.getType() == UserLevelModel.RANKING_TYPE) {
                rank = userLevelModel.getScore();
            } else if (userLevelModel.getType() == UserLevelModel.SUB_RANKING_TYPE) {
                subRank = userLevelModel.getScore();
                levelDesc = userLevelModel.getDesc();
            } else if (userLevelModel.getType() == UserLevelModel.TOTAL_RANKING_STAR_TYPE) {
                starNum = userLevelModel.getScore();
            } else if (userLevelModel.getType() == UserLevelModel.REAL_RANKING_STAR_TYPE) {
                starLimit = userLevelModel.getScore();
            }
        }
        mLevelView.bindData(rank, subRank);
        mLevelTv.setText(levelDesc);
    }

    @Override
    public void showGameStatic(List<GameStatisModel> list) {
        mSmartRefreshLayout.finishRefresh();
        for (GameStatisModel gameStatisModel : list) {
            if (gameStatisModel.getMode() == GameModeType.GAME_MODE_CLASSIC_RANK) {
                SpannableStringBuilder stringBuilder = new SpanUtils()
                        .append(String.valueOf(gameStatisModel.getTotalTimes())).setFontSize(14, true)
                        .append("场").setFontSize(10, true)
                        .create();
                mRankNumTv.setText(stringBuilder);
            } else if (gameStatisModel.getMode() == GameModeType.GAME_MODE_GRAB) {
                SpannableStringBuilder stringBuilder = new SpanUtils()
                        .append(String.valueOf(gameStatisModel.getTotalTimes())).setFontSize(14, true)
                        .append("首").setFontSize(10, true)
                        .create();
                mSingendNumTv.setText(stringBuilder);
            }
        }
    }

    private void initBaseInfo() {
        mUserInfoTitle.showBaseInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        initBaseInfo();
    }

    @Override
    public void showRankView(UserRankModel userRankModel) {
        MyLog.d(TAG, "showRankView" + " userRankModel=" + userRankModel);
        mUserInfoTitle.showRankView(userRankModel);
        MyLog.d(TAG, "showRankView" + " userRankModel=" + userRankModel);

        if (userRankModel.getDiff() == 0) {
            // 默认按照上升显示
            mRankDiffIv.setVisibility(View.GONE);
            mRankText.setText(highlight(userRankModel.getText(), userRankModel.getHighlight(), true));
        } else if (userRankModel.getDiff() > 0) {
            mRankDiffIv.setVisibility(View.VISIBLE);
            mRankDiffIv.setImageResource(R.drawable.shangsheng_ic);
            mRankText.setText(highlight(userRankModel.getText(), userRankModel.getHighlight(), true));
        } else if (userRankModel.getDiff() < 0) {
            mRankDiffIv.setVisibility(View.VISIBLE);
            mRankDiffIv.setImageResource(R.drawable.xiajiang_ic);
            mRankText.setText(highlight(userRankModel.getText(), userRankModel.getHighlight(), false));
        }

//        showPopWindow(userRankModel.getDiff());

        if (userRankModel.getBadge() == STAR_BADGE) {
            mMedalIv.setBackground(getResources().getDrawable(R.drawable.paiming));
        } else if (userRankModel.getBadge() == TOP_BADGE) {
            mMedalIv.setBackground(getResources().getDrawable(R.drawable.paihang));
        } else if (userRankModel.getBadge() == SHANDIAN_BADGE) {
            mMedalIv.setBackground(getResources().getDrawable(R.drawable.dabai));
        }
    }

    private SpannableString highlight(String text, String target, boolean isUp) {
        SpannableString spannableString = new SpannableString(text);
        Pattern pattern = Pattern.compile(target);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            ForegroundColorSpan span = new ForegroundColorSpan(Color.parseColor("#FF3B3C"));
            spannableString.setSpan(span, matcher.start(), matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }

    private String formatRank(int rankSeq) {
        if (rankSeq < 10000) {
            return String.valueOf(rankSeq);
        } else {
            float result = (float) (Math.round(((float) rankSeq / 10000) * 10)) / 10;
            return String.valueOf(result) + "w";
        }
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        initBaseInfo();
        mPkInfoPresenter.getHomePage(MyUserInfoManager.getInstance().getUid(), false);
    }

    @Override
    public boolean isInViewPager() {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
