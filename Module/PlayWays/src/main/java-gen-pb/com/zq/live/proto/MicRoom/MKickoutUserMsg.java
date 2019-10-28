// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: mic_room.proto
package com.zq.live.proto.MicRoom;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class MKickoutUserMsg extends Message<MKickoutUserMsg, MKickoutUserMsg.Builder> {
  public static final ProtoAdapter<MKickoutUserMsg> ADAPTER = new ProtoAdapter_MKickoutUserMsg();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_KICKUSERID = 0;

  /**
   * 用户id
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer kickUserID;

  public MKickoutUserMsg(Integer kickUserID) {
    this(kickUserID, ByteString.EMPTY);
  }

  public MKickoutUserMsg(Integer kickUserID, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.kickUserID = kickUserID;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.kickUserID = kickUserID;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof MKickoutUserMsg)) return false;
    MKickoutUserMsg o = (MKickoutUserMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(kickUserID, o.kickUserID);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (kickUserID != null ? kickUserID.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (kickUserID != null) builder.append(", kickUserID=").append(kickUserID);
    return builder.replace(0, 2, "MKickoutUserMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return MKickoutUserMsg.ADAPTER.encode(this);
  }

  public static final MKickoutUserMsg parseFrom(byte[] data) throws IOException {
    MKickoutUserMsg c = null;
       c = MKickoutUserMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 用户id
   */
  public Integer getKickUserID() {
    if(kickUserID==null){
        return DEFAULT_KICKUSERID;
    }
    return kickUserID;
  }

  /**
   * 用户id
   */
  public boolean hasKickUserID() {
    return kickUserID!=null;
  }

  public static final class Builder extends Message.Builder<MKickoutUserMsg, Builder> {
    private Integer kickUserID;

    public Builder() {
    }

    /**
     * 用户id
     */
    public Builder setKickUserID(Integer kickUserID) {
      this.kickUserID = kickUserID;
      return this;
    }

    @Override
    public MKickoutUserMsg build() {
      return new MKickoutUserMsg(kickUserID, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_MKickoutUserMsg extends ProtoAdapter<MKickoutUserMsg> {
    public ProtoAdapter_MKickoutUserMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, MKickoutUserMsg.class);
    }

    @Override
    public int encodedSize(MKickoutUserMsg value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.kickUserID)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, MKickoutUserMsg value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.kickUserID);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public MKickoutUserMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setKickUserID(ProtoAdapter.UINT32.decode(reader)); break;
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
    public MKickoutUserMsg redact(MKickoutUserMsg value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}