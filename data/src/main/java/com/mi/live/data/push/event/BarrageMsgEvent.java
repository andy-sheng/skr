package com.mi.live.data.push.event;

import com.mi.live.data.push.model.BarrageMsg;

import java.util.List;

/**
 * @module com.wali.live.message.data
 * <p>
 * Created by MK on 16/2/23.
 */
public class BarrageMsgEvent {
    public static class ReceivedBarrageMsgEvent {
        private List<BarrageMsg> msgList;
        private boolean mFromSelf = false;
        public String from;

        public ReceivedBarrageMsgEvent(List<BarrageMsg> list) {
            msgList = list;
        }

        public ReceivedBarrageMsgEvent(List<BarrageMsg> list,String from) {
            msgList = list;
            this.from = from;
        }

        public ReceivedBarrageMsgEvent(List<BarrageMsg> list, boolean fromSelf) {
            msgList = list;
            this.mFromSelf = fromSelf;
        }

        public List<BarrageMsg> getMsgList() {
            return msgList;
        }

        public boolean isFromSelf() {
            return mFromSelf;
        }
    }

    public static class SendBarrageResponseEvent {
        private long cid;
        private long sentTime;

        public SendBarrageResponseEvent(long id, long time) {
            this.cid = id;
            this.sentTime = time;
        }

        public long getCid() {
            return cid;
        }

        public long getSentTime() {
            return sentTime;
        }

    }


    public static class CleanBarrageMsgEvent{

    }
}
