package com.module.playways.grab.room.view.normal;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.SingCountDownView2;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.grab.room.view.normal.view.SelfSingLyricView;
import com.module.playways.R;

/**
 * 你的主场景歌词
 */
public class NormalSelfSingCardView extends RelativeLayout {
    public final static String TAG = "SelfSingCardView2";

    GrabRoomData mRoomData;

    SelfSingLyricView mSelfSingLyricView;
    SingCountDownView2 mSingCountDownView;

    public NormalSelfSingCardView(Context context) {
        super(context);
        init();
    }

    public NormalSelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NormalSelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_normal_self_sing_card_layout, this);

        mSelfSingLyricView = (SelfSingLyricView) findViewById(R.id.self_sing_lyric_view);
        mSingCountDownView = (SingCountDownView2) findViewById(R.id.sing_count_down_view);
    }

    public void playLyric() {
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel == null) {
            MyLog.d(TAG, "infoModel 是空的");
            return;
        }

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
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            mSingCountDownView.reset();
            mSelfSingLyricView.reset();
        }
    }

    public void destroy() {
        if (mSelfSingLyricView != null) {
            mSelfSingLyricView.destroy();
        }
    }

    public void setRoomData(GrabRoomData roomData) {
        this.mRoomData = roomData;
        if (mSelfSingLyricView != null) {
            mSelfSingLyricView.setRoomData(roomData);
        }
    }

    SelfSingCardView.Listener mListener;

    public void setListener(SelfSingCardView.Listener l) {
        mListener = l;
        if (mSingCountDownView != null) {
            mSingCountDownView.setListener(mListener);
        }
    }

}
