package com.module.playways.grab.room.guide.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Room.EQRoundStatus;
import com.zq.live.proto.Room.EQUserRole;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GrabGuideInfoModel implements Serializable {
    @JSONField(name = "aRoundMusics")
    List<SongModel> aRoundMusics = new ArrayList<>();

    @JSONField(name = "bRoundMusic")
    SongModel bRoundMusics;

    @JSONField(name = "bRoundUserID")
    int bRoundUserID = 0;

    @JSONField(name = "bRoundVoiceURL")
    String bRoundVoiceURL;

    @JSONField(name = "skrers")
    List<UserInfoModel> skrers = new ArrayList<>();

    public List<SongModel> getaRoundMusics() {
        return aRoundMusics;
    }

    public void setaRoundMusics(List<SongModel> aRoundMusics) {
        this.aRoundMusics = aRoundMusics;
    }

    public SongModel getbRoundMusics() {
        return bRoundMusics;
    }

    public void setbRoundMusics(SongModel bRoundMusics) {
        this.bRoundMusics = bRoundMusics;
    }

    public int getbRoundUserID() {
        return bRoundUserID;
    }

    public void setbRoundUserID(int bRoundUserID) {
        this.bRoundUserID = bRoundUserID;
    }

    public String getbRoundVoiceURL() {
        return bRoundVoiceURL;
    }

    public void setbRoundVoiceURL(String bRoundVoiceURL) {
        this.bRoundVoiceURL = bRoundVoiceURL;
    }

    public List<UserInfoModel> getSkrers() {
        return skrers;
    }

    public void setSkrers(List<UserInfoModel> skrers) {
        this.skrers = skrers;
    }

    public GrabRoundInfoModel createARoundInfo() {
        GrabRoundInfoModel grabRoundInfoModel = new GrabRoundInfoModel();
        grabRoundInfoModel.setRoundSeq(1);

        grabRoundInfoModel.updatePlayUsers(getPlayUsers());

        grabRoundInfoModel.setStatus(EQRoundStatus.QRS_INTRO.getValue());
        grabRoundInfoModel.setParticipant(true);
        grabRoundInfoModel.setElapsedTimeMs(0);

        if(getaRoundMusics().size()>0){
            SongModel songModel = getaRoundMusics().get(0);
            grabRoundInfoModel.setMusic(songModel);
        }
        return grabRoundInfoModel;
    }


    public GrabRoundInfoModel createBRoundInfo() {
        GrabRoundInfoModel grabRoundInfoModel = new GrabRoundInfoModel();
        grabRoundInfoModel.setRoundSeq(2);

        grabRoundInfoModel.updatePlayUsers(getPlayUsers());

        grabRoundInfoModel.setStatus(EQRoundStatus.QRS_INTRO.getValue());
        grabRoundInfoModel.setParticipant(true);
        grabRoundInfoModel.setElapsedTimeMs(0);

            grabRoundInfoModel.setMusic(getbRoundMusics());
        return grabRoundInfoModel;
    }

    private List<GrabPlayerInfoModel> getPlayUsers(){
        List<GrabPlayerInfoModel> playerInfoModelList = new ArrayList<>();
        {
            // 把自己添加进去
            GrabPlayerInfoModel playerInfoModel = new GrabPlayerInfoModel();
            UserInfoModel userInfoModel = new UserInfoModel();
            userInfoModel.setAvatar(MyUserInfoManager.getInstance().getAvatar());
            userInfoModel.setUserId((int) MyUserInfoManager.getInstance().getUid());
            userInfoModel.setNickname(MyUserInfoManager.getInstance().getNickName());

            playerInfoModel.setUserID((int) MyUserInfoManager.getInstance().getUid());
            playerInfoModel.setRole(EQUserRole.EQUR_PLAY_USER.getValue());
            playerInfoModel.setUserInfo(userInfoModel);

            playerInfoModelList.add(playerInfoModel);
        }
        for (int i = 0; i < getSkrers().size(); i++) {
            GrabPlayerInfoModel playerInfoModel = new GrabPlayerInfoModel();
            UserInfoModel userInfoModel1 = getSkrers().get(i);

            playerInfoModel.setUserID(userInfoModel1.getUserId());
            playerInfoModel.setRole(EQUserRole.EQUR_PLAY_USER.getValue());
            playerInfoModel.setUserInfo(userInfoModel1);

            playerInfoModelList.add(playerInfoModel);
        }
        return playerInfoModelList;
    }
}
