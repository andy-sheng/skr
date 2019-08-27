// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: race_room.proto
package com.zq.live.proto.RaceRoom;

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

public final class RSyncStatusMsg extends Message<RSyncStatusMsg, RSyncStatusMsg.Builder> {
  public static final ProtoAdapter<RSyncStatusMsg> ADAPTER = new ProtoAdapter_RSyncStatusMsg();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_SYNCSTATUSTIMEMS = 0L;

  /**
   * 状态同步时的毫秒时间戳
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long syncStatusTimeMs;

  /**
   * 当前轮次信息
   */
  @WireField(
      tag = 2,
      adapter = "com.zq.live.proto.RaceRoom.RaceRoundInfo#ADAPTER"
  )
  private final RaceRoundInfo currentRound;

  public RSyncStatusMsg(Long syncStatusTimeMs, RaceRoundInfo currentRound) {
    this(syncStatusTimeMs, currentRound, ByteString.EMPTY);
  }

  public RSyncStatusMsg(Long syncStatusTimeMs, RaceRoundInfo currentRound,
      ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.syncStatusTimeMs = syncStatusTimeMs;
    this.currentRound = currentRound;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.syncStatusTimeMs = syncStatusTimeMs;
    builder.currentRound = currentRound;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof RSyncStatusMsg)) return false;
    RSyncStatusMsg o = (RSyncStatusMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(syncStatusTimeMs, o.syncStatusTimeMs)
        && Internal.equals(currentRound, o.currentRound);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (syncStatusTimeMs != null ? syncStatusTimeMs.hashCode() : 0);
      result = result * 37 + (currentRound != null ? currentRound.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (syncStatusTimeMs != null) builder.append(", syncStatusTimeMs=").append(syncStatusTimeMs);
    if (currentRound != null) builder.append(", currentRound=").append(currentRound);
    return builder.replace(0, 2, "RSyncStatusMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return RSyncStatusMsg.ADAPTER.encode(this);
  }

  public static final RSyncStatusMsg parseFrom(byte[] data) throws IOException {
    RSyncStatusMsg c = null;
       c = RSyncStatusMsg.ADAPTER.decode(data);
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
   * 当前轮次信息
   */
  public RaceRoundInfo getCurrentRound() {
    if(currentRound==null){
        return new RaceRoundInfo.Builder().build();
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
   * 当前轮次信息
   */
  public boolean hasCurrentRound() {
    return currentRound!=null;
  }

  public static final class Builder extends Message.Builder<RSyncStatusMsg, Builder> {
    private Long syncStatusTimeMs;

    private RaceRoundInfo currentRound;

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
     * 当前轮次信息
     */
    public Builder setCurrentRound(RaceRoundInfo currentRound) {
      this.currentRound = currentRound;
      return this;
    }

    @Override
    public RSyncStatusMsg build() {
      return new RSyncStatusMsg(syncStatusTimeMs, currentRound, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_RSyncStatusMsg extends ProtoAdapter<RSyncStatusMsg> {
    public ProtoAdapter_RSyncStatusMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, RSyncStatusMsg.class);
    }

    @Override
    public int encodedSize(RSyncStatusMsg value) {
      return ProtoAdapter.SINT64.encodedSizeWithTag(1, value.syncStatusTimeMs)
          + RaceRoundInfo.ADAPTER.encodedSizeWithTag(2, value.currentRound)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, RSyncStatusMsg value) throws IOException {
      ProtoAdapter.SINT64.encodeWithTag(writer, 1, value.syncStatusTimeMs);
      RaceRoundInfo.ADAPTER.encodeWithTag(writer, 2, value.currentRound);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public RSyncStatusMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setSyncStatusTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
          case 2: builder.setCurrentRound(RaceRoundInfo.ADAPTER.decode(reader)); break;
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
    public RSyncStatusMsg redact(RSyncStatusMsg value) {
      Builder builder = value.newBuilder();
      if (builder.currentRound != null) builder.currentRound = RaceRoundInfo.ADAPTER.redact(builder.currentRound);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
