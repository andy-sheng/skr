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
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class PBackSeatMsg extends Message<PBackSeatMsg, PBackSeatMsg.Builder> {
  public static final ProtoAdapter<PBackSeatMsg> ADAPTER = new ProtoAdapter_PBackSeatMsg();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_SEATSEQ = 0;

  /**
   * 用户信息
   */
  @WireField(
      tag = 1,
      adapter = "com.zq.live.proto.PartyRoom.POnlineInfo#ADAPTER"
  )
  private final POnlineInfo user;

  /**
   * 座位顺序
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer seatSeq;

  public PBackSeatMsg(POnlineInfo user, Integer seatSeq) {
    this(user, seatSeq, ByteString.EMPTY);
  }

  public PBackSeatMsg(POnlineInfo user, Integer seatSeq, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.user = user;
    this.seatSeq = seatSeq;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.user = user;
    builder.seatSeq = seatSeq;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof PBackSeatMsg)) return false;
    PBackSeatMsg o = (PBackSeatMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(user, o.user)
        && Internal.equals(seatSeq, o.seatSeq);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (user != null ? user.hashCode() : 0);
      result = result * 37 + (seatSeq != null ? seatSeq.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (user != null) builder.append(", user=").append(user);
    if (seatSeq != null) builder.append(", seatSeq=").append(seatSeq);
    return builder.replace(0, 2, "PBackSeatMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return PBackSeatMsg.ADAPTER.encode(this);
  }

  public static final PBackSeatMsg parseFrom(byte[] data) throws IOException {
    PBackSeatMsg c = null;
       c = PBackSeatMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 用户信息
   */
  public POnlineInfo getUser() {
    if(user==null){
        return new POnlineInfo.Builder().build();
    }
    return user;
  }

  /**
   * 座位顺序
   */
  public Integer getSeatSeq() {
    if(seatSeq==null){
        return DEFAULT_SEATSEQ;
    }
    return seatSeq;
  }

  /**
   * 用户信息
   */
  public boolean hasUser() {
    return user!=null;
  }

  /**
   * 座位顺序
   */
  public boolean hasSeatSeq() {
    return seatSeq!=null;
  }

  public static final class Builder extends Message.Builder<PBackSeatMsg, Builder> {
    private POnlineInfo user;

    private Integer seatSeq;

    public Builder() {
    }

    /**
     * 用户信息
     */
    public Builder setUser(POnlineInfo user) {
      this.user = user;
      return this;
    }

    /**
     * 座位顺序
     */
    public Builder setSeatSeq(Integer seatSeq) {
      this.seatSeq = seatSeq;
      return this;
    }

    @Override
    public PBackSeatMsg build() {
      return new PBackSeatMsg(user, seatSeq, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_PBackSeatMsg extends ProtoAdapter<PBackSeatMsg> {
    public ProtoAdapter_PBackSeatMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, PBackSeatMsg.class);
    }

    @Override
    public int encodedSize(PBackSeatMsg value) {
      return POnlineInfo.ADAPTER.encodedSizeWithTag(1, value.user)
          + ProtoAdapter.UINT32.encodedSizeWithTag(2, value.seatSeq)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, PBackSeatMsg value) throws IOException {
      POnlineInfo.ADAPTER.encodeWithTag(writer, 1, value.user);
      ProtoAdapter.UINT32.encodeWithTag(writer, 2, value.seatSeq);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public PBackSeatMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setUser(POnlineInfo.ADAPTER.decode(reader)); break;
          case 2: builder.setSeatSeq(ProtoAdapter.UINT32.decode(reader)); break;
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
    public PBackSeatMsg redact(PBackSeatMsg value) {
      Builder builder = value.newBuilder();
      if (builder.user != null) builder.user = POnlineInfo.ADAPTER.redact(builder.user);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
