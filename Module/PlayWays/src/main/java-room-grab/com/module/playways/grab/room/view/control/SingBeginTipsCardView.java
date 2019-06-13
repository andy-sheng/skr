package com.module.playways.grab.room.view.control;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.MINIGameRoundInfoModel;
import com.module.playways.grab.room.model.SPkRoundInfoModel;
import com.module.playways.grab.room.view.chorus.ChorusSingBeginTipsCardView;
import com.module.playways.grab.room.view.minigame.MiniGameSingBeginTipsCardView;
import com.module.playways.grab.room.view.normal.NormalSingBeginTipsCardView;
import com.module.playways.grab.room.view.pk.PKSingBeginTipsCardView;
import com.module.playways.R;
import com.opensource.svgaplayer.SVGAImageView;

import java.util.List;

public class SingBeginTipsCardView {

    NormalSingBeginTipsCardView mNormalSingBeginTipsCardView = new NormalSingBeginTipsCardView(); // 提示xxx演唱开始的卡片
    ChorusSingBeginTipsCardView mChorusSingBeginTipsCardView = new ChorusSingBeginTipsCardView(); // 合唱对战开始
    PKSingBeginTipsCardView mPKSingBeginTipsCardView = new PKSingBeginTipsCardView();         // pk对战开始
    MiniGameSingBeginTipsCardView mMiniGameSingBegin = new MiniGameSingBeginTipsCardView();         // 小游戏开始

    ViewStub mViewStub;
    ViewGroup mParentView;
    SVGAImageView mSVGAImageView;
    GrabRoomData mRoomData;

    public SingBeginTipsCardView(ViewStub viewStub, GrabRoomData roomData) {
        mRoomData = roomData;
        mViewStub = viewStub;
    }

    void inflate() {
        mParentView = (ViewGroup) mViewStub.inflate();
        mSVGAImageView = mParentView.findViewById(R.id.sing_begin_svga);
        mViewStub = null;
    }

    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            if (mParentView != null) {
                mParentView.setVisibility(View.GONE);
            }
            if (mSVGAImageView != null) {
                mSVGAImageView.setCallback(null);
                mSVGAImageView.stopAnimation(true);
            }
        } else if (visibility == View.VISIBLE) {
            if (mParentView == null) {
                inflate();
            }
            mParentView.setVisibility(View.VISIBLE);
            mSVGAImageView.setVisibility(View.VISIBLE);
        }
    }

    public void bindData(SVGAListener svgaListener) {
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        if (grabRoundInfoModel != null) {
            if (RoomDataUtils.isChorusRound(mRoomData)) {
                List<ChorusRoundInfoModel> list = grabRoundInfoModel.getChorusRoundInfoModels();
                if (list != null && list.size() >= 2) {
                    UserInfoModel userInfoModel1 = mRoomData.getUserInfo(list.get(0).getUserID());
                    UserInfoModel userInfoModel2 = mRoomData.getUserInfo(list.get(1).getUserID());
                    ViewGroup.LayoutParams lp = mSVGAImageView.getLayoutParams();
                    lp.height = U.getDisplayUtils().dip2px(154);
                    mChorusSingBeginTipsCardView.bindData(mSVGAImageView, userInfoModel1, userInfoModel2, svgaListener);
                }
            } else if (RoomDataUtils.isPKRound(mRoomData)) {
                List<SPkRoundInfoModel> list = grabRoundInfoModel.getsPkRoundInfoModels();
                if (list != null && list.size() >= 2) {
                    UserInfoModel userInfoModel1 = mRoomData.getUserInfo(list.get(0).getUserID());
                    UserInfoModel userInfoModel2 = mRoomData.getUserInfo(list.get(1).getUserID());
                    ViewGroup.LayoutParams lp = mSVGAImageView.getLayoutParams();
                    lp.height = U.getDisplayUtils().dip2px(181);
                    mPKSingBeginTipsCardView.bindData(mSVGAImageView, userInfoModel1, userInfoModel2, svgaListener);
                }
            } else if (RoomDataUtils.isMiniGameRound(mRoomData)) {
                List<MINIGameRoundInfoModel> list = grabRoundInfoModel.getMINIGameRoundInfoModels();
                if (list != null && list.size() >= 2) {
                    UserInfoModel userInfoModel1 = mRoomData.getUserInfo(list.get(0).getUserID());
                    UserInfoModel userInfoModel2 = mRoomData.getUserInfo(list.get(1).getUserID());
                    ViewGroup.LayoutParams lp = mSVGAImageView.getLayoutParams();
                    lp.height = U.getDisplayUtils().dip2px(154);
                    mMiniGameSingBegin.bindData(mSVGAImageView, userInfoModel1, userInfoModel2, svgaListener);
                }
            } else {
                ViewGroup.LayoutParams lp = mSVGAImageView.getLayoutParams();
                lp.height = U.getDisplayUtils().dip2px(181);
                mNormalSingBeginTipsCardView.bindData(mSVGAImageView, mRoomData.getUserInfo(grabRoundInfoModel.getUserID()), grabRoundInfoModel.getMusic(), svgaListener, grabRoundInfoModel.isChallengeRound());
            }
        }
    }


}
