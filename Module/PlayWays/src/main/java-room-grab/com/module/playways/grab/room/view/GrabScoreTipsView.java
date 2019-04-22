package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabConfigModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.GrabScoreTipMsgModel;
import com.module.playways.room.room.score.bar.ScoreTipsView;
import com.module.rank.R;

import java.util.List;

public class GrabScoreTipsView extends RelativeLayout {

    public final static String TAG = "GrabScoreTipsView";

    GrabRoomData mRoomData;

    ScoreTipsView.Item mLastItem;

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

    public void updateScore(int score1, int songLineNum) {
        MyLog.d(TAG, "updateScore" + " score1=" + score1 + " songLineNum=" + songLineNum);
        int score = score1;
//        for (int i = 0; i < 1; i++) {
//            score = (int) (Math.sqrt(score) * 10);
//        }

        GrabConfigModel gameConfigModel = mRoomData.getGrabConfigModel();
        ScoreTipsView.Item item = new ScoreTipsView.Item();

        if (gameConfigModel != null) {
            // 总分是这个肯定没错
            List<GrabScoreTipMsgModel> scoreTipMsgModelList = gameConfigModel.getQScoreTipMsg();
            if (scoreTipMsgModelList != null) {
                for (GrabScoreTipMsgModel m : scoreTipMsgModelList) {
                    if (score >= m.getFromScore() && score < m.getToScore()) {
                        // 命中
                        switch (m.getTipType()) {
                            case 0:
                                break;
                            case 1:
                                item.setLevel(ScoreTipsView.Level.Grab_renzhen);
                                break;
                            case 2:
                                item.setLevel(ScoreTipsView.Level.Grab_jiayou);
                                break;
                            case 3:
                                item.setLevel(ScoreTipsView.Level.Grab_bucuo);
                                break;
                            case 4:
                                item.setLevel(ScoreTipsView.Level.Grab_taibang);
                                break;
                            case 5:
                                item.setLevel(ScoreTipsView.Level.Grab_wanmei);
                                break;
                        }
                        break;
                    }
                }
            }
        } else {
            if (score >= 85) {
                item.setLevel(ScoreTipsView.Level.Grab_wanmei);
            } else if (score >= 60) {
                item.setLevel(ScoreTipsView.Level.Grab_taibang);
            } else if (score >= 36) {
                item.setLevel(ScoreTipsView.Level.Grab_bucuo);
            } else if (score < 7) {
                item.setLevel(ScoreTipsView.Level.Grab_jiayou);
            } else if (score >= 0) {
                item.setLevel(ScoreTipsView.Level.Grab_renzhen);
            }
        }
        if (item.getLevel() != null) {
            if (mLastItem != null && item.getLevel() == mLastItem.getLevel()) {
                item.setNum(mLastItem.getNum() + 1);
            }
            mLastItem = item;
            GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
            if (now != null && now.singBySelf()) {
                ScoreTipsView.play(this, item, 1);
            } else {
                ScoreTipsView.play(this, item,1);
            }
        }
    }

    public void reset() {
        mLastItem = null;
    }
}
