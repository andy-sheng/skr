// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Room.proto
package com.module.playways.room.msg.event;

import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.Room.QGetSingChanceMsg;

public final class QGetSingChanceMsgEvent  {
  public BasePushInfo info;

  public  Integer userID;

  /**
   * 轮次顺序
   */

  public  Integer roundSeq;

  /**
   * 当前轮次信息
   */

  public GrabRoundInfoModel currentRound;

  public QGetSingChanceMsgEvent(BasePushInfo info, QGetSingChanceMsg qGetSingChanceMsg) {
    this.info = info;
    this.userID = qGetSingChanceMsg.getUserID();
    this.roundSeq = qGetSingChanceMsg.getRoundSeq();
    this.currentRound = GrabRoundInfoModel.parseFromRoundInfo(qGetSingChanceMsg.getCurrentRound());
  }

  public BasePushInfo getInfo() {
    return info;
  }

  public Integer getUserID() {
    return userID;
  }

  public Integer getRoundSeq() {
    return roundSeq;
  }

  public GrabRoundInfoModel getCurrentRound() {
    return currentRound;
  }
}
