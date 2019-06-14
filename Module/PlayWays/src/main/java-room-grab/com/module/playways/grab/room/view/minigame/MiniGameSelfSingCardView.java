package com.module.playways.grab.room.view.minigame;

import android.content.Context;
import android.util.AttributeSet;

import com.common.log.MyLog;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.SingCountDownView2;
import com.module.playways.grab.room.view.control.SelfSingCardView;

/**
 * 小游戏自己视角的卡片
 */
public class MiniGameSelfSingCardView extends BaseMiniGameSelfSingCardView {

    public final static String TAG = "MiniGameSelfSingCardView";

    SingCountDownView2 mSingCountDownView;

    public MiniGameSelfSingCardView(Context context) {
        super(context);
    }

    public MiniGameSelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MiniGameSelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        inflate(getContext(), R.layout.grab_mini_game_selft_sing_layout, this);

        mAvatarIv = findViewById(R.id.avatar_iv);
        mFirstTipsTv = findViewById(R.id.first_tips_tv);
        mSvLyric = findViewById(R.id.sv_lyric);
        mTvLyric = findViewById(R.id.tv_lyric);
        mSingCountDownView = findViewById(R.id.sing_count_down_view);

    }

    public void setRoomData(GrabRoomData roomData) {
        mGrabRoomData = roomData;
    }

    public void setListener(SelfSingCardView.Listener l) {
        super.setListener(l);
        if (mSingCountDownView != null) {
            mSingCountDownView.setListener(mListener);
        }
    }

    public void playLyric() {
        GrabRoundInfoModel infoModel = mGrabRoomData.getRealRoundInfo();
        if (infoModel == null) {
            MyLog.w(TAG, "infoModel 是空的");
            return;
        }

        if (infoModel.getMusic() == null) {
            MyLog.w(TAG, "songModel 是空的");
            return;
        }

        super.playLyric();
        mMiniGameInfoModel = infoModel.getMusic().getMiniGame();
        if (mMiniGameInfoModel == null) {
            MyLog.w(TAG, "MiniGame 是空的");
            return;
        }

        int totalTs = infoModel.getSingTotalMs();
        mSingCountDownView.startPlay(0, totalTs, true);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            mSingCountDownView.reset();
        }
    }
}
