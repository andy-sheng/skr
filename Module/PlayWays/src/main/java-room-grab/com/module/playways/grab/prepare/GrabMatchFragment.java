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
    SimpleDraweeView mSdvIcon1;
    SimpleDraweeView mSdvSubIcon1;
    SimpleDraweeView mSdvIcon3;
    SimpleDraweeView mSdvSubIcon3;
    SimpleDraweeView mSdvIcon2;
    ExTextView mTvTip;
    ExTextView mIvCancelMatch;

    AnimatorSet mIconAnimatorSet;

    RelativeLayout mRlIconContainer;

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
        mSdvIcon1 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon1);
        mSdvIcon3 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon3);
        mSdvIcon2 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon2);
        mTvTip = (ExTextView) mRootView.findViewById(R.id.tv_tip);
        mIvCancelMatch = (ExTextView) mRootView.findViewById(R.id.iv_cancel_match);
        mRlIconContainer = (RelativeLayout) mRootView.findViewById(R.id.rl_icon_container);
        mSdvSubIcon1 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_sub_icon1);
        mSdvSubIcon3 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_sub_icon3);
//        mWaveView = (WaveView)mRootView.findViewById(R.id.wave_view);
        mSdvOwnIcon = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_own_icon);
        mRlIcon1Root = (ExRelativeLayout) mRootView.findViewById(R.id.rl_icon1_root);
        mSvgaMatchBg = (SVGAImageView) mRootView.findViewById(R.id.svga_match_bg);

        U.getSoundUtils().preLoad(TAG, R.raw.allclick, R.raw.general_back);
        U.getSoundUtils().preLoad(GrabMatchSuccessFragment.TAG, R.raw.pregame_animation, R.raw.pregame_ready, R.raw.general_countdown);

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
                U.getSoundUtils().play(TAG, R.raw.allclick);
                goBack();
            }
        });

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getSoundUtils().play(TAG, R.raw.general_back, 500);
                goBack();
            }
        });

