// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Room.proto
package com.zq.live.proto.Room;

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
import java.util.List;
import okio.ByteString;

/**
 * 加入通知消息
 */
public final class JoinNoticeMsg extends Message<JoinNoticeMsg, JoinNoticeMsg.Builder> {
  public static final ProtoAdapter<JoinNoticeMsg> ADAPTER = new ProtoAdapter_JoinNoticeMsg();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_HASJOINEDUSERCNT = 0;

  public static final Integer DEFAULT_READYCLOCKRESMS = 0;

  /**
   * 加入游戏的信息
   */
  @WireField(
      tag = 1,
      adapter = "com.zq.live.proto.Room.JoinInfo#ADAPTER",
      label = WireField.Label.REPEATED
  )
  public final List<JoinInfo> joinInfo;

  /**
   * 已经加入游戏的人数
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  public final Integer hasJoinedUserCnt;

  /**
   * 准备时钟剩余秒数,最后一个玩家加入后，赋值
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#SINT32"
  )
  public final Integer readyClockResMs;

  public JoinNoticeMsg(List<JoinInfo> joinInfo, Integer hasJoinedUserCnt, Integer readyClockResMs) {
    this(joinInfo, hasJoinedUserCnt, readyClockResMs, ByteString.EMPTY);
  }

  public JoinNoticeMsg(List<JoinInfo> joinInfo, Integer hasJoinedUserCnt, Integer readyClockResMs,
      ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.joinInfo = Internal.immutableCopyOf("joinInfo", joinInfo);
    this.hasJoinedUserCnt = hasJoinedUserCnt;
    this.readyClockResMs = readyClockResMs;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.joinInfo = Internal.copyOf("joinInfo", joinInfo);
    builder.hasJoinedUserCnt = hasJoinedUserCnt;
    builder.readyClockResMs = readyClockResMs;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof JoinNoticeMsg)) return false;
    JoinNoticeMsg o = (JoinNoticeMsg) other;
    return unknownFields().equals(o.unknownFields())
        && joinInfo.equals(o.joinInfo)
        && Internal.equals(hasJoinedUserCnt, o.hasJoinedUserCnt)
        && Internal.equals(readyClockResMs, o.readyClockResMs);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + joinInfo.hashCode();
      result = result * 37 + (hasJoinedUserCnt != null ? hasJoinedUserCnt.hashCode() : 0);
      result = result * 37 + (readyClockResMs != null ? readyClockResMs.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (!joinInfo.isEmpty()) builder.append(", joinInfo=").append(joinInfo);
    if (hasJoinedUserCnt != null) builder.append(", hasJoinedUserCnt=").append(hasJoinedUserCnt);
    if (readyClockResMs != null) builder.append(", readyClockResMs=").append(readyClockResMs);
    return builder.replace(0, 2, "JoinNoticeMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return JoinNoticeMsg.ADAPTER.encode(this);
  }

  public static final JoinNoticeMsg parseFrom(byte[] data) throws IOException {
    JoinNoticeMsg c = null;
       c = JoinNoticeMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 加入游戏的信息
   */
  public List<JoinInfo> getJoinInfoList() {
    if(joinInfo==null){
        return new java.util.ArrayList<JoinInfo>();
    }
    return joinInfo;
  }

  /**
   * 已经加入游戏的人数
   */
  public Integer getHasJoinedUserCnt() {
    if(hasJoinedUserCnt==null){
        return DEFAULT_HASJOINEDUSERCNT;
    }
    return hasJoinedUserCnt;
  }

  /**
   * 准备时钟剩余秒数,最后一个玩家加入后，赋值
   */
  public Integer getReadyClockResMs() {
    if(readyClockResMs==null){
        return DEFAULT_READYCLOCKRESMS;
    }
    return readyClockResMs;
  }

  /**
   * 加入游戏的信息
   */
  public boolean hasJoinInfoList() {
    return joinInfo!=null;
  }

  /**
   * 已经加入游戏的人数
   */
  public boolean hasHasJoinedUserCnt() {
    return hasJoinedUserCnt!=null;
  }

  /**
   * 准备时钟剩余秒数,最后一个玩家加入后，赋值
   */
  public boolean hasReadyClockResMs() {
    return readyClockResMs!=null;
  }

  public static final class Builder extends Message.Builder<JoinNoticeMsg, Builder> {
    public List<JoinInfo> joinInfo;

    public Integer hasJoinedUserCnt;

    public Integer readyClockResMs;

    public Builder() {
      joinInfo = Internal.newMutableList();
    }

    /**
     * 加入游戏的信息
     */
    public Builder addAllJoinInfo(List<JoinInfo> joinInfo) {
      Internal.checkElementsNotNull(joinInfo);
      this.joinInfo = joinInfo;
      return this;
    }

    /**
     * 已经加入游戏的人数
     */
    public Builder setHasJoinedUserCnt(Integer hasJoinedUserCnt) {
      this.hasJoinedUserCnt = hasJoinedUserCnt;
      return this;
    }

    /**
     * 准备时钟剩余秒数,最后一个玩家加入后，赋值
     */
    public Builder setReadyClockResMs(Integer readyClockResMs) {
      this.readyClockResMs = readyClockResMs;
      return this;
    }

    @Override
    public JoinNoticeMsg build() {
      return new JoinNoticeMsg(joinInfo, hasJoinedUserCnt, readyClockResMs, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_JoinNoticeMsg extends ProtoAdapter<JoinNoticeMsg> {
    public ProtoAdapter_JoinNoticeMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, JoinNoticeMsg.class);
    }

    @Override
    public int encodedSize(JoinNoticeMsg value) {
      return JoinInfo.ADAPTER.asRepeated().encodedSizeWithTag(1, value.joinInfo)
          + ProtoAdapter.UINT32.encodedSizeWithTag(2, value.hasJoinedUserCnt)
          + ProtoAdapter.SINT32.encodedSizeWithTag(3, value.readyClockResMs)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, JoinNoticeMsg value) throws IOException {
      JoinInfo.ADAPTER.asRepeated().encodeWithTag(writer, 1, value.joinInfo);
      ProtoAdapter.UINT32.encodeWithTag(writer, 2, value.hasJoinedUserCnt);
      ProtoAdapter.SINT32.encodeWithTag(writer, 3, value.readyClockResMs);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public JoinNoticeMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.joinInfo.add(JoinInfo.ADAPTER.decode(reader)); break;
          case 2: builder.setHasJoinedUserCnt(ProtoAdapter.UINT32.decode(reader)); break;
          case 3: builder.setReadyClockResMs(ProtoAdapter.SINT32.decode(reader)); break;
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
    public JoinNoticeMsg redact(JoinNoticeMsg value) {
      Builder builder = value.newBuilder();
      Internal.redactElements(builder.joinInfo, JoinInfo.ADAPTER);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
