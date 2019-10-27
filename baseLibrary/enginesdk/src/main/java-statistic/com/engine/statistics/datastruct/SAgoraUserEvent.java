package com.engine.statistics.datastruct;


import com.engine.statistics.SUtils;

import static io.agora.rtc.Constants.*;

//单独起一个类，用于记录一系列用户行为事件
public class SAgoraUserEvent
{

    public long ts;//timestamp
    public int type = 0;
    public int uid = -1; //-1 means this client's ID!!!!!
    public Object event;

    private final static  int EVENT_TYPE_NONE = 0;
    public final static int EVENT_TYPE_REMOTE_JOINED        = EVENT_TYPE_NONE + 1;
    public final static int EVENT_TYPE_REMOTE_Mute_Audio    = EVENT_TYPE_NONE + 2;
    public final static int EVENT_TYPE_REMOTE_Offline    = EVENT_TYPE_NONE + 3;
    public final static int EVENT_TYPE_REMOTE_MuteVideo     = EVENT_TYPE_NONE + 4;
    public final static int EVENT_TYPE_REMOTE_EnableVideo     = EVENT_TYPE_NONE + 5;
    public final static int EVENT_TYPE_VideoSizeChanged     = EVENT_TYPE_NONE + 6;
    public final static int EVENT_TYPE_ClientRoleChanged     = EVENT_TYPE_NONE + 7;
    public final static int EVENT_TYPE_FIRST_REMOTE_VIDEO_DECODED = EVENT_TYPE_NONE + 8;
    public final static int EVENT_TYPE_onAudioMixingStateChanged = EVENT_TYPE_NONE + 9;
    public final static int EVENT_TYPE_onAudioRouteChanged = EVENT_TYPE_NONE + 10;
    public final static int EVENT_TYPE_onError          = EVENT_TYPE_NONE + 11;

    private String transEventType2String(int type) {
        switch (type) {
            case EVENT_TYPE_REMOTE_JOINED:
                return "Remote.User.Joined";
            case EVENT_TYPE_REMOTE_Mute_Audio:
                return "Remote.Mute.Audio";
            case EVENT_TYPE_REMOTE_Offline:
                return "Remote.Offline";
            case EVENT_TYPE_REMOTE_MuteVideo:
                return "Remote.Mute.Video";
            case EVENT_TYPE_REMOTE_EnableVideo:
                return "Remote.Enable.Video";
            case EVENT_TYPE_VideoSizeChanged:
                return "Video.Size.Changed";
            case EVENT_TYPE_ClientRoleChanged:
                return "Client.Role.Changed";
            case EVENT_TYPE_FIRST_REMOTE_VIDEO_DECODED:
                return "First.Remote.Video.Decoded";
            case EVENT_TYPE_onAudioMixingStateChanged:
                return "Audio.Mixing.State.Changed";
            case EVENT_TYPE_onAudioRouteChanged:
                return "Audio.Route.Changed";
            case EVENT_TYPE_onError:
                return "Error";
            default:
                return "Unsupported event("+type+")!!!";

        }
    }



    public static SAgoraUserEvent remoteJoin(int uid, int elapsed) {
        SAgoraUserEvent n = new SAgoraUserEvent();
        n.ts = System.currentTimeMillis();
        n.type = EVENT_TYPE_REMOTE_JOINED;
        n.uid = uid;

        n.event = Integer.valueOf(elapsed);
        return n;
    }
    public static SAgoraUserEvent remoteMuteAudio(int uid, boolean muted) {
        SAgoraUserEvent n = new SAgoraUserEvent();
        n.ts = System.currentTimeMillis();
        n.type = EVENT_TYPE_REMOTE_Mute_Audio;
        n.uid = uid;

        n.event = Boolean.valueOf(muted);
        return n;
    }
    public static SAgoraUserEvent remoteOffline(int uid, int reason) {
        SAgoraUserEvent n = new SAgoraUserEvent();
        n.ts = System.currentTimeMillis();
        n.type = EVENT_TYPE_REMOTE_Offline;
        n.uid = uid;

        n.event = Integer.valueOf(reason);
        return n;
    }
    public static SAgoraUserEvent remoteMuteVicdeo(int uid, boolean muted) {
        SAgoraUserEvent n = new SAgoraUserEvent();
        n.ts = System.currentTimeMillis();
        n.type = EVENT_TYPE_REMOTE_MuteVideo;
        n.uid = uid;

        n.event = Boolean.valueOf(muted);
        return n;
    }
    public static SAgoraUserEvent remoteEnableVideo(int uid, boolean enabled) {
        SAgoraUserEvent n = new SAgoraUserEvent();
        n.ts = System.currentTimeMillis();
        n.type = EVENT_TYPE_REMOTE_EnableVideo;
        n.uid = uid;

        n.event = Boolean.valueOf(enabled);
        return n;
    }

