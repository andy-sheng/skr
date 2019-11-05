package com.engine.agora;

import android.graphics.Rect;

import com.common.log.MyLog;
import com.engine.statistics.SDataManager;
import com.engine.statistics.datastruct.SAgoraUserEvent;


import io.agora.rtc.IRtcEngineEventHandlerEx;

public class AgoraEngineCallbackWithLog extends IRtcEngineEventHandlerEx {
    private String TAG;

    // 比较不重要的log
    boolean vLogShow = false;

    public AgoraEngineCallbackWithLog(String tag) {
        super();
        TAG = tag;
        MyLog.d(TAG, "AgoraEngineCallbackWithLog");
    }

    @Override
    public void onAudioTransportQuality(int uid, int bitrate, short delay, short lost) {
        MyLog.d(TAG, "onAudioTransportQuality" + " uid=" + uid + " bitrate=" + bitrate + " delay=" + delay + " lost=" + lost);
        super.onAudioTransportQuality(uid, bitrate, delay, lost);
    }

    @Override
    public void onVideoTransportQuality(int uid, int bitrate, short delay, short lost) {
        MyLog.d(TAG, "onVideoTransportQuality" + " uid=" + uid + " bitrate=" + bitrate + " delay=" + delay + " lost=" + lost);
        super.onVideoTransportQuality(uid, bitrate, delay, lost);
    }

    @Override
    public void onRecap(byte[] recap) {
        MyLog.d(TAG, "onRecap" + " recap=" + recap);
        super.onRecap(recap);
    }

    @Override
    public void onWarning(int warn) {
        MyLog.d(TAG, "onWarning" + " warn=" + warn);
        super.onWarning(warn);
    }

    @Override
    public void onError(int err) {
        MyLog.d(TAG, "onError" + " err=" + err);
        super.onError(err);
    }

    @Override
    public void onApiCallExecuted(int error, String api, String result) {
        if(vLogShow) {
            MyLog.d(TAG, "onApiCallExecuted" + " error=" + error + " api=" + api + " result=" + result);
        }
        super.onApiCallExecuted(error, api, result);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        SDataManager.getInstance().setChannelID(channel).setUserID(uid).setChannelJoinElipse(elapsed);
        super.onJoinChannelSuccess(channel, uid, elapsed);
    }

