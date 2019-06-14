package com.module.playways.grab.room.view.normal;

import android.view.View;
import android.view.ViewStub;

import com.common.log.MyLog;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.ExViewStub;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.grab.room.view.normal.view.SelfSingLyricView;
import com.module.playways.grab.room.view.normal.view.SingCountDownView;
import com.module.playways.grab.room.view.normal.view.VideoSelfSingLyricView;

/**
 * 你的主场景歌词
 */
public class VideoNormalSelfSingCardView extends ExViewStub {
    public final static String TAG = "SelfSingCardView2";

    GrabRoomData mRoomData;

    VideoSelfSingLyricView mSelfSingLyricView;

    public VideoNormalSelfSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
        mRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mSelfSingLyricView =  mParentView.findViewById(R.id.self_sing_lyric_view);
        if (mSelfSingLyricView != null) {
            mSelfSingLyricView.setRoomData(mRoomData);
        }
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        super.onViewAttachedToWindow(v);
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
    }

    public void playLyric() {
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel == null) {
            MyLog.d(TAG, "infoModel 是空的");
            return;
        }
        tryInflate();
        mSelfSingLyricView.initLyric();
        if (infoModel.getMusic() == null) {
            MyLog.d(TAG, "songModel 是空的");
            return;
        }

        int totalTs = infoModel.getSingTotalMs();
        boolean withAcc = false;
        if (infoModel.isAccRound() && mRoomData != null && mRoomData.isAccEnable()) {
            withAcc = true;
        }
        if (!withAcc) {
            mSelfSingLyricView.playWithNoAcc(infoModel.getMusic());
        } else {
            mSelfSingLyricView.playWithAcc(infoModel, totalTs);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.GONE) {
            mSelfSingLyricView.reset();
        }
    }

    public void destroy() {
        if (mSelfSingLyricView != null) {
            mSelfSingLyricView.destroy();
        }
    }

    SelfSingCardView.Listener mListener;

    public void setListener(SelfSingCardView.Listener l) {
        mListener = l;
    }

}
