package com.module.playways.grab.room.view.minigame;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;

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

    public MiniGameSelfSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub, roomData);
    }

    @Override
    protected void init(View parentView) {
        mSingCountDownView = mParentView.findViewById(R.id.sing_count_down_view);
        mSingCountDownView.setListener(mListener);
    }

    public void setListener(SelfSingCardView.Listener l) {
        super.setListener(l);
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
        if (visibility == View.GONE) {
            mSingCountDownView.reset();
        }
    }
}