    @Override
    public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
        SDataManager.getInstance().setChannelID(channel).setUserID(uid).setChannelJoinElipse(elapsed);
        super.onRejoinChannelSuccess(channel, uid, elapsed);
    }

    @Override
    public void onLeaveChannel(RtcStats stats) {
        MyLog.w(TAG, "onLeaveChannel" + " stats=" + stats);
        super.onLeaveChannel(stats);
    }

    @Override
    public void onClientRoleChanged(int oldRole, int newRole) {
        SAgoraUserEvent e = SAgoraUserEvent.clientRoleChanged(oldRole, newRole);
        SDataManager.getInstance().getAgoraDataHolder().addUserEvent(e);

        super.onClientRoleChanged(oldRole, newRole);
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        SAgoraUserEvent e = SAgoraUserEvent.remoteJoin(uid, elapsed);
        SDataManager.getInstance().getAgoraDataHolder().addUserEvent(e);

        super.onUserJoined(uid, elapsed);
    }

    @Override
    public void onUserOffline(int uid, int reason) {
//        MyLog.w(TAG, "onUserOffline" + " uid=" + uid + " reason=" + reason);

        SAgoraUserEvent e = SAgoraUserEvent.remoteOffline(uid, reason);
        SDataManager.getInstance().getAgoraDataHolder().addUserEvent(e);

        super.onUserOffline(uid, reason);
    }

    @Override
    public void onLastmileQuality(int quality) {
        MyLog.d(TAG, "onLastmileQuality" + " quality=" + quality);
        super.onLastmileQuality(quality);
    }

    @Override
    public void onConnectionInterrupted() {
        MyLog.d(TAG, "onConnectionInterrupted");
        super.onConnectionInterrupted();
    }

    @Override
    public void onConnectionLost() {
        MyLog.d(TAG, "onConnectionLost");
        super.onConnectionLost();
    }

    @Override
    public void onConnectionBanned() {
        MyLog.d(TAG, "onConnectionBanned");
        super.onConnectionBanned();
    }

    @Override
    public void onTokenPrivilegeWillExpire(String token) {
        MyLog.d(TAG, "onTokenPrivilegeWillExpire" + " token=" + token);
        super.onTokenPrivilegeWillExpire(token);
    }

    @Override
    public void onRequestToken() {
        MyLog.d(TAG, "onRequestToken");
        super.onRequestToken();
    }

    @Override
    public void onMicrophoneEnabled(boolean enabled) {
        MyLog.d(TAG, "onMicrophoneEnabled" + " enabled=" + enabled);
        super.onMicrophoneEnabled(enabled);
    }

    @Override
    public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
        if (vLogShow) {
            MyLog.d(TAG, "onAudioVolumeIndication" + " speakers=" + speakers + " totalVolume=" + totalVolume);
        }
        super.onAudioVolumeIndication(speakers, totalVolume);
    }

    @Override
    public void onActiveSpeaker(int uid) {
        MyLog.d(TAG, "onActiveSpeaker" + " uid=" + uid);
        super.onActiveSpeaker(uid);
    }

    @Override
    public void onFirstLocalAudioFrame(int elapsed) {
        MyLog.d(TAG, "onFirstLocalAudioFrame" + " elapsed=" + elapsed);
        super.onFirstLocalAudioFrame(elapsed);
    }

    @Override
    public void onFirstRemoteAudioFrame(int uid, int elapsed) {
        MyLog.d(TAG, "onFirstRemoteAudioFrame" + " uid=" + uid + " elapsed=" + elapsed);
        super.onFirstRemoteAudioFrame(uid, elapsed);
    }

    @Override
    public void onVideoStopped() {
        MyLog.d(TAG, "onVideoStopped");
        super.onVideoStopped();
    }

    @Override
    public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
        MyLog.d(TAG, "onFirstLocalVideoFrame" + " width=" + width + " height=" + height + " elapsed=" + elapsed);
        super.onFirstLocalVideoFrame(width, height, elapsed);
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
//        MyLog.d(TAG, "onFirstRemoteVideoDecoded" + " uid=" + uid + " width=" + width + " height=" + height + " elapsed=" + elapsed);

        SAgoraUserEvent e = SAgoraUserEvent.firstRemoteVideoDecoded(uid, width, height, elapsed);
        SDataManager.getInstance().getAgoraDataHolder().addUserEvent(e);

        super.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
    }

    @Override
    public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
        MyLog.d(TAG, "onFirstRemoteVideoFrame" + " uid=" + uid + " width=" + width + " height=" + height + " elapsed=" + elapsed);
        super.onFirstRemoteVideoFrame(uid, width, height, elapsed);
    }

    @Override
    public void onUserMuteAudio(int uid, boolean muted) {
//        MyLog.d(TAG, "onUserMuteAudio" + " uid=" + uid + " muted=" + muted);

        SAgoraUserEvent e = SAgoraUserEvent.remoteMuteAudio(uid, muted);
        SDataManager.getInstance().getAgoraDataHolder().addUserEvent(e);
        super.onUserMuteAudio(uid, muted);
    }

    @Override
    public void onUserMuteVideo(int uid, boolean muted) {
//        MyLog.d(TAG, "onUserMuteVideo" + " uid=" + uid + " muted=" + muted);

        SAgoraUserEvent e = SAgoraUserEvent.remoteMuteVicdeo(uid, muted);
        SDataManager.getInstance().getAgoraDataHolder().addUserEvent(e);
        super.onUserMuteVideo(uid, muted);
    }

    @Override
    public void onUserEnableVideo(int uid, boolean enabled) {
//        MyLog.d(TAG, "onUserEnableVideo" + " uid=" + uid + " enabled=" + enabled);

        SAgoraUserEvent e = SAgoraUserEvent.remoteEnableVideo(uid, enabled);
        SDataManager.getInstance().getAgoraDataHolder().addUserEvent(e);
        super.onUserEnableVideo(uid, enabled);
    }

    @Override
    public void onUserEnableLocalVideo(int uid, boolean enabled) {
        MyLog.d(TAG, "onUserEnableLocalVideo" + " uid=" + uid + " enabled=" + enabled);
        super.onUserEnableLocalVideo(uid, enabled);
    }

    @Override
    public void onAudioMixingStateChanged(int state, int errorCode) {
        SAgoraUserEvent e = SAgoraUserEvent.audioMixingStateChange(state, errorCode);
        SDataManager.getInstance().getAgoraDataHolder().addUserEvent(e);
        super.onAudioMixingStateChanged(state, errorCode);
    }

    @Override
    public void onRemoteVideoStateChanged(int uid, int state) {
        MyLog.d(TAG, "onRemoteVideoStateChanged" + " uid=" + uid + " state=" + state);
        super.onRemoteVideoStateChanged(uid, state);
    }

    @Override
    public void onLocalPublishFallbackToAudioOnly(boolean isFallbackOrRecover) {
        MyLog.d(TAG, "onLocalPublishFallbackToAudioOnly" + " isFallbackOrRecover=" + isFallbackOrRecover);
        super.onLocalPublishFallbackToAudioOnly(isFallbackOrRecover);
    }

    @Override
    public void onRemoteSubscribeFallbackToAudioOnly(int uid, boolean isFallbackOrRecover) {
        MyLog.d(TAG, "onRemoteSubscribeFallbackToAudioOnly" + " uid=" + uid + " isFallbackOrRecover=" + isFallbackOrRecover);
        super.onRemoteSubscribeFallbackToAudioOnly(uid, isFallbackOrRecover);
    }

    @Override
    public void onAudioRouteChanged(int routing) {
//        MyLog.d(TAG, "onAudioRouteChanged" + " routing=" + routing);
        SAgoraUserEvent e = SAgoraUserEvent.audioRouteChanged(routing);
        SDataManager.getInstance().getAgoraDataHolder().addUserEvent(e);

        super.onAudioRouteChanged(routing);
    }

    @Override
    public void onCameraReady() {
        MyLog.d(TAG, "onCameraReady");
        super.onCameraReady();
    }

    @Override
    public void onCameraFocusAreaChanged(Rect rect) {
        MyLog.d(TAG, "onCameraFocusAreaChanged" + " rect=" + rect);
        super.onCameraFocusAreaChanged(rect);
    }

    @Override
    public void onAudioQuality(int uid, int quality, short delay, short lost) {
        MyLog.d(TAG, "onAudioQuality" + " uid=" + uid + " quality=" + quality + " delay=" + delay + " lost=" + lost);
        super.onAudioQuality(uid, quality, delay, lost);
    }

    @Override
    public void onRtcStats(RtcStats stats) {
//        if(vLogShow) {
            SDataManager.getInstance().getAgoraDataHolder().addRtcStats(stats);
//        }
        super.onRtcStats(stats);
    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
//        if(vLogShow) {
            SDataManager.getInstance().getAgoraDataHolder().addNetQualityStats(uid, txQuality, rxQuality);
//        }
        super.onNetworkQuality(uid, txQuality, rxQuality);
    }

    @Override
    public void onRemoteAudioStats(RemoteAudioStats stats) {
//        if(vLogShow) {
            SDataManager.getInstance().getAgoraDataHolder().addRemoteAudioStats(stats);
//        }
        super.onRemoteAudioStats(stats);
    }

    @Override
    public void onLocalVideoStats(LocalVideoStats stats) {
        SDataManager.getInstance().getAgoraDataHolder().addLocalVideoStats(stats);
        super.onLocalVideoStats(stats);
    }

    @Override
    public void onRemoteVideoStats(RemoteVideoStats stats) {
        SDataManager.getInstance().getAgoraDataHolder().addRemoteVideoStats(stats);
        super.onRemoteVideoStats(stats);
    }

    @Override
    public void onLocalVideoStat(int sentBitrate, int sentFrameRate) {
        MyLog.d(TAG, "onLocalVideoStat" + " sentBitrate=" + sentBitrate + " sentFrameRate=" + sentFrameRate);
        super.onLocalVideoStat(sentBitrate, sentFrameRate);
    }

    @Override
    public void onRemoteVideoStat(int uid, int delay, int receivedBitrate, int receivedFrameRate) {
        MyLog.d(TAG, "onRemoteVideoStat" + " uid=" + uid + " delay=" + delay + " receivedBitrate=" + receivedBitrate + " receivedFrameRate=" + receivedFrameRate);
        super.onRemoteVideoStat(uid, delay, receivedBitrate, receivedFrameRate);
    }

    @Override
    public void onRemoteAudioTransportStats(int uid, int delay, int lost, int rxKBitRate) {
        SDataManager.getInstance().getAgoraDataHolder().addRemoteAudioTransStats(uid, delay, lost, rxKBitRate);
        super.onRemoteAudioTransportStats(uid, delay, lost, rxKBitRate);
    }

    @Override
    public void onRemoteVideoTransportStats(int uid, int delay, int lost, int rxKBitRate) {
        SDataManager.getInstance().getAgoraDataHolder().addRemoteVideoTransStata(uid, delay, lost, rxKBitRate);
        super.onRemoteVideoTransportStats(uid, delay, lost, rxKBitRate);
    }

    @Override
    public void onAudioMixingFinished() {
        MyLog.d(TAG, "onAudioMixingFinished");
        super.onAudioMixingFinished();
    }

    @Override
    public void onAudioEffectFinished(int soundId) {
        MyLog.d(TAG, "onAudioEffectFinished" + " soundId=" + soundId);
        super.onAudioEffectFinished(soundId);
    }

    @Override
    public void onStreamPublished(String url, int error) {
        MyLog.d(TAG, "onStreamPublished" + " url=" + url + " error=" + error);
        super.onStreamPublished(url, error);
    }

    @Override
    public void onStreamUnpublished(String url) {
        MyLog.d(TAG, "onStreamUnpublished" + " url=" + url);
        super.onStreamUnpublished(url);
    }

    @Override
    public void onTranscodingUpdated() {
        MyLog.d(TAG, "onTranscodingUpdated");
        super.onTranscodingUpdated();
    }

    @Override
    public void onStreamInjectedStatus(String url, int uid, int status) {
        MyLog.d(TAG, "onStreamInjectedStatus" + " url=" + url + " uid=" + uid + " status=" + status);
        super.onStreamInjectedStatus(url, uid, status);
    }

    @Override
    public void onStreamMessage(int uid, int streamId, byte[] data) {
        MyLog.d(TAG, "onStreamMessage" + " uid=" + uid + " streamId=" + streamId + " data=" + data);
        super.onStreamMessage(uid, streamId, data);
    }

    @Override
    public void onStreamMessageError(int uid, int streamId, int error, int missed, int cached) {
        MyLog.d(TAG, "onStreamMessageError" + " uid=" + uid + " streamId=" + streamId + " error=" + error + " missed=" + missed + " cached=" + cached);
        super.onStreamMessageError(uid, streamId, error, missed, cached);
    }

    @Override
    public void onMediaEngineLoadSuccess() {
        MyLog.d(TAG, "onMediaEngineLoadSuccess");
        super.onMediaEngineLoadSuccess();
    }

    @Override
    public void onMediaEngineStartCallSuccess() {
        MyLog.d(TAG, "onMediaEngineStartCallSuccess");
        super.onMediaEngineStartCallSuccess();
    }

    @Override
    public void onVideoSizeChanged(int uid, int width, int height, int rotation) {
//        MyLog.d(TAG, "onVideoSizeChanged" + " uid=" + uid + " width=" + width + " height=" + height + " rotation=" + rotation);

        SAgoraUserEvent e = SAgoraUserEvent.videoSizeChanegd(uid, width, height,rotation);
        SDataManager.getInstance().getAgoraDataHolder().addUserEvent(e);


        super.onVideoSizeChanged(uid, width, height, rotation);
    }
}
