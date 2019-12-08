// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: party_room.proto
package com.zq.live.proto.PartyRoom;

import com.squareup.wire.EnumAdapter;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireEnum;
import java.lang.Override;

public enum EMicStatus implements WireEnum {
  /**
   * 未知
   */
  MS_UNKNOWN(0),

  /**
   * 开麦
   */
  MS_OPEN(1),

  /**
   * 闭麦
   */
  MS_CLOSE(2);

  public static final ProtoAdapter<EMicStatus> ADAPTER = new ProtoAdapter_EMicStatus();

  private final int value;

  EMicStatus(int value) {
    this.value = value;
  }

  /**
   * Return the constant for {@code value} or null.
   */
  public static EMicStatus fromValue(int value) {
    switch (value) {
      case 0: return MS_UNKNOWN;
      case 1: return MS_OPEN;
      case 2: return MS_CLOSE;
      default: return null;
    }
  }

  @Override
  public int getValue() {
    return value;
  }

  public static final class Builder {
    public EMicStatus build() {
      return MS_UNKNOWN;
    }
  }

  private static final class ProtoAdapter_EMicStatus extends EnumAdapter<EMicStatus> {
    ProtoAdapter_EMicStatus() {
      super(EMicStatus.class);
    }

    @Override
    protected EMicStatus fromValue(int value) {
      return EMicStatus.fromValue(value);
    }
  }
}
