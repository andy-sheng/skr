// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Private.proto
package com.zq.live.proto.Private;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import com.zq.live.proto.Common.UserInfo;
import java.io.IOException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

/**
 * java -jar -Dfile.encoding=UTF-8 ./proto/wire-compiler-2.3.0-SNAPSHOT-jar-with-dependencies.jar \
 * --proto_path=./pb --java_out=./Module/RankingMode/src/main/java-gen-pb/ Private.proto
 * 私聊消息：此结构会通过pb编码后，通过IM单聊通道到达客户端
 */
public final class PrivateMsg extends Message<PrivateMsg, PrivateMsg.Builder> {
  public static final ProtoAdapter<PrivateMsg> ADAPTER = new ProtoAdapter_PrivateMsg();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_TIMEMS = 0L;

  public static final EPrivateMsgType DEFAULT_MSGTYPE = EPrivateMsgType.PM_UNKNOWN;

  public static final Integer DEFAULT_TOUSERID = 0;

  public static final Long DEFAULT_NO = 0L;

  /**
   * 房间消息产生时间，单位毫秒
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  public final Long timeMs;

  /**
   * 消息类型
   */
  @WireField(
      tag = 2,
      adapter = "com.zq.live.proto.Private.EPrivateMsgType#ADAPTER"
  )
  public final EPrivateMsgType msgType;

  /**
   * 消息接受者的ID
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  public final Integer toUserID;

  /**
   * 消息序号
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  public final Long no;

  /**
   * 发送者简要信息
   */
  @WireField(
      tag = 5,
      adapter = "com.zq.live.proto.Common.UserInfo#ADAPTER"
  )
  public final UserInfo sender;

  /**
   * 文字消息，当MsgType == PM_TEXT 时应存在
   */
  @WireField(
      tag = 10,
      adapter = "com.zq.live.proto.Private.TextMsg#ADAPTER"
  )
  public final TextMsg textMsg;

  public PrivateMsg(Long timeMs, EPrivateMsgType msgType, Integer toUserID, Long no,
      UserInfo sender, TextMsg textMsg) {
    this(timeMs, msgType, toUserID, no, sender, textMsg, ByteString.EMPTY);
  }

