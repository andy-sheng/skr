// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Common.proto
package com.zq.live.proto.Common;

import com.squareup.wire.EnumAdapter;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireEnum;

public enum ESceneType implements WireEnum {
  /**
   * 未知场景
   */
  ST_Unknown(0),

  /**
   * 游戏场景
   */
  ST_Game(1),

  /**
   * 唱歌场景
   */
  ST_Sing(2),

  /**
   * 聊天场景
   */
  ST_Chat(3);

  public static final ProtoAdapter<ESceneType> ADAPTER = new ProtoAdapter_ESceneType();

  private final int value;

  ESceneType(int value) {
    this.value = value;
  }

  /**
   * Return the constant for {@code value} or null.
   */
  public static ESceneType fromValue(int value) {
    switch (value) {
      case 0: return ST_Unknown;
      case 1: return ST_Game;
      case 2: return ST_Sing;
      case 3: return ST_Chat;
      default: return null;
    }
  }

  @Override
  public int getValue() {
    return value;
  }

  public static final class Builder {
    public ESceneType build() {
      return ST_Unknown;
    }
  }

  private static final class ProtoAdapter_ESceneType extends EnumAdapter<ESceneType> {
    ProtoAdapter_ESceneType() {
      super(ESceneType.class);
    }

    @Override
    protected ESceneType fromValue(int value) {
      return ESceneType.fromValue(value);
    }
  }
}
