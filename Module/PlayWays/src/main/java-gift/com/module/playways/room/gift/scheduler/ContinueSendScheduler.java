package com.module.playways.room.gift.scheduler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.module.playways.room.gift.model.BaseGift;

public class ContinueSendScheduler {
    private static final int MSG_END_CONTINUE_SEND = 100;

    private BaseGift mBaseGift;

    private long mReceiverId;

    private int continueCount = 1;

    private long continueId = 0;

    private long mCanContinueDuration;

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_END_CONTINUE_SEND) {
                endContinueSend();
            }
        }
    };

    public ContinueSendScheduler(long canContinueDuration) {
        mCanContinueDuration = canContinueDuration;

    }

    public BuyGiftParam sendParam(BaseGift baseGift, long receiverId) {
        if (baseGift != mBaseGift || mReceiverId != receiverId) {
            continueCount = 1;
            mBaseGift = baseGift;
            mReceiverId = receiverId;
            continueId = System.currentTimeMillis();
        }

        mHandler.removeMessages(MSG_END_CONTINUE_SEND);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_END_CONTINUE_SEND), mCanContinueDuration);
        return new BuyGiftParam(continueId, continueCount);
    }

    public void sendGiftSuccess() {
        continueCount++;
    }

    private void endContinueSend() {
        mBaseGift = null;
        continueCount = 1;
    }

    public static class BuyGiftParam {
        long continueId;
        int continueCount;

        public BuyGiftParam(long continueId, int continueCount) {
            this.continueId = continueId;
            this.continueCount = continueCount;
        }

        public long getContinueId() {
            return continueId;
        }

        public int getContinueCount() {
            return continueCount;
        }
    }
}
