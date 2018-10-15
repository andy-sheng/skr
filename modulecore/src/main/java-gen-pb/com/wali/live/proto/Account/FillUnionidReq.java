// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Account.proto
package com.wali.live.proto.Account;

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
 * 用户客户端上传code，补充微信用户的unionid  cmd：zhibo.account.fillunionid
 */
public final class FillUnionidReq extends Message<FillUnionidReq, FillUnionidReq.Builder> {
  public static final ProtoAdapter<FillUnionidReq> ADAPTER = new ProtoAdapter_FillUnionidReq();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_UUID = 0L;

  public static final String DEFAULT_CODE = "";

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64",
      label = WireField.Label.REQUIRED
  )
  public final Long uuid;

  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String code;

  public FillUnionidReq(Long uuid, String code) {
    this(uuid, code, ByteString.EMPTY);
  }

  public FillUnionidReq(Long uuid, String code, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.uuid = uuid;
    this.code = code;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.uuid = uuid;
    builder.code = code;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof FillUnionidReq)) return false;
    FillUnionidReq o = (FillUnionidReq) other;
    return unknownFields().equals(o.unknownFields())
        && uuid.equals(o.uuid)
        && Internal.equals(code, o.code);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + uuid.hashCode();
      result = result * 37 + (code != null ? code.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(", uuid=").append(uuid);
    if (code != null) builder.append(", code=").append(code);
    return builder.replace(0, 2, "FillUnionidReq{").append('}').toString();
  }

  public byte[] toByteArray() {
    return FillUnionidReq.ADAPTER.encode(this);
  }

  public static final class Builder extends Message.Builder<FillUnionidReq, Builder> {
    public Long uuid;

    public String code;

    public Builder() {
    }

    public Builder setUuid(Long uuid) {
      this.uuid = uuid;
      return this;
    }

    public Builder setCode(String code) {
      this.code = code;
      return this;
    }

    @Override
    public FillUnionidReq build() {
      if (uuid == null) {
        throw Internal.missingRequiredFields(uuid, "uuid");
      }
      return new FillUnionidReq(uuid, code, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_FillUnionidReq extends ProtoAdapter<FillUnionidReq> {
    public ProtoAdapter_FillUnionidReq() {
      super(FieldEncoding.LENGTH_DELIMITED, FillUnionidReq.class);
    }

    @Override
    public int encodedSize(FillUnionidReq value) {
      return ProtoAdapter.UINT64.encodedSizeWithTag(1, value.uuid)
          + ProtoAdapter.STRING.encodedSizeWithTag(2, value.code)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, FillUnionidReq value) throws IOException {
      ProtoAdapter.UINT64.encodeWithTag(writer, 1, value.uuid);
      ProtoAdapter.STRING.encodeWithTag(writer, 2, value.code);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public FillUnionidReq decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setUuid(ProtoAdapter.UINT64.decode(reader)); break;
          case 2: builder.setCode(ProtoAdapter.STRING.decode(reader)); break;
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
    public FillUnionidReq redact(FillUnionidReq value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