//        AvatarUtils.loadAvatarByUrl(mSdvIcon2,
//                AvatarUtils.newParamsBuilder(mPrepareData.getSongModel().getCover())
//                        .setCircle(true)
//                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
//                        .setBorderColor(Color.WHITE)
//                        .build());

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
                        doAnimation(integer);
                        changeQuotation(integer);
                    }
                });
    }

    private void doAnimation(final Integer integer) {
        if (mIconAnimatorSet != null && mIconAnimatorSet.isRunning()) {
            mIconAnimatorSet.cancel();
        }

        mIconAnimatorSet = new AnimatorSet();
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(mSdvIcon1, "translationX", 0, mRlIconContainer.getMeasuredWidth() - mSdvIcon1.getMeasuredWidth());
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(mSdvIcon1, "translationX", mRlIconContainer.getMeasuredWidth() - mSdvIcon1.getMeasuredWidth(), 0);
        ObjectAnimator sub_anim1 = ObjectAnimator.ofFloat(mSdvSubIcon1, "translationX", 0, mRlIconContainer.getMeasuredWidth() - mSdvIcon1.getMeasuredWidth());
        ObjectAnimator sub_anim2 = ObjectAnimator.ofFloat(mSdvSubIcon1, "translationX", mRlIconContainer.getMeasuredWidth() - mSdvIcon1.getMeasuredWidth(), 0);

        ObjectAnimator anim3 = ObjectAnimator.ofFloat(mSdvIcon3, "translationX", 0, -(mRlIconContainer.getMeasuredWidth() - mSdvIcon1.getMeasuredWidth()));
        ObjectAnimator anim4 = ObjectAnimator.ofFloat(mSdvIcon3, "translationX", -(mRlIconContainer.getMeasuredWidth() - mSdvIcon1.getMeasuredWidth()), 0);
        ObjectAnimator sub_anim3 = ObjectAnimator.ofFloat(mSdvSubIcon3, "translationX", 0, -(mRlIconContainer.getMeasuredWidth() - mSdvIcon1.getMeasuredWidth()));
        ObjectAnimator sub_anim4 = ObjectAnimator.ofFloat(mSdvSubIcon3, "translationX", -(mRlIconContainer.getMeasuredWidth() - mSdvIcon1.getMeasuredWidth()), 0);

        final float[] preDegree = new float[]{0.0f};

        //1号在上面
        anim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                float p = mRlIconContainer.getMeasuredWidth() / 2 - (float) mSdvIcon1.getMeasuredWidth() / 2;
                float degree = f - p;

                if (degree * preDegree[0] < 0) {
                    MyLog.d(TAG, "is good 1");
                    changeIcons(1);
                }

                preDegree[0] = degree;
            }
        });

        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                preDegree[0] = 0.0f;
                mSdvIcon1.setVisibility(View.INVISIBLE);
                mSdvIcon3.setVisibility(View.INVISIBLE);
                mSdvSubIcon1.setVisibility(View.VISIBLE);
                mSdvSubIcon3.setVisibility(View.VISIBLE);
            }
        });

        anim2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mSdvSubIcon1.setVisibility(View.INVISIBLE);
                mSdvSubIcon3.setVisibility(View.INVISIBLE);
                mSdvIcon1.setVisibility(View.VISIBLE);
                mSdvIcon3.setVisibility(View.VISIBLE);
            }
        });

        //3号在上面
        anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                float p = mRlIconContainer.getMeasuredWidth() / 2 - (float) mSdvIcon1.getMeasuredWidth() / 2;
                float degree = f - p;

                if (degree * preDegree[0] < 0) {
                    MyLog.d(TAG, "is good 2");
                    changeIcons(3);
                }

                preDegree[0] = degree;
            }
        });

        mIconAnimatorSet.setDuration(ANIMATION_DURATION);
        mIconAnimatorSet.play(anim1).with(sub_anim1).with(anim3).with(sub_anim3).before(anim2);
        mIconAnimatorSet.play(anim2).with(sub_anim2).with(anim4).with(sub_anim4);

        mIconAnimatorSet.start();
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


    private void changeIcons(Integer num) {
        if (mAvatarURL == null || mAvatarURL.size() == 0) {
            return;
        }


        if (num != 1) {
            mIconListIndex += 1;
            int index1 = mIconListIndex % (mAvatarURL.size() - 1);
            loadIconInImage(mAvatarURL.get(index1).getAvatarURL(), mSdvIcon1, mAvatarURL.get(index1).getSex() == ESex.SX_MALE.getValue());
            loadIconInImage(mAvatarURL.get(index1).getAvatarURL(), mSdvSubIcon1, mAvatarURL.get(index1).getSex() == ESex.SX_MALE.getValue());
        }

        mIconListIndex += 1;
        int index2 = mIconListIndex % (mAvatarURL.size() - 1);
        loadIconInImage(mAvatarURL.get(index2).getAvatarURL(), mSdvIcon2, mAvatarURL.get(index2).getSex() == ESex.SX_MALE.getValue());

        if (num != 3) {
            mIconListIndex += 1;
            int index3 = mIconListIndex % (mAvatarURL.size() - 1);
            loadIconInImage(mAvatarURL.get(index3).getAvatarURL(), mSdvIcon3, mAvatarURL.get(index3).getSex() == ESex.SX_MALE.getValue());
            loadIconInImage(mAvatarURL.get(index3).getAvatarURL(), mSdvSubIcon3, mAvatarURL.get(index3).getSex() == ESex.SX_MALE.getValue());
        }
    }

    private void loadIconInImage(String url, SimpleDraweeView simpleDraweeView, boolean isMale) {
        AvatarUtils.loadAvatarByUrl(simpleDraweeView,
                AvatarUtils.newParamsBuilder(url)
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColorBySex(isMale)
                        .build());
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
                                U.getSoundUtils().play(TAG, R.raw.allclick);
                            }

                            if (view.getId() == R.id.cancel_tv) {
                                dialog.dismiss();
                                U.getSoundUtils().play(GrabMatchFragment.TAG, R.raw.general_back, 500);
                                U.getSoundUtils().release(GrabMatchSuccessFragment.TAG);
                                mMatchPresenter.cancelMatch();
                                if (mPrepareData.getGameType() == GameModeType.GAME_MODE_GRAB) {
                                    BgMusicManager.getInstance().destory();
                                }
                                stopTimeTask();
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
//                                ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
//                                        .withInt("key_game_type", mPrepareData.getGameType())
//                                        .withBoolean("selectSong", true)
//                                        .navigation();
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

    private int mIconListIndex = 0;

    List<MatchIconModel> mAvatarURL = null;

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
