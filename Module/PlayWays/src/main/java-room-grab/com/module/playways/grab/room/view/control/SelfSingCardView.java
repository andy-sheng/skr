package com.module.playways.grab.room.view.control;

import android.view.View;
import android.view.ViewStub;

import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.view.chorus.ChorusSelfSingCardView;
import com.module.playways.grab.room.view.freemic.FreeMicSelfSingCardView;
import com.module.playways.grab.room.view.minigame.MiniGameSelfSingCardView;
import com.module.playways.grab.room.view.normal.NormalSelfSingCardView;
import com.module.playways.grab.room.view.pk.PKSelfSingCardView;
import com.module.playways.R;

import java.util.ArrayList;
import java.util.List;

public class SelfSingCardView {

    NormalSelfSingCardView mNormalSelfSingCardView;     // 自己唱歌卡片效果
    ChorusSelfSingCardView mChorusSelfSingCardView;     // 合唱自己唱歌卡片效果
    PKSelfSingCardView mPKSelfSingCardView;             // pk自己唱歌
    MiniGameSelfSingCardView mMiniGameSelfSingView;     // 小游戏时卡片效果
    FreeMicSelfSingCardView mFreeMicSelfSingCardView;  // 自由麦
    GrabRoomData mRoomData;

    public SelfSingCardView(View mRootView, GrabRoomData roomData) {
        mRoomData = roomData;
        {
            ViewStub viewStub = mRootView.findViewById(R.id.normal_self_sing_card_view_stub);
            mNormalSelfSingCardView = new NormalSelfSingCardView(viewStub, mRoomData);
        }
        {
            ViewStub viewStub = mRootView.findViewById(R.id.chorus_self_sing_card_view_stub);
            mChorusSelfSingCardView = new ChorusSelfSingCardView(viewStub, mRoomData);
        }
        {
            ViewStub viewStub = mRootView.findViewById(R.id.pk_self_sing_card_view_stub);
            mPKSelfSingCardView = new PKSelfSingCardView(viewStub, mRoomData);
        }
        {
            ViewStub viewStub = mRootView.findViewById(R.id.mini_game_self_sing_card_view_stub);
            mMiniGameSelfSingView = new MiniGameSelfSingCardView(viewStub, mRoomData);
        }
        {
            ViewStub viewStub = mRootView.findViewById(R.id.free_mic_self_sing_card_view_stub);
            mFreeMicSelfSingCardView = new FreeMicSelfSingCardView(viewStub, mRoomData);
        }

    }

    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            mNormalSelfSingCardView.setVisibility(View.GONE);
            mChorusSelfSingCardView.setVisibility(View.GONE);
            mPKSelfSingCardView.setVisibility(View.GONE);
            mMiniGameSelfSingView.setVisibility(View.GONE);
            mFreeMicSelfSingCardView.setVisibility(View.GONE);
        } else if (visibility == View.VISIBLE) {
            if (RoomDataUtils.isChorusRound(mRoomData)) {
                mChorusSelfSingCardView.setVisibility(View.VISIBLE);
                mNormalSelfSingCardView.setVisibility(View.GONE);
                mPKSelfSingCardView.setVisibility(View.GONE);
                mMiniGameSelfSingView.setVisibility(View.GONE);
                mFreeMicSelfSingCardView.setVisibility(View.GONE);
            } else if (RoomDataUtils.isPKRound(mRoomData)) {
                mPKSelfSingCardView.setVisibility(View.VISIBLE);
                mNormalSelfSingCardView.setVisibility(View.GONE);
                mChorusSelfSingCardView.setVisibility(View.GONE);
                mMiniGameSelfSingView.setVisibility(View.GONE);
                mFreeMicSelfSingCardView.setVisibility(View.GONE);
            } else if (RoomDataUtils.isMiniGameRound(mRoomData)) {
                mMiniGameSelfSingView.setVisibility(View.VISIBLE);
                mNormalSelfSingCardView.setVisibility(View.GONE);
                mChorusSelfSingCardView.setVisibility(View.GONE);
                mPKSelfSingCardView.setVisibility(View.GONE);
                mFreeMicSelfSingCardView.setVisibility(View.GONE);
            }else if(RoomDataUtils.isFreeMicRound(mRoomData)){
                mMiniGameSelfSingView.setVisibility(View.GONE);
                mNormalSelfSingCardView.setVisibility(View.GONE);
                mChorusSelfSingCardView.setVisibility(View.GONE);
                mPKSelfSingCardView.setVisibility(View.GONE);
                mFreeMicSelfSingCardView.setVisibility(View.VISIBLE);
            } else {
                mNormalSelfSingCardView.setVisibility(View.VISIBLE);
                mChorusSelfSingCardView.setVisibility(View.GONE);
                mPKSelfSingCardView.setVisibility(View.GONE);
                mMiniGameSelfSingView.setVisibility(View.GONE);
                mFreeMicSelfSingCardView.setVisibility(View.GONE);
            }
        }
    }

    public void playLyric() {
        if (RoomDataUtils.isChorusRound(mRoomData)) {
            mChorusSelfSingCardView.playLyric();
        } else if (RoomDataUtils.isPKRound(mRoomData)) {
            mPKSelfSingCardView.playLyric();
        } else if (RoomDataUtils.isMiniGameRound(mRoomData)) {
            mMiniGameSelfSingView.playLyric();
        } else if (RoomDataUtils.isFreeMicRound(mRoomData)) {
            mFreeMicSelfSingCardView.playLyric();
        } else {
            mNormalSelfSingCardView.playLyric();
        }
    }

    public void destroy() {
        mNormalSelfSingCardView.destroy();
        mChorusSelfSingCardView.destroy();
        mPKSelfSingCardView.destroy();
        mMiniGameSelfSingView.destroy();
        mFreeMicSelfSingCardView.destroy();
    }

    public void setListener(SelfSingCardView.Listener listener) {
        mNormalSelfSingCardView.setListener(listener);
        mChorusSelfSingCardView.setListener(listener);
        mPKSelfSingCardView.setListener(listener);
        mMiniGameSelfSingView.setListener(listener);
    }

    public void setListener4FreeMic(SelfSingCardView.Listener listener) {
        mFreeMicSelfSingCardView.setListener(listener);
    }

    public List<View> getRealViews() {
        List<View> list = new ArrayList<>();
        list.add(mNormalSelfSingCardView.getRealView());
        list.add(mChorusSelfSingCardView.getRealView());
        list.add(mPKSelfSingCardView.getRealView());
        list.add(mMiniGameSelfSingView.getRealView());
        list.add(mFreeMicSelfSingCardView.getRealView());
        return list;
    }

    public void setTranslateY(float ty) {
        mChorusSelfSingCardView.setTranslateY(ty);
        mPKSelfSingCardView.setTranslateY(ty);
        mMiniGameSelfSingView.setTranslateY(ty);
        mNormalSelfSingCardView.setTranslateY(ty);
        mFreeMicSelfSingCardView.setTranslateY(ty);
    }

    public interface Listener {
        void onSelfSingOver();
    }
}
