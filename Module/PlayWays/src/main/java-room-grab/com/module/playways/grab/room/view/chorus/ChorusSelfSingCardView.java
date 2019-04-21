package com.module.playways.grab.room.view.chorus;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.rank.R;

import java.util.List;

/**
 * 合唱的歌唱者看到的板子
 */
public class ChorusSelfSingCardView extends RelativeLayout {
    public ChorusSelfSingCardView(Context context) {
        super(context);
        init();
    }

    public ChorusSelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChorusSelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_chorus_self_sing_card_layout, this);
    }

    GrabRoomData mRoomData;

    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
    }

    public void playLyric() {
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel != null) {
            List<ChorusRoundInfoModel> chorusRoundInfoModelList = infoModel.getChorusRoundInfoModels();
            if (chorusRoundInfoModelList != null && chorusRoundInfoModelList.size() >= 2) {
                int uid1 = chorusRoundInfoModelList.get(0).getUserID();
                int uid2 = chorusRoundInfoModelList.get(1).getUserID();
                UserInfoModel userInfoModel1 = mRoomData.getUserInfo(uid1);
                UserInfoModel userInfoModel2 = mRoomData.getUserInfo(uid2);
            }
        }

    }
}
