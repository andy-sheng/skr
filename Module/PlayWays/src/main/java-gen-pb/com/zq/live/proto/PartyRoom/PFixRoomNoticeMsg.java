// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: party_room.proto
package com.zq.live.proto.PartyRoom;

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

public final class PFixRoomNoticeMsg extends Message<PFixRoomNoticeMsg, PFixRoomNoticeMsg.Builder> {
  public static final ProtoAdapter<PFixRoomNoticeMsg> ADAPTER = new ProtoAdapter_PFixRoomNoticeMsg();

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_NEWROOMNOTICE = "";

  /**
   * 新的公告信息
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String newRoomNotice;

  public PFixRoomNoticeMsg(String newRoomNotice) {
    this(newRoomNotice, ByteString.EMPTY);
  }

  public PFixRoomNoticeMsg(String newRoomNotice, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.newRoomNotice = newRoomNotice;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.newRoomNotice = newRoomNotice;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof PFixRoomNoticeMsg)) return false;
    PFixRoomNoticeMsg o = (PFixRoomNoticeMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(newRoomNotice, o.newRoomNotice);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (newRoomNotice != null ? newRoomNotice.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (newRoomNotice != null) builder.append(", newRoomNotice=").append(newRoomNotice);
    return builder.replace(0, 2, "PFixRoomNoticeMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return PFixRoomNoticeMsg.ADAPTER.encode(this);
  }

  public static final PFixRoomNoticeMsg parseFrom(byte[] data) throws IOException {
    PFixRoomNoticeMsg c = null;
       c = PFixRoomNoticeMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 新的公告信息
   */
  public String getNewRoomNotice() {
    if(newRoomNotice==null){
        return DEFAULT_NEWROOMNOTICE;
    }
    return newRoomNotice;
  }

  /**
   * 新的公告信息
   */
  public boolean hasNewRoomNotice() {
    return newRoomNotice!=null;
  }

  public static final class Builder extends Message.Builder<PFixRoomNoticeMsg, Builder> {
    private String newRoomNotice;

    public Builder() {
    }

    /**
     * 新的公告信息
     */
    public Builder setNewRoomNotice(String newRoomNotice) {
      this.newRoomNotice = newRoomNotice;
      return this;
    }

    @Override
    public PFixRoomNoticeMsg build() {
      return new PFixRoomNoticeMsg(newRoomNotice, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_PFixRoomNoticeMsg extends ProtoAdapter<PFixRoomNoticeMsg> {
    public ProtoAdapter_PFixRoomNoticeMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, PFixRoomNoticeMsg.class);
    }

    @Override
    public int encodedSize(PFixRoomNoticeMsg value) {
      return ProtoAdapter.STRING.encodedSizeWithTag(1, value.newRoomNotice)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, PFixRoomNoticeMsg value) throws IOException {
      ProtoAdapter.STRING.encodeWithTag(writer, 1, value.newRoomNotice);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public PFixRoomNoticeMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setNewRoomNotice(ProtoAdapter.STRING.decode(reader)); break;
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
    public PFixRoomNoticeMsg redact(PFixRoomNoticeMsg value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
