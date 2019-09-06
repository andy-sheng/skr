package com.module.playways.grab.room.view.normal;

import android.view.View;
import android.view.ViewStub;

import com.common.log.MyLog;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.SingCountDownView2;
import com.common.view.ExViewStub;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.grab.room.view.normal.view.SelfSingLyricView;
import com.module.playways.R;

/**
 * 你的主场景歌词
 */
public class NormalSelfSingCardView extends ExViewStub {
    public final String TAG = "SelfSingCardView2";

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
        {
            ViewStub viewStub = getMParentView().findViewById(R.id.self_sing_lyric_view_stub);
            mSelfSingLyricView = new SelfSingLyricView(viewStub, mRoomData);
        }
        mSingCountDownView = getMParentView().findViewById(R.id.sing_count_down_view);
        mSingCountDownView.setListener(mListener);
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_normal_self_sing_card_stub_layout;
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
