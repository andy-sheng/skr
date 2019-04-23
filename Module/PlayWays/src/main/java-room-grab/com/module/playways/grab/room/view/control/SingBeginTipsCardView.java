package com.module.playways.grab.room.view.control;

import android.view.View;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.SPkRoundInfoModel;
import com.module.playways.grab.room.view.chorus.ChorusSingBeginTipsCardView;
import com.module.playways.grab.room.view.normal.NormalSingBeginTipsCardView;
import com.module.playways.grab.room.view.pk.PKSingBeginTipsCardView;
import com.module.playways.R;

import java.util.List;

public class SingBeginTipsCardView {

    NormalSingBeginTipsCardView mNormalSingBeginTipsCardView; // 提示xxx演唱开始的卡片
    ChorusSingBeginTipsCardView mChorusSingBeginTipsCardView; // 合唱对战开始
    PKSingBeginTipsCardView mPKSingBeginTipsCardView;         // pk对战开始

    GrabRoomData mRoomData;

    public SingBeginTipsCardView(View mRootView, GrabRoomData roomData) {
        mRoomData = roomData;
        mNormalSingBeginTipsCardView = mRootView.findViewById(R.id.normla_sing_beign);
        mChorusSingBeginTipsCardView = mRootView.findViewById(R.id.chorus_sing_begin);
        mPKSingBeginTipsCardView = mRootView.findViewById(R.id.pk_sing_begin);
    }

    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            mNormalSingBeginTipsCardView.setVisibility(View.GONE);
            mChorusSingBeginTipsCardView.setVisibility(View.GONE);
            mPKSingBeginTipsCardView.setVisibility(View.GONE);
        } else if (visibility == View.VISIBLE) {
            if (RoomDataUtils.isChorusRound(mRoomData)) {
                mChorusSingBeginTipsCardView.setVisibility(View.VISIBLE);
                mNormalSingBeginTipsCardView.setVisibility(View.GONE);
                mPKSingBeginTipsCardView.setVisibility(View.GONE);
            } else if (RoomDataUtils.isPKRound(mRoomData)) {
                mPKSingBeginTipsCardView.setVisibility(View.VISIBLE);
                mNormalSingBeginTipsCardView.setVisibility(View.GONE);
                mChorusSingBeginTipsCardView.setVisibility(View.GONE);
            } else {
                mNormalSingBeginTipsCardView.setVisibility(View.VISIBLE);
                mPKSingBeginTipsCardView.setVisibility(View.GONE);
                mChorusSingBeginTipsCardView.setVisibility(View.GONE);
            }
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
                    mChorusSingBeginTipsCardView.bindData(userInfoModel1, userInfoModel2, svgaListener);
                }
            } else if (RoomDataUtils.isPKRound(mRoomData)) {
                List<SPkRoundInfoModel> list = grabRoundInfoModel.getsPkRoundInfoModels();
                if (list != null && list.size() >= 2) {
                    UserInfoModel userInfoModel1 = mRoomData.getUserInfo(list.get(0).getUserID());
                    UserInfoModel userInfoModel2 = mRoomData.getUserInfo(list.get(1).getUserID());
                    mPKSingBeginTipsCardView.bindData(userInfoModel1, userInfoModel2,svgaListener);
                }
            } else {
                mNormalSingBeginTipsCardView.bindData(mRoomData.getUserInfo(grabRoundInfoModel.getUserID()), grabRoundInfoModel.getMusic(), svgaListener, grabRoundInfoModel.isChallengeRound());
            }
        }
    }
}
