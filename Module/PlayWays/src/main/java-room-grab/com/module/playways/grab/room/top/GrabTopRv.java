package com.module.playways.grab.room.top;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExLinearLayout;
import com.module.playways.RoomData;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;
import com.module.rank.R;
import com.zq.live.proto.Common.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GrabTopRv extends RelativeLayout {
    private HashMap<Integer, GrabTopItemView> mInfoMap = new HashMap<>();
    private RoomData mRoomData;
    private boolean mInited = false;
    AnimatorSet mAnimatorSet;

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
        for (int uId : mInfoMap.keySet()) {
            GrabTopItemView grabTopItemView = mInfoMap.get(uId);
            if (grabTopItemView != null) {
                if (mRoomData.getRealRoundInfo().getHasGrabUserSet().contains(uId)) {
                    grabTopItemView.setGrap(true);
                } else {
                    grabTopItemView.setGrap(false);
                }
            }
        }
    }

    public void setModeSing(long singUid) {
        GrabTopItemView grabTopItemView = mInfoMap.get(singUid);
        if (grabTopItemView != null) {
            grabTopItemView.setGetSingChance();
        }

        // 切换到抢唱模式,
        for (int uId : mInfoMap.keySet()) {
            grabTopItemView = mInfoMap.get(uId);
            if (grabTopItemView != null) {
                if (uId == singUid) {
                    grabTopItemView.setGrap(false);
                } else {
                    if (mRoomData.getRealRoundInfo().getHasLightOffUserSet().contains(uId)) {
                        grabTopItemView.setLight(true);
                    } else {
                        grabTopItemView.setLight(false);
                    }
                }
            }
        }

        //播动画
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(grabTopItemView, View.TRANSLATION_Y, 0, -U.getDisplayUtils().dip2px(100));
        objectAnimator1.setDuration(800);
        objectAnimator1.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(grabTopItemView, View.ALPHA, 1, 0.1f);
        objectAnimator2.setDuration(800);
        objectAnimator1.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(mErjiIv, View.TRANSLATION_Y, -U.getDisplayUtils().dip2px(100), 0);
        objectAnimator3.setDuration(1000);
        objectAnimator3.setStartDelay(500);
        objectAnimator3.setInterpolator(new AccelerateInterpolator());

        GrabTopItemView finalGrabTopItemView = grabTopItemView;
        objectAnimator3.addListener(new AnimatorListenerAdapter() {
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
                mErjiIv.setTranslationY(0);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mErjiIv.setVisibility(VISIBLE);
            }
        });
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(objectAnimator1, objectAnimator2, objectAnimator3);

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
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }
    }
}
