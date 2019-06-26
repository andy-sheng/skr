// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Notification.proto
package com.zq.live.proto.Notification;

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
 * --proto_path=./proto --java_out=./commoncore/src/main/java-gen-pb/ Notification.proto
 * 房间消息：此结构会通过pb编码后，通过系统消息通道到达客户端
 */
public final class NotificationMsg extends Message<NotificationMsg, NotificationMsg.Builder> {
  public static final ProtoAdapter<NotificationMsg> ADAPTER = new ProtoAdapter_NotificationMsg();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_TIMEMS = 0L;

  public static final ENotificationMsgType DEFAULT_MSGTYPE = ENotificationMsgType.NM_UNKNOWN;

  public static final Integer DEFAULT_ROOMID = 0;

  public static final Long DEFAULT_NO = 0L;

  public static final EMsgPosType DEFAULT_POSTYPE = EMsgPosType.EPT_UNKNOWN;

  /**
   * 消息产生时间，单位毫秒
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long timeMs;

  /**
   * 消息类型
   */
  @WireField(
      tag = 2,
      adapter = "com.zq.live.proto.Notification.ENotificationMsgType#ADAPTER"
  )
  private final ENotificationMsgType msgType;

  /**
   * 房间ID,若不需要为0
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer roomID;

  /**
   * 消息序号
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long no;

  /**
   * 消息显示位置类型
   */
  @WireField(
      tag = 5,
      adapter = "com.zq.live.proto.Notification.EMsgPosType#ADAPTER"
  )
  private final EMsgPosType posType;

  /**
   * 发送者简要信息
   */
  @WireField(
      tag = 6,
      adapter = "com.zq.live.proto.Common.UserInfo#ADAPTER"
  )
  private final UserInfo sender;

  @WireField(
      tag = 10,
      adapter = "com.zq.live.proto.Notification.FollowMsg#ADAPTER"
  )
  private final FollowMsg followMsg;

  @WireField(
      tag = 11,
      adapter = "com.zq.live.proto.Notification.InviteStandMsg#ADAPTER"
  )
  private final InviteStandMsg inviteStandMsg;

  @WireField(
      tag = 12,
      adapter = "com.zq.live.proto.Notification.SysWarningMsg#ADAPTER"
  )
  private final SysWarningMsg sysWarningMsg;

  /**
   * 双人房邀请信令
   */
  @WireField(
      tag = 13,
      adapter = "com.zq.live.proto.Notification.CombineRoomInviteMsg#ADAPTER"
  )
  private final CombineRoomInviteMsg inviteMsg;

  /**
   * 双人房进房信令
   */
  @WireField(
      tag = 14,
      adapter = "com.zq.live.proto.Notification.CombineRoomEnterMsg#ADAPTER"
  )
  private final CombineRoomEnterMsg enterMsg;

  public NotificationMsg(Long timeMs, ENotificationMsgType msgType, Integer roomID, Long no,
      EMsgPosType posType, UserInfo sender, FollowMsg followMsg, InviteStandMsg inviteStandMsg,
      SysWarningMsg sysWarningMsg, CombineRoomInviteMsg inviteMsg, CombineRoomEnterMsg enterMsg) {
    this(timeMs, msgType, roomID, no, posType, sender, followMsg, inviteStandMsg, sysWarningMsg, inviteMsg, enterMsg, ByteString.EMPTY);
  }

