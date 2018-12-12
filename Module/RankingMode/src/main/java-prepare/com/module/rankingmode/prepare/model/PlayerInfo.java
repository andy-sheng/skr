package com.module.rankingmode.prepare.model;

import com.common.core.userinfo.UserInfo;
import com.module.rankingmode.song.model.SongModel;
import com.zq.live.proto.Common.MusicInfo;
ß
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerInfo implements Serializable {

    UserInfo userInfo;
    List<SongModel> songList;

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
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

        this.setUserInfo(UserInfo.loadFrom(playerInfo.getUserInfo()));
        List<SongModel> list = new ArrayList<>();
        for (MusicInfo musicInfo : playerInfo.getMusicInfoList()){
            SongModel songModel = new SongModel();
            songModel.parse(musicInfo);
            list.add(songModel);
        }
        this.setSongList(list);
    }
}
