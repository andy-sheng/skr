package com.module.playways.rank.room.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.userinfo.model.RankInfoModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.log.MyLog;
import com.common.utils.PermissionUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.rank.room.adapter.LeaderBoardAdapter;
import com.module.playways.rank.room.presenter.LeaderboardPresenter;
import com.module.playways.rank.room.view.ILeaderBoardView;
import com.module.rank.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

/**
 * 排行榜
 */
public class LeaderboardFragment extends BaseFragment implements ILeaderBoardView {
    RecyclerView mRecyclerView;

    LeaderBoardAdapter mLeaderBoardAdapter;

    LeaderboardPresenter mLeaderboardPresenter;

    SimpleDraweeView mSdvRightChampainIcon;
    ExTextView mTvRightChanpainName;
    LinearLayout mLlRightChampain;
    TextView mTvRightChanpianStart;
    TextView mTvRightSegmentName;
    SimpleDraweeView mSdvLeftChampainIcon;
    ExTextView mTvLeftChanpainName;
    LinearLayout mLlLeftChampain;
    TextView mTvLeftChanpianStart;
    TextView mTvLeftSegmentName;
    SimpleDraweeView mSdvChampainIcon;
    ExTextView mTvChanpainName;
    LinearLayout mLlChampain;
    TextView mTvChanpianStart;
    TextView mTvSegmentName;
    TextView mTvArea;
    ExImageView mIvBack;

    LinearLayout mLlAreaContainer;
    ExTextView mTvCurArea;
    ExTextView mTvCountry;

    SmartRefreshLayout mRefreshLayout;
    boolean mHasMore = true;

    ImageView mIvRankLeft;
    ImageView mIvRank;
    ImageView mIvRankRight;

