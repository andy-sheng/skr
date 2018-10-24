package com.wali.live.modulewatch.barrage.event;

import com.wali.live.modulewatch.barrage.model.barrage.BarrageMsg;

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

        public ReceivedBarrageMsgEvent(List<BarrageMsg> list, String from) {
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
        private int flyCnt;
        private int adminCnt;
        private int vipCnt;
        private int guardCnt;

        public SendBarrageResponseEvent(long cid, long sentTime, int flyCnt, int adminCnt, int vipCnt, int guardCnt) {
            this.cid = cid;
            this.sentTime = sentTime;
            this.flyCnt = flyCnt;
            this.adminCnt = adminCnt;
            this.vipCnt = vipCnt;
            this.guardCnt = guardCnt;
        }

        public long getCid() {
            return cid;
        }

        public long getSentTime() {
            return sentTime;
        }

        public int getFlyCnt() {
            return flyCnt;
        }

        public int getAdminCnt() {
            return adminCnt;
        }

        public int getVipCnt() {
            return vipCnt;
        }

        public int getGuardCnt() {
            return guardCnt;
        }
    }


    public static class CleanBarrageMsgEvent {

    }
}
