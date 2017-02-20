package com.mi.live.data.query.model;

import com.wali.live.proto.LiveCommonProto;

/**
 * Created by chengsimin on 16/9/8.
 */
public class PkInfo {
    long uid;
    String liveId;
    int pkInitTicket;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getLiveId() {
        return liveId;
    }

    public void setLiveId(String liveId) {
        this.liveId = liveId;
    }

    public int getPkInitTicket() {
        return pkInitTicket;
    }

    public void setPkInitTicket(int pkInitTicket) {
        this.pkInitTicket = pkInitTicket;
    }

    public static PkInfo loadFromPB(LiveCommonProto.PKInfo pkInfo) {
        PkInfo pk = new PkInfo();
        pk.setLiveId(pkInfo.getLiveId());
        pk.setPkInitTicket(pkInfo.getPkInitTicket());
        pk.setUid(pkInfo.getUuid());
        return pk;
    }
}
