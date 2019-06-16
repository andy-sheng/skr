package com.module.playways.grab.room.view.chorus;

import android.view.View;
import android.view.ViewStub;

import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.SingCountDownView2;
import com.module.playways.grab.room.view.control.SelfSingCardView;

import java.util.List;


/**
 * 合唱的歌唱者看到的板子
 */
public class ChorusSelfSingCardView extends BaseChorusSelfCardView {

    public final static String TAG = "ChorusSelfSingCardView";

    SingCountDownView2 mSingCountDownView;

    public ChorusSelfSingCardView(ViewStub viewStub, GrabRoomData roomData) {
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
        return R.layout.grab_chorus_self_sing_card_stub_layout;
    }

    public boolean playLyric() {
        if(super.playLyric()){
            GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
            mSingCountDownView.startPlay(0, infoModel.getSingTotalMs(), true);
            return true;
        }else{
            return false;
        }
    }

    public void setListener(SelfSingCardView.Listener listener) {
        super.setListener(listener);
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
