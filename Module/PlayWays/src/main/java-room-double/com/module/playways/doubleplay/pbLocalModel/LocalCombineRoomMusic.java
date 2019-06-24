package com.module.playways.doubleplay.pbLocalModel;

import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.CombineRoom.CombineRoomMusic;

import java.io.Serializable;

public class LocalCombineRoomMusic implements Serializable {
    SongModel music; //当前歌曲
    int userID; //点歌用户id
    String uniqTag;

    public SongModel getMusic() {
        return music;
    }

    public void setMusic(SongModel music) {
        this.music = music;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUniqTag() {
        return uniqTag;
    }

    public void setUniqTag(String uniqTag) {
        this.uniqTag = uniqTag;
    }

    public LocalCombineRoomMusic() {
    }

    public LocalCombineRoomMusic(CombineRoomMusic combineRoomMusic) {
        this.music = new SongModel();
        this.music.parse(combineRoomMusic.getMusic());
        this.userID = combineRoomMusic.getUserID();
        this.uniqTag = combineRoomMusic.getUniqTag();
    }
}