  public PrivateMsg(Long timeMs, EPrivateMsgType msgType, Integer toUserID, Long no,
      UserInfo sender, TextMsg textMsg, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.timeMs = timeMs;
    this.msgType = msgType;
    this.toUserID = toUserID;
    this.no = no;
    this.sender = sender;
    this.textMsg = textMsg;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.timeMs = timeMs;
    builder.msgType = msgType;
    builder.toUserID = toUserID;
    builder.no = no;
    builder.sender = sender;
    builder.textMsg = textMsg;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof PrivateMsg)) return false;
    PrivateMsg o = (PrivateMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(timeMs, o.timeMs)
        && Internal.equals(msgType, o.msgType)
        && Internal.equals(toUserID, o.toUserID)
        && Internal.equals(no, o.no)
        && Internal.equals(sender, o.sender)
        && Internal.equals(textMsg, o.textMsg);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (timeMs != null ? timeMs.hashCode() : 0);
      result = result * 37 + (msgType != null ? msgType.hashCode() : 0);
      result = result * 37 + (toUserID != null ? toUserID.hashCode() : 0);
      result = result * 37 + (no != null ? no.hashCode() : 0);
      result = result * 37 + (sender != null ? sender.hashCode() : 0);
      result = result * 37 + (textMsg != null ? textMsg.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (timeMs != null) builder.append(", timeMs=").append(timeMs);
    if (msgType != null) builder.append(", msgType=").append(msgType);
    if (toUserID != null) builder.append(", toUserID=").append(toUserID);
    if (no != null) builder.append(", no=").append(no);
    if (sender != null) builder.append(", sender=").append(sender);
    if (textMsg != null) builder.append(", textMsg=").append(textMsg);
    return builder.replace(0, 2, "PrivateMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return PrivateMsg.ADAPTER.encode(this);
  }

  public static final PrivateMsg parseFrom(byte[] data) throws IOException {
    PrivateMsg c = null;
       c = PrivateMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 房间消息产生时间，单位毫秒
   */
  public Long getTimeMs() {
    if(timeMs==null){
        return DEFAULT_TIMEMS;
    }
    return timeMs;
  }

  /**
   * 消息类型
   */
  public EPrivateMsgType getMsgType() {
    if(msgType==null){
        return new EPrivateMsgType.Builder().build();
    }
    return msgType;
  }

  /**
   * 消息接受者的ID
   */
  public Integer getToUserID() {
    if(toUserID==null){
        return DEFAULT_TOUSERID;
    }
    return toUserID;
  }

  /**
   * 消息序号
   */
  public Long getNo() {
    if(no==null){
        return DEFAULT_NO;
    }
    return no;
  }

  /**
   * 发送者简要信息
   */
  public UserInfo getSender() {
    if(sender==null){
        return new UserInfo.Builder().build();
    }
    return sender;
  }

  /**
   * 文字消息，当MsgType == PM_TEXT 时应存在
   */
  public TextMsg getTextMsg() {
    if(textMsg==null){
        return new TextMsg.Builder().build();
    }
    return textMsg;
  }

  /**
   * 房间消息产生时间，单位毫秒
   */
  public boolean hasTimeMs() {
    return timeMs!=null;
  }

  /**
   * 消息类型
   */
  public boolean hasMsgType() {
    return msgType!=null;
  }

  /**
   * 消息接受者的ID
   */
  public boolean hasToUserID() {
    return toUserID!=null;
  }

  /**
   * 消息序号
   */
  public boolean hasNo() {
    return no!=null;
  }

  /**
   * 发送者简要信息
   */
  public boolean hasSender() {
    return sender!=null;
  }

  /**
   * 文字消息，当MsgType == PM_TEXT 时应存在
   */
  public boolean hasTextMsg() {
    return textMsg!=null;
  }

  public static final class Builder extends Message.Builder<PrivateMsg, Builder> {
    public Long timeMs;

    public EPrivateMsgType msgType;

    public Integer toUserID;

    public Long no;

    public UserInfo sender;

    public TextMsg textMsg;

    public Builder() {
    }

    /**
     * 房间消息产生时间，单位毫秒
     */
    public Builder setTimeMs(Long timeMs) {
      this.timeMs = timeMs;
      return this;
    }

    /**
     * 消息类型
     */
    public Builder setMsgType(EPrivateMsgType msgType) {
      this.msgType = msgType;
      return this;
    }

    /**
     * 消息接受者的ID
     */
    public Builder setToUserID(Integer toUserID) {
      this.toUserID = toUserID;
      return this;
    }

    /**
     * 消息序号
     */
    public Builder setNo(Long no) {
      this.no = no;
      return this;
    }

    /**
     * 发送者简要信息
     */
    public Builder setSender(UserInfo sender) {
      this.sender = sender;
      return this;
    }

    /**
     * 文字消息，当MsgType == PM_TEXT 时应存在
     */
    public Builder setTextMsg(TextMsg textMsg) {
      this.textMsg = textMsg;
      return this;
    }

    @Override
    public PrivateMsg build() {
      return new PrivateMsg(timeMs, msgType, toUserID, no, sender, textMsg, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_PrivateMsg extends ProtoAdapter<PrivateMsg> {
    public ProtoAdapter_PrivateMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, PrivateMsg.class);
    }

    @Override
    public int encodedSize(PrivateMsg value) {
      return ProtoAdapter.SINT64.encodedSizeWithTag(1, value.timeMs)
          + EPrivateMsgType.ADAPTER.encodedSizeWithTag(2, value.msgType)
          + ProtoAdapter.UINT32.encodedSizeWithTag(3, value.toUserID)
          + ProtoAdapter.SINT64.encodedSizeWithTag(4, value.no)
          + UserInfo.ADAPTER.encodedSizeWithTag(5, value.sender)
          + TextMsg.ADAPTER.encodedSizeWithTag(10, value.textMsg)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, PrivateMsg value) throws IOException {
      ProtoAdapter.SINT64.encodeWithTag(writer, 1, value.timeMs);
      EPrivateMsgType.ADAPTER.encodeWithTag(writer, 2, value.msgType);
      ProtoAdapter.UINT32.encodeWithTag(writer, 3, value.toUserID);
      ProtoAdapter.SINT64.encodeWithTag(writer, 4, value.no);
      UserInfo.ADAPTER.encodeWithTag(writer, 5, value.sender);
      TextMsg.ADAPTER.encodeWithTag(writer, 10, value.textMsg);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public PrivateMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
          case 2: {
            try {
              builder.setMsgType(EPrivateMsgType.ADAPTER.decode(reader));
            } catch (ProtoAdapter.EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
          case 3: builder.setToUserID(ProtoAdapter.UINT32.decode(reader)); break;
          case 4: builder.setNo(ProtoAdapter.SINT64.decode(reader)); break;
          case 5: builder.setSender(UserInfo.ADAPTER.decode(reader)); break;
          case 10: builder.setTextMsg(TextMsg.ADAPTER.decode(reader)); break;
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
    public PrivateMsg redact(PrivateMsg value) {
      Builder builder = value.newBuilder();
      if (builder.sender != null) builder.sender = UserInfo.ADAPTER.redact(builder.sender);
      if (builder.textMsg != null) builder.textMsg = TextMsg.ADAPTER.redact(builder.textMsg);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
