// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Common.proto
package com.zq.live.proto.Common;

import com.squareup.wire.EnumAdapter;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireEnum;
import java.lang.Override;

public enum EMiniGamePlayType implements WireEnum {
  /**
   * 未知展示类型
   */
  EMGP_UNKNOWN(0),

  /**
   * 关键词展示
   */
  EMGP_KEYWORD(1),

  /**
   * 固定文案
   */
  EMGP_FIXED_TXT(2),

  /**
   * 歌曲详情
   */
  EMGP_SONG_DETAIL(3);

  public static final ProtoAdapter<EMiniGamePlayType> ADAPTER = new ProtoAdapter_EMiniGamePlayType();

  private final int value;

  EMiniGamePlayType(int value) {
    this.value = value;
  }

  /**
   * Return the constant for {@code value} or null.
   */
  public static EMiniGamePlayType fromValue(int value) {
    switch (value) {
      case 0: return EMGP_UNKNOWN;
      case 1: return EMGP_KEYWORD;
      case 2: return EMGP_FIXED_TXT;
      case 3: return EMGP_SONG_DETAIL;
      default: return null;
    }
  }

  @Override
  public int getValue() {
    return value;
  }

  public static final class Builder {
    public EMiniGamePlayType build() {
      return EMGP_UNKNOWN;
    }
  }

  private static final class ProtoAdapter_EMiniGamePlayType extends EnumAdapter<EMiniGamePlayType> {
    ProtoAdapter_EMiniGamePlayType() {
      super(EMiniGamePlayType.class);
    }

    @Override
    protected EMiniGamePlayType fromValue(int value) {
      return EMiniGamePlayType.fromValue(value);
    }
  }
}
