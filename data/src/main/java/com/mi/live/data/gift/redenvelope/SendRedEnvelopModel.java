package com.mi.live.data.gift.redenvelope;

/**
 * @module 红包
 * Created by caoxiangyu on 16-9-13.
 */
public class SendRedEnvelopModel {
    long zuId;
    String roomId;
    String msg;

    public long getZuId() {
        return zuId;
    }

    public void setZuId(long zuId) {
        this.zuId = zuId;
    }

    int gemCnt;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getGemCnt() {
        return gemCnt;
    }

    public void setGemCnt(int gemCnt) {
        this.gemCnt = gemCnt;
    }

}
