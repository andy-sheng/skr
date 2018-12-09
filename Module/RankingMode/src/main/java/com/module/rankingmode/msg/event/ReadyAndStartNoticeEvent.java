package com.module.rankingmode.msg.event;

public class ReadyAndStartNoticeEvent {
    int readyUserID;   //准备用户ID
    long readyTimeMs;  //准备的毫秒时间戳
    long startTimeMs;  //开始的毫秒时间戳
    int firstUserID;   //第一个用户ID
    int firstMusicID; //第一首歌曲ID

    public ReadyAndStartNoticeEvent(int readyUserID, long readyTimeMs,
                                    long startTimeMs, int firstUserID, int firstMusicID) {
        this.readyUserID = readyUserID;
        this.readyTimeMs = readyTimeMs;
        this.startTimeMs = startTimeMs;
        this.firstUserID = firstUserID;
        this.firstMusicID = firstMusicID;
    }
}