    private static class VideoSizeInfo{
        public int w;
        public int h;
        public int rotation;

        public VideoSizeInfo(int w, int h, int rotation) {
            this.w = w;
            this.h = h;
            this.rotation = rotation;
        }

        public String toString() {
            return "w="+w+", h="+h+", rotation="+rotation;
        }
    }

    public static SAgoraUserEvent videoSizeChanegd(int uid, int	width, int height, int rotation) {

        SAgoraUserEvent n = new SAgoraUserEvent();
        n.ts = System.currentTimeMillis();
        n.type = EVENT_TYPE_VideoSizeChanged;
        n.uid = uid;

        n.event = new VideoSizeInfo(width, height, rotation);
        return n;
    }


    private static String transClientRole2String(int role) {
        String retStr = "";
        switch (role) {
            case CLIENT_ROLE_BROADCASTER:
                retStr = "主播";
                break;
            case CLIENT_ROLE_AUDIENCE:
                retStr = "观众";
                break;
            default:
                retStr = "Unknow Role("+role+")";
                break;
        }
        return retStr;
    }

    private static class RolePair{
        public int oldRole;
        public int newRole;

        public RolePair(int oldRole, int newRole) {
            this.oldRole = oldRole;
            this.newRole = newRole;
        }

        public String toString() {
            return "oldRole="+transClientRole2String(oldRole)+", newRole="+transClientRole2String(newRole);
        }
    }

    public static SAgoraUserEvent clientRoleChanged(int oldRole, int newRole) {
        SAgoraUserEvent n = new SAgoraUserEvent();
        n.ts = System.currentTimeMillis();
        n.type = EVENT_TYPE_ClientRoleChanged;
        n.uid = -1;

        n.event = new RolePair(oldRole, newRole);
        return n;
    }





    private static class VideoInfo{
        int w;
        int h;
        int elapsed;

        public VideoInfo(int w, int h, int elapsed) {
            this.w = w;
            this.h = h;
            this.elapsed = elapsed;
        }

        public String toString() {
            return "w=" + w + ", h=" + h + ", elpased="+elapsed/1000+" ms";
        }
    }
    public static SAgoraUserEvent firstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        SAgoraUserEvent n = new SAgoraUserEvent();
        n.ts = System.currentTimeMillis();
        n.type = EVENT_TYPE_FIRST_REMOTE_VIDEO_DECODED;
        n.uid = uid;

