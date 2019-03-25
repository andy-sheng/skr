package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.zq.live.proto.Room.EQCoinChangeReason;
import com.zq.live.proto.Room.QCoinChangeMsg;

public class QCoinChangeEvent {

    public BasePushInfo info;

    public int userID;
    public int changeCoin;
    public int remainCoin;
    public EQCoinChangeReason reason;

    public QCoinChangeEvent(BasePushInfo info, QCoinChangeMsg event) {
        this.info = info;
        this.userID = event.getUserID();
        this.changeCoin = event.getChangeCoin();
        this.remainCoin = event.getRemainCoin();
        this.reason = event.getReason();
    }

    @Override
    public String toString() {
        return "QCoinChangeEvent{" +
                ", userID=" + userID +
                ", changeCoin=" + changeCoin +
                ", remainCoin=" + remainCoin +
                ", reason=" + reason +
                '}';
    }
}
