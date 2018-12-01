package com.engine.agora;

import io.agora.rtc.IRtcEngineEventHandler;

/**
 * 对外业务需要知道的接口
 */
public interface AgoraOutCallback {

    void onUserJoined(int uid, int elapsed);

    void onUserOffline(int uid, int reason);

    void onUserMuteVideo(int uid, boolean muted);

    void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed);

    void onJoinChannelSuccess(String channel, int uid, int elapsed);


    void onRejoinChannelSuccess(String channel, int uid, int elapsed);

    void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats);

    void onClientRoleChanged(int oldRole, int newRole);

    void onUserMuteAudio(int uid, boolean muted);

    void onUserEnableVideo(int uid, boolean enabled);

    void onVideoSizeChanged(int uid, int width, int height, int rotation);

    void onAudioMixingFinished();
    
}
