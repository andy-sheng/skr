package com.module.playways.grab.room.ui;

import android.view.View;

import com.module.playways.grab.room.model.GrabRoundInfoModel;

import static android.view.View.GONE;

public class GrabAudioUiController extends GrabBaseUiController{
    public GrabAudioUiController(GrabRoomFragment f) {
        super(f);
    }

    @Override
    public void grabBegin() {

    }

    @Override
    public void singBySelf() {
// 显示歌词
        mF.getMSelfSingCardView().setVisibility(View.VISIBLE);
        mF.getMOthersSingCardView().setVisibility(GONE);
        mF.getMSelfSingCardView().playLyric();
        if(mF.getMGrabWidgetAnimationController().isOpen()){
            mF.getMSelfSingCardView().setTranslateY(mF.getMGrabWidgetAnimationController().getTranslateByOpenType());
        }else{
            mF.getMSelfSingCardView().setTranslateY(0);
        }
        if (mF.getMRoomData().isNewUser()) {
            mF.tryShowGrabSelfSingTipView();

            GrabRoundInfoModel infoModel = mF.getMRoomData().getRealRoundInfo();
            if (infoModel == null) {
                return;
            }
            boolean withAcc = false;
            if (infoModel.isAccRound() && mF.getMRoomData() != null && mF.getMRoomData().isAccEnable()) {
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
        mF.getMSelfSingCardView().setVisibility(GONE);
        mF.getMOthersSingCardView().setVisibility(View.VISIBLE);
        mF.getMOthersSingCardView().bindData();
        if(mF.getMGrabWidgetAnimationController().isOpen()){
            mF.getMOthersSingCardView().setTranslateY(mF.getMGrabWidgetAnimationController().getTranslateByOpenType());
        }else{
            mF.getMOthersSingCardView().setTranslateY(0);
        }
    }

    @Override
    public void roundOver() {
        mF.getMSelfSingCardView().setVisibility(GONE);
        mF.getMOthersSingCardView().hide();
    }

    @Override
    public void destroy() {
        if (mF.getMSelfSingCardView() != null) {
            mF.getMSelfSingCardView().destroy();
        }
    }

    @Override
    public void stopWork() {
        roundOver();
    }
}
