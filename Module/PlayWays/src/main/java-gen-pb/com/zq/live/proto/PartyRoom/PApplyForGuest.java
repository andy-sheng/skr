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
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class PApplyForGuest extends Message<PApplyForGuest, PApplyForGuest.Builder> {
  public static final ProtoAdapter<PApplyForGuest> ADAPTER = new ProtoAdapter_PApplyForGuest();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_APPLYUSERCNT = 0;

  public static final Boolean DEFAULT_CANCEL = false;

  /**
   * 用户信息
   */
  @WireField(
      tag = 1,
      adapter = "com.zq.live.proto.PartyRoom.POnlineInfo#ADAPTER"
  )
  private final POnlineInfo user;

  /**
   * 申请人数
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer applyUserCnt;

  /**
   * 是否取消申请
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  private final Boolean cancel;

  /**
   * 执行者(若为空，则表示user自己执行)
   */
  @WireField(
      tag = 4,
      adapter = "com.zq.live.proto.PartyRoom.POnlineInfo#ADAPTER"
  )
  private final POnlineInfo opUser;

  public PApplyForGuest(POnlineInfo user, Integer applyUserCnt, Boolean cancel,
      POnlineInfo opUser) {
    this(user, applyUserCnt, cancel, opUser, ByteString.EMPTY);
  }

  public PApplyForGuest(POnlineInfo user, Integer applyUserCnt, Boolean cancel, POnlineInfo opUser,
      ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.user = user;
    this.applyUserCnt = applyUserCnt;
    this.cancel = cancel;
    this.opUser = opUser;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.user = user;
    builder.applyUserCnt = applyUserCnt;
    builder.cancel = cancel;
    builder.opUser = opUser;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof PApplyForGuest)) return false;
    PApplyForGuest o = (PApplyForGuest) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(user, o.user)
        && Internal.equals(applyUserCnt, o.applyUserCnt)
        && Internal.equals(cancel, o.cancel)
        && Internal.equals(opUser, o.opUser);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (user != null ? user.hashCode() : 0);
      result = result * 37 + (applyUserCnt != null ? applyUserCnt.hashCode() : 0);
      result = result * 37 + (cancel != null ? cancel.hashCode() : 0);
      result = result * 37 + (opUser != null ? opUser.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (user != null) builder.append(", user=").append(user);
    if (applyUserCnt != null) builder.append(", applyUserCnt=").append(applyUserCnt);
    if (cancel != null) builder.append(", cancel=").append(cancel);
    if (opUser != null) builder.append(", opUser=").append(opUser);
    return builder.replace(0, 2, "PApplyForGuest{").append('}').toString();
  }

  public byte[] toByteArray() {
    return PApplyForGuest.ADAPTER.encode(this);
  }

  public static final PApplyForGuest parseFrom(byte[] data) throws IOException {
    PApplyForGuest c = null;
       c = PApplyForGuest.ADAPTER.decode(data);
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
   * 申请人数
   */
  public Integer getApplyUserCnt() {
    if(applyUserCnt==null){
        return DEFAULT_APPLYUSERCNT;
    }
    return applyUserCnt;
  }

  /**
   * 是否取消申请
   */
  public Boolean getCancel() {
    if(cancel==null){
        return DEFAULT_CANCEL;
    }
    return cancel;
  }

  /**
   * 执行者(若为空，则表示user自己执行)
   */
  public POnlineInfo getOpUser() {
    if(opUser==null){
        return new POnlineInfo.Builder().build();
    }
    return opUser;
  }

  /**
   * 用户信息
   */
  public boolean hasUser() {
    return user!=null;
  }

  /**
   * 申请人数
   */
  public boolean hasApplyUserCnt() {
    return applyUserCnt!=null;
  }

  /**
   * 是否取消申请
   */
  public boolean hasCancel() {
    return cancel!=null;
  }

  /**
   * 执行者(若为空，则表示user自己执行)
   */
  public boolean hasOpUser() {
    return opUser!=null;
  }

  public static final class Builder extends Message.Builder<PApplyForGuest, Builder> {
    private POnlineInfo user;

    private Integer applyUserCnt;

    private Boolean cancel;

    private POnlineInfo opUser;

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
     * 申请人数
     */
    public Builder setApplyUserCnt(Integer applyUserCnt) {
      this.applyUserCnt = applyUserCnt;
      return this;
    }

    /**
     * 是否取消申请
     */
    public Builder setCancel(Boolean cancel) {
      this.cancel = cancel;
      return this;
    }

    /**
     * 执行者(若为空，则表示user自己执行)
     */
    public Builder setOpUser(POnlineInfo opUser) {
      this.opUser = opUser;
      return this;
    }

    @Override
    public PApplyForGuest build() {
      return new PApplyForGuest(user, applyUserCnt, cancel, opUser, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_PApplyForGuest extends ProtoAdapter<PApplyForGuest> {
    public ProtoAdapter_PApplyForGuest() {
      super(FieldEncoding.LENGTH_DELIMITED, PApplyForGuest.class);
    }

    @Override
    public int encodedSize(PApplyForGuest value) {
      return POnlineInfo.ADAPTER.encodedSizeWithTag(1, value.user)
          + ProtoAdapter.UINT32.encodedSizeWithTag(2, value.applyUserCnt)
          + ProtoAdapter.BOOL.encodedSizeWithTag(3, value.cancel)
          + POnlineInfo.ADAPTER.encodedSizeWithTag(4, value.opUser)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, PApplyForGuest value) throws IOException {
      POnlineInfo.ADAPTER.encodeWithTag(writer, 1, value.user);
      ProtoAdapter.UINT32.encodeWithTag(writer, 2, value.applyUserCnt);
      ProtoAdapter.BOOL.encodeWithTag(writer, 3, value.cancel);
      POnlineInfo.ADAPTER.encodeWithTag(writer, 4, value.opUser);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public PApplyForGuest decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setUser(POnlineInfo.ADAPTER.decode(reader)); break;
          case 2: builder.setApplyUserCnt(ProtoAdapter.UINT32.decode(reader)); break;
          case 3: builder.setCancel(ProtoAdapter.BOOL.decode(reader)); break;
          case 4: builder.setOpUser(POnlineInfo.ADAPTER.decode(reader)); break;
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
    public PApplyForGuest redact(PApplyForGuest value) {
      Builder builder = value.newBuilder();
      if (builder.user != null) builder.user = POnlineInfo.ADAPTER.redact(builder.user);
      if (builder.opUser != null) builder.opUser = POnlineInfo.ADAPTER.redact(builder.opUser);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
