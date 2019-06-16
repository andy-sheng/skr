package com.module.playways.grab.room.ui;

import android.util.Pair;
import android.view.View;

import com.common.callback.Callback;
import com.common.core.userinfo.model.UserInfoModel;
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
            if (infoModel.isNormalRound()) {
                // 普通轮次
                mF.mGrabVideoDisplayView.bindVideoStream(infoModel.getUserID());
            } else if (infoModel.isPKRound()) {
                if (infoModel.getsPkRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getsPkRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getsPkRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.mGrabVideoDisplayView.bindVideoStream(p.first, p.second);
                }
            } else if (infoModel.isChorusRound()) {
                if (infoModel.getChorusRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getChorusRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getChorusRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.mGrabVideoDisplayView.bindVideoStream(p.first, p.second);
                }
            } else if (infoModel.isMiniGameRound()) {
                if (infoModel.getMINIGameRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getMINIGameRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getMINIGameRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.mGrabVideoDisplayView.bindVideoStream(p.first, p.second);
                }
            }
            mF.mGrabWidgetAnimationController.openBelowLyricView();
        }
        // ZqEngineKit.getInstance().startPreview(mF.mMainVideoView);
    }

    Pair<UserInfoModel, UserInfoModel> getUserInfoModel(int userID1, int userID2) {
        UserInfoModel userInfoModel1 = mF.mRoomData.getUserInfo(userID1);
        if (userInfoModel1 == null) {
            userInfoModel1 = new UserInfoModel(userID1);
        }
        UserInfoModel userInfoModel2 = mF.mRoomData.getUserInfo(userID2);
        if (userInfoModel2 == null) {
            userInfoModel2 = new UserInfoModel(userID2);
        }
        return new Pair<>(userInfoModel1, userInfoModel2);
    }

    @Override
    public void singByOthers() {
        GrabRoundInfoModel infoModel = mF.mRoomData.getRealRoundInfo();
        if (infoModel != null) {
            // 显示歌词view
            if (infoModel.isNormalRound()) {
                // 普通轮次
                mF.mGrabVideoDisplayView.bindVideoStream(infoModel.getUserID());
            } else if (infoModel.isPKRound()) {
                if (infoModel.getsPkRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getsPkRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getsPkRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.mGrabVideoDisplayView.bindVideoStream(p.first, p.second);
                }
            } else if (infoModel.isChorusRound()) {
                if (infoModel.getChorusRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getChorusRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getChorusRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.mGrabVideoDisplayView.bindVideoStream(p.first, p.second);
                }
            } else if (infoModel.isMiniGameRound()) {
                if (infoModel.getMINIGameRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getMINIGameRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getMINIGameRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.mGrabVideoDisplayView.bindVideoStream(p.first, p.second);
                }
            }
        }
    }

    @Override
    public void roundOver() {
        // 轮次结束了
        mF.mGrabVideoDisplayView.reset();
        mF.mGrabVideoDisplayView.setVisibility(View.GONE);
        mF.mGrabVideoSelfSingCardView.setVisibility(View.GONE);
        if (mF.mGrabWidgetAnimationController.isOpen() && mF.mGrabWidgetAnimationController.getOpenType() == GrabWidgetAnimationController.OPEN_TYPE_FOR_LYRIC) {
            mF.mGrabWidgetAnimationController.close();//关闭
        }
        mF.mGrabWidgetAnimationController.setOpenType(GrabWidgetAnimationController.OPEN_TYPE_FOR_NORMAL);
    }

    @Override
    public void destroy() {
        if (mF.mGrabVideoSelfSingCardView != null) {
            mF.mGrabVideoSelfSingCardView.destroy();
        }
    }
}
