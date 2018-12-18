package com.engine;

import io.agora.rtc.IRtcEngineEventHandler;

public class EngineEvent {
    public static final int TYPE_USER_JOIN = 1;
    public static final int TYPE_USER_LEAVE = 2;
    public static final int TYPE_FIRST_VIDEO_DECODED = 3;
    public static final int TYPE_USER_MUTE_AUDIO = 4;
    public static final int TYPE_USER_MUTE_VIDEO = 5;
    public static final int TYPE_USER_REJOIN = 6;
    public static final int TYPE_USER_ROLE_CHANGE = 7;
    public static final int TYPE_USER_VIDEO_ENABLE = 8;
    public static final int TYPE_USER_AUDIO_VOLUME_INDICATION = 9;

    public static final int TYPE_MUSIC_PLAY_START = 10;// 伴奏开始
    public static final int TYPE_MUSIC_PLAY_PAUSE = 11;// 伴奏暂停
    public static final int TYPE_MUSIC_PLAY_STOP = 12;// 伴奏停止
    public static final int TYPE_MUSIC_PLAY_FINISH = 13;// 伴奏结束

    public static final int TYPE_MUSIC_PLAY_TIME_FLY_LISTENER = 14;// 伴奏时间流逝

    public static final int TYPE_ENGINE_DESTROY = 99;

    int type;
    UserStatus userStatus;
    Object obj;

    public EngineEvent(int type) {
        this.type = type;
    }

    public EngineEvent(int type, UserStatus userStatus) {
        this.type = type;
        this.userStatus = userStatus;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public static class UserVolumeInfo {
        int uid;
        int volume;

        public UserVolumeInfo(int uid, int volume) {
            this.uid = uid;
            this.volume = volume;
        }

        public int getUid() {
            return uid;
        }

        public void setUid(int uid) {
            this.uid = uid;
        }

        public int getVolume() {
            return volume;
        }

        public void setVolume(int volume) {
            this.volume = volume;
        }
    }

    public static class MixMusicTimeInfo {
        int current;
        int duration;

        public MixMusicTimeInfo(int current, int duration) {
            this.current = current;
            this.duration = duration;
        }

        public int getCurrent() {
            return current;
        }

        public void setCurrent(int current) {
            this.current = current;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }
    }

    @Override
    public String toString() {
        return "EngineEvent{" +
                "type=" + type +
                '}';
    }
}
