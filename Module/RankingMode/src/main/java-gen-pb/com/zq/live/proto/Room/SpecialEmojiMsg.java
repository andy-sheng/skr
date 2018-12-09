// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Room.proto
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
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

/**
 * 特殊表情消息
 */
public final class SpecialEmojiMsg extends Message<SpecialEmojiMsg, SpecialEmojiMsg.Builder> {
  public static final ProtoAdapter<SpecialEmojiMsg> ADAPTER = new ProtoAdapter_SpecialEmojiMsg();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_ID = 0;

  /**
   * 表情包id
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  public final Integer id;

  public SpecialEmojiMsg(Integer id) {
    this(id, ByteString.EMPTY);
  }

  public SpecialEmojiMsg(Integer id, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.id = id;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.id = id;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof SpecialEmojiMsg)) return false;
    SpecialEmojiMsg o = (SpecialEmojiMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(id, o.id);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (id != null ? id.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (id != null) builder.append(", id=").append(id);
    return builder.replace(0, 2, "SpecialEmojiMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return SpecialEmojiMsg.ADAPTER.encode(this);
  }

  public static final SpecialEmojiMsg parseFrom(byte[] data) throws IOException {
    SpecialEmojiMsg c = null;
       c = SpecialEmojiMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 表情包id
   */
  public Integer getId() {
    if(id==null){
        return DEFAULT_ID;
    }
    return id;
  }

  /**
   * 表情包id
   */
  public boolean hasId() {
    return id!=null;
  }

  public static final class Builder extends Message.Builder<SpecialEmojiMsg, Builder> {
    public Integer id;

    public Builder() {
    }

    /**
     * 表情包id
     */
    public Builder setId(Integer id) {
      this.id = id;
      return this;
    }

    @Override
    public SpecialEmojiMsg build() {
      return new SpecialEmojiMsg(id, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_SpecialEmojiMsg extends ProtoAdapter<SpecialEmojiMsg> {
    public ProtoAdapter_SpecialEmojiMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, SpecialEmojiMsg.class);
    }

    @Override
    public int encodedSize(SpecialEmojiMsg value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.id)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, SpecialEmojiMsg value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.id);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public SpecialEmojiMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setId(ProtoAdapter.UINT32.decode(reader)); break;
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
    public SpecialEmojiMsg redact(SpecialEmojiMsg value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
