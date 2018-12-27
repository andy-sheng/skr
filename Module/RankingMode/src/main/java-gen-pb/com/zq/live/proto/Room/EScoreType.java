// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Room.proto
package com.zq.live.proto.Room;

import com.squareup.wire.EnumAdapter;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireEnum;
import java.lang.Override;

public enum EScoreType implements WireEnum {
  /**
   * 未知
   */
  ST_UNKNOWN(0),

  /**
   * 段位
   */
  ST_LEVEL(1),

  /**
   * 晋级赛
   */
  ST_UP_LEVEL_GAME(2),

  /**
   * 总星星数
   */
  ST_TOTAL_STAR(3),

  /**
   * 子段位星星数
   */
  ST_SUB_LEVEL_STAR(4),

  /**
   * 晋级赛星星数
   */
  ST_UP_LEVEL_STAR(5),

  /**
   * 战力值
   */
  ST_BATTLE_INDEX(6),

  /**
   * 总战力值
   */
  ST_BATTLE_INDEX_TOTAL(7),

  /**
   * 战斗评价, sss or ss or s or a...
   */
  ST_BATTLE_RATING(8);

  public static final ProtoAdapter<EScoreType> ADAPTER = new ProtoAdapter_EScoreType();

  private final int value;

  EScoreType(int value) {
    this.value = value;
  }

  /**
   * Return the constant for {@code value} or null.
   */
  public static EScoreType fromValue(int value) {
    switch (value) {
      case 0: return ST_UNKNOWN;
      case 1: return ST_LEVEL;
      case 2: return ST_UP_LEVEL_GAME;
      case 3: return ST_TOTAL_STAR;
      case 4: return ST_SUB_LEVEL_STAR;
      case 5: return ST_UP_LEVEL_STAR;
      case 6: return ST_BATTLE_INDEX;
      case 7: return ST_BATTLE_INDEX_TOTAL;
      case 8: return ST_BATTLE_RATING;
      default: return null;
    }
  }

  @Override
  public int getValue() {
    return value;
  }

  public static final class Builder {
    public EScoreType build() {
      return ST_UNKNOWN;
    }
  }

  private static final class ProtoAdapter_EScoreType extends EnumAdapter<EScoreType> {
    ProtoAdapter_EScoreType() {
      super(EScoreType.class);
    }

    @Override
    protected EScoreType fromValue(int value) {
      return EScoreType.fromValue(value);
    }
  }
}
