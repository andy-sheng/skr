package com.module.playways.grab.room.view.minigame;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.common.log.MyLog;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.grab.room.view.normal.view.SingCountDownView;
import com.module.playways.room.song.model.MiniGameInfoModel;

/**
 * 小游戏自己视角的卡片
 */
public class MiniGameSelfSingCardView extends RelativeLayout {

    public final static String TAG = "MiniGameSelfSingCardView";

    GrabRoomData mGrabRoomData;
    MiniGameInfoModel mMiniGameInfoModel;
    SelfSingCardView.Listener mListener;

    ImageView mIvBg;
    ScrollView mSvLyric;
    TextView mTvLyric;    //用来显示游戏内容
    SingCountDownView mSingCountDownView;

    public MiniGameSelfSingCardView(Context context) {
        super(context);
        init();
    }

    public MiniGameSelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MiniGameSelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_mini_game_selft_sing_layout, this);

        mIvBg = findViewById(R.id.iv_bg);
        mSvLyric = findViewById(R.id.sv_lyric);
        mTvLyric = findViewById(R.id.tv_lyric);
        mSingCountDownView = findViewById(R.id.sing_count_down_view);

    }

    public void setRoomData(GrabRoomData roomData) {
        mGrabRoomData = roomData;
    }

    public void setListener(SelfSingCardView.Listener l) {
        mListener = l;
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
        mMiniGameInfoModel = infoModel.getMusic().getMiniGame();
        if (mMiniGameInfoModel == null) {
            MyLog.w(TAG, "MiniGame 是空的");
            return;
        }

        int totalTs = infoModel.getSingTotalMs();
        mSingCountDownView.setTagTvText(mMiniGameInfoModel.getGameName());
        mSingCountDownView.startPlay(0, totalTs, true);
        // TODO: 2019-05-29 怎么展示游戏内容
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            mSingCountDownView.reset();
        }
    }

    public void destroy() {

    }
}
