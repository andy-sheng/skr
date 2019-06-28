package com.module.playways.grab.room.view.video;

import android.view.View;
import android.view.ViewStub;

import com.module.playways.R;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.view.chorus.VideoChorusSelfSingCardView;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.grab.room.view.minigame.VideoMiniGameSelfSingCardView;
import com.module.playways.grab.room.view.normal.VideoNormalSelfSingCardView;

public class GrabVideoSelfSingCardView {
    GrabRoomData mRoomData;
    VideoChorusSelfSingCardView mVideoChorusSelfSingCardView;
    VideoNormalSelfSingCardView mVideoNormalSelfSingCardView;
    VideoMiniGameSelfSingCardView mVideoMiniGameSelfSingCardView;

    public GrabVideoSelfSingCardView(View rootView, GrabRoomData roomData) {
        mRoomData = roomData;
        {
            ViewStub viewStub = rootView.findViewById(R.id.grab_video_normal_lyric_view_stub);
            mVideoNormalSelfSingCardView = new VideoNormalSelfSingCardView(viewStub, roomData);
        }
        {
            ViewStub viewStub = rootView.findViewById(R.id.grab_video_chorus_lyric_view_stub);
            mVideoChorusSelfSingCardView = new VideoChorusSelfSingCardView(viewStub, roomData);
        }
        {
            ViewStub viewStub = rootView.findViewById(R.id.grab_video_mini_game_lyric_view_stub);
            mVideoMiniGameSelfSingCardView = new VideoMiniGameSelfSingCardView(viewStub, roomData);
        }

    }

    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            mVideoNormalSelfSingCardView.setVisibility(View.GONE);
            mVideoChorusSelfSingCardView.setVisibility(View.GONE);
            mVideoMiniGameSelfSingCardView.setVisibility(View.GONE);
        } else if (visibility == View.VISIBLE) {
            if (RoomDataUtils.isChorusRound(mRoomData)) {
                mVideoNormalSelfSingCardView.setVisibility(View.GONE);
                mVideoChorusSelfSingCardView.setVisibility(View.VISIBLE);
                mVideoMiniGameSelfSingCardView.setVisibility(View.GONE);
            } else if (RoomDataUtils.isPKRound(mRoomData)) {
                mVideoNormalSelfSingCardView.setVisibility(View.VISIBLE);
                mVideoChorusSelfSingCardView.setVisibility(View.GONE);
                mVideoMiniGameSelfSingCardView.setVisibility(View.GONE);
            } else if (RoomDataUtils.isMiniGameRound(mRoomData)) {
                mVideoNormalSelfSingCardView.setVisibility(View.GONE);
                mVideoChorusSelfSingCardView.setVisibility(View.GONE);
                mVideoMiniGameSelfSingCardView.setVisibility(View.VISIBLE);
            } else {
                mVideoNormalSelfSingCardView.setVisibility(View.VISIBLE);
                mVideoChorusSelfSingCardView.setVisibility(View.GONE);
                mVideoMiniGameSelfSingCardView.setVisibility(View.GONE);
            }
        }
    }

    public void playLyric() {
        if (RoomDataUtils.isChorusRound(mRoomData)) {
            mVideoChorusSelfSingCardView.playLyric();
        } else if (RoomDataUtils.isPKRound(mRoomData)) {
            mVideoNormalSelfSingCardView.playLyric();
        } else if (RoomDataUtils.isMiniGameRound(mRoomData)) {
            mVideoMiniGameSelfSingCardView.playLyric();
        } else {
            mVideoNormalSelfSingCardView.playLyric();
        }
    }

    public void destroy() {
        mVideoChorusSelfSingCardView.destroy();
        mVideoNormalSelfSingCardView.destroy();
        mVideoMiniGameSelfSingCardView.destroy();
    }

    public void setListener(SelfSingCardView.Listener listener) {
        mVideoChorusSelfSingCardView.setListener(listener);
        mVideoNormalSelfSingCardView.setListener(listener);
        mVideoMiniGameSelfSingCardView.setListener(listener);
    }

//    public View getRealView() {
//        if(mVideoNormalSelfSingCardView.getVisibility()==View.VISIBLE){
//            return mVideoNormalSelfSingCardView.getRealView();
//        }
//        if(mVideoChorusSelfSingCardView.getVisibility()==View.VISIBLE){
//            return mVideoChorusSelfSingCardView.getRealView();
//        }
//        return null;
//    }
}
