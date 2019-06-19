package com.module.playways.doubleplay.pbLocalModel;

import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.CombineRoom.CombineRoomMusic;
import com.zq.live.proto.Common.MusicInfo;

public class LocalCombineRoomMusic {
    SongModel music; //当前歌曲
    int userID; //点歌用户id

    public LocalCombineRoomMusic(CombineRoomMusic combineRoomMusic) {
        this.music = new SongModel();
        this.music.parse(combineRoomMusic.getMusic());
        this.userID = combineRoomMusic.getUserID();
    }
}
