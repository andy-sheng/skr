// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: party_room.proto
package com.zq.live.proto.PartyRoom;

import com.squareup.wire.EnumAdapter;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireEnum;
import java.lang.Override;

public enum EFailedBeGuestReason implements WireEnum {
  /**
   * 未知
   */
  ER_UNKNOWN(0),

  /**
   * 不同意
   */
  ER_DISAGREE(1),

  /**
   * 没座位
   */
  ER_NO_SEAT(2);

  public static final ProtoAdapter<EFailedBeGuestReason> ADAPTER = new ProtoAdapter_EFailedBeGuestReason();

  private final int value;

  EFailedBeGuestReason(int value) {
    this.value = value;
  }

  /**
   * Return the constant for {@code value} or null.
   */
  public static EFailedBeGuestReason fromValue(int value) {
    switch (value) {
      case 0: return ER_UNKNOWN;
      case 1: return ER_DISAGREE;
      case 2: return ER_NO_SEAT;
      default: return null;
    }
  }

  @Override
  public int getValue() {
    return value;
  }

  public static final class Builder {
    public EFailedBeGuestReason build() {
      return ER_UNKNOWN;
    }
  }

  private static final class ProtoAdapter_EFailedBeGuestReason extends EnumAdapter<EFailedBeGuestReason> {
    ProtoAdapter_EFailedBeGuestReason() {
      super(EFailedBeGuestReason.class);
    }

    @Override
    protected EFailedBeGuestReason fromValue(int value) {
      return EFailedBeGuestReason.fromValue(value);
    }
  }
}