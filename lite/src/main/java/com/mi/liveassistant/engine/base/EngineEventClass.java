package com.mi.liveassistant.engine.base;

/**
 * Created by chenyong on 2017/1/22.
 */

public class EngineEventClass {

    public static class StreamerEvent {
        public int type;
        public Object obj;
        public static final int EVENT_TYPE_OPEN_STREAM_SUCC = 1;
        public static final int EVENT_TYPE_OPEN_CAMERA_FAILED = 2;
        public static final int EVENT_TYPE_OPEN_MIC_FAILED = 3;
        public static final int EVENT_TYPE_NEED_RECONNECT = 4;
        public static final int EVENT_TYPE_ERROR = 5;
        public static final int EVENT_TYPE_ON_STREAM_PUBLISHED = 6;
        public static final int EVENT_TYPE_ON_STREAM_CLOSED = 7;

        public StreamerEvent(int type) {
            this.type = type;
        }

        public StreamerEvent(int type, Object obj) {
            this.type = type;
            this.obj = obj;
        }
    }

    public static class LocalBindEvent {

    }

    public static class ConferenceCallBackEvent {
        public int type;
        public Object retObj;

        public static final int TYPE_ON_LOAD = 0;
        public static final int TYPE_ON_JOIN = 1;
        public static final int TYPE_ON_LEAVE = 2;
        public static final int TYPE_ON_ERROR = 3;
        public static final int TYPE_ON_CALL_END = 4;
        public static final int TYPE_ON_REMOTE_STREAM_CREATED = 5;
        public static final int TYPE_ON_REMOTE_STREAM_ARRIVED = 6;
        public static final int TYPE_ON_REMOTE_STREAM_REMOVED = 7;
        public static final int TYPE_ON_START_CAMERA = 8;
        public static final int TYPE_ON_STOP_CAMERA = 9;
        public static final int TYPE_ON_LOCAL_STREAM_ACTIVE = 10;
        public static final int TYPE_ON_LOCAL_STREAM_DEACTIVE = 11;
        public static final int TYPE_ON_SPEAKER_REPORT = 12;
        public static final int TYPE_ON_STREAM_REMOVED_ALL = 13;
        public static final int TYPE_ON_REMOTE_STREAM_CREATED_FORCE = 14;
        public static final int TYPE_PLAY_TONE = 15;
        public static final int TYPE_CONNECTION_LOST = 16;
        public static final int TYPE_RECONNECT = 17;
        public static final int TYPE_USER_OFFLINE = 18;
        public static final int TYPE_USER_ONLINE = 19;
        public static final int TYPE_ON_AUDIO_MUTED = 20;
        public static final int TYPE_ON_AUDIO_UNMUTED = 21;
        public static final int TYPE_ON_REMOTE_RESIZE = 22;

        public ConferenceCallBackEvent(int type, Object retObj) {
            this.type = type;
            this.retObj = retObj;
        }
    }
}
