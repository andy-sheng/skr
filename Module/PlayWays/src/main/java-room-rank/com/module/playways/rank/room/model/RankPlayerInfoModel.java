package com.module.playways.rank.room.model;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.rank.prepare.model.DataUtils;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.ResourceInfoModel;
import com.module.playways.rank.song.model.SongModel;
import com.zq.live.proto.Common.MusicInfo;

import java.util.ArrayList;
import java.util.List;

public class RankPlayerInfoModel extends PlayerInfoModel {
    protected List<SongModel> songList;
    protected boolean isAI;//是否是AI裁判
    protected List<ResourceInfoModel> resourceInfoList;

    public List<SongModel> getSongList() {
        return songList;
    }

    public void setSongList(List<SongModel> songList) {
        this.songList = songList;
    }

    public boolean isAI() {
        return isAI;
    }

    public void setAI(boolean AI) {
        isAI = AI;
    }

    public List<ResourceInfoModel> getResourceInfoList() {
        return resourceInfoList;
    }

    public void setResourceInfoList(List<ResourceInfoModel> resourceInfoList) {
        this.resourceInfoList = resourceInfoList;
    }

    public void parse(com.zq.live.proto.Room.PlayerInfo playerInfo) {
        if (playerInfo == null) {
            return;
        }
        UserInfoModel userInfo = DataUtils.parse2UserInfo(playerInfo.getUserInfo());
        this.setUserInfo(userInfo);
        List<SongModel> list = new ArrayList<>();
        for (MusicInfo musicInfo : playerInfo.getMusicInfoList()) {
            SongModel songModel = new SongModel();
            songModel.parse(musicInfo);
            list.add(songModel);
        }
        this.setSongList(list);
        this.isSkrer = playerInfo.getIsSkrer();
        this.resourceInfoList = ResourceInfoModel.parse(playerInfo.getResourceList());
        this.isAI = playerInfo.getIsAIUser();
    }
}
