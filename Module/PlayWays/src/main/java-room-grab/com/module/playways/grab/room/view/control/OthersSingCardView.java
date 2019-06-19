package com.module.playways.grab.room.view.control;

import android.view.View;
import android.view.ViewStub;

import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.view.chorus.ChorusOthersSingCardView;
import com.module.playways.grab.room.view.minigame.MiniGameOtherSingCardView;
import com.module.playways.grab.room.view.normal.NormalOthersSingCardView;
import com.module.playways.grab.room.view.pk.PKOthersSingCardView;
import com.module.playways.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OthersSingCardView {

    NormalOthersSingCardView mNormalOthersSingCardView;  // 他人唱歌卡片效果
    ChorusOthersSingCardView mChorusOtherSingCardView;   // 合唱他人唱歌卡片效果
    PKOthersSingCardView mPKOtherSingCardView;           // PK他人唱歌卡片效果
    MiniGameOtherSingCardView mMiniGameOtherSingView;    // 小游戏卡片效果

    GrabRoomData mRoomData;

    public OthersSingCardView(View mRootView, GrabRoomData roomData) {
        mRoomData = roomData;
        {
            ViewStub viewStub = mRootView.findViewById(R.id.normal_other_sing_card_view_stub);
            mNormalOthersSingCardView = new NormalOthersSingCardView(viewStub, mRoomData);
        }
        {
            ViewStub viewStub = mRootView.findViewById(R.id.chorus_other_sing_card_view_stub);
            mChorusOtherSingCardView = new ChorusOthersSingCardView(viewStub, mRoomData);
        }
        {
            ViewStub viewStub = mRootView.findViewById(R.id.pk_other_sing_card_view_stub);
            mPKOtherSingCardView = new PKOthersSingCardView(viewStub, mRoomData);
        }
        {
            ViewStub viewStub = mRootView.findViewById(R.id.mini_game_other_sing_card_view_stub);
            mMiniGameOtherSingView = new MiniGameOtherSingCardView(viewStub, mRoomData);
        }
    }

    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            mNormalOthersSingCardView.setVisibility(View.GONE);
            mChorusOtherSingCardView.setVisibility(View.GONE);
            mPKOtherSingCardView.setVisibility(View.GONE);
            mMiniGameOtherSingView.setVisibility(View.GONE);
        } else if (visibility == View.VISIBLE) {
            if (RoomDataUtils.isChorusRound(mRoomData)) {
                mChorusOtherSingCardView.setVisibility(View.VISIBLE);
                mPKOtherSingCardView.setVisibility(View.GONE);
                mNormalOthersSingCardView.setVisibility(View.GONE);
                mMiniGameOtherSingView.setVisibility(View.GONE);
            } else if (RoomDataUtils.isPKRound(mRoomData)) {
                mPKOtherSingCardView.setVisibility(View.VISIBLE);
                mChorusOtherSingCardView.setVisibility(View.GONE);
                mNormalOthersSingCardView.setVisibility(View.GONE);
                mMiniGameOtherSingView.setVisibility(View.GONE);
            } else if (RoomDataUtils.isMiniGameRound(mRoomData)) {
                mNormalOthersSingCardView.setVisibility(View.GONE);
                mChorusOtherSingCardView.setVisibility(View.GONE);
                mPKOtherSingCardView.setVisibility(View.GONE);
                mMiniGameOtherSingView.setVisibility(View.VISIBLE);
            } else {
                mNormalOthersSingCardView.setVisibility(View.VISIBLE);
                mChorusOtherSingCardView.setVisibility(View.GONE);
                mPKOtherSingCardView.setVisibility(View.GONE);
                mMiniGameOtherSingView.setVisibility(View.GONE);
            }
        }
    }

    public void bindData() {
        if (RoomDataUtils.isChorusRound(mRoomData)) {
            mChorusOtherSingCardView.bindData();
        } else if (RoomDataUtils.isPKRound(mRoomData)) {
            mPKOtherSingCardView.bindData();
        } else if (RoomDataUtils.isMiniGameRound(mRoomData)) {
            mMiniGameOtherSingView.bindData();
        } else {
            mNormalOthersSingCardView.bindData();
        }
    }

    public void tryStartCountDown() {
        mNormalOthersSingCardView.tryStartCountDown();
        mChorusOtherSingCardView.tryStartCountDown();
        mMiniGameOtherSingView.tryStartCountDown();
    }

    public void hide() {
        mNormalOthersSingCardView.hide();
        mChorusOtherSingCardView.hide();
        mPKOtherSingCardView.hide();
        mMiniGameOtherSingView.hide();
    }

    public void setTranslateY(int ty) {
        mChorusOtherSingCardView.setTranslateY(ty);
        mPKOtherSingCardView.setTranslateY(ty);
        mMiniGameOtherSingView.setTranslateY(ty);
    }

    public List<View> getRealViews() {
        List<View> list = new ArrayList<>();
        list.add(mChorusOtherSingCardView.getRealView());
        list.add(mPKOtherSingCardView.getRealView());
        list.add(mMiniGameOtherSingView.getRealView());
        return list;
    }

}
