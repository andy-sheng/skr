package com.module.playways.grab.room.view.chorus;

import android.view.View;
import android.view.ViewStub;

import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.chorus.BaseChorusSelfCardView;

import java.util.List;


/**
 * 合唱的歌唱者看到的板子
 */
public class VideoChorusSelfSingCardView extends BaseChorusSelfCardView {

    public final static String TAG = "VideoChorusSelfSingCardView";

    public VideoChorusSelfSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub, roomData);
    }

    @Override
    protected void init(View parentView) {
        super.init(parentView);
    }

    public void playLyric() {
        if (mRoomData == null) {
            return;
        }
        tryInflate();
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
