package com.mi.liveassistant.room.manager.watch;

import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mi.liveassistant.barrage.callback.InternalMsgCallBack;
import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.data.MessageExt;
import com.mi.liveassistant.barrage.data.MessageType;
import com.mi.liveassistant.barrage.facade.MessageFacade;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.engine.player.widget.VideoPlayerPresenter;
import com.mi.liveassistant.engine.player.widget.VideoPlayerView;
import com.mi.liveassistant.room.RoomConstant;
import com.mi.liveassistant.room.manager.watch.callback.IWatchCallback;
import com.mi.liveassistant.room.presenter.streamer.PullStreamerPresenter;
import com.mi.liveassistant.room.presenter.watch.WatchPresenter;
import com.mi.liveassistant.room.view.IWatchView;
import com.mi.liveassistant.room.viewer.IViewerObserver;
import com.mi.liveassistant.room.viewer.IViewerRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 17/4/20.
 */
public class WatchManager implements IWatchView, IViewerRegister {
    private static final String TAG = RoomConstant.LOG_PREFIX + WatchManager.class.getSimpleName();

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

    public WatchManager() {
        mWatchPresenter = new WatchPresenter(this);
        mStreamerPresenter = new PullStreamerPresenter();
    }

    public void setContainerView(@NonNull ViewGroup containerView) {
        if (mSurfaceView == null) {
            mSurfaceView = new VideoPlayerView(containerView.getContext());
        }
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        containerView.addView(mSurfaceView, lp);
    }

    @Override
    public void enterLive(long playerId, String liveId, IWatchCallback callback) {
        mPlayerId = playerId;
        mLiveId = liveId;
        mOutEnterCallback = callback;
        mWatchPresenter.enterLive(playerId, liveId);
    }

    @Override
    public void notifyEnterLiveFail(int errCode) {
        if (mOutEnterCallback != null) {
            mOutEnterCallback.notifyFail(errCode);
        }
    }

    @Override
    public void notifyEnterLiveSuccess(String downStreamUrl) {
        mDownStreamUrl = downStreamUrl;

        if (mOutEnterCallback != null) {
            mOutEnterCallback.notifySuccess();
        }

        // TODO 其实拉流和进房间没有必然关系
        mVideoPlayerPresenter = mSurfaceView.getVideoPlayerPresenter();
        mVideoPlayerPresenter.setRealTime(true);
        mVideoPlayerPresenter.setVideoStreamBufferTime(2);

        mStreamerPresenter.setStreamer(mVideoPlayerPresenter);
        mStreamerPresenter.setOriginalStreamUrl(mDownStreamUrl);
        mStreamerPresenter.startLive();

        registerInternalMsg();
    }

    private void registerInternalMsg() {
        MessageFacade.getInstance().registInternalMsgCallBack(new InternalMsgCallBack() {
            @Override
            public void handleMessage(List<Message> messageList) {
                List<Message> viewerMessageList = new ArrayList();
                MyLog.d(TAG, "handleMessage message=" + messageList.size());
                boolean isViewerChange = false;
                for (Message message : messageList) {
                    if (message.getMsgType() == MessageType.MSG_TYPE_VIEWER_CHANGE) {
                        viewerMessageList.add(message);
                    } else if (message.getMsgType() == MessageType.MSG_TYPE_JOIN
                            || message.getMsgType() == MessageType.MSG_TYPE_LEAVE) {
                        isViewerChange = true;
                    }
                }
                if (viewerMessageList.size() > 0) {
                    MyLog.d(TAG, "handleMessage viewerMessage=" + viewerMessageList.size());
                    if (mViewerObserver != null) {
                        Message message = viewerMessageList.get(viewerMessageList.size() - 1);
                        mViewerObserver.dependOnList(((MessageExt.ViewChangeMessageExt) message.getMessageExt()).viewerList);
                    }
                } else if (isViewerChange) {
                    MyLog.d(TAG, "handleMessage viewerJoinOrLeave=" + isViewerChange);
                    if (mViewerObserver != null) {
                        mViewerObserver.dependOnSelf();
                    }
                }
            }
        });
    }

    @Override
    public void leaveLive() {
        mWatchPresenter.leaveLive(mPlayerId, mLiveId);

        mStreamerPresenter.stopLive();
        mStreamerPresenter.destroy();
    }

    @Override
    public void registerObserver(IViewerObserver observer) {
        mViewerObserver = observer;
    }
}
