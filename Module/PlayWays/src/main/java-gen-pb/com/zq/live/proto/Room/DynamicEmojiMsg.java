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
 * 动态表情消息
 */
public final class DynamicEmojiMsg extends Message<DynamicEmojiMsg, DynamicEmojiMsg.Builder> {
  public static final ProtoAdapter<DynamicEmojiMsg> ADAPTER = new ProtoAdapter_DynamicEmojiMsg();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_ID = 0;

  /**
   * 表情包id
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer id;

  public DynamicEmojiMsg(Integer id) {
    this(id, ByteString.EMPTY);
  }

  public DynamicEmojiMsg(Integer id, ByteString unknownFields) {
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
    if (!(other instanceof DynamicEmojiMsg)) return false;
    DynamicEmojiMsg o = (DynamicEmojiMsg) other;
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
    return builder.replace(0, 2, "DynamicEmojiMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return DynamicEmojiMsg.ADAPTER.encode(this);
  }

  public static final DynamicEmojiMsg parseFrom(byte[] data) throws IOException {
    DynamicEmojiMsg c = null;
       c = DynamicEmojiMsg.ADAPTER.decode(data);
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

  public static final class Builder extends Message.Builder<DynamicEmojiMsg, Builder> {
    private Integer id;

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
    public DynamicEmojiMsg build() {
      return new DynamicEmojiMsg(id, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_DynamicEmojiMsg extends ProtoAdapter<DynamicEmojiMsg> {
    public ProtoAdapter_DynamicEmojiMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, DynamicEmojiMsg.class);
    }

    @Override
    public int encodedSize(DynamicEmojiMsg value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.id)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, DynamicEmojiMsg value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.id);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public DynamicEmojiMsg decode(ProtoReader reader) throws IOException {
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
    public DynamicEmojiMsg redact(DynamicEmojiMsg value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
