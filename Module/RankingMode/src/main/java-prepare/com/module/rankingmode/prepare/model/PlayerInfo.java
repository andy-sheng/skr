package com.module.rankingmode.prepare.model;

import com.module.rankingmode.song.model.SongModel;
import com.zq.live.proto.Common.MusicInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerInfo implements Serializable {

    com.common.core.userinfo.UserInfo userInfo;
    List<SongModel> songList;

    public com.common.core.userinfo.UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(com.common.core.userinfo.UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public List<SongModel> getSongList() {
        return songList;
    }

    public void setSongList(List<SongModel> songList) {
        this.songList = songList;
    }

    public void parse(com.zq.live.proto.Room.PlayerInfo playerInfo){
        if (playerInfo == null){
            return;
        }
        com.common.core.userinfo.UserInfo userInfo = DataUtils.parse2UserInfo(playerInfo.getUserInfo());
        this.setUserInfo(userInfo);
        List<SongModel> list = new ArrayList<>();
        for (MusicInfo musicInfo : playerInfo.getMusicInfoList()){
            SongModel songModel = new SongModel();
            songModel.parse(musicInfo);
            list.add(songModel);
        }
        this.setSongList(list);
    }

    @Override
    public String toString() {
        return "PlayerInfo{" +
                "userInfo=" + userInfo +
                ", songList=" + songList +
                '}';
    }
}
