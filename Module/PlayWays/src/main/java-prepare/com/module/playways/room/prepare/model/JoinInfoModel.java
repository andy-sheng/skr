package com.module.playways.room.prepare.model;
import com.common.log.MyLog;
import com.component.live.proto.Room.JoinInfo;

import java.io.Serializable;

public class JoinInfoModel implements Serializable {

    /**
     * userID : 30
     * joinSeq : 1
     * joinTimeMs : 1544439278416
     */

    private int userID;
    private int joinSeq;
    private long joinTimeMs;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getJoinSeq() {
        return joinSeq;
    }

    public void setJoinSeq(int joinSeq) {
        this.joinSeq = joinSeq;
    }

    public long getJoinTimeMs() {
        return joinTimeMs;
    }

    public void setJoinTimeMs(long joinTimeMs) {
        this.joinTimeMs = joinTimeMs;
    }

    public void parse(JoinInfo joinInfo){
        if (joinInfo == null){
            MyLog.e("JsonJoinInfo joinInfo == null");
            return;
        }

        this.setUserID(joinInfo.getUserID());
        this.setJoinSeq(joinInfo.getJoinSeq());
        this.setJoinTimeMs(joinInfo.getJoinTimeMs());
        return;
    }
}
