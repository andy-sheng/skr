package com.mi.liveassistant.room.manager.live;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.data.Location;
import com.mi.liveassistant.proto.LiveCommonProto;
import com.mi.liveassistant.room.RoomConstant;
import com.mi.liveassistant.room.callback.ICallback;
import com.mi.liveassistant.room.heartbeat.HeartbeatManager;
import com.mi.liveassistant.room.presenter.live.BaseLivePresenter;
import com.mi.liveassistant.room.presenter.streamer.StreamerPresenter;
import com.mi.liveassistant.room.view.ILiveView;

import java.util.List;

/**
 * Created by lan on 17/4/20.
 */
public abstract class BaseLiveManager<LP extends BaseLivePresenter> implements ILiveView {
    protected final String TAG = getTAG();

    /*直播开启关闭控制*/
    protected LP mLivePresenter;
    protected String mLiveId;
    /*外部接口的回调*/
    protected ICallback mOutBeginCallback;
    protected ICallback mOutEndCallback;

    protected boolean mIsGameLive = false;
    protected boolean mIsPaused = false;

    /*心跳管理*/
    protected HeartbeatManager mHeartbeatManager;

    /*直播推流控制*/
    protected StreamerPresenter mStreamerPresenter;
    protected boolean mIsRecording;

    protected BaseLiveManager() {
        mStreamerPresenter = new StreamerPresenter();
        // TODO 还是觉的放在startLive初始化比较好
        mHeartbeatManager = new HeartbeatManager();
    }

    protected String getTAG() {
        return RoomConstant.LOG_PREFIX + getClass().getSimpleName();
    }

    @Override
    public void beginLive(Location location, String title, String coverUrl, ICallback callback) {
        MyLog.w(TAG, "beginNormalLive");
        mOutBeginCallback = callback;
        mLivePresenter.beginLive(location, title, coverUrl);
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
            mStreamerPresenter.setOriginalStreamUrl(upStreamUrlList, udpUpStreamUrl);
            if (mIsGameLive) {
                createStreamer();
            }
            startLive();
            mHeartbeatManager.setParam(liveId, mIsGameLive);
            mHeartbeatManager.start(new HeartbeatManager.ICallback() {
                @Override
                public void notifyTimeout() {
                    endLiveException("heartbeat timeout");
                }
            });
        }
    }

    protected abstract void createStreamer();

    protected void startLive() {
        mStreamerPresenter.startLive();
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
            stopLive();
            mHeartbeatManager.stop();
            mIsRecording = false;
        }
    }

    protected void stopLive() {
        mStreamerPresenter.stopLive();
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

    @Override
    public void destroy() {
        innerEndLive();
    }
}
