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
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

/**
 * 查询黑名单列表
 */
public final class BlockerListRequest extends Message<BlockerListRequest, BlockerListRequest.Builder> {
  public static final ProtoAdapter<BlockerListRequest> ADAPTER = new ProtoAdapter_BlockerListRequest();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_USERID = 0L;

  public static final Integer DEFAULT_OFFSET = 0;

  public static final Integer DEFAULT_LIMIT = 0;

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64",
      label = WireField.Label.REQUIRED
  )
  public final Long userId;

  /**
   * 偏移量,默认0
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  public final Integer offset;

  /**
   * 拉取数量,默认100
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  public final Integer limit;

  public BlockerListRequest(Long userId, Integer offset, Integer limit) {
    this(userId, offset, limit, ByteString.EMPTY);
  }

  public BlockerListRequest(Long userId, Integer offset, Integer limit, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.userId = userId;
    this.offset = offset;
    this.limit = limit;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.userId = userId;
    builder.offset = offset;
    builder.limit = limit;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof BlockerListRequest)) return false;
    BlockerListRequest o = (BlockerListRequest) other;
    return unknownFields().equals(o.unknownFields())
        && userId.equals(o.userId)
        && Internal.equals(offset, o.offset)
        && Internal.equals(limit, o.limit);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + userId.hashCode();
      result = result * 37 + (offset != null ? offset.hashCode() : 0);
      result = result * 37 + (limit != null ? limit.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(", userId=").append(userId);
    if (offset != null) builder.append(", offset=").append(offset);
    if (limit != null) builder.append(", limit=").append(limit);
    return builder.replace(0, 2, "BlockerListRequest{").append('}').toString();
  }

  public byte[] toByteArray() {
    return BlockerListRequest.ADAPTER.encode(this);
  }

  public static final class Builder extends Message.Builder<BlockerListRequest, Builder> {
    public Long userId;

    public Integer offset;

    public Integer limit;

    public Builder() {
    }

    public Builder setUserId(Long userId) {
      this.userId = userId;
      return this;
    }

    /**
     * 偏移量,默认0
     */
    public Builder setOffset(Integer offset) {
      this.offset = offset;
      return this;
    }

    /**
     * 拉取数量,默认100
     */
    public Builder setLimit(Integer limit) {
      this.limit = limit;
      return this;
    }

    @Override
    public BlockerListRequest build() {
      if (userId == null) {
        throw Internal.missingRequiredFields(userId, "userId");
      }
      return new BlockerListRequest(userId, offset, limit, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_BlockerListRequest extends ProtoAdapter<BlockerListRequest> {
    public ProtoAdapter_BlockerListRequest() {
      super(FieldEncoding.LENGTH_DELIMITED, BlockerListRequest.class);
    }

    @Override
    public int encodedSize(BlockerListRequest value) {
      return ProtoAdapter.UINT64.encodedSizeWithTag(1, value.userId)
          + ProtoAdapter.UINT32.encodedSizeWithTag(2, value.offset)
          + ProtoAdapter.UINT32.encodedSizeWithTag(3, value.limit)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, BlockerListRequest value) throws IOException {
      ProtoAdapter.UINT64.encodeWithTag(writer, 1, value.userId);
      ProtoAdapter.UINT32.encodeWithTag(writer, 2, value.offset);
      ProtoAdapter.UINT32.encodeWithTag(writer, 3, value.limit);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public BlockerListRequest decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setUserId(ProtoAdapter.UINT64.decode(reader)); break;
          case 2: builder.setOffset(ProtoAdapter.UINT32.decode(reader)); break;
          case 3: builder.setLimit(ProtoAdapter.UINT32.decode(reader)); break;
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
    public BlockerListRequest redact(BlockerListRequest value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
