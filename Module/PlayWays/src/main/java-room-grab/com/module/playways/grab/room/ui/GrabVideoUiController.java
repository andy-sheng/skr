package com.module.playways.grab.room.ui;


import com.module.playways.grab.room.model.GrabRoundInfoModel;

public class GrabVideoUiController extends GrabBaseUiController {
    public GrabVideoUiController(GrabRoomFragment f) {
        super(f);
    }

    @Override
    public void singBySelf() {
        GrabRoundInfoModel infoModel = mF.mRoomData.getRealRoundInfo();
        if (infoModel != null) {
            if(infoModel.isNormalRound()){
                // 普通轮次
                mF.mGrabVideoView.bindVideoStream(infoModel.getUserID());
            }else if(infoModel.isPKRound()){
                if(infoModel.getsPkRoundInfoModels().size()>=2){
                    int userID1 = infoModel.getsPkRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getsPkRoundInfoModels().get(1).getUserID();
                    mF.mGrabVideoView.bindVideoStream(userID1,userID2);
                }
            }else if(infoModel.isChorusRound()){
                if(infoModel.getChorusRoundInfoModels().size()>=2){
                    int userID1 = infoModel.getChorusRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getChorusRoundInfoModels().get(1).getUserID();
                    mF.mGrabVideoView.bindVideoStream(userID1,userID2);
                }
            }else if(infoModel.isMiniGameRound()){
                if(infoModel.getMINIGameRoundInfoModels().size()>=2){
                    int userID1 = infoModel.getMINIGameRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getMINIGameRoundInfoModels().get(1).getUserID();
                    mF.mGrabVideoView.bindVideoStream(userID1,userID2);
                }
            }
        }
        // TODO 绑定视图
        // ZqEngineKit.getInstance().startPreview(mF.mMainVideoView);
    }

    @Override
    public void singByOthers() {

    }
}
