package com.module.playways.grab.room.view.control;

import android.view.View;
import android.view.ViewStub;

import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.minigame.MiniGameRoundOverCardView;
import com.module.playways.grab.room.view.normal.NormalRoundOverCardView;
import com.module.playways.grab.room.view.pk.PKRoundOverCardView;
import com.module.playways.R;
import com.zq.live.proto.Common.StandPlayType;
import com.zq.live.proto.Room.EQRoundOverReason;
import com.zq.live.proto.Room.EQRoundStatus;
import com.zq.live.proto.Room.EWantSingType;

public class RoundOverCardView {

    NormalRoundOverCardView mNormalRoundOverCardView;   // 轮次结束的卡片
    PKRoundOverCardView mPKRoundOverCardView;           // pk轮次结束卡片
    MiniGameRoundOverCardView mMiniGameOverCardView;    // 小游戏结束卡片

    GrabRoomData mRoomData;

    public RoundOverCardView(View mRootView, GrabRoomData roomData) {
        mRoomData = roomData;
        {
            ViewStub viewStub = mRootView.findViewById(R.id.normal_round_over_card_view_stub);
            mNormalRoundOverCardView = new NormalRoundOverCardView(viewStub);
        }

        {
            ViewStub viewStub = mRootView.findViewById(R.id.pk_round_over_card_view_stub);
            mPKRoundOverCardView = new PKRoundOverCardView(viewStub, mRoomData);
        }

        {
            ViewStub viewStub = mRootView.findViewById(R.id.mini_game_over_card_view_stub);
            mMiniGameOverCardView = new MiniGameRoundOverCardView(viewStub);
        }
    }

    public void bindData(GrabRoundInfoModel lastRoundInfo, SVGAListener svgaListener) {
        if (lastRoundInfo != null) {
            if (lastRoundInfo.getMusic() != null && lastRoundInfo.getMusic().getPlayType() == StandPlayType.PT_SPK_TYPE.getValue()) {
                // 是pk的轮次 并且 两个轮次 userId 有效 ，说明有人玩了
                if (lastRoundInfo.getsPkRoundInfoModels().size() >= 2) {
                    if (lastRoundInfo.getsPkRoundInfoModels().get(0).getUserID() != 0
                            && lastRoundInfo.getsPkRoundInfoModels().get(1).getUserID() != 0) {
                        mPKRoundOverCardView.bindData(lastRoundInfo, svgaListener);
                        return;
                    }
                }
            }
        }

        if (lastRoundInfo != null) {
            if (lastRoundInfo.getMusic() != null && lastRoundInfo.getMusic().getPlayType() == StandPlayType.PT_MINI_GAME_TYPE.getValue()) {
                // 是小游戏的轮次 并且 两个轮次 userId 有效 ，说明有人玩了
                if (lastRoundInfo.getMINIGameRoundInfoModels().size() >= 2) {
                    if (lastRoundInfo.getMINIGameRoundInfoModels().get(0).getUserID() != 0
                            && lastRoundInfo.getMINIGameRoundInfoModels().get(1).getUserID() != 0) {
                        mMiniGameOverCardView.bindData(lastRoundInfo, svgaListener);
                        return;
                    }
                }
            }
        }

        mNormalRoundOverCardView.bindData(lastRoundInfo, svgaListener);
    }

    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            mNormalRoundOverCardView.setVisibility(View.GONE);
            mPKRoundOverCardView.setVisibility(View.GONE);
            mMiniGameOverCardView.setVisibility(View.GONE);
        } else if (visibility == View.VISIBLE) {
            if (RoomDataUtils.isChorusRound(mRoomData)) {
                mNormalRoundOverCardView.setVisibility(View.VISIBLE);
                mPKRoundOverCardView.setVisibility(View.GONE);
                mMiniGameOverCardView.setVisibility(View.GONE);
            } else if (RoomDataUtils.isPKRound(mRoomData)) {
                mPKRoundOverCardView.setVisibility(View.VISIBLE);
                mNormalRoundOverCardView.setVisibility(View.GONE);
                mMiniGameOverCardView.setVisibility(View.GONE);
            } else if (RoomDataUtils.isMiniGameRound(mRoomData)) {
                mMiniGameOverCardView.setVisibility(View.VISIBLE);
                mNormalRoundOverCardView.setVisibility(View.GONE);
                mPKRoundOverCardView.setVisibility(View.GONE);
            } else {
                mNormalRoundOverCardView.setVisibility(View.VISIBLE);
                mPKRoundOverCardView.setVisibility(View.GONE);
                mMiniGameOverCardView.setVisibility(View.GONE);
            }
        }
    }
}
