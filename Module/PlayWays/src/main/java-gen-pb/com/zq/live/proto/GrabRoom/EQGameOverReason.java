// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: grab_room.proto
package com.zq.live.proto.GrabRoom;

import com.squareup.wire.EnumAdapter;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireEnum;
import java.lang.Override;

public enum EQGameOverReason implements WireEnum {
  /**
   * 未知
   */
  GOR_UNKNOWN(0),

  /**
   * 正常结束游戏
   */
  GOR_NORMAL(1),

  /**
   * 房主退出结束游戏
   */
  GOR_OWNER_EXIT(2);

  public static final ProtoAdapter<EQGameOverReason> ADAPTER = new ProtoAdapter_EQGameOverReason();

  private final int value;

  EQGameOverReason(int value) {
    this.value = value;
  }

  /**
   * Return the constant for {@code value} or null.
   */
  public static EQGameOverReason fromValue(int value) {
    switch (value) {
      case 0: return GOR_UNKNOWN;
      case 1: return GOR_NORMAL;
      case 2: return GOR_OWNER_EXIT;
      default: return null;
    }
  }

  @Override
  public int getValue() {
    return value;
  }

  public static final class Builder {
    public EQGameOverReason build() {
      return GOR_UNKNOWN;
    }
  }

  private static final class ProtoAdapter_EQGameOverReason extends EnumAdapter<EQGameOverReason> {
    ProtoAdapter_EQGameOverReason() {
      super(EQGameOverReason.class);
    }

    @Override
    protected EQGameOverReason fromValue(int value) {
      return EQGameOverReason.fromValue(value);
    }
  }
}
