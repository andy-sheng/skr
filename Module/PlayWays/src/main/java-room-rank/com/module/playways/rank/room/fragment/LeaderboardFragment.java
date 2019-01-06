package com.module.playways.rank.room.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.RankInfoModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.rank.room.adapter.LeaderBoardAdapter;
import com.module.playways.rank.room.presenter.LeaderboardPresenter;
import com.module.playways.rank.room.view.ILeaderBoardView;
import com.module.rank.R;

import java.util.List;

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

        mLeaderboardPresenter.getLeaderBoardInfo();
        mLeaderboardPresenter.getOwnInfo();

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

    }

    @Override
    public void showRankList(List<RankInfoModel> rankInfoModel) {
        mLeaderBoardAdapter.setDataList(rankInfoModel);
    }

    @Override
    public void showOwnRankInfo(UserRankModel userRankModel) {
        ExTextView tvRank = (ExTextView)mRootView.findViewById(R.id.tv_rank);
        SimpleDraweeView sdvIcon = (SimpleDraweeView)mRootView.findViewById(R.id.sdv_icon);
        ExTextView tvName = (ExTextView)mRootView.findViewById(R.id.tv_name);
        ExTextView tvSegment = (ExTextView)mRootView.findViewById(R.id.tv_segment);
        ExTextView tvStar = (ExTextView)mRootView.findViewById(R.id.tv_star);

        tvRank.setText(userRankModel.getSeq() + "");
        tvName.setText(MyUserInfoManager.getInstance().getNickName());
        tvSegment.setText(userRankModel.getRegionDesc());
        tvStar.setText("X" + userRankModel.getCategoy());
        AvatarUtils.loadAvatarByUrl(sdvIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.WHITE)
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
                            .setBorderColor(Color.WHITE)
                            .build());

            mTvChanpainName.setText(rankInfoModel.getNickname());

            mTvChanpianStart.setText("X" + rankInfoModel.getStarCnt());
            mTvSegmentName.setText(rankInfoModel.getLevelDesc());
        } else if (rankInfoModel.getRankSeq() == 2) {
            AvatarUtils.loadAvatarByUrl(mSdvRightChampainIcon,
                    AvatarUtils.newParamsBuilder(rankInfoModel.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .setBorderColor(Color.WHITE)
                            .build());

            mTvRightChanpainName.setText(rankInfoModel.getNickname());

            mTvRightChanpianStart.setText("X" + rankInfoModel.getStarCnt());
            mTvRightSegmentName.setText(rankInfoModel.getLevelDesc());
        } else if (rankInfoModel.getRankSeq() == 3) {
            AvatarUtils.loadAvatarByUrl(mSdvLeftChampainIcon,
                    AvatarUtils.newParamsBuilder(rankInfoModel.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .setBorderColor(Color.WHITE)
                            .build());

            mTvLeftChanpainName.setText(rankInfoModel.getNickname());

            mTvLeftChanpianStart.setText("X" + rankInfoModel.getStarCnt());
            mTvLeftSegmentName.setText(rankInfoModel.getLevelDesc());
        }


    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
