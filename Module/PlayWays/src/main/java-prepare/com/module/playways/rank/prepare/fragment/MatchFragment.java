package com.module.playways.rank.prepare.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.avatar.AvatarUtils;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.manager.BgMusicManager;
import com.dialog.view.TipsDialogView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.rank.prepare.model.MatchIconModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.prepare.presenter.MatchPresenter;
import com.module.playways.rank.prepare.view.IMatchingView;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.live.proto.Common.ESex;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

//这个是匹配界面，之前的FastMatchingSence
public class MatchFragment extends BaseFragment implements IMatchingView {

    public final static String TAG = "MatchFragment";

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
    ExImageView mIvCancelMatch;

    AnimatorSet mIconAnimatorSet;

    RelativeLayout mRlIconContainer;

    MatchPresenter mMatchPresenter;
    PrepareData mPrepareData;

    List<String> mQuotationsArray;

    HandlerTaskTimer mMatchTimeTask;

    DialogPlus mExitDialog;

    @Override
    public int initView() {
        return R.layout.match_fragment_layout;
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
        mIvCancelMatch = (ExImageView) mRootView.findViewById(R.id.iv_cancel_match);
        mRlIconContainer = (RelativeLayout) mRootView.findViewById(R.id.rl_icon_container);
        mSdvSubIcon1 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_sub_icon1);
        mSdvSubIcon3 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_sub_icon3);


        Resources res = getResources();
        mQuotationsArray = Arrays.asList(res.getStringArray(R.array.match_quotations));

        U.getSoundUtils().preLoad(TAG, R.raw.allclick, R.raw.general_back);

        RxView.clicks(mIvCancelMatch)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    U.getSoundUtils().play(TAG, R.raw.allclick);
                    goBack();
                });

        RxView.clicks(mIvBack)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    goBack();
                });

        AvatarUtils.loadAvatarByUrl(mSdvIcon2,
                AvatarUtils.newParamsBuilder(mPrepareData.getSongModel().getCover())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.WHITE)
                        .build());

        mMatchPresenter = new MatchPresenter(this);
        addPresent(mMatchPresenter);
        mMatchPresenter.startLoopMatchTask(mPrepareData.getSongModel().getItemID(), mPrepareData.getGameType());

        startTimeTask();
        startMatchQuotationTask();
        mMatchPresenter.getMatchingUserIconList();

        if (!BgMusicManager.getInstance().isPlaying()) {
            BgMusicManager.getInstance().starPlay(mPrepareData.getSongModel().getRankUserVoice(), 0, "MatchFragment");
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
            iconListIndex += 1;
            int index1 = iconListIndex % (mAvatarURL.size() - 1);
            loadIconInImage(mAvatarURL.get(index1).getAvatarURL(), mSdvIcon1, mAvatarURL.get(index1).getSex() == ESex.SX_MALE.getValue());
            loadIconInImage(mAvatarURL.get(index1).getAvatarURL(), mSdvSubIcon1, mAvatarURL.get(index1).getSex() == ESex.SX_MALE.getValue());
        }

        iconListIndex += 1;
        int index2 = iconListIndex % (mAvatarURL.size() - 1);
        loadIconInImage(mAvatarURL.get(index2).getAvatarURL(), mSdvIcon2, mAvatarURL.get(index2).getSex() == ESex.SX_MALE.getValue());

        if (num != 3) {
            iconListIndex += 1;
            int index3 = iconListIndex % (mAvatarURL.size() - 1);
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
        return false;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
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
                                U.getSoundUtils().play(TAG, R.raw.general_back);
                                mMatchPresenter.cancelMatch();
                                stopTimeTask();
                                U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                                        .setPopFragment(MatchFragment.this)
                                        .setPopAbove(false)
                                        .setHasAnimation(true)
                                        .setNotifyShowFragment(PrepareResFragment.class)
                                        .build());
                            }
                        }
                    }
                })
                .create();
        mExitDialog.show();

    }

    @Override
    public void matchSucess(int gameId, long gameCreatMs, List<PlayerInfoModel> playerInfoList, String avatar, List<SongModel> songModels) {
        MyLog.d(TAG, "matchSucess" + " gameId=" + gameId + " gameCreatMs=" + gameCreatMs + " playerInfoList=" + playerInfoList);
        mPrepareData.setGameId(gameId);
        mPrepareData.setSysAvatar(avatar);
        mPrepareData.setGameCreatMs(gameCreatMs);
        mPrepareData.setPlayerInfoList(playerInfoList);
        mPrepareData.setSongModelList(songModels);
        stopTimeTask();
        BgMusicManager.getInstance().destory();

        //先添加成功界面面
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), MatchSuccessFragment.class)
                .setAddToBackStack(false)
                .setNotifyHideFragment(MatchFragment.class)
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
                .setPopFragment(MatchFragment.this)
                .setPopAbove(false)
                .setHasAnimation(false)
                .build());

    }

    private int iconListIndex = 0;

    List<MatchIconModel> mAvatarURL = null;

    @Override
    public void showUserIconList(List<MatchIconModel> avatarURL) {
        if (avatarURL == null || avatarURL.size() == 0) {
            return;
        }

        mAvatarURL = avatarURL;

        changeIcons(0);
    }

    @Override
    protected boolean onBackPressed() {
        goBack();
        return true;
    }

    @Override
    public void notifyToShow() {
        MyLog.d(TAG, "toStaskTop");
        if (!BgMusicManager.getInstance().isPlaying()) {
            BgMusicManager.getInstance().starPlay(mPrepareData.getSongModel().getRankUserVoice(), 0, "MatchFragment");
        }
        mRootView.setVisibility(View.VISIBLE);
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
