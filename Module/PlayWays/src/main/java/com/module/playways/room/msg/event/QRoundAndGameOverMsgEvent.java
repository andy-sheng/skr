// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Room.proto
package com.module.playways.room.msg.event;

import com.common.core.myinfo.MyUserInfoManager;
import com.module.playways.room.msg.BasePushInfo;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;
import com.zq.live.proto.GrabRoom.EQGameOverReason;
import com.zq.live.proto.GrabRoom.QRoundAndGameOverMsg;
import com.zq.live.proto.GrabRoom.QUserCoin;

public final class QRoundAndGameOverMsgEvent {
    public BasePushInfo info;


    /**
     * 本轮次结束的毫秒时间戳
     */
    public Long roundOverTimeMs;

    /**
     * 退出用户的ID
     */
    public BaseRoundInfoModel roundInfoModel;

    public int myCoin = -1;

    public EQGameOverReason mOverReason;

    public QRoundAndGameOverMsgEvent(BasePushInfo info, QRoundAndGameOverMsg qRoundAndGameOverMsg) {
        this.info = info;
        this.roundOverTimeMs = qRoundAndGameOverMsg.getRoundOverTimeMs();
        this.roundInfoModel = GrabRoundInfoModel.parseFromRoundInfo(qRoundAndGameOverMsg.getCurrentRound());

        for (QUserCoin c : qRoundAndGameOverMsg.getQUserCoinList()) {
            if (c.getUserID() == MyUserInfoManager.INSTANCE.getUid()) {
                long a = c.getCoin();
                myCoin = (int) a;
            }
        }
        this.mOverReason = qRoundAndGameOverMsg.getOverReason();
    }

    public BasePushInfo getInfo() {
        return info;
    }

    public Long getRoundOverTimeMs() {
        return roundOverTimeMs;
    }

    public BaseRoundInfoModel getRoundInfoModel() {
        return roundInfoModel;
    }

    public EQGameOverReason getOverReason() {
        return mOverReason;
    }
}
