package com.module.playways.grab.room.ui;

import android.view.View;

import com.module.playways.grab.room.model.GrabRoundInfoModel;

import static android.view.View.GONE;

public class GrabAudioUiController extends GrabBaseUiController{
    public GrabAudioUiController(GrabRoomFragment f) {
        super(f);
    }

    @Override
    public void singBySelf() {
// 显示歌词
        mF.mSelfSingCardView.setVisibility(View.VISIBLE);
        mF.mOthersSingCardView.setVisibility(GONE);
        mF.mSelfSingCardView.playLyric();
        if(mF.mGrabWidgetAnimationController.isOpen()){
            mF.mSelfSingCardView.setTranslateY(mF.mGrabWidgetAnimationController.getTranslateByOpenType());
        }else{
            mF.mSelfSingCardView.setTranslateY(0);
        }
        if (mF.mRoomData.isNewUser()) {
            mF.tryShowGrabSelfSingTipView();

            GrabRoundInfoModel infoModel = mF.mRoomData.getRealRoundInfo();
            if (infoModel == null) {
                return;
            }
            boolean withAcc = false;
            if (infoModel.isAccRound() && mF.mRoomData != null && mF.mRoomData.isAccEnable()) {
                withAcc = true;
            }
            if (!withAcc) {
                mF.tryShowNoAccSrollTipsView();
            }
        }
    }

    @Override
    public void singByOthers() {
        // 显示收音机
        mF.mSelfSingCardView.setVisibility(GONE);
        mF.mOthersSingCardView.setVisibility(View.VISIBLE);
        mF.mOthersSingCardView.bindData();
        if(mF.mGrabWidgetAnimationController.isOpen()){
            mF.mOthersSingCardView.setTranslateY(mF.mGrabWidgetAnimationController.getTranslateByOpenType());
        }else{
            mF.mOthersSingCardView.setTranslateY(0);
        }
    }

    @Override
    public void roundOver() {
        mF.mSelfSingCardView.setVisibility(GONE);
        mF.mOthersSingCardView.hide();
    }

    @Override
    public void destroy() {
        if (mF.mSelfSingCardView != null) {
            mF.mSelfSingCardView.destroy();
        }
    }
}
