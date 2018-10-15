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
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

/**
 * 搜索
 * cmd:zhibo.user.search
 */
public final class SearchUserInfoReq extends Message<SearchUserInfoReq, SearchUserInfoReq.Builder> {
  public static final ProtoAdapter<SearchUserInfoReq> ADAPTER = new ProtoAdapter_SearchUserInfoReq();

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_KEYWORD = "";

  public static final Integer DEFAULT_OFFSET = 0;

  public static final Integer DEFAULT_LIMIT = 0;

  public static final Boolean DEFAULT_LIVE_STATUS = false;

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#STRING",
      label = WireField.Label.REQUIRED
  )
  public final String keyword;

  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32",
      label = WireField.Label.REQUIRED
  )
  public final Integer offset;

  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32",
      label = WireField.Label.REQUIRED
  )
  public final Integer limit;

  /**
   * 是否显示直播状态
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  public final Boolean live_status;

  public SearchUserInfoReq(String keyword, Integer offset, Integer limit, Boolean live_status) {
    this(keyword, offset, limit, live_status, ByteString.EMPTY);
  }

  public SearchUserInfoReq(String keyword, Integer offset, Integer limit, Boolean live_status,
      ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.keyword = keyword;
    this.offset = offset;
    this.limit = limit;
    this.live_status = live_status;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.keyword = keyword;
    builder.offset = offset;
    builder.limit = limit;
    builder.live_status = live_status;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof SearchUserInfoReq)) return false;
    SearchUserInfoReq o = (SearchUserInfoReq) other;
    return unknownFields().equals(o.unknownFields())
        && keyword.equals(o.keyword)
        && offset.equals(o.offset)
        && limit.equals(o.limit)
        && Internal.equals(live_status, o.live_status);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + keyword.hashCode();
      result = result * 37 + offset.hashCode();
      result = result * 37 + limit.hashCode();
      result = result * 37 + (live_status != null ? live_status.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(", keyword=").append(keyword);
    builder.append(", offset=").append(offset);
    builder.append(", limit=").append(limit);
    if (live_status != null) builder.append(", live_status=").append(live_status);
    return builder.replace(0, 2, "SearchUserInfoReq{").append('}').toString();
  }

  public byte[] toByteArray() {
    return SearchUserInfoReq.ADAPTER.encode(this);
  }

  public static final class Builder extends Message.Builder<SearchUserInfoReq, Builder> {
    public String keyword;

    public Integer offset;

    public Integer limit;

    public Boolean live_status;

    public Builder() {
    }

    public Builder setKeyword(String keyword) {
      this.keyword = keyword;
      return this;
    }

    public Builder setOffset(Integer offset) {
      this.offset = offset;
      return this;
    }

    public Builder setLimit(Integer limit) {
      this.limit = limit;
      return this;
    }

    /**
     * 是否显示直播状态
     */
    public Builder setLiveStatus(Boolean live_status) {
      this.live_status = live_status;
      return this;
    }

    @Override
    public SearchUserInfoReq build() {
      if (keyword == null
          || offset == null
          || limit == null) {
        throw Internal.missingRequiredFields(keyword, "keyword",
            offset, "offset",
            limit, "limit");
      }
      return new SearchUserInfoReq(keyword, offset, limit, live_status, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_SearchUserInfoReq extends ProtoAdapter<SearchUserInfoReq> {
    public ProtoAdapter_SearchUserInfoReq() {
      super(FieldEncoding.LENGTH_DELIMITED, SearchUserInfoReq.class);
    }

    @Override
    public int encodedSize(SearchUserInfoReq value) {
      return ProtoAdapter.STRING.encodedSizeWithTag(1, value.keyword)
          + ProtoAdapter.UINT32.encodedSizeWithTag(2, value.offset)
          + ProtoAdapter.UINT32.encodedSizeWithTag(3, value.limit)
          + ProtoAdapter.BOOL.encodedSizeWithTag(4, value.live_status)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, SearchUserInfoReq value) throws IOException {
      ProtoAdapter.STRING.encodeWithTag(writer, 1, value.keyword);
      ProtoAdapter.UINT32.encodeWithTag(writer, 2, value.offset);
      ProtoAdapter.UINT32.encodeWithTag(writer, 3, value.limit);
      ProtoAdapter.BOOL.encodeWithTag(writer, 4, value.live_status);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public SearchUserInfoReq decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setKeyword(ProtoAdapter.STRING.decode(reader)); break;
          case 2: builder.setOffset(ProtoAdapter.UINT32.decode(reader)); break;
          case 3: builder.setLimit(ProtoAdapter.UINT32.decode(reader)); break;
          case 4: builder.setLiveStatus(ProtoAdapter.BOOL.decode(reader)); break;
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
    public SearchUserInfoReq redact(SearchUserInfoReq value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
