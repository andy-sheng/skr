package com.module.playways.grab.room.view.minigame;

import android.content.Context;
import android.util.AttributeSet;

import com.module.playways.R;

public class VideoMiniGameSelfSingCardView extends BaseMiniGameSelfSingCardView {
    public VideoMiniGameSelfSingCardView(Context context) {
        super(context);
    }

    public VideoMiniGameSelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoMiniGameSelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        inflate(getContext(), R.layout.grab_mini_game_selft_sing_layout, this);
        mAvatarIv = findViewById(R.id.avatar_iv);
        mFirstTipsTv = findViewById(R.id.first_tips_tv);
        mSvLyric = findViewById(R.id.sv_lyric);
        mTvLyric = findViewById(R.id.tv_lyric);
    }
}
