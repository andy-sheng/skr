package com.module.playways.grab.room.view.control;

import android.view.View;

import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.chorus.ChorusSelfSingCardView;
import com.module.playways.grab.room.view.normal.NormalSelfSingCardView;
import com.module.rank.R;

public class SelfSingCardView {

    NormalSelfSingCardView mNormalSelfSingCardView;     // 自己唱歌卡片效果
    ChorusSelfSingCardView mChorusSelfSingCardView;     // 合唱自己唱歌卡片效果
    GrabRoomData mRoomData;
    SelfSingCardView.Listener mListener;

    public SelfSingCardView(View mRootView, GrabRoomData roomData) {
        mRoomData = roomData;
        mNormalSelfSingCardView = mRootView.findViewById(R.id.self_sing_card_view);
        mNormalSelfSingCardView.setRoomData(mRoomData);
        mChorusSelfSingCardView = mRootView.findViewById(R.id.chorus_self_sing_card_view);
        mChorusSelfSingCardView.setRoomData(mRoomData);
    }

    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            mNormalSelfSingCardView.setVisibility(View.GONE);
            mChorusSelfSingCardView.setVisibility(View.GONE);
        } else if (visibility == View.VISIBLE) {
            if (RoomDataUtils.isChorusRound(mRoomData)) {
                mChorusSelfSingCardView.setVisibility(View.VISIBLE);
                mNormalSelfSingCardView.setVisibility(View.GONE);
            } else if (RoomDataUtils.isPKRound(mRoomData)) {
                mNormalSelfSingCardView.setVisibility(View.VISIBLE);
                mChorusSelfSingCardView.setVisibility(View.GONE);
            } else {
                mNormalSelfSingCardView.setVisibility(View.VISIBLE);
                mChorusSelfSingCardView.setVisibility(View.GONE);
            }
        }
    }

    public void playLyric() {
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        if (grabRoundInfoModel != null) {
            if (RoomDataUtils.isChorusRound(mRoomData)) {
                mChorusSelfSingCardView.playLyric();
            } else if (RoomDataUtils.isPKRound(mRoomData)) {
                mNormalSelfSingCardView.playLyric();
            } else {
                mNormalSelfSingCardView.playLyric();
            }
        }
    }

    public void destroy() {
        mNormalSelfSingCardView.destroy();
    }

    public void setListener(SelfSingCardView.Listener listener) {
        mListener = listener;
        mNormalSelfSingCardView.setListener(listener);
        mChorusSelfSingCardView.setListener(listener);
    }

    public interface Listener {
        void onSelfSingOver();
    }
}
