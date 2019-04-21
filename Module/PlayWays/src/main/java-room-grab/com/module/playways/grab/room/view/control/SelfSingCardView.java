package com.module.playways.grab.room.view.control;

import android.view.View;

import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.chorus.ChorusSelfSingCardView;
import com.module.playways.grab.room.view.normal.NormalSelfSingCardView;
import com.module.playways.grab.room.view.pk.PKSelfSingCardView;
import com.module.rank.R;

public class SelfSingCardView {

    NormalSelfSingCardView mNormalSelfSingCardView;     // 自己唱歌卡片效果
    ChorusSelfSingCardView mChorusSelfSingCardView;     // 合唱自己唱歌卡片效果
    PKSelfSingCardView mPKSelfSingCardView;             // PK自己唱歌卡片效果
    GrabRoomData mRoomData;

    public SelfSingCardView(View mRootView, GrabRoomData roomData) {
        mRoomData = roomData;
        mNormalSelfSingCardView = mRootView.findViewById(R.id.self_sing_card_view);
        mChorusSelfSingCardView = mRootView.findViewById(R.id.chorus_self_sing_card_view);
        mPKSelfSingCardView = mRootView.findViewById(R.id.pk_self_sing_card_view);
        mNormalSelfSingCardView.setRoomData(mRoomData);
    }

    public void setVisibility(int visible) {
        mNormalSelfSingCardView.setVisibility(visible);
        mChorusSelfSingCardView.setVisibility(visible);
        mPKSelfSingCardView.setVisibility(visible);
    }

    public void playLyric(GrabRoundInfoModel infoModel, boolean accEnable) {
        mNormalSelfSingCardView.playLyric(infoModel, accEnable);
    }

    public void destroy() {
        mNormalSelfSingCardView.destroy();
    }

    SelfSingCardView.Listener mListener;

    public void setListener(SelfSingCardView.Listener listener){
        mListener = listener;
        mNormalSelfSingCardView.setListener(listener);
    }
    public interface Listener {
        void onSelfSingOver();
    }
}
