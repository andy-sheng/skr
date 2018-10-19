// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Relation.proto
package com.wali.live.proto.Relation;

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
 * 查询某个主播的订阅情况
 */
public final class GetSubscribeInfoRequest extends Message<GetSubscribeInfoRequest, GetSubscribeInfoRequest.Builder> {
  public static final ProtoAdapter<GetSubscribeInfoRequest> ADAPTER = new ProtoAdapter_GetSubscribeInfoRequest();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_USERID = 0L;

  public static final Long DEFAULT_TARGETID = 0L;

  /**
   * 主动订阅方userid(用户)
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64",
      label = WireField.Label.REQUIRED
  )
  public final Long userId;

  /**
   * 被动订阅方userid(主播)
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64",
      label = WireField.Label.REQUIRED
  )
  public final Long targetId;

  public GetSubscribeInfoRequest(Long userId, Long targetId) {
    this(userId, targetId, ByteString.EMPTY);
  }

  public GetSubscribeInfoRequest(Long userId, Long targetId, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.userId = userId;
    this.targetId = targetId;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.userId = userId;
    builder.targetId = targetId;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof GetSubscribeInfoRequest)) return false;
    GetSubscribeInfoRequest o = (GetSubscribeInfoRequest) other;
    return unknownFields().equals(o.unknownFields())
        && userId.equals(o.userId)
        && targetId.equals(o.targetId);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + userId.hashCode();
      result = result * 37 + targetId.hashCode();
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(", userId=").append(userId);
    builder.append(", targetId=").append(targetId);
    return builder.replace(0, 2, "GetSubscribeInfoRequest{").append('}').toString();
  }

  public byte[] toByteArray() {
    return GetSubscribeInfoRequest.ADAPTER.encode(this);
  }

  public static final class Builder extends Message.Builder<GetSubscribeInfoRequest, Builder> {
    public Long userId;

    public Long targetId;

    public Builder() {
    }

    /**
     * 主动订阅方userid(用户)
     */
    public Builder setUserId(Long userId) {
      this.userId = userId;
      return this;
    }

    /**
     * 被动订阅方userid(主播)
     */
    public Builder setTargetId(Long targetId) {
      this.targetId = targetId;
      return this;
    }

    @Override
    public GetSubscribeInfoRequest build() {
      if (userId == null
          || targetId == null) {
        throw Internal.missingRequiredFields(userId, "userId",
            targetId, "targetId");
      }
      return new GetSubscribeInfoRequest(userId, targetId, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_GetSubscribeInfoRequest extends ProtoAdapter<GetSubscribeInfoRequest> {
    public ProtoAdapter_GetSubscribeInfoRequest() {
      super(FieldEncoding.LENGTH_DELIMITED, GetSubscribeInfoRequest.class);
    }

    @Override
    public int encodedSize(GetSubscribeInfoRequest value) {
      return ProtoAdapter.UINT64.encodedSizeWithTag(1, value.userId)
          + ProtoAdapter.UINT64.encodedSizeWithTag(2, value.targetId)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, GetSubscribeInfoRequest value) throws IOException {
      ProtoAdapter.UINT64.encodeWithTag(writer, 1, value.userId);
      ProtoAdapter.UINT64.encodeWithTag(writer, 2, value.targetId);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public GetSubscribeInfoRequest decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setUserId(ProtoAdapter.UINT64.decode(reader)); break;
          case 2: builder.setTargetId(ProtoAdapter.UINT64.decode(reader)); break;
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
    public GetSubscribeInfoRequest redact(GetSubscribeInfoRequest value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
