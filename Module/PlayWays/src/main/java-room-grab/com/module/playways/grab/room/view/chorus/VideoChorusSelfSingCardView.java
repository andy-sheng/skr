package com.module.playways.grab.room.view.chorus;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

import com.module.playways.R;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;


/**
 * 合唱的歌唱者看到的板子
 */
public class VideoChorusSelfSingCardView extends BaseChorusSelfCardView {

    public final static String TAG = "VideoChorusSelfSingCardView";

    public VideoChorusSelfSingCardView(Context context) {
        super(context);
    }

    public VideoChorusSelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoChorusSelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void init() {
        inflate(getContext(), R.layout.video_grab_chorus_self_sing_card_layout, this);
        mLyricRecycleView = findViewById(R.id.lyric_recycle_view);
        mLyricRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mChorusSelfLyricAdapter = new ChorusSelfLyricAdapter(mLeft, mRight);
        mLyricRecycleView.setAdapter(mChorusSelfLyricAdapter);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void playLyric() {
        if (mRoomData == null) {
            return;
        }
        mLeft.reset();
        mRight.reset();
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel != null) {
            List<ChorusRoundInfoModel> chorusRoundInfoModelList = infoModel.getChorusRoundInfoModels();
            if (chorusRoundInfoModelList != null && chorusRoundInfoModelList.size() >= 2) {
                int uid1 = chorusRoundInfoModelList.get(0).getUserID();
                int uid2 = chorusRoundInfoModelList.get(1).getUserID();
                mLeft.mUserInfoModel = mRoomData.getUserInfo(uid1);
                mLeft.mChorusRoundInfoModel = chorusRoundInfoModelList.get(0);
                mRight.mUserInfoModel = mRoomData.getUserInfo(uid2);
                mRight.mChorusRoundInfoModel = chorusRoundInfoModelList.get(1);
            }
            mSongModel = infoModel.getMusic();
            playWithNoAcc();
        }
    }
}
