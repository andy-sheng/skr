// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: party_room.proto
package com.zq.live.proto.PartyRoom;

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

public final class PExitGameMsg extends Message<PExitGameMsg, PExitGameMsg.Builder> {
  public static final ProtoAdapter<PExitGameMsg> ADAPTER = new ProtoAdapter_PExitGameMsg();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_ONLINEUSERCNT = 0;

  public static final Integer DEFAULT_APPLYUSERCNT = 0;

  /**
   * 用户信息
   */
  @WireField(
      tag = 1,
      adapter = "com.zq.live.proto.PartyRoom.POnlineInfo#ADAPTER"
  )
  private final POnlineInfo user;

  /**
   * 在线人数
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer onlineUserCnt;

  /**
   * 申请人数
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer applyUserCnt;

  public PExitGameMsg(POnlineInfo user, Integer onlineUserCnt, Integer applyUserCnt) {
    this(user, onlineUserCnt, applyUserCnt, ByteString.EMPTY);
  }

  public PExitGameMsg(POnlineInfo user, Integer onlineUserCnt, Integer applyUserCnt,
      ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.user = user;
    this.onlineUserCnt = onlineUserCnt;
    this.applyUserCnt = applyUserCnt;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.user = user;
    builder.onlineUserCnt = onlineUserCnt;
    builder.applyUserCnt = applyUserCnt;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof PExitGameMsg)) return false;
    PExitGameMsg o = (PExitGameMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(user, o.user)
        && Internal.equals(onlineUserCnt, o.onlineUserCnt)
        && Internal.equals(applyUserCnt, o.applyUserCnt);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (user != null ? user.hashCode() : 0);
      result = result * 37 + (onlineUserCnt != null ? onlineUserCnt.hashCode() : 0);
      result = result * 37 + (applyUserCnt != null ? applyUserCnt.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (user != null) builder.append(", user=").append(user);
    if (onlineUserCnt != null) builder.append(", onlineUserCnt=").append(onlineUserCnt);
    if (applyUserCnt != null) builder.append(", applyUserCnt=").append(applyUserCnt);
    return builder.replace(0, 2, "PExitGameMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return PExitGameMsg.ADAPTER.encode(this);
  }

  public static final PExitGameMsg parseFrom(byte[] data) throws IOException {
    PExitGameMsg c = null;
       c = PExitGameMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 用户信息
   */
  public POnlineInfo getUser() {
    if(user==null){
        return new POnlineInfo.Builder().build();
    }
    return user;
  }

  /**
   * 在线人数
   */
  public Integer getOnlineUserCnt() {
    if(onlineUserCnt==null){
        return DEFAULT_ONLINEUSERCNT;
    }
    return onlineUserCnt;
  }

  /**
   * 申请人数
   */
  public Integer getApplyUserCnt() {
    if(applyUserCnt==null){
        return DEFAULT_APPLYUSERCNT;
    }
    return applyUserCnt;
  }

  /**
   * 用户信息
   */
  public boolean hasUser() {
    return user!=null;
  }

  /**
   * 在线人数
   */
  public boolean hasOnlineUserCnt() {
    return onlineUserCnt!=null;
  }

  /**
   * 申请人数
   */
  public boolean hasApplyUserCnt() {
    return applyUserCnt!=null;
  }

  public static final class Builder extends Message.Builder<PExitGameMsg, Builder> {
    private POnlineInfo user;

    private Integer onlineUserCnt;

    private Integer applyUserCnt;

    public Builder() {
    }

    /**
     * 用户信息
     */
    public Builder setUser(POnlineInfo user) {
      this.user = user;
      return this;
    }

    /**
     * 在线人数
     */
    public Builder setOnlineUserCnt(Integer onlineUserCnt) {
      this.onlineUserCnt = onlineUserCnt;
      return this;
    }

    /**
     * 申请人数
     */
    public Builder setApplyUserCnt(Integer applyUserCnt) {
      this.applyUserCnt = applyUserCnt;
      return this;
    }

    @Override
    public PExitGameMsg build() {
      return new PExitGameMsg(user, onlineUserCnt, applyUserCnt, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_PExitGameMsg extends ProtoAdapter<PExitGameMsg> {
    public ProtoAdapter_PExitGameMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, PExitGameMsg.class);
    }

    @Override
    public int encodedSize(PExitGameMsg value) {
      return POnlineInfo.ADAPTER.encodedSizeWithTag(1, value.user)
          + ProtoAdapter.UINT32.encodedSizeWithTag(2, value.onlineUserCnt)
          + ProtoAdapter.UINT32.encodedSizeWithTag(3, value.applyUserCnt)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, PExitGameMsg value) throws IOException {
      POnlineInfo.ADAPTER.encodeWithTag(writer, 1, value.user);
      ProtoAdapter.UINT32.encodeWithTag(writer, 2, value.onlineUserCnt);
      ProtoAdapter.UINT32.encodeWithTag(writer, 3, value.applyUserCnt);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public PExitGameMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setUser(POnlineInfo.ADAPTER.decode(reader)); break;
          case 2: builder.setOnlineUserCnt(ProtoAdapter.UINT32.decode(reader)); break;
          case 3: builder.setApplyUserCnt(ProtoAdapter.UINT32.decode(reader)); break;
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
    public PExitGameMsg redact(PExitGameMsg value) {
      Builder builder = value.newBuilder();
      if (builder.user != null) builder.user = POnlineInfo.ADAPTER.redact(builder.user);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
