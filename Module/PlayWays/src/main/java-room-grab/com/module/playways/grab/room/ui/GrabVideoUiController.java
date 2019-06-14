package com.module.playways.grab.room.ui;


import android.view.View;

import com.module.playways.grab.room.model.GrabRoundInfoModel;

public class GrabVideoUiController extends GrabBaseUiController {
    public GrabVideoUiController(GrabRoomFragment f) {
        super(f);
    }

    @Override
    public void singBySelf() {
        GrabRoundInfoModel infoModel = mF.mRoomData.getRealRoundInfo();
        if (infoModel != null) {
            // 显示歌词view
            mF.mGrabVideoSelfSingCardView.playLyric();
            if(infoModel.isNormalRound()){
                // 普通轮次
                mF.mGrabVideoDisplayView.bindVideoStream(infoModel.getUserID());
            }else if(infoModel.isPKRound()){
                if(infoModel.getsPkRoundInfoModels().size()>=2){
                    int userID1 = infoModel.getsPkRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getsPkRoundInfoModels().get(1).getUserID();
                    mF.mGrabVideoDisplayView.bindVideoStream(userID1,userID2);
                }
            }else if(infoModel.isChorusRound()){
                if(infoModel.getChorusRoundInfoModels().size()>=2){
                    int userID1 = infoModel.getChorusRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getChorusRoundInfoModels().get(1).getUserID();
                    mF.mGrabVideoDisplayView.bindVideoStream(userID1,userID2);
                }
            }else if(infoModel.isMiniGameRound()){
                if(infoModel.getMINIGameRoundInfoModels().size()>=2){
                    int userID1 = infoModel.getMINIGameRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getMINIGameRoundInfoModels().get(1).getUserID();
                    mF.mGrabVideoDisplayView.bindVideoStream(userID1,userID2);
                }
            }
        }
        // ZqEngineKit.getInstance().startPreview(mF.mMainVideoView);




    }

    @Override
    public void singByOthers() {

    }
}
