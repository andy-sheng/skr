// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Room.proto
package com.zq.live.proto.Room;

import com.squareup.wire.EnumAdapter;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireEnum;
import java.lang.Override;

public enum ELightAction implements WireEnum {
  InvalidLightAction(0),

  /**
   * 灭灯
   */
  LightOff(1),

  /**
   * 爆灯
   */
  LightOn(2);

  public static final ProtoAdapter<ELightAction> ADAPTER = new ProtoAdapter_ELightAction();

  private final int value;

  ELightAction(int value) {
    this.value = value;
  }

  /**
   * Return the constant for {@code value} or null.
   */
  public static ELightAction fromValue(int value) {
    switch (value) {
      case 0: return InvalidLightAction;
      case 1: return LightOff;
      case 2: return LightOn;
      default: return null;
    }
  }

  @Override
  public int getValue() {
    return value;
  }

  public static final class Builder {
    public ELightAction build() {
      return InvalidLightAction;
    }
  }

  private static final class ProtoAdapter_ELightAction extends EnumAdapter<ELightAction> {
    ProtoAdapter_ELightAction() {
      super(ELightAction.class);
    }

    @Override
    protected ELightAction fromValue(int value) {
      return ELightAction.fromValue(value);
    }
  }
}
