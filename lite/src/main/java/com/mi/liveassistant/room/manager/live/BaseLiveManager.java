package com.mi.liveassistant.room.manager.live;

import com.mi.liveassistant.account.UserAccountManager;
import com.mi.liveassistant.barrage.callback.InternalMsgCallBack;
import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.data.MessageExt;
import com.mi.liveassistant.barrage.data.MessageType;
import com.mi.liveassistant.barrage.processor.BarrageMainProcessor;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.data.model.Location;
import com.mi.liveassistant.proto.LiveCommonProto;
import com.mi.liveassistant.room.RoomConstant;
import com.mi.liveassistant.room.heartbeat.HeartbeatManager;
import com.mi.liveassistant.room.manager.live.callback.ILiveCallback;
import com.mi.liveassistant.room.presenter.live.BaseLivePresenter;
import com.mi.liveassistant.room.presenter.streamer.StreamerPresenter;
import com.mi.liveassistant.room.view.ILiveView;
import com.mi.liveassistant.room.viewer.IViewerObserver;
import com.mi.liveassistant.room.viewer.IViewerRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 17/4/20.
 */
public abstract class BaseLiveManager<LP extends BaseLivePresenter> implements ILiveView, IViewerRegister {
    protected final String TAG = getTAG();

    /*直播开启关闭控制*/
    protected LP mLivePresenter;
    protected String mLiveId;
    /*外部接口的回调*/
    protected ILiveCallback mOutBeginCallback;
    protected ILiveCallback mOutEndCallback;

    protected boolean mIsGameLive = false;
    protected boolean mIsPaused = false;

    /*心跳管理*/
    protected HeartbeatManager mHeartbeatManager;

    /*直播推流控制*/
    protected StreamerPresenter mStreamerPresenter;
    protected boolean mIsRecording;

    /*顶部观众注册监听*/
    protected IViewerObserver mViewerObserver;

    protected BaseLiveManager() {
        mStreamerPresenter = new StreamerPresenter();
        // TODO 还是觉的放在startLive初始化比较好
        mHeartbeatManager = new HeartbeatManager();
    }

    protected String getTAG() {
        return RoomConstant.LOG_PREFIX + getClass().getSimpleName();
    }

    @Override
    public void beginLive(Location location, String title, String coverUrl, ILiveCallback callback) {
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
            mOutBeginCallback.notifySuccess(UserAccountManager.getInstance().getUuidAsLong(), mLiveId);
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

        registerInternalMsg();
    }

    protected abstract void createStreamer();

    protected void startLive() {
        mStreamerPresenter.startLive();
    }

    @Override
    public void endLive(ILiveCallback callback) {
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
            mOutEndCallback.notifySuccess(UserAccountManager.getInstance().getUuidAsLong(), mLiveId);
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

    @Override
    public void registerObserver(IViewerObserver observer) {
        mViewerObserver = observer;
    }

    protected void registerInternalMsg() {
        BarrageMainProcessor.getInstance().registerInternalMsgCallBack(new InternalMsgCallBack() {
            @Override
            public void handleMessage(List<Message> messageList) {
                List<Message> viewerMessageList = new ArrayList();
                MyLog.d(TAG, "handleMessage message=" + messageList.size());
                for (Message message : messageList) {
                    if (message.getMsgType() == MessageType.MSG_TYPE_JOIN
                            || message.getMsgType() == MessageType.MSG_TYPE_LEAVE) {
                        viewerMessageList.add(message);
                    }
                }
                if (viewerMessageList.size() > 0) {
                    MyLog.d(TAG, "handleMessage viewerMessage=" + viewerMessageList.size());
                    if (mViewerObserver != null) {
                        Message message = viewerMessageList.get(viewerMessageList.size() - 1);
                        if (message.getMsgType() == MessageType.MSG_TYPE_JOIN) {
                            mViewerObserver.observerOnList(((MessageExt.JoinRoomMessageExt) message.getMessageExt()).viewerList);
                        } else if (message.getMsgType() == MessageType.MSG_TYPE_LEAVE) {
                            mViewerObserver.observerOnList(((MessageExt.LeaveRoomMessageExt) message.getMessageExt()).viewerList);
                        }
                    }
                }
            }
        });
    }
}
