// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: grab_room.proto
package com.zq.live.proto.GrabRoom;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class ExitGameAfterPlayMsg extends Message<ExitGameAfterPlayMsg, ExitGameAfterPlayMsg.Builder> {
  public static final ProtoAdapter<ExitGameAfterPlayMsg> ADAPTER = new ProtoAdapter_ExitGameAfterPlayMsg();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_EXITUSERID = 0;

  public static final Long DEFAULT_EXITTIMEMS = 0L;

  /**
   * 退出玩家ID
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer exitUserID;

  /**
   * 退出毫秒时间戳
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long exitTimeMs;

  public ExitGameAfterPlayMsg(Integer exitUserID, Long exitTimeMs) {
    this(exitUserID, exitTimeMs, ByteString.EMPTY);
  }

  public ExitGameAfterPlayMsg(Integer exitUserID, Long exitTimeMs, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.exitUserID = exitUserID;
    this.exitTimeMs = exitTimeMs;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.exitUserID = exitUserID;
    builder.exitTimeMs = exitTimeMs;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof ExitGameAfterPlayMsg)) return false;
    ExitGameAfterPlayMsg o = (ExitGameAfterPlayMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(exitUserID, o.exitUserID)
        && Internal.equals(exitTimeMs, o.exitTimeMs);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (exitUserID != null ? exitUserID.hashCode() : 0);
      result = result * 37 + (exitTimeMs != null ? exitTimeMs.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (exitUserID != null) builder.append(", exitUserID=").append(exitUserID);
    if (exitTimeMs != null) builder.append(", exitTimeMs=").append(exitTimeMs);
    return builder.replace(0, 2, "ExitGameAfterPlayMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return ExitGameAfterPlayMsg.ADAPTER.encode(this);
  }

  public static final ExitGameAfterPlayMsg parseFrom(byte[] data) throws IOException {
    ExitGameAfterPlayMsg c = null;
       c = ExitGameAfterPlayMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 退出玩家ID
   */
  public Integer getExitUserID() {
    if(exitUserID==null){
        return DEFAULT_EXITUSERID;
    }
    return exitUserID;
  }

  /**
   * 退出毫秒时间戳
   */
  public Long getExitTimeMs() {
    if(exitTimeMs==null){
        return DEFAULT_EXITTIMEMS;
    }
    return exitTimeMs;
  }

  /**
   * 退出玩家ID
   */
  public boolean hasExitUserID() {
    return exitUserID!=null;
  }

  /**
   * 退出毫秒时间戳
   */
  public boolean hasExitTimeMs() {
    return exitTimeMs!=null;
  }

  public static final class Builder extends Message.Builder<ExitGameAfterPlayMsg, Builder> {
    private Integer exitUserID;

    private Long exitTimeMs;

    public Builder() {
    }

    /**
     * 退出玩家ID
     */
    public Builder setExitUserID(Integer exitUserID) {
      this.exitUserID = exitUserID;
      return this;
    }

    /**
     * 退出毫秒时间戳
     */
    public Builder setExitTimeMs(Long exitTimeMs) {
      this.exitTimeMs = exitTimeMs;
      return this;
    }

    @Override
    public ExitGameAfterPlayMsg build() {
      return new ExitGameAfterPlayMsg(exitUserID, exitTimeMs, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_ExitGameAfterPlayMsg extends ProtoAdapter<ExitGameAfterPlayMsg> {
    public ProtoAdapter_ExitGameAfterPlayMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, ExitGameAfterPlayMsg.class);
    }

    @Override
    public int encodedSize(ExitGameAfterPlayMsg value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.exitUserID)
          + ProtoAdapter.SINT64.encodedSizeWithTag(2, value.exitTimeMs)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, ExitGameAfterPlayMsg value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.exitUserID);
      ProtoAdapter.SINT64.encodeWithTag(writer, 2, value.exitTimeMs);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public ExitGameAfterPlayMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setExitUserID(ProtoAdapter.UINT32.decode(reader)); break;
          case 2: builder.setExitTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
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
    public ExitGameAfterPlayMsg redact(ExitGameAfterPlayMsg value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
