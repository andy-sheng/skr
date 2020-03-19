// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: battle_room.proto
package com.zq.live.proto.BattleRoom;

import com.squareup.wire.EnumAdapter;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireEnum;
import java.lang.Override;

public enum EChallengeResult implements WireEnum {
  /**
   * 挑战失败
   */
  ECR_FAILED(0),

  /**
   * 挑战成功
   */
  ECR_SUCCESS(1);

  public static final ProtoAdapter<EChallengeResult> ADAPTER = new ProtoAdapter_EChallengeResult();

  private final int value;

  EChallengeResult(int value) {
    this.value = value;
  }

  /**
   * Return the constant for {@code value} or null.
   */
  public static EChallengeResult fromValue(int value) {
    switch (value) {
      case 0: return ECR_FAILED;
      case 1: return ECR_SUCCESS;
      default: return null;
    }
  }

  @Override
  public int getValue() {
    return value;
  }

  public static final class Builder {
    public EChallengeResult build() {
      return ECR_FAILED;
    }
  }

  private static final class ProtoAdapter_EChallengeResult extends EnumAdapter<EChallengeResult> {
    ProtoAdapter_EChallengeResult() {
      super(EChallengeResult.class);
    }

    @Override
    protected EChallengeResult fromValue(int value) {
      return EChallengeResult.fromValue(value);
    }
  }
}