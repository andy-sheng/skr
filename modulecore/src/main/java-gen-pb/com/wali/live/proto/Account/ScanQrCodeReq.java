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
 * cmd: zhibo.account.scanqrcode
 */
public final class ScanQrCodeReq extends Message<ScanQrCodeReq, ScanQrCodeReq.Builder> {
  public static final ProtoAdapter<ScanQrCodeReq> ADAPTER = new ProtoAdapter_ScanQrCodeReq();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_UUID = 0L;

  public static final String DEFAULT_QR_CODE = "";

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64",
      label = WireField.Label.REQUIRED
  )
  public final Long uuid;

  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#STRING",
      label = WireField.Label.REQUIRED
  )
  public final String qr_code;

  public ScanQrCodeReq(Long uuid, String qr_code) {
    this(uuid, qr_code, ByteString.EMPTY);
  }

  public ScanQrCodeReq(Long uuid, String qr_code, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.uuid = uuid;
    this.qr_code = qr_code;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.uuid = uuid;
    builder.qr_code = qr_code;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof ScanQrCodeReq)) return false;
    ScanQrCodeReq o = (ScanQrCodeReq) other;
    return unknownFields().equals(o.unknownFields())
        && uuid.equals(o.uuid)
        && qr_code.equals(o.qr_code);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + uuid.hashCode();
      result = result * 37 + qr_code.hashCode();
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(", uuid=").append(uuid);
    builder.append(", qr_code=").append(qr_code);
    return builder.replace(0, 2, "ScanQrCodeReq{").append('}').toString();
  }

  public byte[] toByteArray() {
    return ScanQrCodeReq.ADAPTER.encode(this);
  }

  public static final class Builder extends Message.Builder<ScanQrCodeReq, Builder> {
    public Long uuid;

    public String qr_code;

    public Builder() {
    }

    public Builder setUuid(Long uuid) {
      this.uuid = uuid;
      return this;
    }

    public Builder setQrCode(String qr_code) {
      this.qr_code = qr_code;
      return this;
    }

    @Override
    public ScanQrCodeReq build() {
      if (uuid == null
          || qr_code == null) {
        throw Internal.missingRequiredFields(uuid, "uuid",
            qr_code, "qr_code");
      }
      return new ScanQrCodeReq(uuid, qr_code, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_ScanQrCodeReq extends ProtoAdapter<ScanQrCodeReq> {
    public ProtoAdapter_ScanQrCodeReq() {
      super(FieldEncoding.LENGTH_DELIMITED, ScanQrCodeReq.class);
    }

    @Override
    public int encodedSize(ScanQrCodeReq value) {
      return ProtoAdapter.UINT64.encodedSizeWithTag(1, value.uuid)
          + ProtoAdapter.STRING.encodedSizeWithTag(2, value.qr_code)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, ScanQrCodeReq value) throws IOException {
      ProtoAdapter.UINT64.encodeWithTag(writer, 1, value.uuid);
      ProtoAdapter.STRING.encodeWithTag(writer, 2, value.qr_code);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public ScanQrCodeReq decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setUuid(ProtoAdapter.UINT64.decode(reader)); break;
          case 2: builder.setQrCode(ProtoAdapter.STRING.decode(reader)); break;
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
    public ScanQrCodeReq redact(ScanQrCodeReq value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
