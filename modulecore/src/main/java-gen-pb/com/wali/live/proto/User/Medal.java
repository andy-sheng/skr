// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: User.proto
package com.wali.live.proto.User;

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

public final class Medal extends Message<Medal, Medal.Builder> {
  public static final ProtoAdapter<Medal> ADAPTER = new ProtoAdapter_Medal();

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_PIC_ID = "";

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String pic_id;

  public Medal(String pic_id) {
    this(pic_id, ByteString.EMPTY);
  }

  public Medal(String pic_id, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.pic_id = pic_id;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.pic_id = pic_id;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof Medal)) return false;
    Medal o = (Medal) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(pic_id, o.pic_id);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (pic_id != null ? pic_id.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (pic_id != null) builder.append(", pic_id=").append(pic_id);
    return builder.replace(0, 2, "Medal{").append('}').toString();
  }

  public byte[] toByteArray() {
    return Medal.ADAPTER.encode(this);
  }

  public static final Medal parseFrom(byte[] data) throws IOException {
    Medal c = null;
       c = Medal.ADAPTER.decode(data);
    return c;
  }

  public String getPicId() {
    if(pic_id==null){
        return DEFAULT_PIC_ID;
    }
    return pic_id;
  }

  public boolean hasPicId() {
    return pic_id!=null;
  }

  public static final class Builder extends Message.Builder<Medal, Builder> {
    public String pic_id;

    public Builder() {
    }

    public Builder setPicId(String pic_id) {
      this.pic_id = pic_id;
      return this;
    }

    @Override
    public Medal build() {
      return new Medal(pic_id, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_Medal extends ProtoAdapter<Medal> {
    public ProtoAdapter_Medal() {
      super(FieldEncoding.LENGTH_DELIMITED, Medal.class);
    }

    @Override
    public int encodedSize(Medal value) {
      return ProtoAdapter.STRING.encodedSizeWithTag(1, value.pic_id)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, Medal value) throws IOException {
      ProtoAdapter.STRING.encodeWithTag(writer, 1, value.pic_id);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public Medal decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setPicId(ProtoAdapter.STRING.decode(reader)); break;
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
    public Medal redact(Medal value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
