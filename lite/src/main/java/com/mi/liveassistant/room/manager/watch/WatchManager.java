package com.mi.liveassistant.room.manager.watch;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mi.liveassistant.barrage.callback.InternalMsgCallBack;
import com.mi.liveassistant.barrage.data.InternalMsgType;
import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.data.MessageExt;
import com.mi.liveassistant.barrage.processor.BarrageMainProcessor;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.engine.player.widget.VideoPlayerPresenter;
import com.mi.liveassistant.engine.player.widget.VideoPlayerView;
import com.mi.liveassistant.room.RoomConstant;
import com.mi.liveassistant.room.manager.LiveEventController;
import com.mi.liveassistant.room.manager.watch.callback.IWatchCallback;
import com.mi.liveassistant.room.manager.watch.callback.IWatchListener;
import com.mi.liveassistant.room.presenter.streamer.PullStreamerPresenter;
import com.mi.liveassistant.room.presenter.watch.WatchPresenter;
import com.mi.liveassistant.room.view.IWatchView;
import com.mi.liveassistant.room.viewer.IViewerObserver;
import com.mi.liveassistant.room.viewer.IViewerRegister;
import com.xiaomi.player.Player;

import java.util.ArrayList;
import java.util.List;

import component.EventController;
import component.IEventObserver;
import component.Params;

import static com.mi.liveassistant.room.manager.LiveEventController.MSG_ENTER_LIVE_FAILED;
import static com.mi.liveassistant.room.manager.LiveEventController.MSG_ENTER_LIVE_SUCCESS;

/**
 * Created by lan on 17/4/20.
 */
public class WatchManager implements IWatchView, IViewerRegister, IEventObserver {
    private static final String TAG = RoomConstant.LOG_PREFIX + WatchManager.class.getSimpleName();

    /*消息总线*/
    protected EventController mEventController;

    private VideoPlayerView mSurfaceView;

    private WatchPresenter mWatchPresenter;

    private VideoPlayerPresenter mVideoPlayerPresenter;
    private PullStreamerPresenter mStreamerPresenter;

    /*外部接口的回调*/
    protected IWatchCallback mOutEnterCallback;

    private long mPlayerId;
    private String mLiveId;

    private String mDownStreamUrl;

    private IViewerObserver mViewerObserver;

    protected IWatchListener mWatchListener;

    /**
     * #API# 构造函数
     *
     * @param watchListener 观看状态回调
     */
    public WatchManager(IWatchListener watchListener) {
        mWatchListener = watchListener;
        mEventController = new LiveEventController();
        mWatchPresenter = new WatchPresenter(mEventController, this);
        mStreamerPresenter = new PullStreamerPresenter();
        registerAction();
    }

    /**
     * #API# 设置容器，用于放置观看画面
     *
     * @param containerView 容器
     */
    public void setContainerView(@NonNull ViewGroup containerView) {
        if (mSurfaceView == null) {
            mSurfaceView = new VideoPlayerView(containerView.getContext());
            mVideoPlayerPresenter = mSurfaceView.getVideoPlayerPresenter();
        }
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        containerView.addView(mSurfaceView, lp);
    }

    /**
     * #API# 设置画面填充方式
     *
     * @param gravity 填充方式
     * @param width   容器宽度
     * @param height  容器高度
     */
    public void setGravity(Player.SurfaceGravity gravity, int width, int height) {
        if (mVideoPlayerPresenter != null) {
            mVideoPlayerPresenter.setGravity(gravity, width, height);
        }
    }

    /**
     * #API# 进入直播房间观看直播
     *
     * @param playerId 主播ID
     * @param liveId   房间ID
     * @param callback 进入房间状态回调通知
     */
    @Override
    public void enterLive(long playerId, String liveId, IWatchCallback callback) {
        mPlayerId = playerId;
        mLiveId = liveId;
        mOutEnterCallback = callback;
        mWatchPresenter.enterLive(playerId, liveId);
    }

    protected void notifyEnterLiveFail(int errCode) {
        MyLog.d(TAG, "notifyEnterLiveFail errCode=" + errCode);
        if (mOutEnterCallback != null) {
            mOutEnterCallback.notifyFail(errCode);
        }
    }

    protected void notifyEnterLiveSuccess(String downStreamUrl) {
        MyLog.d(TAG, "notifyEnterLiveSuccess downStreamUrl=" + downStreamUrl);
        mDownStreamUrl = downStreamUrl;

        if (mOutEnterCallback != null) {
            mOutEnterCallback.notifySuccess();
        }

        // TODO 其实拉流和进房间没有必然关系
        mVideoPlayerPresenter.setRealTime(true);
        mVideoPlayerPresenter.setVideoStreamBufferTime(2);

        mStreamerPresenter.setStreamer(mVideoPlayerPresenter);
        mStreamerPresenter.setOriginalStreamUrl(mDownStreamUrl);
        mStreamerPresenter.startLive();

        registerInternalMsg();
    }

    private void registerInternalMsg() {
        BarrageMainProcessor.getInstance().registerInternalMsgCallBack(new InternalMsgCallBack() {
            @Override
            public void handleMessage(List<Message> messageList) {
                List<Message> viewerMessageList = new ArrayList();
                MyLog.d(TAG, "handleMessage message=" + messageList.size());
                for (Message message : messageList) {
                    if (message.getMsgType() == InternalMsgType.MSG_TYPE_JOIN
                            || message.getMsgType() == InternalMsgType.MSG_TYPE_LEAVE) {
                        viewerMessageList.add(message);
                    }
                }
                if (viewerMessageList.size() > 0) {
                    MyLog.d(TAG, "handleMessage viewerMessage=" + viewerMessageList.size());
                    if (mViewerObserver != null) {
                        Message message = viewerMessageList.get(viewerMessageList.size() - 1);
                        if (message.getMsgType() == InternalMsgType.MSG_TYPE_JOIN) {
                            mViewerObserver.observerOnList(((MessageExt.JoinRoomMessageExt) message.getMessageExt()).viewerList);
                        } else if (message.getMsgType() == InternalMsgType.MSG_TYPE_LEAVE) {
                            mViewerObserver.observerOnList(((MessageExt.LeaveRoomMessageExt) message.getMessageExt()).viewerList);
                        }
                    }
                }
            }
        });
    }

    /**
     * #API# 离开房间结束观看
     */
    @Override
    public void leaveLive() {
        mWatchPresenter.leaveLive(mPlayerId, mLiveId);

        mStreamerPresenter.stopLive();
        mStreamerPresenter.destroy();

        BarrageMainProcessor.getInstance().unregisterInternalMsgCallBack();
    }

    /**
     * #API# 注册观察者监听观众列表变化
     *
     * @param observer 观察者
     */
    @Override
    public void registerObserver(IViewerObserver observer) {
        mViewerObserver = observer;
    }

    protected void registerAction() {
        mEventController.registerObserverForEvent(MSG_ENTER_LIVE_SUCCESS, this);
        mEventController.registerObserverForEvent(MSG_ENTER_LIVE_FAILED, this);
    }

    /**
     * @hide
     */
    @Override
    public boolean onEvent(int event, @Nullable Params params) {
        switch (event) {
            case MSG_ENTER_LIVE_SUCCESS:
                notifyEnterLiveSuccess((String) params.getItem(0));
                return true;
            case MSG_ENTER_LIVE_FAILED:
                notifyEnterLiveFail((int) params.getItem(0));
                return true;
        }
        return false;
    }
}
