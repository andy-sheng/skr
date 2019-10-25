// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: mic_room.proto
package com.zq.live.proto.Room;

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

public final class MJoinActionMsg extends Message<MJoinActionMsg, MJoinActionMsg.Builder> {
  public static final ProtoAdapter<MJoinActionMsg> ADAPTER = new ProtoAdapter_MJoinActionMsg();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_GAMEID = 0;

  public static final Long DEFAULT_CREATETIMEMS = 0L;

  /**
   * 游戏ID
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer gameID;

  /**
   * 创建毫秒时间戳
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long createTimeMs;

  public MJoinActionMsg(Integer gameID, Long createTimeMs) {
    this(gameID, createTimeMs, ByteString.EMPTY);
  }

  public MJoinActionMsg(Integer gameID, Long createTimeMs, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.gameID = gameID;
    this.createTimeMs = createTimeMs;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.gameID = gameID;
    builder.createTimeMs = createTimeMs;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof MJoinActionMsg)) return false;
    MJoinActionMsg o = (MJoinActionMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(gameID, o.gameID)
        && Internal.equals(createTimeMs, o.createTimeMs);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (gameID != null ? gameID.hashCode() : 0);
      result = result * 37 + (createTimeMs != null ? createTimeMs.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (gameID != null) builder.append(", gameID=").append(gameID);
    if (createTimeMs != null) builder.append(", createTimeMs=").append(createTimeMs);
    return builder.replace(0, 2, "MJoinActionMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return MJoinActionMsg.ADAPTER.encode(this);
  }

  public static final MJoinActionMsg parseFrom(byte[] data) throws IOException {
    MJoinActionMsg c = null;
       c = MJoinActionMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 游戏ID
   */
  public Integer getGameID() {
    if(gameID==null){
        return DEFAULT_GAMEID;
    }
    return gameID;
  }

  /**
   * 创建毫秒时间戳
   */
  public Long getCreateTimeMs() {
    if(createTimeMs==null){
        return DEFAULT_CREATETIMEMS;
    }
    return createTimeMs;
  }

  /**
   * 游戏ID
   */
  public boolean hasGameID() {
    return gameID!=null;
  }

  /**
   * 创建毫秒时间戳
   */
  public boolean hasCreateTimeMs() {
    return createTimeMs!=null;
  }

  public static final class Builder extends Message.Builder<MJoinActionMsg, Builder> {
    private Integer gameID;

    private Long createTimeMs;

    public Builder() {
    }

    /**
     * 游戏ID
     */
    public Builder setGameID(Integer gameID) {
      this.gameID = gameID;
      return this;
    }

    /**
     * 创建毫秒时间戳
     */
    public Builder setCreateTimeMs(Long createTimeMs) {
      this.createTimeMs = createTimeMs;
      return this;
    }

    @Override
    public MJoinActionMsg build() {
      return new MJoinActionMsg(gameID, createTimeMs, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_MJoinActionMsg extends ProtoAdapter<MJoinActionMsg> {
    public ProtoAdapter_MJoinActionMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, MJoinActionMsg.class);
    }

    @Override
    public int encodedSize(MJoinActionMsg value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.gameID)
          + ProtoAdapter.SINT64.encodedSizeWithTag(2, value.createTimeMs)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, MJoinActionMsg value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.gameID);
      ProtoAdapter.SINT64.encodeWithTag(writer, 2, value.createTimeMs);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public MJoinActionMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setGameID(ProtoAdapter.UINT32.decode(reader)); break;
          case 2: builder.setCreateTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
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
    public MJoinActionMsg redact(MJoinActionMsg value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}