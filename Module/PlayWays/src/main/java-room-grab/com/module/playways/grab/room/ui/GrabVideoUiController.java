package com.module.playways.grab.room.ui;

import android.util.Pair;
import android.view.View;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.zq.live.proto.Room.EQRoundStatus;

public class GrabVideoUiController extends GrabBaseUiController {
    public GrabVideoUiController(GrabRoomFragment f) {
        super(f);
    }

    @Override
    public void grabBegin() {
        mF.mGrabVideoDisplayView.reset();
        mF.mGrabVideoDisplayView.setVisibility(View.GONE);
    }

    @Override
    public void singBySelf() {
        GrabRoundInfoModel infoModel = mF.mRoomData.getRealRoundInfo();
        if (infoModel != null) {
            // 显示歌词view
            mF.mGrabVideoSelfSingCardView.playLyric();
            if (infoModel.isNormalRound()) {
                // 普通轮次
                UserInfoModel userInfoModel = getUserInfoModel(infoModel.getUserID());
                mF.mGrabVideoDisplayView.bindVideoStream(userInfoModel);
                mF.mGrabTopContentView.setVisibility(View.GONE);
                mF.mGrabTopOpView.setVisibility(View.GONE);
                mF.mGrabVideoDisplayView.setTranslateY(0);
                mF.mGrabVideoDisplayView.adjustViewPostionWhenSolo();
            } else if (infoModel.isChorusRound()) {
                if (infoModel.getChorusRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getChorusRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getChorusRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.mGrabVideoDisplayView.bindVideoStream(p.first, p.second, 0);
                    mF.mGrabTopContentView.setVisibility(View.GONE);
                }
                mF.mGrabWidgetAnimationController.openBelowLyricView();
            } else if (infoModel.isMiniGameRound()) {
                if (infoModel.getMINIGameRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getMINIGameRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getMINIGameRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.mGrabVideoDisplayView.bindVideoStream(p.first, p.second, 0);
                    mF.mGrabTopContentView.setVisibility(View.GONE);
                }
                mF.mGrabWidgetAnimationController.openBelowLyricView();
            } else if (infoModel.isPKRound()) {
                if (infoModel.getsPkRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getsPkRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getsPkRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.mGrabTopContentView.setVisibility(View.GONE);
                    if (infoModel.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
                        mF.mGrabVideoDisplayView.bindVideoStream(p.first, p.second, 1);
                    } else if (infoModel.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
                        mF.mGrabVideoDisplayView.bindVideoStream(p.first, p.second, 2);
                    }
                    mF.mGrabVideoDisplayView.setMarginTop(0);
                }
                mF.mGrabWidgetAnimationController.openBelowLyricView();
            }

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

    UserInfoModel getUserInfoModel(int userID) {
        UserInfoModel userInfoModel1 = mF.mRoomData.getUserInfo(userID);
        if (userInfoModel1 == null) {
            userInfoModel1 = new UserInfoModel(userID);
        }
        return userInfoModel1;
    }

    @Override
    public void singByOthers() {
        GrabRoundInfoModel infoModel = mF.mRoomData.getRealRoundInfo();
        if (infoModel != null) {
            // 显示歌词view
            int halfVideoMarginTop = U.getDisplayUtils().dip2px(82);
            if (infoModel.isNormalRound()) {
                // 普通轮次
                UserInfoModel userInfoModel = getUserInfoModel(infoModel.getUserID());
                mF.mGrabVideoDisplayView.bindVideoStream(userInfoModel);
                mF.mGrabVideoDisplayView.setMarginTop(0);
            } else if (infoModel.isChorusRound()) {
                if (infoModel.getChorusRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getChorusRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getChorusRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.mGrabVideoDisplayView.bindVideoStream(p.first, p.second, 0);
                    mF.mGrabVideoDisplayView.setMarginTop(halfVideoMarginTop);
                }
            } else if (infoModel.isMiniGameRound()) {
                if (infoModel.getMINIGameRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getMINIGameRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getMINIGameRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.mGrabVideoDisplayView.bindVideoStream(p.first, p.second, 0);
                    mF.mGrabVideoDisplayView.setMarginTop(halfVideoMarginTop);
                }
            } else if (infoModel.isPKRound()) {
                if (infoModel.getsPkRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getsPkRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getsPkRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    if (infoModel.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
                        //pk第一轮
                        mF.mGrabVideoDisplayView.bindVideoStream(p.first, p.second, 1);
                    } else if (infoModel.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
                        //pk第二轮
                        if (infoModel.isEnterInSingStatus() && !infoModel.isParticipant()) {
                            // 是一个刚进来的用户
                            mF.mGrabVideoDisplayView.bindVideoStream(p.first, p.second, 0);
                        } else {
                            mF.mGrabVideoDisplayView.bindVideoStream(p.first, p.second, 2);
                        }
                    }
                    mF.mGrabVideoDisplayView.setMarginTop(halfVideoMarginTop);
                    //可能是pk第一个人 手动visible一下
                    mF.mGrabTopContentView.setVisibility(View.VISIBLE);
                }
            }
            /**
             * 动画会做些歌词view的隐藏
             */
            if (mF.mGrabWidgetAnimationController.isOpen()) {
                // 重新调整下位置
                mF.mGrabWidgetAnimationController.openBelowOpView();
            } else {
                mF.mGrabVideoDisplayView.adjustViewPostion(false, mF.mGrabTopContentView.getVisibility() == View.VISIBLE);
            }
        }
    }

    @Override
    public void roundOver() {
        // 轮次结束了
        mF.mGrabVideoDisplayView.reset();
        mF.mGrabVideoDisplayView.setVisibility(View.GONE);
        mF.mGrabVideoSelfSingCardView.setVisibility(View.GONE);
        mF.mGrabTopContentView.setVisibility(View.VISIBLE);
        if (mF.mGrabWidgetAnimationController.isOpen()){
            if(mF.mGrabWidgetAnimationController.getOpenType() == GrabWidgetAnimationController.OPEN_TYPE_FOR_LYRIC){
                mF.mGrabWidgetAnimationController.close();//关闭
            }else{
                mF.mGrabTopOpView.setVisibility(View.VISIBLE);
            }
        }
        mF.mGrabWidgetAnimationController.setOpenType(GrabWidgetAnimationController.OPEN_TYPE_FOR_NORMAL);
    }

    @Override
    public void destroy() {
        if (mF.mGrabVideoSelfSingCardView != null) {
            mF.mGrabVideoSelfSingCardView.destroy();
//            mF.mGrabVideoDisplayView.destroy();
        }
    }

    @Override
    public void stopWork() {
        roundOver();
    }
}
