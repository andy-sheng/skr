// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Room.proto
package com.zq.live.proto.GrabRoom;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import com.zq.live.proto.Common.UserInfo;
import java.io.IOException;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class JoinInfo extends Message<JoinInfo, JoinInfo.Builder> {
  public static final ProtoAdapter<JoinInfo> ADAPTER = new ProtoAdapter_JoinInfo();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_USERID = 0;

  public static final Integer DEFAULT_JOINSEQ = 0;

  public static final Long DEFAULT_JOINTIMEMS = 0L;

  public static final Boolean DEFAULT_ISSKRER = false;

  /**
   * 用户ID
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer userID;

  /**
   * 加入顺序
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer joinSeq;

  /**
   * 加入毫秒时间戳
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long joinTimeMs;

  /**
   * 用户详细资料，一唱到底会用到
   */
  @WireField(
      tag = 4,
      adapter = "com.zq.live.proto.Common.UserInfo#ADAPTER"
  )
  private final UserInfo userInfo;

  /**
   * 是否机器人
   */
  @WireField(
      tag = 5,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  private final Boolean isSkrer;

  public JoinInfo(Integer userID, Integer joinSeq, Long joinTimeMs, UserInfo userInfo,
      Boolean isSkrer) {
    this(userID, joinSeq, joinTimeMs, userInfo, isSkrer, ByteString.EMPTY);
  }

  public JoinInfo(Integer userID, Integer joinSeq, Long joinTimeMs, UserInfo userInfo,
      Boolean isSkrer, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.userID = userID;
    this.joinSeq = joinSeq;
    this.joinTimeMs = joinTimeMs;
    this.userInfo = userInfo;
    this.isSkrer = isSkrer;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.userID = userID;
    builder.joinSeq = joinSeq;
    builder.joinTimeMs = joinTimeMs;
    builder.userInfo = userInfo;
    builder.isSkrer = isSkrer;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof JoinInfo)) return false;
    JoinInfo o = (JoinInfo) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(userID, o.userID)
        && Internal.equals(joinSeq, o.joinSeq)
        && Internal.equals(joinTimeMs, o.joinTimeMs)
        && Internal.equals(userInfo, o.userInfo)
        && Internal.equals(isSkrer, o.isSkrer);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (userID != null ? userID.hashCode() : 0);
      result = result * 37 + (joinSeq != null ? joinSeq.hashCode() : 0);
      result = result * 37 + (joinTimeMs != null ? joinTimeMs.hashCode() : 0);
      result = result * 37 + (userInfo != null ? userInfo.hashCode() : 0);
      result = result * 37 + (isSkrer != null ? isSkrer.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (userID != null) builder.append(", userID=").append(userID);
    if (joinSeq != null) builder.append(", joinSeq=").append(joinSeq);
    if (joinTimeMs != null) builder.append(", joinTimeMs=").append(joinTimeMs);
    if (userInfo != null) builder.append(", userInfo=").append(userInfo);
    if (isSkrer != null) builder.append(", isSkrer=").append(isSkrer);
    return builder.replace(0, 2, "JoinInfo{").append('}').toString();
  }

  public byte[] toByteArray() {
    return JoinInfo.ADAPTER.encode(this);
  }

  public static final JoinInfo parseFrom(byte[] data) throws IOException {
    JoinInfo c = null;
       c = JoinInfo.ADAPTER.decode(data);
    return c;
  }

  /**
   * 用户ID
   */
  public Integer getUserID() {
    if(userID==null){
        return DEFAULT_USERID;
    }
    return userID;
  }

  /**
   * 加入顺序
   */
  public Integer getJoinSeq() {
    if(joinSeq==null){
        return DEFAULT_JOINSEQ;
    }
    return joinSeq;
  }

  /**
   * 加入毫秒时间戳
   */
  public Long getJoinTimeMs() {
    if(joinTimeMs==null){
        return DEFAULT_JOINTIMEMS;
    }
    return joinTimeMs;
  }

  /**
   * 用户详细资料，一唱到底会用到
   */
  public UserInfo getUserInfo() {
    if(userInfo==null){
        return new UserInfo.Builder().build();
    }
    return userInfo;
  }

  /**
   * 是否机器人
   */
  public Boolean getIsSkrer() {
    if(isSkrer==null){
        return DEFAULT_ISSKRER;
    }
    return isSkrer;
  }

  /**
   * 用户ID
   */
  public boolean hasUserID() {
    return userID!=null;
  }

  /**
   * 加入顺序
   */
  public boolean hasJoinSeq() {
    return joinSeq!=null;
  }

  /**
   * 加入毫秒时间戳
   */
  public boolean hasJoinTimeMs() {
    return joinTimeMs!=null;
  }

  /**
   * 用户详细资料，一唱到底会用到
   */
  public boolean hasUserInfo() {
    return userInfo!=null;
  }

  /**
   * 是否机器人
   */
  public boolean hasIsSkrer() {
    return isSkrer!=null;
  }

  public static final class Builder extends Message.Builder<JoinInfo, Builder> {
    private Integer userID;

    private Integer joinSeq;

    private Long joinTimeMs;

    private UserInfo userInfo;

    private Boolean isSkrer;

    public Builder() {
    }

    /**
     * 用户ID
     */
    public Builder setUserID(Integer userID) {
      this.userID = userID;
      return this;
    }

    /**
     * 加入顺序
     */
    public Builder setJoinSeq(Integer joinSeq) {
      this.joinSeq = joinSeq;
      return this;
    }

    /**
     * 加入毫秒时间戳
     */
    public Builder setJoinTimeMs(Long joinTimeMs) {
      this.joinTimeMs = joinTimeMs;
      return this;
    }

    /**
     * 用户详细资料，一唱到底会用到
     */
    public Builder setUserInfo(UserInfo userInfo) {
      this.userInfo = userInfo;
      return this;
    }

    /**
     * 是否机器人
     */
    public Builder setIsSkrer(Boolean isSkrer) {
      this.isSkrer = isSkrer;
      return this;
    }

    @Override
    public JoinInfo build() {
      return new JoinInfo(userID, joinSeq, joinTimeMs, userInfo, isSkrer, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_JoinInfo extends ProtoAdapter<JoinInfo> {
    public ProtoAdapter_JoinInfo() {
      super(FieldEncoding.LENGTH_DELIMITED, JoinInfo.class);
    }

    @Override
    public int encodedSize(JoinInfo value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.userID)
          + ProtoAdapter.UINT32.encodedSizeWithTag(2, value.joinSeq)
          + ProtoAdapter.SINT64.encodedSizeWithTag(3, value.joinTimeMs)
          + UserInfo.ADAPTER.encodedSizeWithTag(4, value.userInfo)
          + ProtoAdapter.BOOL.encodedSizeWithTag(5, value.isSkrer)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, JoinInfo value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.userID);
      ProtoAdapter.UINT32.encodeWithTag(writer, 2, value.joinSeq);
      ProtoAdapter.SINT64.encodeWithTag(writer, 3, value.joinTimeMs);
      UserInfo.ADAPTER.encodeWithTag(writer, 4, value.userInfo);
      ProtoAdapter.BOOL.encodeWithTag(writer, 5, value.isSkrer);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public JoinInfo decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setUserID(ProtoAdapter.UINT32.decode(reader)); break;
          case 2: builder.setJoinSeq(ProtoAdapter.UINT32.decode(reader)); break;
          case 3: builder.setJoinTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
          case 4: builder.setUserInfo(UserInfo.ADAPTER.decode(reader)); break;
          case 5: builder.setIsSkrer(ProtoAdapter.BOOL.decode(reader)); break;
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
    public JoinInfo redact(JoinInfo value) {
      Builder builder = value.newBuilder();
      if (builder.userInfo != null) builder.userInfo = UserInfo.ADAPTER.redact(builder.userInfo);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}