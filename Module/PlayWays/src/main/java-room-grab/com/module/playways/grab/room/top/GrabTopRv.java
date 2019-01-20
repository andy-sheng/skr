package com.module.playways.grab.room.top;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.module.playways.RoomData;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;
import com.module.rank.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GrabTopRv extends RelativeLayout {
    public final static String TAG = "GrabTopRv";

    private LinkedHashMap<Integer, GrabTopItemView> mInfoMap = new LinkedHashMap<>();
    private RoomData mRoomData;
    private boolean mInited = false;
    AnimatorSet mAnimatorAllSet;

    LinearLayout mContentLl;
    ExImageView mErjiIv;

    public GrabTopRv(Context context) {
        super(context);
        init();
    }

    public GrabTopRv(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabTopRv(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_top_content_view_layout, this);
        mContentLl = (LinearLayout) this.findViewById(R.id.content_ll);
        mErjiIv = (ExImageView) this.findViewById(R.id.erji_iv);
    }

    private void initData() {
        if (mInited) {
            return;
        }
        mInited = true;
        RoundInfoModel now = mRoomData.getRealRoundInfo();
        List<PlayerInfoModel> playerInfoModels = mRoomData.getPlayerInfoList();
        int i = 0;
        for (PlayerInfoModel playerInfoModel : playerInfoModels) {
            UserInfoModel userInfo = playerInfoModel.getUserInfo();
            GrabTopItemView grabTopItemView = mInfoMap.get(userInfo.getUserId());
            if (grabTopItemView == null) {
                grabTopItemView = new GrabTopItemView(getContext());
                mInfoMap.put(userInfo.getUserId(), grabTopItemView);
            }
            grabTopItemView.setVisibility(VISIBLE);
            grabTopItemView.bindData(userInfo);
            grabTopItemView.setGrap(false);
            grabTopItemView.tryAddParent(mContentLl);
            if (i % 2 == 0) {
                grabTopItemView.setBackgroundColor(U.getColor(R.color.yellow));
            } else {
                grabTopItemView.setBackgroundColor(U.getColor(R.color.blue));
            }
            i++;
        }
        if (now != null) {
            for (int uid : now.getHasGrabUserSet()) {
                GrabTopItemView grabTopItemView = mInfoMap.get(uid);
                if (grabTopItemView != null) {
                    grabTopItemView.setGrap(true);
                }
            }
        }
    }

    public void setModeGrab() {
        // 切换到抢唱模式,
        mErjiIv.setVisibility(GONE);
        for (int uId : mInfoMap.keySet()) {
            GrabTopItemView grabTopItemView = mInfoMap.get(uId);
            if (grabTopItemView != null) {
                grabTopItemView.setVisibility(VISIBLE);
                grabTopItemView.reset();
                if (mRoomData.getRealRoundInfo().getHasGrabUserSet().contains(uId)) {
                    grabTopItemView.setGrap(true);
                } else {
                    grabTopItemView.setGrap(false);
                }
            }
        }
    }

    public void setModeSing(int singUid) {
        MyLog.d(TAG, "setModeSing" + " singUid=" + singUid);
        GrabTopItemView grabTopItemView = mInfoMap.get(singUid);
        if (grabTopItemView != null) {
            grabTopItemView.setGetSingChance();
        }
        MyLog.d(TAG, "setModeSing" + " grabTopItemView=" + grabTopItemView);

        //播动画
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(grabTopItemView, View.SCALE_X, 1, 0);
        objectAnimator1.setDuration(800);
        objectAnimator1.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(grabTopItemView, View.SCALE_Y, 1, 0);
        objectAnimator2.setDuration(800);
        objectAnimator2.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(grabTopItemView, View.ALPHA, 1, 0.1f);
        objectAnimator3.setDuration(800);
        objectAnimator3.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator objectAnimator4 = ObjectAnimator.ofFloat(mErjiIv, View.TRANSLATION_Y, -U.getDisplayUtils().dip2px(100), 0);
        objectAnimator4.setDuration(1000);
        objectAnimator4.setStartDelay(500);
        objectAnimator4.setInterpolator(new AccelerateInterpolator());

        GrabTopItemView finalGrabTopItemView = grabTopItemView;
        objectAnimator4.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mErjiIv.setVisibility(VISIBLE);
            }
        });

        AnimatorSet animatorSet1 = new AnimatorSet();
        animatorSet1.playTogether(objectAnimator1, objectAnimator2, objectAnimator3, objectAnimator4);
        animatorSet1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                finalGrabTopItemView.setVisibility(GONE);
                finalGrabTopItemView.setAlpha(1);
                finalGrabTopItemView.setTranslationY(0);
                finalGrabTopItemView.setScaleX(1);
                finalGrabTopItemView.setScaleY(1);
                mErjiIv.setTranslationY(0);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                finalGrabTopItemView.setVisibility(VISIBLE);
            }
        });
        List<Animator> setList = new ArrayList<>();
        List<Animator> setList2 = new ArrayList<>();
        // 切换到抢唱模式,
        int i = 0;
        for (int uId : mInfoMap.keySet()) {
            grabTopItemView = mInfoMap.get(uId);
            i++;
            if (grabTopItemView != null) {
                if (uId == singUid) {

                } else {
                    //灭灯动画
                    GrabTopItemView finalGrabTopItemView1 = grabTopItemView;
                    {
                        ObjectAnimator objectAnimator11 = ObjectAnimator.ofFloat(grabTopItemView.mFlagIv, View.SCALE_X, 1f, 1.5f, 1f);
                        ObjectAnimator objectAnimator12 = ObjectAnimator.ofFloat(grabTopItemView.mFlagIv, View.SCALE_Y, 1f, 1.5f, 1f);
                        ObjectAnimator objectAnimator13 = ObjectAnimator.ofFloat(grabTopItemView.mFlagIv, View.ALPHA, 0.5f, 1);
                        ObjectAnimator objectAnimator14 = ObjectAnimator.ofFloat(grabTopItemView.mFlagIv, View.TRANSLATION_Y, U.getDisplayUtils().dip2px(30), 0);
                        AnimatorSet animatorSet21 = new AnimatorSet();
                        animatorSet21.playTogether(objectAnimator11, objectAnimator12, objectAnimator13, objectAnimator14);
                        animatorSet21.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                finalGrabTopItemView1.setLight(false);
                            }
                        });
                        animatorSet21.setDuration(200);
                        setList.add(animatorSet21);
                    }
                    // 亮灯动画
                    {
                        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(grabTopItemView.mFlagIv, View.TRANSLATION_X, 0, 0);
                        objectAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationCancel(Animator animation) {
                                super.onAnimationCancel(animation);
                                onAnimationEnd(animation);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                finalGrabTopItemView1.setLight(true);
                            }
                        });
                        objectAnimator.setDuration(100);
                        setList2.add(objectAnimator);
                    }
                }
            }
        }
        //灭灯动画
        AnimatorSet animatorSet2 = new AnimatorSet();
        animatorSet2.playSequentially(setList);

        //亮灯动画
        AnimatorSet animatorSet3 = new AnimatorSet();
        animatorSet3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 把灯的情况纠正下
                syncLight();
            }
        });
        animatorSet3.playSequentially(setList2);

        if (mAnimatorAllSet != null) {
            mAnimatorAllSet.cancel();
        }

        mAnimatorAllSet = new AnimatorSet();
        mAnimatorAllSet.playSequentially(animatorSet1, animatorSet2, animatorSet3);
        mAnimatorAllSet.start();
    }

    private void syncLight() {
        RoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            for (int uid : now.getHasLightOffUserSet()) {
                GrabTopItemView grabTopItemView = mInfoMap.get(uid);
                if (grabTopItemView != null) {
                    grabTopItemView.setLight(false);
                }
            }
        }
    }

    public void grap(int uid) {
        GrabTopItemView grabTopItemView = mInfoMap.get(uid);
        if (grabTopItemView != null) {
            grabTopItemView.setGrap(true);
        }
    }

    public void lightOff(int uid) {
        GrabTopItemView grabTopItemView = mInfoMap.get(uid);
        if (grabTopItemView != null) {
            grabTopItemView.setLight(false);
        }
    }


    public void setRoomData(RoomData roomData) {
        mRoomData = roomData;
        initData();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimatorAllSet != null) {
            mAnimatorAllSet.cancel();
        }
    }
}
