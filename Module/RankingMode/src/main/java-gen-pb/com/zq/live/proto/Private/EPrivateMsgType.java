// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Private.proto
package com.zq.live.proto.Private;

import com.squareup.wire.EnumAdapter;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireEnum;
import java.lang.Override;

/**
 * EPrivateMsgType 私聊消息类型
 */
public enum EPrivateMsgType implements WireEnum {
  /**
   * 未知消息
   */
  PM_UNKNOWN(0),

  /**
   * /////////////////////////// 通用的消息类型 1-99 Start ////////////////////////////////////////
   * 文字消息
   */
  PM_TEXT(1);

  public static final ProtoAdapter<EPrivateMsgType> ADAPTER = new ProtoAdapter_EPrivateMsgType();

  private final int value;

  EPrivateMsgType(int value) {
    this.value = value;
  }

  /**
   * Return the constant for {@code value} or null.
   */
  public static EPrivateMsgType fromValue(int value) {
    switch (value) {
      case 0: return PM_UNKNOWN;
      case 1: return PM_TEXT;
      default: return null;
    }
  }

  @Override
  public int getValue() {
    return value;
  }

  public static final class Builder {
    public EPrivateMsgType build() {
      return PM_UNKNOWN;
    }
  }

  private static final class ProtoAdapter_EPrivateMsgType extends EnumAdapter<EPrivateMsgType> {
    ProtoAdapter_EPrivateMsgType() {
      super(EPrivateMsgType.class);
    }

    @Override
    protected EPrivateMsgType fromValue(int value) {
      return EPrivateMsgType.fromValue(value);
    }
  }
}
