// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Room.proto
package com.zq.live.proto.Room;

import com.squareup.wire.EnumAdapter;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireEnum;
import java.lang.Override;

public enum EWinType implements WireEnum {
  InvalidEWinType(0),

  Win(1),

  Draw(2),

  Lose(3);

  public static final ProtoAdapter<EWinType> ADAPTER = new ProtoAdapter_EWinType();

  private final int value;

  EWinType(int value) {
    this.value = value;
  }

  /**
   * Return the constant for {@code value} or null.
   */
  public static EWinType fromValue(int value) {
    switch (value) {
      case 0: return InvalidEWinType;
      case 1: return Win;
      case 2: return Draw;
      case 3: return Lose;
      default: return null;
    }
  }

  @Override
  public int getValue() {
    return value;
  }

  public static final class Builder {
    public EWinType build() {
      return InvalidEWinType;
    }
  }

  private static final class ProtoAdapter_EWinType extends EnumAdapter<EWinType> {
    ProtoAdapter_EWinType() {
      super(EWinType.class);
    }

    @Override
    protected EWinType fromValue(int value) {
      return EWinType.fromValue(value);
    }
  }
}
