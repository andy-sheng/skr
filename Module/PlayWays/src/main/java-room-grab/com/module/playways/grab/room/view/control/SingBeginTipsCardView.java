package com.module.playways.grab.room.view.control;

import android.view.View;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.view.chorus.ChorusSelfSingCardView;
import com.module.playways.grab.room.view.chorus.ChorusSingBeginTipsCardView;
import com.module.playways.grab.room.view.normal.NormalSelfSingCardView;
import com.module.playways.grab.room.view.normal.NormalSingBeginTipsCardView;
import com.module.playways.grab.room.view.pk.PKSelfSingCardView;
import com.module.playways.grab.room.view.pk.PKSingBeginTipsCardView;
import com.module.playways.room.song.model.SongModel;
import com.module.rank.R;

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

    public void setVisibility(int gone) {
        mNormalSingBeginTipsCardView.setVisibility(gone);
        mChorusSingBeginTipsCardView.setVisibility(gone);
        mPKSingBeginTipsCardView.setVisibility(gone);
    }

    public void bindData(UserInfoModel userInfo, SongModel music, SVGAListener svgaListener, boolean challengeRound) {
        mNormalSingBeginTipsCardView.bindData(userInfo,music,svgaListener,challengeRound);
    }
}
