package com.module.rankingmode.room.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.PlayerInfo;
import com.module.rankingmode.room.model.RoomData;
import com.module.rankingmode.room.presenter.EndGamePresenter;
import com.module.rankingmode.room.view.IVoteView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// 游戏结束页
public class EvaluationFragment extends BaseFragment implements IVoteView {

    RoomData mRoomData;

    RelativeLayout mMainActContainer;

    // 左边视图
    SimpleDraweeView mVoteLeftIv;
    ExTextView mVoteLeftNameTv;
    ExTextView mVoteLeftSongTv;
    ExImageView mVoteLeftShadowIv;
    ExImageView mVoteLeftMie;

    // 右边视图
    SimpleDraweeView mVoteRightIv;
    ExTextView mVoteRigntNameTv;
    ExTextView mVoteRightSongTv;
    ExImageView mVoteRightShadowIv;
    ExImageView mVoteRightMie;

    ExTextView mVoteDownTv;
    ExImageView mVoteVsIv;

    EndGamePresenter mPresenter;

    PlayerInfo left;
    PlayerInfo right;

    HandlerTaskTimer mVoteTimeTask;

    @Override
    public int initView() {
        return R.layout.ranking_room_evaluation_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);

        mVoteDownTv = (ExTextView) mRootView.findViewById(R.id.vote_down_tv);
        mVoteVsIv = (ExImageView) mRootView.findViewById(R.id.vote_vs_iv);

        mVoteLeftIv = (SimpleDraweeView) mRootView.findViewById(R.id.vote_left_iv);
        mVoteLeftNameTv = (ExTextView) mRootView.findViewById(R.id.vote_left_name_tv);
        mVoteLeftSongTv = (ExTextView) mRootView.findViewById(R.id.vote_left_song_tv);
        mVoteLeftShadowIv = (ExImageView) mRootView.findViewById(R.id.vote_left_shadow_iv);

        mVoteRightIv = (SimpleDraweeView) mRootView.findViewById(R.id.vote_right_iv);
        mVoteRigntNameTv = (ExTextView) mRootView.findViewById(R.id.vote_rignt_name_tv);
        mVoteRightSongTv = (ExTextView) mRootView.findViewById(R.id.vote_right_song_tv);
        mVoteRightShadowIv = (ExImageView) mRootView.findViewById(R.id.vote_right_shadow_iv);

        mVoteLeftMie = (ExImageView) mRootView.findViewById(R.id.vote_left_mie);
        mVoteRightMie = (ExImageView) mRootView.findViewById(R.id.vote_right_mie);

        if (left != null) {
            AvatarUtils.loadAvatarByUrl(mVoteLeftIv, AvatarUtils.newParamsBuilder(left.getUserInfo().getAvatar())
                    .setCircle(true)
                    .setBorderWidth(U.getDisplayUtils().dip2px(3))
                    .setBorderColor(Color.parseColor("#33A4E1"))
                    .build());
            mVoteLeftNameTv.setText(left.getUserInfo().getUserNickname());
            mVoteLeftSongTv.setText(left.getSongList().get(0).getItemName());
        }

        if (right != null) {
            AvatarUtils.loadAvatarByUrl(mVoteRightIv, AvatarUtils.newParamsBuilder(right.getUserInfo().getAvatar())
                    .setCircle(true)
                    .setBorderWidth(U.getDisplayUtils().dip2px(3))
                    .setBorderColor(Color.parseColor("#FF75A2"))
                    .build());
            mVoteRigntNameTv.setText(right.getUserInfo().getUserNickname());
            mVoteRightSongTv.setText(right.getSongList().get(0).getItemName());

        }

        mPresenter = new EndGamePresenter(this);
        addPresent(mPresenter);

        RxView.clicks(mVoteLeftMie)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    mPresenter.vote(mRoomData.getGameId(), left.getUserInfo().getUserId());
                });

        RxView.clicks(mVoteRightMie)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    mPresenter.vote(mRoomData.getGameId(), right.getUserInfo().getUserId());
                });

        startTimeTask();
    }

    /**
     * 更新评分倒计时
     */
    public void startTimeTask() {
        mVoteTimeTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(12)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        if (integer == 12) {
                            mPresenter.getVoteResult(mRoomData.getGameId());
                        }
                    }
                });
    }

    public void stopTimeTask() {
        if (mVoteTimeTask != null) {
            mVoteTimeTask.dispose();
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if (type == 0) {
            mRoomData = (RoomData) data;
            if (mRoomData.getPlayerInfoList() != null && mRoomData.getPlayerInfoList().size() > 0) {
                for (PlayerInfo playerInfo : mRoomData.getPlayerInfoList()) {
                    if (left != null && playerInfo.getUserInfo().getUserId() != MyUserInfoManager.getInstance().getUid()) {
                        right = playerInfo;
                    } else if (playerInfo.getUserInfo().getUserId() != MyUserInfoManager.getInstance().getUid()) {
                        left = playerInfo;
                    }
                }
            }
        }
    }

    @Override
    public void voteSucess(long votedUserId) {
        // TODO: 2018/12/18  可能要加上星星的特效 
        if (left.getUserInfo().getUserId() == votedUserId) {
            mVoteLeftMie.setSelected(true);
            mVoteLeftMie.setClickable(false);
            mVoteRightMie.setClickable(false);
            mVoteLeftShadowIv.setVisibility(View.VISIBLE);
        } else if (right.getUserInfo().getUserId() == votedUserId) {
            mVoteRightMie.setSelected(true);
            mVoteRightMie.setClickable(false);
            mVoteLeftMie.setClickable(false);
            mVoteRightShadowIv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void voteFailed() {

    }

    @Override
    public void destroy() {
        super.destroy();
        stopTimeTask();
    }

    @Override
    protected boolean onBackPressed() {
        stopTimeTask();
        getActivity().finish();
        return true;
    }
}
