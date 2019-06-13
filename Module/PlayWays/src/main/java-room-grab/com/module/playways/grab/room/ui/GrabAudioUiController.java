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
    }
}
