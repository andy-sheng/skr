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

    public final String TAG = "MiniGameSelfSingCardView";

    SingCountDownView2 mSingCountDownView;

    public MiniGameSelfSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub, roomData);
    }

    @Override
    protected void init(View parentView) {
        super.init(parentView);
        mSingCountDownView = mParentView.findViewById(R.id.sing_count_down_view);
        mSingCountDownView.setListener(mListener);
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_mini_game_self_sing_card_stub_layout;
    }

    public void setListener(SelfSingCardView.Listener l) {
        super.setListener(l);
    }

    public boolean playLyric() {
        if(super.playLyric()){
            GrabRoundInfoModel infoModel = mGrabRoomData.getRealRoundInfo();
            int totalTs = infoModel.getSingTotalMs();
            mSingCountDownView.startPlay(0, totalTs, true);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.GONE) {
            if (mSingCountDownView != null) {
                mSingCountDownView.reset();
            }
        }
    }
}
