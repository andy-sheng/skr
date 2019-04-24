package com.module.playways.grab.room.view.control;

import android.view.View;

import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.normal.NormalRoundOverCardView;
import com.module.playways.grab.room.view.pk.PKRoundOverCardView;
import com.module.playways.R;
import com.zq.live.proto.Room.EWantSingType;

public class RoundOverCardView {

    NormalRoundOverCardView mNormalRoundOverCardView;   // 轮次结束的卡片
    PKRoundOverCardView mPKRoundOverCardView;           // pk轮次结束卡片

    GrabRoomData mRoomData;

    public RoundOverCardView(View mRootView, GrabRoomData roomData) {
        mRoomData = roomData;
        mNormalRoundOverCardView = mRootView.findViewById(R.id.normal_round_over_card_view);
        mPKRoundOverCardView = mRootView.findViewById(R.id.pk_round_over_card_view);
        mPKRoundOverCardView.setRoomData(mRoomData);
    }

    public void bindData(GrabRoundInfoModel lastRoundInfo, SVGAListener svgaListener) {
        if(lastRoundInfo.getWantSingType() == EWantSingType.EWST_SPK.getValue()){
            mPKRoundOverCardView.bindData(lastRoundInfo);
        }else{
            mNormalRoundOverCardView.bindData(lastRoundInfo, svgaListener);
        }
    }

    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            mNormalRoundOverCardView.setVisibility(View.GONE);
            mPKRoundOverCardView.setVisibility(View.GONE);
        } else if (visibility == View.VISIBLE) {
            if (RoomDataUtils.isChorusRound(mRoomData)) {
                mNormalRoundOverCardView.setVisibility(View.VISIBLE);
                mPKRoundOverCardView.setVisibility(View.GONE);
            } else if (RoomDataUtils.isPKRound(mRoomData)) {
                mPKRoundOverCardView.setVisibility(View.VISIBLE);
                mNormalRoundOverCardView.setVisibility(View.GONE);
            } else {
                mNormalRoundOverCardView.setVisibility(View.VISIBLE);
                mPKRoundOverCardView.setVisibility(View.GONE);
            }
        }
    }
}
