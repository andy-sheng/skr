package com.mi.liveassistant.barrage.facade;

import com.mi.liveassistant.barrage.callback.IChatMsgListener;
import com.mi.liveassistant.barrage.callback.ISysMsgListener;
import com.mi.liveassistant.barrage.data.MessageType;
import com.mi.liveassistant.barrage.manager.BarragePullMessageManager;
import com.mi.liveassistant.barrage.manager.BarragePushMessageManager;
import com.mi.liveassistant.barrage.processor.BarrageMainProcessor;
import com.mi.liveassistant.common.log.MyLog;

/**
 * Created by wuxiaoshan on 17-5-4.
 */
public class MessageFacade {

    private static final String TAG = MessageFacade.class.getSimpleName();

    private static MessageFacade mInstance = new MessageFacade();

    private BarragePullMessageManager mBarragePullMessageManager;

    public static MessageFacade getInstance() {
        return mInstance;
    }

    private MessageFacade() {
    }

    public void init(String roomId, IChatMsgListener chatMsgListener, ISysMsgListener sysMsgListener) {
        BarrageMainProcessor.getInstance().init(roomId, chatMsgListener, sysMsgListener);
    }

    public void destroy() {
        BarrageMainProcessor.getInstance().destroy();
    }

    public void startPull() {
        if (mBarragePullMessageManager != null && mBarragePullMessageManager.isRunning()) {
            return;
        }
        mBarragePullMessageManager = new BarragePullMessageManager();
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
