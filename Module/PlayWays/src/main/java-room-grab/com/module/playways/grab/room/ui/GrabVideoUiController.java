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
        mF.getMGrabVideoDisplayView().reset();
        mF.getMGrabVideoDisplayView().setVisibility(View.GONE);
    }

    @Override
    public void singBySelf() {
        GrabRoundInfoModel infoModel = mF.getMRoomData().getRealRoundInfo();
        if (infoModel != null) {
            // 显示歌词view
            mF.getMGrabVideoSelfSingCardView().playLyric();
            if (infoModel.isNormalRound()) {
                // 普通轮次
                UserInfoModel userInfoModel = getUserInfoModel(infoModel.getUserID());
                mF.getMGrabVideoDisplayView().bindVideoStream(userInfoModel);
                mF.getMGrabTopContentView().setVisibility(View.GONE);
                mF.getMGrabTopOpView().setVisibility(View.GONE);
                mF.getMGrabVideoDisplayView().setTranslateY(0);
                mF.getMGrabVideoDisplayView().adjustViewPostionWhenSolo();
            } else if (infoModel.isChorusRound()) {
                if (infoModel.getChorusRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getChorusRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getChorusRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.getMGrabVideoDisplayView().bindVideoStream(p.first, p.second, 0);
                    mF.getMGrabTopContentView().setVisibility(View.GONE);
                }
                mF.getMGrabWidgetAnimationController().openBelowLyricView();
            } else if (infoModel.isMiniGameRound()) {
                if (infoModel.getMINIGameRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getMINIGameRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getMINIGameRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.getMGrabVideoDisplayView().bindVideoStream(p.first, p.second, 0);
                    mF.getMGrabTopContentView().setVisibility(View.GONE);
                }
                mF.getMGrabWidgetAnimationController().openBelowLyricView();
            } else if (infoModel.isPKRound()) {
                if (infoModel.getsPkRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getsPkRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getsPkRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.getMGrabTopContentView().setVisibility(View.GONE);
                    if (infoModel.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
                        mF.getMGrabVideoDisplayView().bindVideoStream(p.first, p.second, 1);
                    } else if (infoModel.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
                        mF.getMGrabVideoDisplayView().bindVideoStream(p.first, p.second, 2);
                    }
                    mF.getMGrabVideoDisplayView().setMarginTop(0);
                }
                mF.getMGrabWidgetAnimationController().openBelowLyricView();
            }

        }
        // ZqEngineKit.getInstance().startPreview(mF.mMainVideoView);
    }

    Pair<UserInfoModel, UserInfoModel> getUserInfoModel(int userID1, int userID2) {
        UserInfoModel userInfoModel1 = mF.getMRoomData().getUserInfo(userID1);
        if (userInfoModel1 == null) {
            userInfoModel1 = new UserInfoModel(userID1);
        }
        UserInfoModel userInfoModel2 = mF.getMRoomData().getUserInfo(userID2);
        if (userInfoModel2 == null) {
            userInfoModel2 = new UserInfoModel(userID2);
        }
        return new Pair<>(userInfoModel1, userInfoModel2);
    }

    UserInfoModel getUserInfoModel(int userID) {
        UserInfoModel userInfoModel1 = mF.getMRoomData().getUserInfo(userID);
        if (userInfoModel1 == null) {
            userInfoModel1 = new UserInfoModel(userID);
        }
        return userInfoModel1;
    }

    @Override
    public void singByOthers() {
        GrabRoundInfoModel infoModel = mF.getMRoomData().getRealRoundInfo();
        if (infoModel != null) {
            // 显示歌词view
            int halfVideoMarginTop = U.getDisplayUtils().dip2px(82);
            if (infoModel.isNormalRound()) {
                // 普通轮次
                UserInfoModel userInfoModel = getUserInfoModel(infoModel.getUserID());
                mF.getMGrabVideoDisplayView().bindVideoStream(userInfoModel);
                mF.getMGrabVideoDisplayView().setMarginTop(0);
            } else if (infoModel.isChorusRound()) {
                if (infoModel.getChorusRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getChorusRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getChorusRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.getMGrabVideoDisplayView().bindVideoStream(p.first, p.second, 0);
                    mF.getMGrabVideoDisplayView().setMarginTop(halfVideoMarginTop);
                }
            } else if (infoModel.isMiniGameRound()) {
                if (infoModel.getMINIGameRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getMINIGameRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getMINIGameRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    mF.getMGrabVideoDisplayView().bindVideoStream(p.first, p.second, 0);
                    mF.getMGrabVideoDisplayView().setMarginTop(halfVideoMarginTop);
                }
            } else if (infoModel.isPKRound()) {
                if (infoModel.getsPkRoundInfoModels().size() >= 2) {
                    int userID1 = infoModel.getsPkRoundInfoModels().get(0).getUserID();
                    int userID2 = infoModel.getsPkRoundInfoModels().get(1).getUserID();
                    Pair<UserInfoModel, UserInfoModel> p = getUserInfoModel(userID1, userID2);
                    if (infoModel.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
                        //pk第一轮
                        mF.getMGrabVideoDisplayView().bindVideoStream(p.first, p.second, 1);
                    } else if (infoModel.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
                        //pk第二轮
                        if (infoModel.isEnterInSingStatus() && !infoModel.isParticipant()) {
                            // 是一个刚进来的用户
                            mF.getMGrabVideoDisplayView().bindVideoStream(p.first, p.second, 0);
                        } else {
                            mF.getMGrabVideoDisplayView().bindVideoStream(p.first, p.second, 2);
                        }
                    }
                    mF.getMGrabVideoDisplayView().setMarginTop(halfVideoMarginTop);
                    //可能是pk第一个人 手动visible一下
                    mF.getMGrabTopContentView().setVisibility(View.VISIBLE);
                }
            }
            /**
             * 动画会做些歌词view的隐藏
             */
            if (mF.getMGrabWidgetAnimationController().isOpen()) {
                // 重新调整下位置
                mF.getMGrabWidgetAnimationController().openBelowOpView();
            } else {
                mF.getMGrabVideoDisplayView().adjustViewPostion(false, mF.getMGrabTopContentView().getVisibility() == View.VISIBLE);
            }
        }
    }

    @Override
    public void roundOver() {
        // 轮次结束了
        mF.getMGrabVideoDisplayView().reset();
        mF.getMGrabVideoDisplayView().setVisibility(View.GONE);
        mF.getMGrabVideoSelfSingCardView().setVisibility(View.GONE);
        mF.getMGrabTopContentView().setVisibility(View.VISIBLE);
        if (mF.getMGrabWidgetAnimationController().isOpen()){
            if(mF.getMGrabWidgetAnimationController().getOpenType() == GrabWidgetAnimationController.OPEN_TYPE_FOR_LYRIC){
                mF.getMGrabWidgetAnimationController().close();//关闭
            }else{
                mF.getMGrabTopOpView().setVisibility(View.VISIBLE);
            }
        }
        mF.getMGrabWidgetAnimationController().setOpenType(GrabWidgetAnimationController.OPEN_TYPE_FOR_NORMAL);
    }

    @Override
    public void destroy() {
        if (mF.getMGrabVideoSelfSingCardView() != null) {
            mF.getMGrabVideoSelfSingCardView().destroy();
//            mF.mGrabVideoDisplayView.destroy();
        }
    }

    @Override
    public void stopWork() {
        roundOver();
    }
}
