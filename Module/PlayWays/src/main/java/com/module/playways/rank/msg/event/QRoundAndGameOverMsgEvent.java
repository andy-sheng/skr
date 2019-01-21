// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Room.proto
package com.module.playways.rank.msg.event;

import com.module.playways.grab.room.model.GrabResultInfoModel;
import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.prepare.model.RoundInfoModel;
import com.zq.live.proto.Room.EQRoundOverReason;
import com.zq.live.proto.Room.EQRoundResultType;
import com.zq.live.proto.Room.QResultInfo;
import com.zq.live.proto.Room.QRoundAndGameOverMsg;

import java.util.ArrayList;
import java.util.List;

public final class QRoundAndGameOverMsgEvent {
    public BasePushInfo info;


    /**
     * 本轮次结束的毫秒时间戳
     */
    public Long roundOverTimeMs;

    /**
     * 退出用户的ID
     */
    public RoundInfoModel roundInfoModel;

    /**
     * 最终结果信息
     */
    public List<GrabResultInfoModel> resultInfo;

    public QRoundAndGameOverMsgEvent(BasePushInfo info, QRoundAndGameOverMsg qRoundAndGameOverMsg) {
        this.info = info;
        this.roundOverTimeMs = qRoundAndGameOverMsg.getRoundOverTimeMs();
        this.roundInfoModel = RoundInfoModel.parseFromRoundInfo(qRoundAndGameOverMsg.getCurrentRound());
        resultInfo = new ArrayList<>();
        for(QResultInfo qResultInfo:qRoundAndGameOverMsg.getResultInfoList()){
            resultInfo.add(GrabResultInfoModel.parse(qResultInfo));
        }
    }

    public BasePushInfo getInfo() {
        return info;
    }

    public Long getRoundOverTimeMs() {
        return roundOverTimeMs;
    }

    public RoundInfoModel getRoundInfoModel() {
        return roundInfoModel;
    }

    public List<GrabResultInfoModel> getResultInfo() {
        return resultInfo;
    }
}
