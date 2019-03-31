package com.module.home.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.account.UserAccountManager;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserRankModel;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GameModeType;
import com.module.RouterConstants;
import com.module.home.R;

import com.module.rank.IRankingModeService;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

public class PkInfoFragment extends BaseFragment {
    public final static String TAG = "PkInfoFragment";

    SmartRefreshLayout mSmartRefreshLayout;
    ExTextView mTvBg;
    BaseImageView mIvUserIcon;
    TextView mTvUserName;
    LinearLayout mLlNumContainer;
    TextView mTvNum;
    TextView mTvArea;
    LinearLayout mLlCountry;
    TextView mTvNumCountry;
    TextView mTvCountry;
    ExTextView mBottomBg;
    ExTextView mTvToLeaderBoard;
    ExImageView mIvVoiceRoom;
    ExImageView mIvAthleticsPk;
    RelativeLayout mRlAreaContainer;
    ClassicsHeader mClassicsHeader;

    UserInfoServerApi mUserInfoServerApi;

    @Override
    public int initView() {
        return R.layout.pkinfo_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mSmartRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.smart_refresh_layout);
        mClassicsHeader = (ClassicsHeader) mRootView.findViewById(R.id.classics_header);
        mTvBg = (ExTextView) mRootView.findViewById(R.id.tv_bg);
        mIvUserIcon = (BaseImageView) mRootView.findViewById(R.id.iv_user_icon);
        mTvUserName = (TextView) mRootView.findViewById(R.id.tv_user_name);
        mLlNumContainer = (LinearLayout) mRootView.findViewById(R.id.ll_num_container);
        mTvNum = (TextView) mRootView.findViewById(R.id.tv_num);
        mTvArea = (TextView) mRootView.findViewById(R.id.tv_area);
        mLlCountry = (LinearLayout) mRootView.findViewById(R.id.ll_country);
        mTvNumCountry = (TextView) mRootView.findViewById(R.id.tv_num_country);
        mTvCountry = (TextView) mRootView.findViewById(R.id.tv_country);
        mBottomBg = (ExTextView) mRootView.findViewById(R.id.bottom_bg);
        mTvToLeaderBoard = (ExTextView) mRootView.findViewById(R.id.tv_to_leader_board);
        mIvVoiceRoom = (ExImageView) mRootView.findViewById(R.id.iv_voice_room);
        mIvAthleticsPk = (ExImageView) mRootView.findViewById(R.id.iv_athletics_pk);
        mRlAreaContainer = (RelativeLayout) mRootView.findViewById(R.id.rl_area_container);

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
                showRankInfo();
            }
        });

        AvatarUtils.loadAvatarByUrl(mIvUserIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(U.getColor(R.color.white))
                        .build());

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

        mIvVoiceRoom.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_AUDIOROOM)
                        .withBoolean("selectSong", true)
                        .navigation();
            }
        });

        mTvToLeaderBoard.setOnClickListener(new DebounceViewClickListener() {
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

        mTvUserName.setText(MyUserInfoManager.getInstance().getNickName());
        showRankInfo();
    }

    private void showRankInfo() {
        if (mUserInfoServerApi == null) {
            mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        }

        ApiMethods.subscribe(mUserInfoServerApi.getReginRank(MyUserInfoManager.getInstance().getUid()), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                mSmartRefreshLayout.finishRefresh();
                if (result.getErrno() == 0) {
                    List<UserRankModel> userRankModels = JSON.parseArray(result.getData().getString("seqInfo"), UserRankModel.class);
                    showRankView(userRankModels);
                } else {
                    MyLog.d(TAG, "showRankInfo" + " result=" + result);
                }
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort("网络异常");
                mSmartRefreshLayout.finishRefresh();
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                mSmartRefreshLayout.finishRefresh();
                U.getToastUtil().showShort("网络超时");
            }
        }, this);
    }

    private void showRankView(List<UserRankModel> userRankModelList) {
        for (UserRankModel userRankModel : userRankModelList) {
            if (userRankModel.getCategory() == 1) {
                mTvNumCountry.setText(userRankModel.getRankSeq() == 0 ? "**" : formatRank(userRankModel.getRankSeq()));
            } else if (userRankModel.getCategory() == 4) {
                if (userRankModel.getRankSeq() == 0) {
                    mRlAreaContainer.setVisibility(View.GONE);
                } else {
                    mRlAreaContainer.setVisibility(View.VISIBLE);
                    mTvNum.setText(formatRank(userRankModel.getRankSeq()));
                    mTvArea.setText(userRankModel.getRegionDesc());
                }
            }
        }

        mTvUserName.setText(MyUserInfoManager.getInstance().getNickName());
        AvatarUtils.loadAvatarByUrl(mIvUserIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(U.getColor(R.color.white))
                        .build());
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
