package com.mi.liveassistant.room;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.data.Location;
import com.mi.liveassistant.proto.LiveCommonProto;
import com.mi.liveassistant.room.callback.ICallback;
import com.mi.liveassistant.room.heartbeat.HeartbeatManager;
import com.mi.liveassistant.room.presenter.LivePresenter;
import com.mi.liveassistant.room.streamer.StreamerPresenter;
import com.mi.liveassistant.room.view.ILiveView;

import java.util.List;

/**
 * Created by lan on 17/4/20.
 */
public abstract class BaseLiveManager implements ILiveView {
    protected final String TAG = getTAG();

    protected LivePresenter mLivePresenter;
    protected String mLiveId;

    protected boolean mIsGameLive = false;
    protected boolean mIsPaused = false;

    protected ICallback mOutBeginCallback;
    protected ICallback mOutEndCallback;

    protected StreamerPresenter mStreamerPresenter;
    protected boolean mIsRecording;

    protected HeartbeatManager mHeartbeatManager;

    protected BaseLiveManager() {
        mLivePresenter = new LivePresenter(this);
    }

    public String getTAG() {
        return RoomConstant.LOG_PREFIX + getClass().getSimpleName();
    }

    @Override
    public void beginLive(Location location, String title, String coverUrl, ICallback callback) {
        MyLog.w(TAG, "beginNormalLive");
        mOutBeginCallback = callback;
    }

    @Override
    public void notifyBeginLiveFail(int errCode) {
        MyLog.d(TAG, "notifyBeginLiveFail errCode=" + errCode);
        if (mOutBeginCallback != null) {
            mOutBeginCallback.notifyFail(errCode);
        }
    }

    @Override
    public void notifyBeginLiveSuccess(String liveId, List<LiveCommonProto.UpStreamUrl> upStreamUrlList, String udpUpStreamUrl) {
        MyLog.d(TAG, "notifyBeginLiveSuccess liveId=" + liveId);
        mLiveId = liveId;
        if (mOutBeginCallback != null) {
            mOutBeginCallback.notifySuccess();
        }

        // 开始推流
        if (!mIsRecording) {
            mIsRecording = true;

            if (mStreamerPresenter == null) {
                mStreamerPresenter = new StreamerPresenter();
            }
            mStreamerPresenter.setOriginalStreamUrl(upStreamUrlList, udpUpStreamUrl);
            mStreamerPresenter.startLive();

            if (mHeartbeatManager == null) {
                mHeartbeatManager = new HeartbeatManager(mLiveId, mIsGameLive);
            }
            mHeartbeatManager.start(new HeartbeatManager.ICallback() {
                @Override
                public void notifyTimeout() {
                    endLiveException("heartbeat timeout");
                }
            });
        }
    }

    @Override
    public void endLive(ICallback callback) {
        MyLog.w(TAG, "endLive");
        mOutEndCallback = callback;
        innerEndLive();
    }

    private void endLiveException(String reason) {
        MyLog.w(TAG, "endLiveException reason=" + reason);
        innerEndLive();
    }

    private void innerEndLive() {
        mLivePresenter.endLive(mLiveId);

        // 结束推流
        if (mIsRecording) {
            mStreamerPresenter.stopLive();
            mHeartbeatManager.stop();
            mIsRecording = false;
        }
    }

    @Override
    public void notifyEndLiveFail(int errCode) {
        MyLog.d(TAG, "notifyEndLiveFail errCode=" + errCode);
        if (mOutEndCallback != null) {
            mOutEndCallback.notifyFail(errCode);
        }
    }

    @Override
    public void notifyEndLiveSuccess() {
        MyLog.d(TAG, "notifyEndLiveSuccess");
        if (mOutEndCallback != null) {
            mOutEndCallback.notifySuccess();
        }
    }

    @Override
    public void pause() {
        mIsPaused = true;
        mHeartbeatManager.pause();
    }

    @Override
    public void resume() {
        mIsPaused = false;
        mHeartbeatManager.resume();
    }
}
