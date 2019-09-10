package com.module.playways.grab.room.view.control;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.listener.SVGAListener;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.MINIGameRoundInfoModel;
import com.module.playways.grab.room.model.SPkRoundInfoModel;
import com.common.view.ExViewStub;
import com.module.playways.grab.room.view.chorus.ChorusSingBeginTipsCardView;
import com.module.playways.grab.room.view.minigame.MiniGameSingBeginTipsCardView;
import com.module.playways.grab.room.view.normal.NormalSingBeginTipsCardView;
import com.module.playways.grab.room.view.pk.PKSingBeginTipsCardView;
import com.module.playways.R;
import com.opensource.svgaplayer.SVGAImageView;

import java.util.List;

public class SingBeginTipsCardView extends ExViewStub {

    NormalSingBeginTipsCardView mNormalSingBeginTipsCardView = new NormalSingBeginTipsCardView(); // 提示xxx演唱开始的卡片
    ChorusSingBeginTipsCardView mChorusSingBeginTipsCardView = new ChorusSingBeginTipsCardView(); // 合唱对战开始
    PKSingBeginTipsCardView mPKSingBeginTipsCardView = new PKSingBeginTipsCardView();         // pk对战开始
    MiniGameSingBeginTipsCardView mMiniGameSingBegin = new MiniGameSingBeginTipsCardView();         // 小游戏开始

    SVGAImageView mSVGAImageView;
    GrabRoomData mRoomData;

    public SingBeginTipsCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
        mRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mSVGAImageView = getMParentView().findViewById(R.id.sing_begin_svga);
        getMParentView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (mSVGAImageView != null) {
                    mSVGAImageView.setCallback(null);
                    mSVGAImageView.stopAnimation(true);
                }
            }
        });
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_sing_begin_tips_card_stub_layout;
    }


    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.GONE) {
            if (mSVGAImageView != null) {
                mSVGAImageView.setCallback(null);
                mSVGAImageView.stopAnimation(true);
            }
        }
    }

    public void bindData(SVGAListener svgaListener) {
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        tryInflate();
        setVisibility(View.VISIBLE);
        mSVGAImageView.setVisibility(View.VISIBLE);
        if (grabRoundInfoModel != null) {
            if (RoomDataUtils.isChorusRound(mRoomData)) {
                List<ChorusRoundInfoModel> list = grabRoundInfoModel.getChorusRoundInfoModels();
                if (list != null && list.size() >= 2) {
                    UserInfoModel userInfoModel1 = mRoomData.getPlayerOrWaiterInfo(list.get(0).getUserID());
                    UserInfoModel userInfoModel2 = mRoomData.getPlayerOrWaiterInfo(list.get(1).getUserID());
                    ViewGroup.LayoutParams lp = mSVGAImageView.getLayoutParams();
                    lp.height = U.getDisplayUtils().dip2px(154);
                    mChorusSingBeginTipsCardView.bindData(mSVGAImageView, userInfoModel1, userInfoModel2, svgaListener);
                }
            } else if (RoomDataUtils.isPKRound(mRoomData)) {
                List<SPkRoundInfoModel> list = grabRoundInfoModel.getsPkRoundInfoModels();
                if (list != null && list.size() >= 2) {
                    UserInfoModel userInfoModel1 = mRoomData.getPlayerOrWaiterInfo(list.get(0).getUserID());
                    UserInfoModel userInfoModel2 = mRoomData.getPlayerOrWaiterInfo(list.get(1).getUserID());
                    ViewGroup.LayoutParams lp = mSVGAImageView.getLayoutParams();
                    lp.height = U.getDisplayUtils().dip2px(181);
                    mPKSingBeginTipsCardView.bindData(mSVGAImageView, userInfoModel1, userInfoModel2, svgaListener);
                }
            } else if (RoomDataUtils.isMiniGameRound(mRoomData)) {
                List<MINIGameRoundInfoModel> list = grabRoundInfoModel.getMINIGameRoundInfoModels();
                if (list != null && list.size() >= 2) {
                    UserInfoModel userInfoModel1 = mRoomData.getPlayerOrWaiterInfo(list.get(0).getUserID());
                    UserInfoModel userInfoModel2 = mRoomData.getPlayerOrWaiterInfo(list.get(1).getUserID());
                    ViewGroup.LayoutParams lp = mSVGAImageView.getLayoutParams();
                    lp.height = U.getDisplayUtils().dip2px(154);
                    mMiniGameSingBegin.bindData(mSVGAImageView, userInfoModel1, userInfoModel2, svgaListener);
                }
            } else {
                ViewGroup.LayoutParams lp = mSVGAImageView.getLayoutParams();
                lp.height = U.getDisplayUtils().dip2px(181);
                mNormalSingBeginTipsCardView.bindData(mSVGAImageView, mRoomData.getPlayerOrWaiterInfo(grabRoundInfoModel.getUserID()), grabRoundInfoModel.getMusic(), svgaListener, grabRoundInfoModel.isChallengeRound());
            }
        }
    }


}
