package com.module.playways.grab.prepare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.ActivityUtils;
import com.common.utils.FragmentUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.component.busilib.constans.GameModeType;
import com.component.busilib.manager.BgMusicManager;
import com.dialog.view.TipsDialogView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.RouterConstants;
import com.module.playways.grab.prepare.presenter.RankMatchPresenter;
import com.module.playways.rank.msg.event.JoinActionEvent;
import com.module.playways.rank.prepare.model.JoinGrabRoomRspModel;
import com.module.playways.rank.prepare.model.MatchIconModel;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.prepare.presenter.BaseMatchPresenter;
import com.module.playways.rank.prepare.presenter.GrabMatchPresenter;
import com.module.playways.rank.prepare.view.IGrabMatchingView;
import com.module.playways.rank.prepare.view.IRankMatchingView;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.live.proto.Common.ESex;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//这个是匹配界面，之前的FastMatchingSence
public class GrabMatchFragment extends BaseFragment implements IGrabMatchingView, IRankMatchingView {

    public final static String TAG = "GrabMatchFragment";

    public static final long ANIMATION_DURATION = 1800;
    ExImageView mIvBack;
    ExImageView mIvTop;
    ExTextView mTvMatchedTime;
    ExTextView mTvTip;
    ExTextView mIvCancelMatch;

    AnimatorSet mIconAnimatorSet;

    BaseMatchPresenter mMatchPresenter;
    PrepareData mPrepareData;

    List<String> mQuotationsArray;

    HandlerTaskTimer mMatchTimeTask;

    SimpleDraweeView mSdvOwnIcon;

    ExRelativeLayout mRlIcon1Root;

    SVGAImageView mSvgaMatchBg;

    DialogPlus mExitDialog;

