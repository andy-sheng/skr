package com.engine.agora;

/**
 * 对外业务需要知道的接口
 */
public interface AgoraOutCallback {

    void onUserJoined(int uid, int elapsed);

    void onUserOffline(int uid, int reason);

    void onUserMuteVideo(int uid, boolean muted);

    void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed);
}