  public NotificationMsg(Long timeMs, ENotificationMsgType msgType, Integer roomID, Long no,
      EMsgPosType posType, UserInfo sender, FollowMsg followMsg, InviteStandMsg inviteStandMsg,
      SysWarningMsg sysWarningMsg, CombineRoomInviteMsg inviteMsg, CombineRoomEnterMsg enterMsg,
      ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.timeMs = timeMs;
    this.msgType = msgType;
    this.roomID = roomID;
    this.no = no;
    this.posType = posType;
    this.sender = sender;
    this.followMsg = followMsg;
    this.inviteStandMsg = inviteStandMsg;
    this.sysWarningMsg = sysWarningMsg;
    this.inviteMsg = inviteMsg;
    this.enterMsg = enterMsg;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.timeMs = timeMs;
    builder.msgType = msgType;
    builder.roomID = roomID;
    builder.no = no;
    builder.posType = posType;
    builder.sender = sender;
    builder.followMsg = followMsg;
    builder.inviteStandMsg = inviteStandMsg;
    builder.sysWarningMsg = sysWarningMsg;
    builder.inviteMsg = inviteMsg;
    builder.enterMsg = enterMsg;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof NotificationMsg)) return false;
    NotificationMsg o = (NotificationMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(timeMs, o.timeMs)
        && Internal.equals(msgType, o.msgType)
        && Internal.equals(roomID, o.roomID)
        && Internal.equals(no, o.no)
        && Internal.equals(posType, o.posType)
        && Internal.equals(sender, o.sender)
        && Internal.equals(followMsg, o.followMsg)
        && Internal.equals(inviteStandMsg, o.inviteStandMsg)
        && Internal.equals(sysWarningMsg, o.sysWarningMsg)
        && Internal.equals(inviteMsg, o.inviteMsg)
        && Internal.equals(enterMsg, o.enterMsg);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (timeMs != null ? timeMs.hashCode() : 0);
      result = result * 37 + (msgType != null ? msgType.hashCode() : 0);
      result = result * 37 + (roomID != null ? roomID.hashCode() : 0);
      result = result * 37 + (no != null ? no.hashCode() : 0);
      result = result * 37 + (posType != null ? posType.hashCode() : 0);
      result = result * 37 + (sender != null ? sender.hashCode() : 0);
      result = result * 37 + (followMsg != null ? followMsg.hashCode() : 0);
      result = result * 37 + (inviteStandMsg != null ? inviteStandMsg.hashCode() : 0);
      result = result * 37 + (sysWarningMsg != null ? sysWarningMsg.hashCode() : 0);
      result = result * 37 + (inviteMsg != null ? inviteMsg.hashCode() : 0);
      result = result * 37 + (enterMsg != null ? enterMsg.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (timeMs != null) builder.append(", timeMs=").append(timeMs);
    if (msgType != null) builder.append(", msgType=").append(msgType);
    if (roomID != null) builder.append(", roomID=").append(roomID);
    if (no != null) builder.append(", no=").append(no);
    if (posType != null) builder.append(", posType=").append(posType);
    if (sender != null) builder.append(", sender=").append(sender);
    if (followMsg != null) builder.append(", followMsg=").append(followMsg);
    if (inviteStandMsg != null) builder.append(", inviteStandMsg=").append(inviteStandMsg);
    if (sysWarningMsg != null) builder.append(", sysWarningMsg=").append(sysWarningMsg);
    if (inviteMsg != null) builder.append(", inviteMsg=").append(inviteMsg);
    if (enterMsg != null) builder.append(", enterMsg=").append(enterMsg);
    return builder.replace(0, 2, "NotificationMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return NotificationMsg.ADAPTER.encode(this);
  }

  public static final NotificationMsg parseFrom(byte[] data) throws IOException {
    NotificationMsg c = null;
       c = NotificationMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 消息产生时间，单位毫秒
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
  public ENotificationMsgType getMsgType() {
    if(msgType==null){
        return new ENotificationMsgType.Builder().build();
    }
    return msgType;
  }

  /**
   * 房间ID,若不需要为0
   */
  public Integer getRoomID() {
    if(roomID==null){
        return DEFAULT_ROOMID;
    }
    return roomID;
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
   * 消息显示位置类型
   */
  public EMsgPosType getPosType() {
    if(posType==null){
        return new EMsgPosType.Builder().build();
    }
    return posType;
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

  public FollowMsg getFollowMsg() {
    if(followMsg==null){
        return new FollowMsg.Builder().build();
    }
    return followMsg;
  }

  public InviteStandMsg getInviteStandMsg() {
    if(inviteStandMsg==null){
        return new InviteStandMsg.Builder().build();
    }
    return inviteStandMsg;
  }

  public SysWarningMsg getSysWarningMsg() {
    if(sysWarningMsg==null){
        return new SysWarningMsg.Builder().build();
    }
    return sysWarningMsg;
  }

  /**
   * 双人房邀请信令
   */
  public CombineRoomInviteMsg getInviteMsg() {
    if(inviteMsg==null){
        return new CombineRoomInviteMsg.Builder().build();
    }
    return inviteMsg;
  }

  /**
   * 双人房进房信令
   */
  public CombineRoomEnterMsg getEnterMsg() {
    if(enterMsg==null){
        return new CombineRoomEnterMsg.Builder().build();
    }
    return enterMsg;
  }

  /**
   * 消息产生时间，单位毫秒
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
   * 房间ID,若不需要为0
   */
  public boolean hasRoomID() {
    return roomID!=null;
  }

  /**
   * 消息序号
   */
  public boolean hasNo() {
    return no!=null;
  }

  /**
   * 消息显示位置类型
   */
  public boolean hasPosType() {
    return posType!=null;
  }

  /**
   * 发送者简要信息
   */
  public boolean hasSender() {
    return sender!=null;
  }

  public boolean hasFollowMsg() {
    return followMsg!=null;
  }

  public boolean hasInviteStandMsg() {
    return inviteStandMsg!=null;
  }

  public boolean hasSysWarningMsg() {
    return sysWarningMsg!=null;
  }

  /**
   * 双人房邀请信令
   */
  public boolean hasInviteMsg() {
    return inviteMsg!=null;
  }

  /**
   * 双人房进房信令
   */
  public boolean hasEnterMsg() {
    return enterMsg!=null;
  }

  public static final class Builder extends Message.Builder<NotificationMsg, Builder> {
    private Long timeMs;

    private ENotificationMsgType msgType;

    private Integer roomID;

    private Long no;

    private EMsgPosType posType;

    private UserInfo sender;

    private FollowMsg followMsg;

    private InviteStandMsg inviteStandMsg;

    private SysWarningMsg sysWarningMsg;

    private CombineRoomInviteMsg inviteMsg;

    private CombineRoomEnterMsg enterMsg;

    public Builder() {
    }

    /**
     * 消息产生时间，单位毫秒
     */
    public Builder setTimeMs(Long timeMs) {
      this.timeMs = timeMs;
      return this;
    }

    /**
     * 消息类型
     */
    public Builder setMsgType(ENotificationMsgType msgType) {
      this.msgType = msgType;
      return this;
    }

    /**
     * 房间ID,若不需要为0
     */
    public Builder setRoomID(Integer roomID) {
      this.roomID = roomID;
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
     * 消息显示位置类型
     */
    public Builder setPosType(EMsgPosType posType) {
      this.posType = posType;
      return this;
    }

    /**
     * 发送者简要信息
     */
    public Builder setSender(UserInfo sender) {
      this.sender = sender;
      return this;
    }

    public Builder setFollowMsg(FollowMsg followMsg) {
      this.followMsg = followMsg;
      return this;
    }

    public Builder setInviteStandMsg(InviteStandMsg inviteStandMsg) {
      this.inviteStandMsg = inviteStandMsg;
      return this;
    }

    public Builder setSysWarningMsg(SysWarningMsg sysWarningMsg) {
      this.sysWarningMsg = sysWarningMsg;
      return this;
    }

    /**
     * 双人房邀请信令
     */
    public Builder setInviteMsg(CombineRoomInviteMsg inviteMsg) {
      this.inviteMsg = inviteMsg;
      return this;
    }

    /**
     * 双人房进房信令
     */
    public Builder setEnterMsg(CombineRoomEnterMsg enterMsg) {
      this.enterMsg = enterMsg;
      return this;
    }

    @Override
    public NotificationMsg build() {
      return new NotificationMsg(timeMs, msgType, roomID, no, posType, sender, followMsg, inviteStandMsg, sysWarningMsg, inviteMsg, enterMsg, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_NotificationMsg extends ProtoAdapter<NotificationMsg> {
    public ProtoAdapter_NotificationMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, NotificationMsg.class);
    }

    @Override
    public int encodedSize(NotificationMsg value) {
      return ProtoAdapter.SINT64.encodedSizeWithTag(1, value.timeMs)
          + ENotificationMsgType.ADAPTER.encodedSizeWithTag(2, value.msgType)
          + ProtoAdapter.UINT32.encodedSizeWithTag(3, value.roomID)
          + ProtoAdapter.SINT64.encodedSizeWithTag(4, value.no)
          + EMsgPosType.ADAPTER.encodedSizeWithTag(5, value.posType)
          + UserInfo.ADAPTER.encodedSizeWithTag(6, value.sender)
          + FollowMsg.ADAPTER.encodedSizeWithTag(10, value.followMsg)
          + InviteStandMsg.ADAPTER.encodedSizeWithTag(11, value.inviteStandMsg)
          + SysWarningMsg.ADAPTER.encodedSizeWithTag(12, value.sysWarningMsg)
          + CombineRoomInviteMsg.ADAPTER.encodedSizeWithTag(13, value.inviteMsg)
          + CombineRoomEnterMsg.ADAPTER.encodedSizeWithTag(14, value.enterMsg)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, NotificationMsg value) throws IOException {
      ProtoAdapter.SINT64.encodeWithTag(writer, 1, value.timeMs);
      ENotificationMsgType.ADAPTER.encodeWithTag(writer, 2, value.msgType);
      ProtoAdapter.UINT32.encodeWithTag(writer, 3, value.roomID);
      ProtoAdapter.SINT64.encodeWithTag(writer, 4, value.no);
      EMsgPosType.ADAPTER.encodeWithTag(writer, 5, value.posType);
      UserInfo.ADAPTER.encodeWithTag(writer, 6, value.sender);
      FollowMsg.ADAPTER.encodeWithTag(writer, 10, value.followMsg);
      InviteStandMsg.ADAPTER.encodeWithTag(writer, 11, value.inviteStandMsg);
      SysWarningMsg.ADAPTER.encodeWithTag(writer, 12, value.sysWarningMsg);
      CombineRoomInviteMsg.ADAPTER.encodeWithTag(writer, 13, value.inviteMsg);
      CombineRoomEnterMsg.ADAPTER.encodeWithTag(writer, 14, value.enterMsg);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public NotificationMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
          case 2: {
            try {
              builder.setMsgType(ENotificationMsgType.ADAPTER.decode(reader));
            } catch (ProtoAdapter.EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
          case 3: builder.setRoomID(ProtoAdapter.UINT32.decode(reader)); break;
          case 4: builder.setNo(ProtoAdapter.SINT64.decode(reader)); break;
          case 5: {
            try {
              builder.setPosType(EMsgPosType.ADAPTER.decode(reader));
            } catch (ProtoAdapter.EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
          case 6: builder.setSender(UserInfo.ADAPTER.decode(reader)); break;
          case 10: builder.setFollowMsg(FollowMsg.ADAPTER.decode(reader)); break;
          case 11: builder.setInviteStandMsg(InviteStandMsg.ADAPTER.decode(reader)); break;
          case 12: builder.setSysWarningMsg(SysWarningMsg.ADAPTER.decode(reader)); break;
          case 13: builder.setInviteMsg(CombineRoomInviteMsg.ADAPTER.decode(reader)); break;
          case 14: builder.setEnterMsg(CombineRoomEnterMsg.ADAPTER.decode(reader)); break;
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
    public NotificationMsg redact(NotificationMsg value) {
      Builder builder = value.newBuilder();
      if (builder.sender != null) builder.sender = UserInfo.ADAPTER.redact(builder.sender);
      if (builder.followMsg != null) builder.followMsg = FollowMsg.ADAPTER.redact(builder.followMsg);
      if (builder.inviteStandMsg != null) builder.inviteStandMsg = InviteStandMsg.ADAPTER.redact(builder.inviteStandMsg);
      if (builder.sysWarningMsg != null) builder.sysWarningMsg = SysWarningMsg.ADAPTER.redact(builder.sysWarningMsg);
      if (builder.inviteMsg != null) builder.inviteMsg = CombineRoomInviteMsg.ADAPTER.redact(builder.inviteMsg);
      if (builder.enterMsg != null) builder.enterMsg = CombineRoomEnterMsg.ADAPTER.redact(builder.enterMsg);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
