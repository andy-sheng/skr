// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: battle_room.proto
package com.zq.live.proto.BattleRoom;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class BSyncMsg extends Message<BSyncMsg, BSyncMsg.Builder> {
  public static final ProtoAdapter<BSyncMsg> ADAPTER = new ProtoAdapter_BSyncMsg();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_SYNCSTATUSTIMEMS = 0L;

  public static final Long DEFAULT_PASSEDTIMEMS = 0L;

  /**
   * 状态同步时的毫秒时间戳
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long syncStatusTimeMs;

  /**
   * 房间已经经历的毫秒数
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long passedTimeMs;

  /**
   * 当前轮次信息
   */
  @WireField(
      tag = 3,
      adapter = "com.zq.live.proto.BattleRoom.BRoundInfo#ADAPTER"
  )
  private final BRoundInfo currentRound;

  public BSyncMsg(Long syncStatusTimeMs, Long passedTimeMs, BRoundInfo currentRound) {
    this(syncStatusTimeMs, passedTimeMs, currentRound, ByteString.EMPTY);
  }

  public BSyncMsg(Long syncStatusTimeMs, Long passedTimeMs, BRoundInfo currentRound,
      ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.syncStatusTimeMs = syncStatusTimeMs;
    this.passedTimeMs = passedTimeMs;
    this.currentRound = currentRound;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.syncStatusTimeMs = syncStatusTimeMs;
    builder.passedTimeMs = passedTimeMs;
    builder.currentRound = currentRound;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof BSyncMsg)) return false;
    BSyncMsg o = (BSyncMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(syncStatusTimeMs, o.syncStatusTimeMs)
        && Internal.equals(passedTimeMs, o.passedTimeMs)
        && Internal.equals(currentRound, o.currentRound);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (syncStatusTimeMs != null ? syncStatusTimeMs.hashCode() : 0);
      result = result * 37 + (passedTimeMs != null ? passedTimeMs.hashCode() : 0);
      result = result * 37 + (currentRound != null ? currentRound.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (syncStatusTimeMs != null) builder.append(", syncStatusTimeMs=").append(syncStatusTimeMs);
    if (passedTimeMs != null) builder.append(", passedTimeMs=").append(passedTimeMs);
    if (currentRound != null) builder.append(", currentRound=").append(currentRound);
    return builder.replace(0, 2, "BSyncMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return BSyncMsg.ADAPTER.encode(this);
  }

  public static final BSyncMsg parseFrom(byte[] data) throws IOException {
    BSyncMsg c = null;
       c = BSyncMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 状态同步时的毫秒时间戳
   */
  public Long getSyncStatusTimeMs() {
    if(syncStatusTimeMs==null){
        return DEFAULT_SYNCSTATUSTIMEMS;
    }
    return syncStatusTimeMs;
  }

  /**
   * 房间已经经历的毫秒数
   */
  public Long getPassedTimeMs() {
    if(passedTimeMs==null){
        return DEFAULT_PASSEDTIMEMS;
    }
    return passedTimeMs;
  }

  /**
   * 当前轮次信息
   */
  public BRoundInfo getCurrentRound() {
    if(currentRound==null){
        return new BRoundInfo.Builder().build();
    }
    return currentRound;
  }

  /**
   * 状态同步时的毫秒时间戳
   */
  public boolean hasSyncStatusTimeMs() {
    return syncStatusTimeMs!=null;
  }

  /**
   * 房间已经经历的毫秒数
   */
  public boolean hasPassedTimeMs() {
    return passedTimeMs!=null;
  }

  /**
   * 当前轮次信息
   */
  public boolean hasCurrentRound() {
    return currentRound!=null;
  }

  public static final class Builder extends Message.Builder<BSyncMsg, Builder> {
    private Long syncStatusTimeMs;

    private Long passedTimeMs;

    private BRoundInfo currentRound;

    public Builder() {
    }

    /**
     * 状态同步时的毫秒时间戳
     */
    public Builder setSyncStatusTimeMs(Long syncStatusTimeMs) {
      this.syncStatusTimeMs = syncStatusTimeMs;
      return this;
    }

    /**
     * 房间已经经历的毫秒数
     */
    public Builder setPassedTimeMs(Long passedTimeMs) {
      this.passedTimeMs = passedTimeMs;
      return this;
    }

    /**
     * 当前轮次信息
     */
    public Builder setCurrentRound(BRoundInfo currentRound) {
      this.currentRound = currentRound;
      return this;
    }

    @Override
    public BSyncMsg build() {
      return new BSyncMsg(syncStatusTimeMs, passedTimeMs, currentRound, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_BSyncMsg extends ProtoAdapter<BSyncMsg> {
    public ProtoAdapter_BSyncMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, BSyncMsg.class);
    }

    @Override
    public int encodedSize(BSyncMsg value) {
      return ProtoAdapter.SINT64.encodedSizeWithTag(1, value.syncStatusTimeMs)
          + ProtoAdapter.SINT64.encodedSizeWithTag(2, value.passedTimeMs)
          + BRoundInfo.ADAPTER.encodedSizeWithTag(3, value.currentRound)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, BSyncMsg value) throws IOException {
      ProtoAdapter.SINT64.encodeWithTag(writer, 1, value.syncStatusTimeMs);
      ProtoAdapter.SINT64.encodeWithTag(writer, 2, value.passedTimeMs);
      BRoundInfo.ADAPTER.encodeWithTag(writer, 3, value.currentRound);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public BSyncMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setSyncStatusTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
          case 2: builder.setPassedTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
          case 3: builder.setCurrentRound(BRoundInfo.ADAPTER.decode(reader)); break;
          default: {
            FieldEncoding fieldEncoding = reader.peekFieldEncoding();
            Object value = fieldEncoding.rawProtoAdapter().decode(reader);
            builder.addUnknownField(tag, fieldEncoding, value);
          }
        }
      }
      reader.endMessage(token);
      return builder.build();
    }

    @Override
    public BSyncMsg redact(BSyncMsg value) {
      Builder builder = value.newBuilder();
      if (builder.currentRound != null) builder.currentRound = BRoundInfo.ADAPTER.redact(builder.currentRound);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}