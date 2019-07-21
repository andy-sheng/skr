package com.module.playways.grab.room.view.pk;

import android.view.View;
import android.view.ViewStub;

import com.common.log.MyLog;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.grab.room.view.normal.view.SelfSingLyricView;

/**
 * 你的主场景歌词
 */
public class VideoPkSelfSingCardView extends SelfSingLyricView {
    public final String TAG = "SelfSingCardView2";


    public VideoPkSelfSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub, roomData);
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_video_pk_self_sing_card_stub_layout;
    }

    SelfSingCardView.Listener mListener;

    public void setListener(SelfSingCardView.Listener l) {
        mListener = l;
    }

    public void playLyric() {
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel == null) {
            MyLog.d(TAG, "infoModel 是空的");
            return;
        }
        tryInflate();
        setVisibility(View.VISIBLE);
        if (infoModel.getMusic() == null) {
            MyLog.d(TAG, "songModel 是空的");
            return;
        }

        int totalTs = infoModel.getSingTotalMs();
        boolean withAcc = false;
        if (infoModel.isAccRound() && mRoomData.isAccEnable() || infoModel.isPKRound()) {
            withAcc = true;
        }
        if (!withAcc) {
            playWithNoAcc(infoModel.getMusic());
        } else {
            playWithAcc(infoModel, totalTs);
        }
    }
}
