package com.module.playways.grab.room.view.video;

import android.view.View;
import android.view.ViewStub;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.module.playways.R;
import com.module.playways.doubleplay.DoubleRoomData;
import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomMusic;
import com.module.playways.doubleplay.view.DoubleChorusSelfSingCardView;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.doubleplay.view.DoubleMiniGameSelfSingCardView;
import com.module.playways.doubleplay.view.DoubleNormalSelfSingCardView;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Common.StandPlayType;

public class DoubleSelfSingCardView {
    public final static String TAG = "DoubleSelfSingCardView";
    DoubleChorusSelfSingCardView mDoubleChorusSelfSingCardView;
    DoubleNormalSelfSingCardView mDoubleNormalSelfSingCardView;
    DoubleMiniGameSelfSingCardView mDoubleMiniGameSelfSingCardView;

    SongModel mSongModel;

    public DoubleSelfSingCardView(View rootView, DoubleRoomData doubleRoomData) {
        {
            ViewStub viewStub = rootView.findViewById(R.id.double_normal_lyric_view_stub);
            mDoubleNormalSelfSingCardView = new DoubleNormalSelfSingCardView(viewStub, null);
        }
        {
            ViewStub viewStub = rootView.findViewById(R.id.grab_video_chorus_lyric_view_stub);
            mDoubleChorusSelfSingCardView = new DoubleChorusSelfSingCardView(viewStub, doubleRoomData);
        }
        {
            ViewStub viewStub = rootView.findViewById(R.id.grab_video_mini_game_lyric_view_stub);
            mDoubleMiniGameSelfSingCardView = new DoubleMiniGameSelfSingCardView(viewStub);
        }

    }

    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            mDoubleNormalSelfSingCardView.setVisibility(View.GONE);
            mDoubleChorusSelfSingCardView.setVisibility(View.GONE);
            mDoubleMiniGameSelfSingCardView.setVisibility(View.GONE);
        } else if (visibility == View.VISIBLE) {
            mDoubleNormalSelfSingCardView.setVisibility(View.GONE);
            mDoubleChorusSelfSingCardView.setVisibility(View.GONE);
            mDoubleMiniGameSelfSingCardView.setVisibility(View.GONE);
            if (mSongModel.getPlayType() == StandPlayType.PT_CHO_TYPE.getValue()) {
                mDoubleChorusSelfSingCardView.setVisibility(View.VISIBLE);
            } else if (mSongModel.getPlayType() == StandPlayType.PT_MINI_GAME_TYPE.getValue()) {
                mDoubleMiniGameSelfSingCardView.setVisibility(View.VISIBLE);
            } else {
                mDoubleNormalSelfSingCardView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void playLyric(LocalCombineRoomMusic songModel, DoubleRoomData roomData) {
        if (songModel == null) {
            MyLog.w(TAG, "playLyric" + " songModel is null");
            return;
        }

        mDoubleNormalSelfSingCardView.setVisibility(View.GONE);
        mDoubleChorusSelfSingCardView.setVisibility(View.GONE);
        mDoubleMiniGameSelfSingCardView.setVisibility(View.GONE);

        mSongModel = songModel.getMusic();

        if (mSongModel.getPlayType() == StandPlayType.PT_CHO_TYPE.getValue()) {
            mDoubleChorusSelfSingCardView.setVisibility(View.VISIBLE);
            if (songModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                mDoubleChorusSelfSingCardView.playLyric(mSongModel, roomData.getMyUser(), roomData.getAntherUser());
            } else {
                mDoubleChorusSelfSingCardView.playLyric(mSongModel, roomData.getAntherUser(), roomData.getMyUser());
            }
        } else if (mSongModel.getPlayType() == StandPlayType.PT_MINI_GAME_TYPE.getValue()) {
            mDoubleMiniGameSelfSingCardView.playLyric(songModel, roomData);
            mDoubleMiniGameSelfSingCardView.setVisibility(View.VISIBLE);
        } else {
            mDoubleNormalSelfSingCardView.playLyric(mSongModel);
            mDoubleNormalSelfSingCardView.setVisibility(View.VISIBLE);
        }
    }

    public void updateLockState() {
        if (mSongModel == null) {
            return;
        }

        if (mSongModel.getPlayType() == StandPlayType.PT_CHO_TYPE.getValue()) {
            mDoubleChorusSelfSingCardView.updateLockState();
        } else if (mSongModel.getPlayType() == StandPlayType.PT_MINI_GAME_TYPE.getValue()) {
            mDoubleMiniGameSelfSingCardView.updateLockState();
        } else {
            mDoubleNormalSelfSingCardView.updateLockState();
        }
    }

    public void destroy() {
        mDoubleChorusSelfSingCardView.destroy();
        mDoubleNormalSelfSingCardView.destroy();
        mDoubleMiniGameSelfSingCardView.destroy();
    }
}
