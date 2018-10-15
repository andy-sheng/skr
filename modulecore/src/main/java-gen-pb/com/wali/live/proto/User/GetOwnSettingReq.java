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
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

/**
 * 获取自己的设置
 * cmd:zhibo.user.getownsetting
 */
public final class GetOwnSettingReq extends Message<GetOwnSettingReq, GetOwnSettingReq.Builder> {
  public static final ProtoAdapter<GetOwnSettingReq> ADAPTER = new ProtoAdapter_GetOwnSettingReq();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_ZUID = 0L;

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64",
      label = WireField.Label.REQUIRED
  )
  public final Long zuid;

  public GetOwnSettingReq(Long zuid) {
    this(zuid, ByteString.EMPTY);
  }

  public GetOwnSettingReq(Long zuid, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.zuid = zuid;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.zuid = zuid;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof GetOwnSettingReq)) return false;
    GetOwnSettingReq o = (GetOwnSettingReq) other;
    return unknownFields().equals(o.unknownFields())
        && zuid.equals(o.zuid);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + zuid.hashCode();
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(", zuid=").append(zuid);
    return builder.replace(0, 2, "GetOwnSettingReq{").append('}').toString();
  }

  public byte[] toByteArray() {
    return GetOwnSettingReq.ADAPTER.encode(this);
  }

  public static final class Builder extends Message.Builder<GetOwnSettingReq, Builder> {
    public Long zuid;

    public Builder() {
    }

    public Builder setZuid(Long zuid) {
      this.zuid = zuid;
      return this;
    }

    @Override
    public GetOwnSettingReq build() {
      if (zuid == null) {
        throw Internal.missingRequiredFields(zuid, "zuid");
      }
      return new GetOwnSettingReq(zuid, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_GetOwnSettingReq extends ProtoAdapter<GetOwnSettingReq> {
    public ProtoAdapter_GetOwnSettingReq() {
      super(FieldEncoding.LENGTH_DELIMITED, GetOwnSettingReq.class);
    }

    @Override
    public int encodedSize(GetOwnSettingReq value) {
      return ProtoAdapter.UINT64.encodedSizeWithTag(1, value.zuid)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, GetOwnSettingReq value) throws IOException {
      ProtoAdapter.UINT64.encodeWithTag(writer, 1, value.zuid);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public GetOwnSettingReq decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setZuid(ProtoAdapter.UINT64.decode(reader)); break;
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
    public GetOwnSettingReq redact(GetOwnSettingReq value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
