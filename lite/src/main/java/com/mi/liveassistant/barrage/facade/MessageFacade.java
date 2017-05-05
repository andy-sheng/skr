package com.mi.liveassistant.barrage.facade;

import com.mi.liveassistant.barrage.callback.MessageCallBack;
import com.mi.liveassistant.barrage.data.MessageType;
import com.mi.liveassistant.barrage.manager.BarragePullMessageManager;
import com.mi.liveassistant.barrage.manager.BarragePushMessageManager;
import com.mi.liveassistant.barrage.processer.BarrageMainProcesser;
import com.mi.liveassistant.common.log.MyLog;

/**
 * Created by wuxiaoshan on 17-5-4.
 */
public class MessageFacade {

    private static final String TAG = MessageFacade.class.getSimpleName();

    private static MessageFacade mInstance = new MessageFacade();

    private BarragePullMessageManager mBarragePullMessageManager;

    private boolean mIsRunning;

    public static MessageFacade getInstance() {
        return mInstance;
    }

    private MessageFacade() {
    }

    public void registCallBack(MessageCallBack callBack) {
        BarrageMainProcesser.getInstance().registCallBack(callBack);
    }

    public void unregistCallBack(MessageCallBack callBack) {
        BarrageMainProcesser.getInstance().unregistCallBack(callBack);
    }

    public void startPull(String roomId) {
        if(mBarragePullMessageManager != null && mBarragePullMessageManager.isRunning()){
            return;
        }
        mBarragePullMessageManager = new BarragePullMessageManager(roomId);
        mBarragePullMessageManager.start();
    }

    public void stopPull() {
        MyLog.w(TAG, "stopPull");
        if (mBarragePullMessageManager != null) {
            mBarragePullMessageManager.stop();
            mBarragePullMessageManager = null;
        }
    }

    public void sendTextMessageAsync(String body, String liveId, long anchorId) {
        BarragePushMessageManager.getInstance().sendBarrageMessageAsync(body, MessageType.MSG_TYPE_TEXT, liveId, anchorId, null);
    }

}
