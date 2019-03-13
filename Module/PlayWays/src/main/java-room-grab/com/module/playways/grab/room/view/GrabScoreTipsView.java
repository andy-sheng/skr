package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.rank.room.model.PkScoreTipMsgModel;
import com.module.playways.rank.room.model.RankGameConfigModel;
import com.module.playways.rank.room.score.bar.ScoreTipsView;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GrabScoreTipsView extends RelativeLayout {

    public final static String TAG = "GrabScoreTipsView";

    GrabRoomData mRoomData;

    public GrabScoreTipsView(Context context) {
        super(context);
        init();
    }

    public GrabScoreTipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabScoreTipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_score_tips_layout, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
    }

    ScoreTipsView.Item mLastItem;

    public void updateScore(int score1, int songLineNum) {
        MyLog.d(TAG, "updateScore" + " score1=" + score1 + " songLineNum=" + songLineNum);
        int score = score1;
        for (int i = 0; i < 1; i++) {
            score = (int) (Math.sqrt(score) * 10);
        }
        ScoreTipsView.Item item = new ScoreTipsView.Item();

        if (score >= 95) {
            item.setLevel(ScoreTipsView.Level.Perfect);
        } else if (score >= 90) {
            item.setLevel(ScoreTipsView.Level.Good);
        } else if (score >= 70) {
            item.setLevel(ScoreTipsView.Level.Ok);
        } else if (score < 20) {
            item.setLevel(ScoreTipsView.Level.Bad);
        }
        if (item.getLevel() != null) {
            if (mLastItem != null && item.getLevel() == mLastItem.getLevel()) {
                item.setNum(mLastItem.getNum() + 1);
            }
            mLastItem = item;
            GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
            if (now != null && now.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                ScoreTipsView.play(this, item, 2);
            } else {
                ScoreTipsView.play(this, item);
            }
        }
    }

    public void reset(){
        mLastItem = null;
    }
}