    @Override
    public int initView() {
        return R.layout.leader_board_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mLeaderBoardAdapter = new LeaderBoardAdapter();
        mLeaderboardPresenter = new LeaderboardPresenter(this);
        mRecyclerView.setAdapter(mLeaderBoardAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mSdvRightChampainIcon = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_right_champain_icon);
        mTvRightChanpainName = (ExTextView) mRootView.findViewById(R.id.tv_right_chanpain_name);
        mLlRightChampain = (LinearLayout) mRootView.findViewById(R.id.ll_right_champain);
        mTvRightChanpianStart = (TextView) mRootView.findViewById(R.id.tv_right_chanpian_start);
        mTvRightSegmentName = (TextView) mRootView.findViewById(R.id.tv_right_segment_name);
        mSdvLeftChampainIcon = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_left_champain_icon);
        mTvLeftChanpainName = (ExTextView) mRootView.findViewById(R.id.tv_left_chanpain_name);
        mLlLeftChampain = (LinearLayout) mRootView.findViewById(R.id.ll_left_champain);
        mTvLeftChanpianStart = (TextView) mRootView.findViewById(R.id.tv_left_chanpian_start);
        mTvLeftSegmentName = (TextView) mRootView.findViewById(R.id.tv_left_segment_name);
        mSdvChampainIcon = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_champain_icon);
        mTvChanpainName = (ExTextView) mRootView.findViewById(R.id.tv_chanpain_name);
        mLlChampain = (LinearLayout) mRootView.findViewById(R.id.ll_champain);
        mTvChanpianStart = (TextView) mRootView.findViewById(R.id.tv_chanpian_start);
        mTvSegmentName = (TextView) mRootView.findViewById(R.id.tv_segment_name);
        mIvRankLeft = (ImageView) mRootView.findViewById(R.id.iv_rank_left);
        mIvRank = (ImageView) mRootView.findViewById(R.id.iv_rank);
        mIvRankRight = (ImageView) mRootView.findViewById(R.id.iv_rank_right);
        mRefreshLayout = mRootView.findViewById(R.id.refreshLayout);
        mTvArea = (ExTextView) mRootView.findViewById(R.id.tv_area);
        mIvBack = (ExImageView)mRootView.findViewById(R.id.iv_back);


        mLlAreaContainer = (LinearLayout) mRootView.findViewById(R.id.ll_area_container);
        mTvCurArea = (ExTextView) mRootView.findViewById(R.id.tv_cur_area);
        mTvCountry = (ExTextView) mRootView.findViewById(R.id.tv_country);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (mHasMore) {
                    mLeaderboardPresenter.getLeaderBoardInfo();
                } else {
                    U.getToastUtil().showShort("没有更多数据了");
                }
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });

        RxView.clicks(mTvArea)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        mLlAreaContainer.setVisibility(mLlAreaContainer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                        Drawable drawable = null;

                        if (mLlAreaContainer.getVisibility() == View.VISIBLE) {
                            drawable = getResources().getDrawable(R.drawable.paihangbang_xuanzediquxialaicon_down);
                            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        } else {
                            drawable = getResources().getDrawable(R.drawable.paihangbang_xuanzediquxialaicon);
                            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        }

                        mTvArea.setCompoundDrawables(null, null, drawable, null);

                        if ("全国榜".equals(mTvArea.getText().toString())) {
                            mTvCountry.setSelected(true);
                            mTvCurArea.setSelected(false);
                        } else {
                            mTvCountry.setSelected(false);
                            mTvCurArea.setSelected(true);
                        }
                    }
                });

        RxView.clicks(mTvCurArea)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        mTvArea.setText(MyUserInfoManager.getInstance().getLocationDesc());
                        mLlAreaContainer.setVisibility(View.GONE);
                        Drawable drawable = getResources().getDrawable(R.drawable.paihangbang_xuanzediquxialaicon);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        mTvArea.setCompoundDrawables(null, null, drawable, null);
                        mLeaderboardPresenter.setRankMode(UserRankModel.REGION);
                    }
                });

        RxView.clicks(mTvCountry)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        mTvArea.setText("全国榜");
                        mLlAreaContainer.setVisibility(View.GONE);
                        Drawable drawable = getResources().getDrawable(R.drawable.paihangbang_xuanzediquxialaicon);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        mTvArea.setCompoundDrawables(null, null, drawable, null);
                        mLeaderboardPresenter.setRankMode(UserRankModel.COUNTRY);
                    }
                });

        RxView.clicks(mIvBack)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        finish();
                    }
                });

        setRankMode();
        if(!MyUserInfoManager.getInstance().hasLocation()){
            tryGetLocation();
        }
    }

    private void setRankMode(){
        if (MyUserInfoManager.getInstance().hasLocation()) {
            mTvArea.setText(MyUserInfoManager.getInstance().getLocationDesc());
            mLeaderboardPresenter.setRankMode(UserRankModel.REGION);
            mTvCurArea.setText(MyUserInfoManager.getInstance().getLocationDesc());
            mTvCountry.setText("全国榜");
        } else {
            mLeaderboardPresenter.setRankMode(UserRankModel.COUNTRY);
            mTvArea.setCompoundDrawables(null, null, null, null);
            mTvArea.setText("全国榜");
        }
    }

    private void tryGetLocation() {
        boolean hasLocationPermmistion = U.getPermissionUtils().checkLocation(getActivity());

        if (!hasLocationPermmistion) {
            U.getPermissionUtils().requestLocation(new PermissionUtils.RequestPermission() {
                @Override
                public void onRequestPermissionSuccess() {
                    MyUserInfoManager.getInstance().uploadLocation();
                }

                @Override
                public void onRequestPermissionFailure(List<String> permissions) {

                }

                @Override
                public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {

                }
            }, getActivity());
        } else {
            MyUserInfoManager.getInstance().uploadLocation();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        if(MyUserInfoManager.getInstance().hasLocation()){
            setRankMode();
        }
    }

    @Override

    public void showRankList(List<RankInfoModel> rankInfoModel, boolean hasMore) {
        mRefreshLayout.setEnableLoadMore(hasMore);
        mHasMore = hasMore;
        mRefreshLayout.finishLoadMore();
        mLeaderBoardAdapter.setDataList(rankInfoModel);
    }

    @Override
    public void clear() {
        mLeaderBoardAdapter.getDataList().clear();
    }

    @Override
    public void showOwnRankInfo(UserRankModel userRankModel) {
        ExTextView tvRank = (ExTextView) mRootView.findViewById(R.id.tv_rank);
        SimpleDraweeView sdvIcon = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon);
        ExTextView tvName = (ExTextView) mRootView.findViewById(R.id.tv_name);
        ExTextView tvSegment = (ExTextView) mRootView.findViewById(R.id.tv_segment);
        ExTextView tvStar = (ExTextView) mRootView.findViewById(R.id.tv_star);

        tvRank.setText(userRankModel.getSeq() + "");
        tvName.setText(MyUserInfoManager.getInstance().getNickName());
        tvSegment.setText(userRankModel.getRegionDesc());
        tvStar.setText("X" + userRankModel.getCategory());
        AvatarUtils.loadAvatarByUrl(sdvIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(Color.parseColor("#FF79A9"))
                        .build());
    }

    @Override
    public void showFirstThreeRankInfo(List<RankInfoModel> rankInfoModelList) {
        Observable.fromIterable(rankInfoModelList)
                .filter(new Predicate<RankInfoModel>() {
                    @Override
                    public boolean test(RankInfoModel rankInfoModel) throws Exception {
                        return rankInfoModel.getRankSeq() == 1
                                || rankInfoModel.getRankSeq() == 2
                                || rankInfoModel.getRankSeq() == 3;
                    }
                })
                .subscribe(new Consumer<RankInfoModel>() {
                    @Override
                    public void accept(RankInfoModel rankInfoModel) throws Exception {
                        setTopThreeInfo(rankInfoModel);
                    }
                }, throwable -> MyLog.e(throwable));
    }


    private void setTopThreeInfo(RankInfoModel rankInfoModel) {
        if (rankInfoModel.getRankSeq() == 1) {
            AvatarUtils.loadAvatarByUrl(mSdvChampainIcon,
                    AvatarUtils.newParamsBuilder(rankInfoModel.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .setBorderColor(0xFFFFD958)
                            .build());

            mTvChanpainName.setText(rankInfoModel.getNickname());

            mTvChanpianStart.setText("X" + rankInfoModel.getStarCnt());
            mTvSegmentName.setText(rankInfoModel.getLevelDesc());
        } else if (rankInfoModel.getRankSeq() == 2) {
            AvatarUtils.loadAvatarByUrl(mSdvRightChampainIcon,
                    AvatarUtils.newParamsBuilder(rankInfoModel.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .setBorderColor(0xFFA2C9DA)
                            .build());

            mTvRightChanpainName.setText(rankInfoModel.getNickname());

            mTvRightChanpianStart.setText("X" + rankInfoModel.getStarCnt());
            mTvRightSegmentName.setText(rankInfoModel.getLevelDesc());
        } else if (rankInfoModel.getRankSeq() == 3) {
            AvatarUtils.loadAvatarByUrl(mSdvLeftChampainIcon,
                    AvatarUtils.newParamsBuilder(rankInfoModel.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .setBorderColor(0xFFEEB874)
                            .build());

            mTvLeftChanpainName.setText(rankInfoModel.getNickname());

            mTvLeftChanpianStart.setText("X" + rankInfoModel.getStarCnt());
            mTvLeftSegmentName.setText(rankInfoModel.getLevelDesc());
        }


    }

    @Override
    public boolean useEventBus() {
        return true;
    }
}
