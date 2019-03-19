package com.module.playways.rank.room.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.share.SharePanel;
import com.common.core.share.ShareType;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.BaseRoomData;
import com.module.playways.rank.room.RankRoomData;
import com.module.playways.rank.room.view.RankResultView;
import com.module.playways.rank.room.view.RankResultView2;
import com.module.rank.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.live.proto.Room.EWinType;

import java.util.concurrent.TimeUnit;

/**
 * pk战绩页面
 */
public class RankResultFragment extends BaseFragment {

    RelativeLayout mResultInfoArea;

    ExRelativeLayout mResultArea;
    RankResultView2 mFirstResult;
    RankResultView2 mSecondResult;
    RankResultView2 mThirdResult;
    ExImageView mResultTop;
    ExImageView mResultExit;
    ExImageView mShareIv;
    ExImageView mIvGameRole;

    RankRoomData mRoomData;

    DialogPlus mGameRoleDialog;

    @Override
    public int initView() {
        return R.layout.rank_result_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mResultInfoArea = (RelativeLayout)mRootView.findViewById(R.id.result_info_area);
        mResultArea = (ExRelativeLayout) mRootView.findViewById(R.id.result_area);
        mFirstResult = (RankResultView2) mRootView.findViewById(R.id.first_result);
        mSecondResult = (RankResultView2) mRootView.findViewById(R.id.second_result);
        mThirdResult = (RankResultView2) mRootView.findViewById(R.id.third_result);
        mResultTop = (ExImageView) mRootView.findViewById(R.id.result_top);
        mResultExit = (ExImageView) mRootView.findViewById(R.id.result_exit);
        mShareIv = (ExImageView) mRootView.findViewById(R.id.share_iv);
        mIvGameRole = (ExImageView) mRootView.findViewById(R.id.iv_game_role);

        mResultExit.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                animationExitGo();
            }
        });

        if (mRoomData.getRecordData() != null) {
            if (mRoomData.getRecordData().getSelfWinType() == EWinType.Win.getValue()) {
                mResultTop.setBackground(getResources().getDrawable(R.drawable.zhanji_top_win));
                mResultExit.setBackground(getResources().getDrawable(R.drawable.zhanji_win_exit));
                mResultArea.setBackground(getResources().getDrawable(R.drawable.rank_win_bg));
            } else if (mRoomData.getRecordData().getSelfWinType() == EWinType.Draw.getValue()) {
                mResultTop.setBackground(getResources().getDrawable(R.drawable.zhanji_top_draw));
                mResultExit.setBackground(getResources().getDrawable(R.drawable.zhanji_loss_exit));
                mResultArea.setBackground(getResources().getDrawable(R.drawable.rank_lose_bg));
            } else if (mRoomData.getRecordData().getSelfWinType() == EWinType.Lose.getValue()) {
                mResultTop.setBackground(getResources().getDrawable(R.drawable.zhanji_top_loss));
                mResultExit.setBackground(getResources().getDrawable(R.drawable.zhanji_loss_exit));
                mResultArea.setBackground(getResources().getDrawable(R.drawable.rank_lose_bg));
            }

            mFirstResult.bindData(mRoomData, mRoomData.getRecordData().getUserIdByRank(1), 1);
            mSecondResult.bindData(mRoomData, mRoomData.getRecordData().getUserIdByRank(2), 2);
            mThirdResult.bindData(mRoomData, mRoomData.getRecordData().getUserIdByRank(3), 3);
        }

        mShareIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                SharePanel sharePanel = new SharePanel(getActivity());
                sharePanel.setShareContent("http://res-static.inframe.mobi/common/skr-share.png");
                sharePanel.show(ShareType.IMAGE_RUL);
            }
        });

        mIvGameRole.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mGameRoleDialog != null) {
                    mGameRoleDialog.dismiss();
                }

                mGameRoleDialog = DialogPlus.newDialog(getContext())
                        .setContentHolder(new ViewHolder(R.layout.game_judge_role_view_layout))
                        .setContentBackgroundResource(R.color.transparent)
                        .setOverlayBackgroundResource(R.color.black_trans_50)
                        .setExpanded(false)
                        .setGravity(Gravity.CENTER)
                        .create();

                mGameRoleDialog.show();
            }
        });

        animationEnterGo();
    }

    private void animationEnterGo() {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator a1 = ObjectAnimator.ofFloat(mResultInfoArea, View.SCALE_X, 0.5f, 1f);
        ObjectAnimator a2 = ObjectAnimator.ofFloat(mResultInfoArea, View.SCALE_Y, 0.5f, 1f);
        ObjectAnimator a3 = ObjectAnimator.ofFloat(mResultInfoArea, View.ALPHA, 0f, 1f);
        set.setDuration(300);
        set.playTogether(a1, a2, a3);
        set.start();
    }


    private void animationExitGo() {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator a1 = ObjectAnimator.ofFloat(mResultInfoArea, View.SCALE_X, 1f, 0.5f);
        ObjectAnimator a2 = ObjectAnimator.ofFloat(mResultInfoArea, View.SCALE_Y, 1f, 0.5f);
        ObjectAnimator a3 = ObjectAnimator.ofFloat(mResultInfoArea, View.ALPHA, 1f, 0f);
        set.setDuration(300);
        set.playTogether(a1, a2, a3);
        set.start();

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                U.getFragmentUtils().popFragment(RankResultFragment.this);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                onAnimationEnd(animator);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }


    @Override
    public void destroy() {
        super.destroy();
        if (mGameRoleDialog != null && mGameRoleDialog.isShowing()) {
            mGameRoleDialog.dismiss();
        }
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if (type == 1) {
            mRoomData = (RankRoomData) data;
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