        n.event = new VideoInfo(width, height, elapsed);
        return n;
    }

    private static class AudioMixState{
        public int state;
        public int err;

        public AudioMixState(int state, int err) {
            this.state = state;
            this.err = err;
        }

        public String transState2String(int state) {
            switch (state) {
                case MEDIA_ENGINE_AUDIO_EVENT_MIXING_PLAY:
                    return "音乐文件正常播放";
                case MEDIA_ENGINE_AUDIO_EVENT_MIXING_PAUSED:
                    return "音乐文件暂停播放";
                case MEDIA_ENGINE_AUDIO_EVENT_MIXING_STOPPED:
                    return "音乐文件停止播放";
                case MEDIA_ENGINE_AUDIO_EVENT_MIXING_ERROR:
                    return "音乐文件报错";
                default:
                    return "Unknow state("+state+")!!!";

            }
        }

        public String transErr2String(int err) {
            switch (err) {
                case 0:
                    return "正常";
                case MEDIA_ENGINE_AUDIO_ERROR_MIXING_OPEN:
                    return "音乐文件打开出错";
                case MEDIA_ENGINE_AUDIO_ERROR_MIXING_TOO_FREQUENT:
                    return "音乐文件打开太频繁";
                case MEDIA_ENGINE_AUDIO_EVENT_MIXING_INTERRUPTED_EOF:
                    return "音乐文件播放异常中断";
                default :
                    return "Unknow err("+err+")!!!";
            }
        }

        public String toString() {
            return "state("+state+")="+transState2String(state)+", err("+err+")="+transErr2String(err);
        }
    }


    public static SAgoraUserEvent audioMixingStateChange(int state, int errorCode) {
        SAgoraUserEvent n = new SAgoraUserEvent();
        n.ts = System.currentTimeMillis();
        n.type = EVENT_TYPE_onAudioMixingStateChanged;
        n.uid = -1;

        n.event = new AudioMixState(state, errorCode);
        return n;
    }


    public static class AudioRouting{
        public int routine;

        public AudioRouting(int routine) {
            this.routine = routine;
        }

        private String transRoutine2String(int routine) {
            switch (routine) {
                case AUDIO_ROUTE_DEFAULT:
                    return "使用默认的音频路由";
                case AUDIO_ROUTE_HEADSET:
                    return "使用耳机为语音路由";
                case AUDIO_ROUTE_EARPIECE:
                    return "使用听筒为语音路由";
                case AUDIO_ROUTE_HEADSETNOMIC:
                    return "使用不带麦的耳机为语音路由";
                case AUDIO_ROUTE_SPEAKERPHONE:
                    return "使用手机的扬声器为语音路由";
                case AUDIO_ROUTE_LOUDSPEAKER:
                    return "使用外接的扬声器为语音路由";
                case AUDIO_ROUTE_HEADSETBLUETOOTH:
                    return "使用蓝牙耳机为语音路由";
                default:
                    return "Unknow audio routine("+routine+")!!!";
            }
        }
        @Override
        public String toString() {
            return "audioRoute("+routine+")="+transRoutine2String(routine);
        }
    }
    public static SAgoraUserEvent audioRouteChanged(int routing) {
        SAgoraUserEvent n = new SAgoraUserEvent();
        n.ts = System.currentTimeMillis();
        n.type = EVENT_TYPE_onAudioRouteChanged;
        n.uid = -1;

        n.event = new AudioRouting(routing);
        return n;
    }


    //refer to Agora Err list for more info:
    //https://docs.agora.io/cn/Voice/API%20Reference/java/v2.4.1/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_error_code.html
    public static SAgoraUserEvent error(int err) {
        SAgoraUserEvent n = new SAgoraUserEvent();
        n.ts = System.currentTimeMillis();
        n.type = EVENT_TYPE_onError;
        n.uid = -1;

        n.event = Integer.valueOf(err);
        return n;
    }



    private String transOfflineReasonString(int reason) {
        String retStr = "";
        switch(reason) {
            case USER_OFFLINE_QUIT:
                retStr = "用户主动离开";
                break;
            case USER_OFFLINE_DROPPED:
                retStr = "因过长时间收不到对方数据包，超时掉线";
                break;
            case USER_OFFLINE_BECOME_AUDIENCE:
                retStr = "用户身份从主播切换为观众（直播模式下）";
                break;
            default:
                retStr = "Unknow reason("+reason+")";
                break;
        }
        return retStr;
    }




    public String toString() {
        String evenStr = "";
        int us2ms = 1000;

        switch (type) {
            case EVENT_TYPE_REMOTE_JOINED:
                evenStr = "elapsed time="+((Integer)event)/ us2ms+"ms";
                break;
            case EVENT_TYPE_REMOTE_Mute_Audio:
            case EVENT_TYPE_REMOTE_MuteVideo:
            case EVENT_TYPE_REMOTE_EnableVideo:
                evenStr = "Is "+event.toString();
                break;
            case EVENT_TYPE_REMOTE_Offline:
                evenStr = "reasone="+transOfflineReasonString(((Integer)event).intValue());
                break;
            default:
                evenStr = event.toString();
        }


        return SUtils.transTime(ts)+ " SAgoraUserEvent: type("+transEventType2String(type)+"), uid="
                + uid + ", " + evenStr +"\n";
    }


}