    @Override
    public int initView() {
        return R.layout.grab_match_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);
        mIvTop = (ExImageView) mRootView.findViewById(R.id.iv_top);
        mTvMatchedTime = (ExTextView) mRootView.findViewById(R.id.tv_matched_time);
        mTvTip = (ExTextView) mRootView.findViewById(R.id.tv_tip);
        mIvCancelMatch = (ExTextView) mRootView.findViewById(R.id.iv_cancel_match);
        mSdvOwnIcon = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_own_icon);
        mRlIcon1Root = (ExRelativeLayout) mRootView.findViewById(R.id.rl_icon1_root);
        mSvgaMatchBg = (SVGAImageView) mRootView.findViewById(R.id.svga_match_bg);

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back, R.raw.normal_click);
        U.getSoundUtils().preLoad(GrabMatchSuccessFragment.TAG, R.raw.rank_matchpeople, R.raw.rank_matchready, R.raw.normal_countdown);

        AvatarUtils.loadAvatarByUrl(mSdvOwnIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setGray(false)
                        .setBorderWidth(U.getDisplayUtils().dip2px(6))
                        .setBorderColor(U.getColor(R.color.white))
                        .build());

        Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                .setStrokeWidth(U.getDisplayUtils().dip2px(3))
                .setStrokeColor(MyUserInfoManager.getInstance().getSex() == ESex.SX_MALE.getValue() ? U.getColor(R.color.color_man_stroke_color) : U.getColor(R.color.color_woman_stroke_color))
                .setSolidColor(MyUserInfoManager.getInstance().getSex() == ESex.SX_MALE.getValue() ? U.getColor(R.color.color_man_stroke_color_trans_20) : U.getColor(R.color.color_woman_stroke_color_trans_20))
                .build();

        mRlIcon1Root.setBackground(drawable);

        Resources res = getResources();
        mQuotationsArray = Arrays.asList(res.getStringArray(R.array.match_quotations));

        mIvCancelMatch.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getSoundUtils().play(TAG, R.raw.normal_click, 500);
                goBack();
            }
        });

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                goBack();
            }
        });

        if (mPrepareData.getGameType() == GameModeType.GAME_MODE_CLASSIC_RANK) {
            mMatchPresenter = new RankMatchPresenter(this);
            addPresent(mMatchPresenter);
            mMatchPresenter.startLoopMatchTask(mPrepareData.getSongModel().getItemID(), mPrepareData.getGameType());
        } else if (mPrepareData.getGameType() == GameModeType.GAME_MODE_GRAB) {
            mMatchPresenter = new GrabMatchPresenter(this);
            addPresent(mMatchPresenter);
            mMatchPresenter.startLoopMatchTask(mPrepareData.getTagId(), mPrepareData.getGameType());
        }

        startTimeTask();
        startMatchQuotationTask();

        showBackground();
        playBackgroundMusic();
    }

    public void showBackground() {
        mSvgaMatchBg.setVisibility(View.VISIBLE);
        mSvgaMatchBg.setLoops(1);

        SVGAParser parser = new SVGAParser(U.app());
        try {
            parser.parse("grab_matching.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem);
                    mSvgaMatchBg.setLoops(-1);
                    mSvgaMatchBg.setImageDrawable(drawable);
                    mSvgaMatchBg.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            System.out.print(true);
        }
    }

    private HandlerTaskTimer mControlTask;

    private void startMatchQuotationTask() {
        mControlTask = HandlerTaskTimer.newBuilder().delay(1000)
                .interval(ANIMATION_DURATION * 2 + 300)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        changeQuotation(integer);
                    }
                });
    }

    private void changeQuotation(Integer integer) {
        int size = mQuotationsArray.size();
        if (integer % size == 0) {
            Collections.shuffle(mQuotationsArray);
        }
        int index = integer % (size - 1);
        String string = mQuotationsArray.get(index);
        String rString = "";

        while (string.length() > 15) {
            rString = rString + string.substring(0, 15) + "\n";
            string = string.substring(15);
        }

        rString = rString + string;
        mTvTip.setText(rString);
    }

    /**
     * 更新已匹配时间
     */
    public void startTimeTask() {
        mMatchTimeTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(-1)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        if (integer == 61) {
                            U.getToastUtil().showShort("现在小伙伴有点少，稍后再匹配试试吧～");
                            mMatchPresenter.cancelMatch();
                            stopTimeTask();
                            if (mPrepareData.getGameType() == GameModeType.GAME_MODE_GRAB) {
                                BgMusicManager.getInstance().destory();
                            }
                            if (getActivity() != null) {
                                getActivity().finish();
                            }
                            ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
                                    .withInt("key_game_type", mPrepareData.getGameType())
                                    .withBoolean("selectSong", true)
                                    .navigation();
                            return;
                        }

                        mTvMatchedTime.setText(String.format(U.app().getString(R.string.match_time_info), integer));
                    }
                });
    }

    public void stopTimeTask() {
        if (mMatchTimeTask != null) {
            mMatchTimeTask.dispose();
        }
    }


    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        MyLog.w(TAG, event.foreground ? "切换到前台" : "切换到后台");
        if (event.foreground) {
            playBackgroundMusic();
        } else {
            BgMusicManager.getInstance().destory();
        }

    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mPrepareData = (PrepareData) data;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mExitDialog != null && mExitDialog.isShowing()) {
            mExitDialog.dismiss();
        }
        stopTimeTask();
        if (mControlTask != null) {
            mControlTask.dispose();
        }
        if (mSvgaMatchBg != null) {
            mSvgaMatchBg.setCallback(null);
            mSvgaMatchBg.stopAnimation(true);
        }
        U.getSoundUtils().release(TAG);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMatchPresenter.destroy();
        stopTimeTask();

        if (mControlTask != null) {
            mControlTask.dispose();
        }

        if (mIconAnimatorSet != null && mIconAnimatorSet.isRunning()) {
            mIconAnimatorSet.cancel();
        }
    }

    void goBack() {
        TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                .setMessageTip("马上要为你匹配到对手了\n还要退出吗？")
                .setCancelTip("退出")
                .setConfirmTip("继续匹配")
                .build();

        mExitDialog = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(tipsDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                        if (view instanceof ExTextView) {
                            if (view.getId() == R.id.confirm_tv) {
                                // 继续匹配
                                dialog.dismiss();
                            }

                            if (view.getId() == R.id.cancel_tv) {
                                dialog.dismiss();
                                U.getSoundUtils().release(GrabMatchSuccessFragment.TAG);
                                mMatchPresenter.cancelMatch();
                                if (mPrepareData.getGameType() == GameModeType.GAME_MODE_GRAB) {
                                    BgMusicManager.getInstance().destory();
                                }
                                stopTimeTask();
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                            }
                        }
                    }
                })
                .create();
        mExitDialog.show();

    }

    //pk
    @Override
    public void matchRankSucess(JoinActionEvent event) {
        BgMusicManager.getInstance().destory();
        mPrepareData.setGameId(event.gameId);
        mPrepareData.setSysAvatar(event.info.getSender().getAvatar());
        mPrepareData.setGameCreatMs(event.gameCreateMs);
        mPrepareData.setPlayerInfoList(event.playerInfoList);
        mPrepareData.setSongModelList(event.songModelList);
        mPrepareData.setGameConfigModel(event.gameConfigModel);
        stopTimeTask();

        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), GrabMatchSuccessFragment.class)
                .setAddToBackStack(false)
                .setNotifyHideFragment(GrabMatchFragment.class)
                .setHasAnimation(false)
                .addDataBeforeAdd(0, mPrepareData)
                .setFragmentDataListener(new FragmentDataListener() {
                    @Override
                    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                    }
                })
                .build());

        //匹配成功直接先把自己pop掉
        U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                .setPopFragment(GrabMatchFragment.this)
                .setPopAbove(false)
                .setHasAnimation(false)
                .build());
    }

    //一唱到底
    @Override
    public void matchGrabSucess(JoinGrabRoomRspModel grabCurGameStateModel) {
        MyLog.d(TAG, "matchSucess" + " event=" + grabCurGameStateModel);
        BgMusicManager.getInstance().destory();
        mPrepareData.setJoinGrabRoomRspModel(grabCurGameStateModel);
        stopTimeTask();

        //先跳转
        ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_ROOM)
                .withSerializable("prepare_data", grabCurGameStateModel)
                .navigation();

        //结束当前Activity
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    protected boolean onBackPressed() {
        goBack();
        return true;
    }

    @Override
    public void notifyToShow() {
        MyLog.d(TAG, "toStaskTop");
        playBackgroundMusic();
        mRootView.setVisibility(View.VISIBLE);
    }

    private void playBackgroundMusic() {
        if (!BgMusicManager.getInstance().isPlaying() && mPrepareData != null && GrabMatchFragment.this.fragmentVisible) {
            if (!TextUtils.isEmpty(mPrepareData.getBgMusic())) {
                BgMusicManager.getInstance().starPlay(mPrepareData.getBgMusic(), 0, "GrabMatchFragment");
            }
        }
    }

    /**
     * MatchSuccessFragment add后，动画播放完再remove掉匹配中页面
     */
    @Override
    public void notifyToHide() {
        if (mExitDialog != null && mExitDialog.isShowing()) {
            mExitDialog.dismiss();
        }
        mRootView.setVisibility(View.GONE);
//        U.getFragmentUtils().popFragment(FragmentUtils.newPopParamsBuilder()
//                .setPopFragment(this)
//                .setPopAbove(false)
//                .build()
//        );
    }
}
