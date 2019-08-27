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
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class RGetSingChanceMsg extends Message<RGetSingChanceMsg, RGetSingChanceMsg.Builder> {
  public static final ProtoAdapter<RGetSingChanceMsg> ADAPTER = new ProtoAdapter_RGetSingChanceMsg();

  private static final long serialVersionUID = 0L;

  /**
   * 当前轮次信息
   */
  @WireField(
      tag = 3,
      adapter = "com.zq.live.proto.RaceRoom.RaceRoundInfo#ADAPTER"
  )
  private final RaceRoundInfo currentRound;

  public RGetSingChanceMsg(RaceRoundInfo currentRound) {
    this(currentRound, ByteString.EMPTY);
  }

  public RGetSingChanceMsg(RaceRoundInfo currentRound, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.currentRound = currentRound;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.currentRound = currentRound;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof RGetSingChanceMsg)) return false;
    RGetSingChanceMsg o = (RGetSingChanceMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(currentRound, o.currentRound);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (currentRound != null ? currentRound.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (currentRound != null) builder.append(", currentRound=").append(currentRound);
    return builder.replace(0, 2, "RGetSingChanceMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return RGetSingChanceMsg.ADAPTER.encode(this);
  }

  public static final RGetSingChanceMsg parseFrom(byte[] data) throws IOException {
    RGetSingChanceMsg c = null;
       c = RGetSingChanceMsg.ADAPTER.decode(data);
    return c;
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
   * 当前轮次信息
   */
  public boolean hasCurrentRound() {
    return currentRound!=null;
  }

  public static final class Builder extends Message.Builder<RGetSingChanceMsg, Builder> {
    private RaceRoundInfo currentRound;

    public Builder() {
    }

    /**
     * 当前轮次信息
     */
    public Builder setCurrentRound(RaceRoundInfo currentRound) {
      this.currentRound = currentRound;
      return this;
    }

    @Override
    public RGetSingChanceMsg build() {
      return new RGetSingChanceMsg(currentRound, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_RGetSingChanceMsg extends ProtoAdapter<RGetSingChanceMsg> {
    public ProtoAdapter_RGetSingChanceMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, RGetSingChanceMsg.class);
    }

    @Override
    public int encodedSize(RGetSingChanceMsg value) {
      return RaceRoundInfo.ADAPTER.encodedSizeWithTag(3, value.currentRound)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, RGetSingChanceMsg value) throws IOException {
      RaceRoundInfo.ADAPTER.encodeWithTag(writer, 3, value.currentRound);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public RGetSingChanceMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 3: builder.setCurrentRound(RaceRoundInfo.ADAPTER.decode(reader)); break;
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
    public RGetSingChanceMsg redact(RGetSingChanceMsg value) {
      Builder builder = value.newBuilder();
      if (builder.currentRound != null) builder.currentRound = RaceRoundInfo.ADAPTER.redact(builder.currentRound);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}