package com.module.playways.grab.room.view.normal;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.SingCountDownView2;
import com.module.playways.grab.room.view.ExViewStub;
import com.module.playways.grab.room.view.normal.view.SingCountDownView;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.grab.room.view.normal.view.SelfSingLyricView;
import com.module.playways.R;

/**
 * 你的主场景歌词
 */
public class NormalSelfSingCardView extends ExViewStub {
    public final static String TAG = "SelfSingCardView2";

    GrabRoomData mRoomData;

    SelfSingLyricView mSelfSingLyricView;
    SingCountDownView2 mSingCountDownView;

    SelfSingCardView.Listener mListener;

    public NormalSelfSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
        mRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mSelfSingLyricView = mParentView.findViewById(R.id.self_sing_lyric_view);
        mSelfSingLyricView.setRoomData(mRoomData);
        mSingCountDownView = mParentView.findViewById(R.id.sing_count_down_view);
        mSingCountDownView.setListener(mListener);
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
        mSingCountDownView.startPlay(0, totalTs, true);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.GONE) {
            if (mSingCountDownView != null) {
                mSingCountDownView.reset();
            }
            if (mSelfSingLyricView != null) {
                mSelfSingLyricView.reset();
            }
        }
    }

    public void destroy() {
        if (mSelfSingLyricView != null) {
            mSelfSingLyricView.destroy();
        }
    }

    public void setListener(SelfSingCardView.Listener l) {
        mListener = l;
    }

}
