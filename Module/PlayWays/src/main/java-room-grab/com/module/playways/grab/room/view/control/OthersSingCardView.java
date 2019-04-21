package com.module.playways.grab.room.view.control;

import android.view.View;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.view.chorus.ChorusOthersSingCardView;
import com.module.playways.grab.room.view.normal.NormalOthersSingCardView;
import com.module.playways.grab.room.view.pk.PKOthersSingCardView;
import com.module.rank.R;

public class OthersSingCardView {

    NormalOthersSingCardView mNormalOthersSingCardView; // 他人唱歌卡片效果
    ChorusOthersSingCardView mChorusOtherSingCardView;   // 合唱他人唱歌卡片效果
    PKOthersSingCardView mPKOtherSingCardView;           // PK他人唱歌卡片效果

    GrabRoomData mRoomData;
    public OthersSingCardView(View mRootView, GrabRoomData roomData) {
        mRoomData = roomData;
        mNormalOthersSingCardView = mRootView.findViewById(R.id.other_sing_card_view);
        mChorusOtherSingCardView = mRootView.findViewById(R.id.chorus_other_sing_card_view);
        mPKOtherSingCardView = mRootView.findViewById(R.id.pk_other_sing_card_view);
        mNormalOthersSingCardView.setRoomData(mRoomData);
    }

    public void setVisibility(int gone) {
        mNormalOthersSingCardView.setVisibility(View.GONE);
        mChorusOtherSingCardView.setVisibility(View.GONE);
        mPKOtherSingCardView.setVisibility(View.GONE);
    }

    public void bindData(UserInfoModel userInfoModel) {
    mNormalOthersSingCardView.bindData(userInfoModel);
    }

    public void tryStartCountDown() {
        mNormalOthersSingCardView.tryStartCountDown();
    }

    public void hide() {
        mNormalOthersSingCardView.hide();
    }
}
