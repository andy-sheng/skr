package com.module.playways.grab.room.view.video;

import android.view.View;
import android.view.ViewStub;

import com.module.playways.R;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.view.chorus.VideoChorusSelfSingCardView;
import com.module.playways.grab.room.view.control.SelfSingCardView;

public class GrabVideoSelfSingCardView {
    GrabRoomData mRoomData;
    VideoChorusSelfSingCardView mVideoChorusSelfSingCardView;

    public GrabVideoSelfSingCardView(View rootView, GrabRoomData roomData) {
        mRoomData = roomData;
        {
            ViewStub viewStub = rootView.findViewById(R.id.grab_video_chorus_lyric_view_stub);
            mVideoChorusSelfSingCardView = new VideoChorusSelfSingCardView(viewStub, roomData);
        }
    }

    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            mVideoChorusSelfSingCardView.setVisibility(View.GONE);
        } else if (visibility == View.VISIBLE) {
            if (RoomDataUtils.isChorusRound(mRoomData)) {
                mVideoChorusSelfSingCardView.setVisibility(View.VISIBLE);
            } else if (RoomDataUtils.isPKRound(mRoomData)) {
            } else if (RoomDataUtils.isMiniGameRound(mRoomData)) {
            } else {
            }
        }
    }

    public void playLyric() {
        if (RoomDataUtils.isChorusRound(mRoomData)) {
            mVideoChorusSelfSingCardView.playLyric();
        } else if (RoomDataUtils.isPKRound(mRoomData)) {
        } else if (RoomDataUtils.isMiniGameRound(mRoomData)) {
        } else {
        }
    }

    public void destroy() {
        mVideoChorusSelfSingCardView.destroy();
    }

    public void setListener(SelfSingCardView.Listener listener) {
        mVideoChorusSelfSingCardView.setListener(listener);
    }
}
