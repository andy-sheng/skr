// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: broadcast.proto
package com.zq.live.proto.broadcast;

import com.squareup.wire.EnumAdapter;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireEnum;
import java.lang.Override;

public enum ERoomBroadcastMsgType implements WireEnum {
  /**
   * 未知消息
   */
  RBRT_UNKNOWN(0),

  /**
   * 歌单战5星评级
   */
  RBRT_STAND_FULL_STAR(1),

  /**
   * 赠送礼物
   */
  RBRT_PRESENT_GIFT(2);

  public static final ProtoAdapter<ERoomBroadcastMsgType> ADAPTER = new ProtoAdapter_ERoomBroadcastMsgType();

  private final int value;

  ERoomBroadcastMsgType(int value) {
    this.value = value;
  }

  /**
   * Return the constant for {@code value} or null.
   */
  public static ERoomBroadcastMsgType fromValue(int value) {
    switch (value) {
      case 0: return RBRT_UNKNOWN;
      case 1: return RBRT_STAND_FULL_STAR;
      case 2: return RBRT_PRESENT_GIFT;
      default: return null;
    }
  }

  @Override
  public int getValue() {
    return value;
  }

  public static final class Builder {
    public ERoomBroadcastMsgType build() {
      return RBRT_UNKNOWN;
    }
  }

  private static final class ProtoAdapter_ERoomBroadcastMsgType extends EnumAdapter<ERoomBroadcastMsgType> {
    ProtoAdapter_ERoomBroadcastMsgType() {
      super(ERoomBroadcastMsgType.class);
    }

    @Override
    protected ERoomBroadcastMsgType fromValue(int value) {
      return ERoomBroadcastMsgType.fromValue(value);
    }
  }
}
