// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: grab_room.proto
package com.zq.live.proto.GrabRoom;

import com.squareup.wire.EnumAdapter;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireEnum;
import java.lang.Override;

public enum ERoundOverReason implements WireEnum {
  /**
   * 未知
   */
  EROR_UNKNOWN(0),

  /**
   * 正常
   */
  EROR_NORMAL(1),

  /**
   * 玩家退出
   */
  EROR_ON_ROUND_USER_EXIT(2),

  /**
   * 足够多灭灯
   */
  EROR_ENOUGH_M_LIGHT(3);

  public static final ProtoAdapter<ERoundOverReason> ADAPTER = new ProtoAdapter_ERoundOverReason();

  private final int value;

  ERoundOverReason(int value) {
    this.value = value;
  }

  /**
   * Return the constant for {@code value} or null.
   */
  public static ERoundOverReason fromValue(int value) {
    switch (value) {
      case 0: return EROR_UNKNOWN;
      case 1: return EROR_NORMAL;
      case 2: return EROR_ON_ROUND_USER_EXIT;
      case 3: return EROR_ENOUGH_M_LIGHT;
      default: return null;
    }
  }

  @Override
  public int getValue() {
    return value;
  }

  public static final class Builder {
    public ERoundOverReason build() {
      return EROR_UNKNOWN;
    }
  }

  private static final class ProtoAdapter_ERoundOverReason extends EnumAdapter<ERoundOverReason> {
    ProtoAdapter_ERoundOverReason() {
      super(ERoundOverReason.class);
    }

    @Override
    protected ERoundOverReason fromValue(int value) {
      return ERoundOverReason.fromValue(value);
    }
  }
}
