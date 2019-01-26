package com.module.playways.rank.prepare.model;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.grab.room.event.SomeOneOnlineChangeEvent;
import com.module.playways.rank.song.model.SongModel;
import com.zq.live.proto.Common.MusicInfo;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerInfoModel implements Serializable {
    UserInfoModel userInfo;
    List<SongModel> songList;
    //是否是机器人
    boolean isSkrer;
    List<ResourceInfoModel> resourceInfoList;
    boolean online = true;

    public boolean isSkrer() {
        return isSkrer;
    }

    public List<ResourceInfoModel> getResourceInfoList() {
        return resourceInfoList;
    }

    public UserInfoModel getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfoModel userInfo) {
        this.userInfo = userInfo;
    }

    public List<SongModel> getSongList() {
        return songList;
    }

    public void setSongList(List<SongModel> songList) {
        this.songList = songList;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        if (this.online != online) {
            this.online = online;
            EventBus.getDefault().post(new SomeOneOnlineChangeEvent(this));
        }
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
    }


    @Override
    public String toString() {
        return "PlayerInfo{" +
                "userInfo=" + userInfo +
                ", songList=" + songList +
                ", isSkrer=" + isSkrer +
                ", resourceInfoList=" + resourceInfoList +
                '}';
    }


}
