// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Room.proto
package com.module.playways.room.msg.event;

import com.module.playways.room.msg.BasePushInfo;
import com.component.live.proto.Room.QWantSingChanceMsg;

public final class QWantSingChanceMsgEvent{
  public BasePushInfo info;
  /**
   * 用户id
   */
  public Integer userID;

  /**
   * 轮次顺序
   */
  public Integer roundSeq;

  public int wantSingType;

  public QWantSingChanceMsgEvent(BasePushInfo info, QWantSingChanceMsg qWantSingChanceMsg) {
    this.info = info;
    this.userID = qWantSingChanceMsg.getUserID();
    this.roundSeq = qWantSingChanceMsg.getRoundSeq();
    this.wantSingType = qWantSingChanceMsg.getWantSingType().getValue();
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

  public int getWantSingType() {
    return wantSingType;
  }
}
