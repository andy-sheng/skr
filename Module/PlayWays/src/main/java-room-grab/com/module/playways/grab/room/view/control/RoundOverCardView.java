package com.module.playways.grab.room.view.control;

import android.view.View;

import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.view.chorus.ChorusSelfSingCardView;
import com.module.playways.grab.room.view.normal.NormalRoundOverCardView;
import com.module.playways.grab.room.view.normal.NormalSelfSingCardView;
import com.module.playways.grab.room.view.pk.PKRoundOverCardView;
import com.module.playways.grab.room.view.pk.PKSelfSingCardView;
import com.module.rank.R;

public class RoundOverCardView {

    NormalRoundOverCardView mNormalRoundOverCardView;   // 轮次结束的卡片
    PKRoundOverCardView mPKRoundOverCardView;           // pk轮次结束卡片

    GrabRoomData mRoomData;

    public RoundOverCardView(View mRootView, GrabRoomData roomData) {
        mRoomData = roomData;
        mNormalRoundOverCardView = mRootView.findViewById(R.id.normal_round_over_card_view);
        mPKRoundOverCardView = mRootView.findViewById(R.id.pk_round_over_card_view);
    }

    public void bindData(int songId, int reason, int resultType, SVGAListener svgaListener) {
        mNormalRoundOverCardView.bindData(songId,reason,resultType,svgaListener);
    }

    public void setVisibility(int gone) {
        mNormalRoundOverCardView.setVisibility(gone);
        mPKRoundOverCardView.setVisibility(gone);
    }
}
