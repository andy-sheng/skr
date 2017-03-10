package com.mi.live.engine.base;

import android.content.Context;
import android.text.TextUtils;

import com.base.utils.Constants;
import com.xiaomi.conferencemanager.ConferenceManager;
import com.xiaomi.conferencemanager.Model.MonitorData;
import com.xiaomi.conferencemanager.callback.ConferenceCallback;

/**
 * Created by chenyong on 2017/2/7.
 */

public enum GalileoConferenceManager {
    INSTANCE;

    private ConferenceManager mConferenceManager;
    private int mInitCount = 0;
    private ConferenceCallback mStreamerCallback;
    private ConferenceCallback mTalkerCallback;

    public void init(Context context, long deviceManager, String userId) {
        if (mInitCount == 0) {
            if (deviceManager == 0 || TextUtils.isEmpty(userId)) {
                return;
            }
            mConferenceManager = new ConferenceManager();
            boolean success = mConferenceManager.init(context, deviceManager, userId, new ConferenceCallback() {
                @Override
                public void onReconnectStatus(int i) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onReconnectStatus(i);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onReconnectStatus(i);
                    }
                }

                @Override
                public void onNetworkStatus(String s, int i, int i1) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onNetworkStatus(s, i, i1);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onNetworkStatus(s, i, i1);
                    }
                }

                @Override
                public void onReceivedRemoteFrameStatus(String s, int i) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onReceivedRemoteFrameStatus(s, i);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onReceivedRemoteFrameStatus(s, i);
                    }
                }

                @Override
                public void onLoad(boolean b) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onLoad(b);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onLoad(b);
                    }
                }

                @Override
                public void onJoin(String s) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onJoin(s);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onJoin(s);
                    }
                }

                @Override
                public void onLeave(String s) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onLeave(s);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onLeave(s);
                    }
                }

                @Override
                public void onError(String s, ConferenceManager.EngineErrorTypeT engineErrorTypeT) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onError(s, engineErrorTypeT);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onError(s, engineErrorTypeT);
                    }
                }

                @Override
                public void onCallEnded() {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onCallEnded();
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onCallEnded();
                    }
                }

                @Override
                public void onRemoteVidStreamCreated(String s) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onRemoteVidStreamCreated(s);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onRemoteVidStreamCreated(s);
                    }
                }

                @Override
                public void onRemoteVidStreamRemoved(String s) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onRemoteVidStreamRemoved(s);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onRemoteVidStreamRemoved(s);
                    }
                }

                @Override
                public void onRemoteVidResize(String s, int i, int i1) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onRemoteVidResize(s, i, i1);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onRemoteVidResize(s, i, i1);
                    }
                }

                @Override
                public void onStartCamera() {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onStartCamera();
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onStartCamera();
                    }
                }

                @Override
                public void onStopCamera() {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onStopCamera();
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onStopCamera();
                    }
                }

                @Override
                public void onLocalVidStreamActive() {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onLocalVidStreamActive();
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onLocalVidStreamActive();
                    }
                }

                @Override
                public void onLocalVidStreamDeactive() {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onLocalVidStreamDeactive();
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onLocalVidStreamDeactive();
                    }
                }

                @Override
                public void onConferenceLeaved() {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onConferenceLeaved();
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onConferenceLeaved();
                    }
                }

                @Override
                public void onConferenceJoined() {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onConferenceJoined();
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onConferenceJoined();
                    }
                }

                @Override
                public void onGetFirstAudioSample() {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onGetFirstAudioSample();
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onGetFirstAudioSample();
                    }
                }

                @Override
                public void onGetFirstVideoSample() {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onGetFirstVideoSample();
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onGetFirstVideoSample();
                    }
                }

                @Override
                public void onAccessServerError(int i) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onAccessServerError(i);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onAccessServerError(i);
                    }
                }

                @Override
                public void OnSelectionChanged(String[] strings) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.OnSelectionChanged(strings);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.OnSelectionChanged(strings);
                    }
                }

                @Override
                public void onGetBestConnectionTime(int i, int i1, MonitorData.Type type) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onGetBestConnectionTime(i, i1, type);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onGetBestConnectionTime(i, i1, type);
                    }
                }

                @Override
                public void onGetSpeekerDetect(String[] strings) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onGetSpeekerDetect(strings);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onGetSpeekerDetect(strings);
                    }
                }

                @Override
                public void onReflectorDown() {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onReflectorDown();
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onReflectorDown();
                    }
                }

                @Override
                public void onReportTraffic(int i) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onReportTraffic(i);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onReportTraffic(i);
                    }
                }

                @Override
                public void onScreamChange(int i) {
                    if (mStreamerCallback != null) {
                        mStreamerCallback.onScreamChange(i);
                    }
                    if (mTalkerCallback != null) {
                        mTalkerCallback.onScreamChange(i);
                    }
                }
            }, Constants.MILINK_APP_ID);
            if (!success) {
                mConferenceManager = null;
                return;
            }
        }
        mInitCount++;
    }

    public void setStreamerConferenceCallback(ConferenceCallback callback) {
        mStreamerCallback = callback;
    }

    public void setTalkerConferenceCallback(ConferenceCallback callback) {
        mTalkerCallback = callback;
    }

    public void destroy() {
        if (mInitCount == 0) {
            return;
        }
        if (mInitCount == 1) {
            mConferenceManager.leaveRoom();
            mConferenceManager.stopVideo();
            mConferenceManager.destroy();
            mConferenceManager = null;
            mStreamerCallback = null;
            mTalkerCallback = null;
        }
        mInitCount--;
    }

    public ConferenceManager getConferenceManager() {
        return mConferenceManager;
    }
}
