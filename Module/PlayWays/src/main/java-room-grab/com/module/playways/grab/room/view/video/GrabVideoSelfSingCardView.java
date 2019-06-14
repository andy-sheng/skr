package com.module.playways.grab.room.view.video;

import android.view.View;
import android.view.ViewStub;

import com.module.playways.R;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.view.chorus.VideoChorusSelfSingCardView;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.grab.room.view.normal.VideoNormalSelfSingCardView;

public class GrabVideoSelfSingCardView {
    GrabRoomData mRoomData;
    VideoChorusSelfSingCardView mVideoChorusSelfSingCardView;
    VideoNormalSelfSingCardView mNormalSelfSingCardView;

    public GrabVideoSelfSingCardView(View rootView, GrabRoomData roomData) {
        mRoomData = roomData;
        {
            ViewStub viewStub = rootView.findViewById(R.id.grab_video_chorus_lyric_view_stub);
            mVideoChorusSelfSingCardView = new VideoChorusSelfSingCardView(viewStub, roomData);
        }
        {
            ViewStub viewStub = rootView.findViewById(R.id.grab_video_normal_lyric_view_stub);
            mNormalSelfSingCardView = new VideoNormalSelfSingCardView(viewStub, roomData);
        }
    }

    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            mVideoChorusSelfSingCardView.setVisibility(View.GONE);
            mNormalSelfSingCardView.setVisibility(View.GONE);
        } else if (visibility == View.VISIBLE) {
            if (RoomDataUtils.isChorusRound(mRoomData)) {
                mVideoChorusSelfSingCardView.setVisibility(View.VISIBLE);
            } else if (RoomDataUtils.isPKRound(mRoomData)) {
            } else if (RoomDataUtils.isMiniGameRound(mRoomData)) {
            } else {
                mNormalSelfSingCardView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void playLyric() {
        if (RoomDataUtils.isChorusRound(mRoomData)) {
            mVideoChorusSelfSingCardView.playLyric();
        } else if (RoomDataUtils.isPKRound(mRoomData)) {
        } else if (RoomDataUtils.isMiniGameRound(mRoomData)) {
        } else {
            mNormalSelfSingCardView.playLyric();
        }
    }

    public void destroy() {
        mVideoChorusSelfSingCardView.destroy();
        mNormalSelfSingCardView.destroy();
    }

    public void setListener(SelfSingCardView.Listener listener) {
        mVideoChorusSelfSingCardView.setListener(listener);
        mNormalSelfSingCardView.setListener(listener);
    }

    public View getRealView() {
        if(mNormalSelfSingCardView.getVisibility()==View.VISIBLE){
            return mNormalSelfSingCardView.getRealView();
        }
        if(mVideoChorusSelfSingCardView.getVisibility()==View.VISIBLE){
            return mVideoChorusSelfSingCardView.getRealView();
        }
        return null;
    }
}
