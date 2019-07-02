package com.module.playways.grab.room.view.normal;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.common.view.ExViewStub;
import com.module.playways.grab.room.view.GrabRootView;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.grab.room.view.normal.view.SelfSingLyricView;

/**
 * 你的主场景歌词
 */
public class VideoNormalSelfSingCardView extends SelfSingLyricView {
    public final static String TAG = "SelfSingCardView2";

    private GrabRootView mGrabRootView;

    public VideoNormalSelfSingCardView(ViewStub viewStub, GrabRoomData roomData, GrabRootView rootView) {
        super(viewStub, roomData);
        this.mGrabRootView = rootView;
    }

    @Override
    protected void init(View parentView) {
        super.init(parentView);
        mManyLyricsView.setSpaceLineHeight(U.getDisplayUtils().dip2px(10));
        mGrabRootView.addOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mSvlyric.isShown()) {
                    return mSvlyric.onTouchEvent(event);
                }
                return false;
            }
        });
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_video_normal_self_sing_card_stub_layout;
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
