// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Room.proto
package com.zq.live.proto.Room;

import com.squareup.wire.EnumAdapter;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireEnum;
import java.lang.Override;

public enum EWantSingType implements WireEnum {
  /**
   * QSyncStatusMsg默认抢唱类型：普通
   */
  EWST_DEFAULT(0),

  /**
   * 带伴奏抢唱
   */
  EWST_ACCOMPANY(1),

  /**
   * 普通加时抢唱
   */
  EWST_COMMON_OVER_TIME(2),

  /**
   * 带伴奏加时抢唱
   */
  EWST_ACCOMPANY_OVER_TIME(3),

  /**
   * 合唱模式
   */
  EWST_CHORUS(4),

  /**
   * 一唱到底spk模式
   */
  EWST_SPK(5);

  public static final ProtoAdapter<EWantSingType> ADAPTER = new ProtoAdapter_EWantSingType();

  private final int value;

  EWantSingType(int value) {
    this.value = value;
  }

  /**
   * Return the constant for {@code value} or null.
   */
  public static EWantSingType fromValue(int value) {
    switch (value) {
      case 0: return EWST_DEFAULT;
      case 1: return EWST_ACCOMPANY;
      case 2: return EWST_COMMON_OVER_TIME;
      case 3: return EWST_ACCOMPANY_OVER_TIME;
      case 4: return EWST_CHORUS;
      case 5: return EWST_SPK;
      default: return null;
    }
  }

  @Override
  public int getValue() {
    return value;
  }

  public static final class Builder {
    public EWantSingType build() {
      return EWST_DEFAULT;
    }
  }

  private static final class ProtoAdapter_EWantSingType extends EnumAdapter<EWantSingType> {
    ProtoAdapter_EWantSingType() {
      super(EWantSingType.class);
    }

    @Override
    protected EWantSingType fromValue(int value) {
      return EWantSingType.fromValue(value);
    }
  }
}
