package com.module.playways.grab.room.view.minigame;

import android.view.View;
import android.view.ViewStub;

import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.view.minigame.BaseMiniGameSelfSingCardView;

public class VideoMiniGameSelfSingCardView extends BaseMiniGameSelfSingCardView {
    public VideoMiniGameSelfSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub, roomData);
    }

    @Override
    protected void init(View parentView) {
        super.init(parentView);
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_video_mini_game_self_sing_card_stub_layout;
    }
}
