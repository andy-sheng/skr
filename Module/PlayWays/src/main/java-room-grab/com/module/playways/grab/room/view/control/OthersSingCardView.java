package com.module.playways.grab.room.view.control;

import android.view.View;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.SPkRoundInfoModel;
import com.module.playways.grab.room.view.chorus.ChorusOthersSingCardView;
import com.module.playways.grab.room.view.normal.NormalOthersSingCardView;
import com.module.playways.grab.room.view.pk.PKOthersSingCardView;
import com.module.rank.R;

import java.util.List;

public class OthersSingCardView {

    NormalOthersSingCardView mNormalOthersSingCardView; // 他人唱歌卡片效果
    ChorusOthersSingCardView mChorusOtherSingCardView;   // 合唱他人唱歌卡片效果
    PKOthersSingCardView mPKOtherSingCardView;           // PK他人唱歌卡片效果

    GrabRoomData mRoomData;

    public OthersSingCardView(View mRootView, GrabRoomData roomData) {
        mRoomData = roomData;
        mNormalOthersSingCardView = mRootView.findViewById(R.id.other_sing_card_view);
        mNormalOthersSingCardView.setRoomData(mRoomData);
        mChorusOtherSingCardView = mRootView.findViewById(R.id.chorus_other_sing_card_view);
        mChorusOtherSingCardView.setRoomData(mRoomData);
        mPKOtherSingCardView = mRootView.findViewById(R.id.pk_other_sing_card_view);
        mPKOtherSingCardView.setRoomData(mRoomData);
    }

    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            mNormalOthersSingCardView.setVisibility(View.GONE);
            mChorusOtherSingCardView.setVisibility(View.GONE);
            mPKOtherSingCardView.setVisibility(View.GONE);
        } else if (visibility == View.VISIBLE) {
            if (RoomDataUtils.isChorusRound(mRoomData)) {
                mChorusOtherSingCardView.setVisibility(View.VISIBLE);
                mPKOtherSingCardView.setVisibility(View.GONE);
                mNormalOthersSingCardView.setVisibility(View.GONE);
            } else if (RoomDataUtils.isPKRound(mRoomData)) {
                mPKOtherSingCardView.setVisibility(View.VISIBLE);
                mChorusOtherSingCardView.setVisibility(View.GONE);
                mNormalOthersSingCardView.setVisibility(View.GONE);
            } else {
                mNormalOthersSingCardView.setVisibility(View.VISIBLE);
                mChorusOtherSingCardView.setVisibility(View.GONE);
                mPKOtherSingCardView.setVisibility(View.GONE);
            }
        }
    }

    public void bindData() {
        if (RoomDataUtils.isChorusRound(mRoomData)) {
            mChorusOtherSingCardView.bindData();
        } else if (RoomDataUtils.isPKRound(mRoomData)) {
            mPKOtherSingCardView.bindData();
        } else {
            mNormalOthersSingCardView.bindData();
        }
    }

    public void tryStartCountDown() {
        mNormalOthersSingCardView.tryStartCountDown();
    }

    public void hide() {
        mNormalOthersSingCardView.hide();
        mChorusOtherSingCardView.hide();
        mPKOtherSingCardView.hide();
    }
}
