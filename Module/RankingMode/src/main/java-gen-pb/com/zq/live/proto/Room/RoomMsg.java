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
 * java -jar -Dfile.encoding=UTF-8 ./proto/wire-compiler-2.3.0-SNAPSHOT-jar-with-dependencies_backup.jar \
 * --proto_path=./pb --java_out=./Module/RankingMode/src/main/java-gen-pb/ Room.proto
 * 房间消息：此结构会通过pb编码后，通过聊天室通道到达客户端
 */
public final class RoomMsg extends Message<RoomMsg, RoomMsg.Builder> {
  public static final ProtoAdapter<RoomMsg> ADAPTER = new ProtoAdapter_RoomMsg();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_TIMEMS = 0L;

  public static final ERoomMsgType DEFAULT_MSGTYPE = ERoomMsgType.RM_UNKNOWN;

  public static final Integer DEFAULT_ROOMID = 0;

  public static final Long DEFAULT_NO = 0L;

  public static final EMsgPosType DEFAULT_POSTYPE = EMsgPosType.EPT_UNKNOWN;

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
      adapter = "com.zq.live.proto.Room.ERoomMsgType#ADAPTER"
  )
  public final ERoomMsgType msgType;

  /**
   * 房间ID
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  public final Integer roomID;

  /**
   * 房间内的消息序号，每个房间有自己的消息序号,不存在则系统生成,一般情况下调用方不必设置
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  public final Long no;

  /**
   * 消息显示位置类型
   */
  @WireField(
      tag = 5,
      adapter = "com.zq.live.proto.Room.EMsgPosType#ADAPTER"
  )
  public final EMsgPosType posType;

  /**
   * 发送者简要信息
   */
  @WireField(
      tag = 6,
      adapter = "com.zq.live.proto.Common.UserInfo#ADAPTER"
  )
  public final UserInfo sender;

  /**
   * 评论消息 msgType == RM_COMMENT
   */
  @WireField(
      tag = 10,
      adapter = "com.zq.live.proto.Room.CommentMsg#ADAPTER"
  )
  public final CommentMsg commentMsg;

  /**
   * 特殊表情消息  msgType == RM_SPECIAL_EMOJI
   */
  @WireField(
      tag = 11,
      adapter = "com.zq.live.proto.Room.SpecialEmojiMsg#ADAPTER"
  )
  public final SpecialEmojiMsg specialEmojiMsg;

  /**
   * 动态表情消息  msgType == RM_DYNAMIC_EMOJI
   */
  @WireField(
      tag = 12,
      adapter = "com.zq.live.proto.Room.DynamicEmojiMsg#ADAPTER"
  )
  public final DynamicEmojiMsg dynamicemojiMsg;

  /**
   * 加入游戏指令消息 msgType == RM_JOIN_ACTION
   */
  @WireField(
      tag = 100,
      adapter = "com.zq.live.proto.Room.JoinActionMsg#ADAPTER"
  )
  public final JoinActionMsg joinActionMsg;

  /**
   * 加入游戏通知消息 msgType == RM_JOIN_NOTICE
   */
  @WireField(
      tag = 101,
      adapter = "com.zq.live.proto.Room.JoinNoticeMsg#ADAPTER"
  )
  public final JoinNoticeMsg joinNoticeMsg;

  /**
   * 准备游戏通知消息 msgType == RM_READY_NOTICE
   */
  @WireField(
      tag = 102,
      adapter = "com.zq.live.proto.Room.ReadyNoticeMsg#ADAPTER"
  )
  public final ReadyNoticeMsg readyNoticeMsg;

  /**
   * 游戏轮次结束通知消息 msgType == RM_ROUND_OVER
   */
  @WireField(
      tag = 104,
      adapter = "com.zq.live.proto.Room.RoundOverMsg#ADAPTER"
  )
  public final RoundOverMsg roundOverMsg;

  /**
   * 轮次和游戏结束通知消息 msgType == RM_ROUND_AND_GAME_OVER
   */
  @WireField(
      tag = 105,
      adapter = "com.zq.live.proto.Room.RoundAndGameOverMsg#ADAPTER"
  )
  public final RoundAndGameOverMsg roundAndGameOverMsg;

  /**
   * 退出游戏通知  msgType == RM_QUIT_GAME
   */
  @WireField(
      tag = 106,
      adapter = "com.zq.live.proto.Room.QuitGameMsg#ADAPTER"
  )
  public final QuitGameMsg quitGameMsg;

  /**
   * app进程后台通知  msgType ==  RM_APP_SWAP
   */
  @WireField(
      tag = 107,
      adapter = "com.zq.live.proto.Room.AppSwapMsg#ADAPTER"
  )
  public final AppSwapMsg appSwapMsg;

  /**
   * 状态同步信令 msgType == RM_SYNC_STATUS
   */
  @WireField(
      tag = 108,
      adapter = "com.zq.live.proto.Room.SyncStatusMsg#ADAPTER"
  )
  public final SyncStatusMsg syncStatusMsg;

  public RoomMsg(Long timeMs, ERoomMsgType msgType, Integer roomID, Long no, EMsgPosType posType,
      UserInfo sender, CommentMsg commentMsg, SpecialEmojiMsg specialEmojiMsg,
      DynamicEmojiMsg dynamicemojiMsg, JoinActionMsg joinActionMsg, JoinNoticeMsg joinNoticeMsg,
      ReadyNoticeMsg readyNoticeMsg, RoundOverMsg roundOverMsg,
      RoundAndGameOverMsg roundAndGameOverMsg, QuitGameMsg quitGameMsg, AppSwapMsg appSwapMsg,
      SyncStatusMsg syncStatusMsg) {
    this(timeMs, msgType, roomID, no, posType, sender, commentMsg, specialEmojiMsg, dynamicemojiMsg, joinActionMsg, joinNoticeMsg, readyNoticeMsg, roundOverMsg, roundAndGameOverMsg, quitGameMsg, appSwapMsg, syncStatusMsg, ByteString.EMPTY);
  }

  public RoomMsg(Long timeMs, ERoomMsgType msgType, Integer roomID, Long no, EMsgPosType posType,
      UserInfo sender, CommentMsg commentMsg, SpecialEmojiMsg specialEmojiMsg,
      DynamicEmojiMsg dynamicemojiMsg, JoinActionMsg joinActionMsg, JoinNoticeMsg joinNoticeMsg,
      ReadyNoticeMsg readyNoticeMsg, RoundOverMsg roundOverMsg,
      RoundAndGameOverMsg roundAndGameOverMsg, QuitGameMsg quitGameMsg, AppSwapMsg appSwapMsg,
      SyncStatusMsg syncStatusMsg, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.timeMs = timeMs;
    this.msgType = msgType;
    this.roomID = roomID;
    this.no = no;
    this.posType = posType;
    this.sender = sender;
    this.commentMsg = commentMsg;
    this.specialEmojiMsg = specialEmojiMsg;
    this.dynamicemojiMsg = dynamicemojiMsg;
    this.joinActionMsg = joinActionMsg;
    this.joinNoticeMsg = joinNoticeMsg;
    this.readyNoticeMsg = readyNoticeMsg;
    this.roundOverMsg = roundOverMsg;
    this.roundAndGameOverMsg = roundAndGameOverMsg;
    this.quitGameMsg = quitGameMsg;
    this.appSwapMsg = appSwapMsg;
    this.syncStatusMsg = syncStatusMsg;
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
    builder.commentMsg = commentMsg;
    builder.specialEmojiMsg = specialEmojiMsg;
    builder.dynamicemojiMsg = dynamicemojiMsg;
    builder.joinActionMsg = joinActionMsg;
    builder.joinNoticeMsg = joinNoticeMsg;
    builder.readyNoticeMsg = readyNoticeMsg;
    builder.roundOverMsg = roundOverMsg;
    builder.roundAndGameOverMsg = roundAndGameOverMsg;
    builder.quitGameMsg = quitGameMsg;
    builder.appSwapMsg = appSwapMsg;
    builder.syncStatusMsg = syncStatusMsg;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof RoomMsg)) return false;
    RoomMsg o = (RoomMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(timeMs, o.timeMs)
        && Internal.equals(msgType, o.msgType)
        && Internal.equals(roomID, o.roomID)
        && Internal.equals(no, o.no)
        && Internal.equals(posType, o.posType)
        && Internal.equals(sender, o.sender)
        && Internal.equals(commentMsg, o.commentMsg)
        && Internal.equals(specialEmojiMsg, o.specialEmojiMsg)
        && Internal.equals(dynamicemojiMsg, o.dynamicemojiMsg)
        && Internal.equals(joinActionMsg, o.joinActionMsg)
        && Internal.equals(joinNoticeMsg, o.joinNoticeMsg)
        && Internal.equals(readyNoticeMsg, o.readyNoticeMsg)
        && Internal.equals(roundOverMsg, o.roundOverMsg)
        && Internal.equals(roundAndGameOverMsg, o.roundAndGameOverMsg)
        && Internal.equals(quitGameMsg, o.quitGameMsg)
        && Internal.equals(appSwapMsg, o.appSwapMsg)
        && Internal.equals(syncStatusMsg, o.syncStatusMsg);
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
      result = result * 37 + (commentMsg != null ? commentMsg.hashCode() : 0);
      result = result * 37 + (specialEmojiMsg != null ? specialEmojiMsg.hashCode() : 0);
      result = result * 37 + (dynamicemojiMsg != null ? dynamicemojiMsg.hashCode() : 0);
      result = result * 37 + (joinActionMsg != null ? joinActionMsg.hashCode() : 0);
      result = result * 37 + (joinNoticeMsg != null ? joinNoticeMsg.hashCode() : 0);
      result = result * 37 + (readyNoticeMsg != null ? readyNoticeMsg.hashCode() : 0);
      result = result * 37 + (roundOverMsg != null ? roundOverMsg.hashCode() : 0);
      result = result * 37 + (roundAndGameOverMsg != null ? roundAndGameOverMsg.hashCode() : 0);
      result = result * 37 + (quitGameMsg != null ? quitGameMsg.hashCode() : 0);
      result = result * 37 + (appSwapMsg != null ? appSwapMsg.hashCode() : 0);
      result = result * 37 + (syncStatusMsg != null ? syncStatusMsg.hashCode() : 0);
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
    if (commentMsg != null) builder.append(", commentMsg=").append(commentMsg);
    if (specialEmojiMsg != null) builder.append(", specialEmojiMsg=").append(specialEmojiMsg);
    if (dynamicemojiMsg != null) builder.append(", dynamicemojiMsg=").append(dynamicemojiMsg);
    if (joinActionMsg != null) builder.append(", joinActionMsg=").append(joinActionMsg);
    if (joinNoticeMsg != null) builder.append(", joinNoticeMsg=").append(joinNoticeMsg);
    if (readyNoticeMsg != null) builder.append(", readyNoticeMsg=").append(readyNoticeMsg);
    if (roundOverMsg != null) builder.append(", roundOverMsg=").append(roundOverMsg);
    if (roundAndGameOverMsg != null) builder.append(", roundAndGameOverMsg=").append(roundAndGameOverMsg);
    if (quitGameMsg != null) builder.append(", quitGameMsg=").append(quitGameMsg);
    if (appSwapMsg != null) builder.append(", appSwapMsg=").append(appSwapMsg);
    if (syncStatusMsg != null) builder.append(", syncStatusMsg=").append(syncStatusMsg);
    return builder.replace(0, 2, "RoomMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return RoomMsg.ADAPTER.encode(this);
  }

  public static final RoomMsg parseFrom(byte[] data) throws IOException {
    RoomMsg c = null;
       c = RoomMsg.ADAPTER.decode(data);
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
  public ERoomMsgType getMsgType() {
    if(msgType==null){
        return new ERoomMsgType.Builder().build();
    }
    return msgType;
  }

  /**
   * 房间ID
   */
  public Integer getRoomID() {
    if(roomID==null){
        return DEFAULT_ROOMID;
    }
    return roomID;
  }

  /**
   * 房间内的消息序号，每个房间有自己的消息序号,不存在则系统生成,一般情况下调用方不必设置
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

  /**
   * 评论消息 msgType == RM_COMMENT
   */
  public CommentMsg getCommentMsg() {
    if(commentMsg==null){
        return new CommentMsg.Builder().build();
    }
    return commentMsg;
  }

  /**
   * 特殊表情消息  msgType == RM_SPECIAL_EMOJI
   */
  public SpecialEmojiMsg getSpecialEmojiMsg() {
    if(specialEmojiMsg==null){
        return new SpecialEmojiMsg.Builder().build();
    }
    return specialEmojiMsg;
  }

  /**
   * 动态表情消息  msgType == RM_DYNAMIC_EMOJI
   */
  public DynamicEmojiMsg getDynamicemojiMsg() {
    if(dynamicemojiMsg==null){
        return new DynamicEmojiMsg.Builder().build();
    }
    return dynamicemojiMsg;
  }

  /**
   * 加入游戏指令消息 msgType == RM_JOIN_ACTION
   */
  public JoinActionMsg getJoinActionMsg() {
    if(joinActionMsg==null){
        return new JoinActionMsg.Builder().build();
    }
    return joinActionMsg;
  }

  /**
   * 加入游戏通知消息 msgType == RM_JOIN_NOTICE
   */
  public JoinNoticeMsg getJoinNoticeMsg() {
    if(joinNoticeMsg==null){
        return new JoinNoticeMsg.Builder().build();
    }
    return joinNoticeMsg;
  }

  /**
   * 准备游戏通知消息 msgType == RM_READY_NOTICE
   */
  public ReadyNoticeMsg getReadyNoticeMsg() {
    if(readyNoticeMsg==null){
        return new ReadyNoticeMsg.Builder().build();
    }
    return readyNoticeMsg;
  }

  /**
   * 游戏轮次结束通知消息 msgType == RM_ROUND_OVER
   */
  public RoundOverMsg getRoundOverMsg() {
    if(roundOverMsg==null){
        return new RoundOverMsg.Builder().build();
    }
    return roundOverMsg;
  }

  /**
   * 轮次和游戏结束通知消息 msgType == RM_ROUND_AND_GAME_OVER
   */
  public RoundAndGameOverMsg getRoundAndGameOverMsg() {
    if(roundAndGameOverMsg==null){
        return new RoundAndGameOverMsg.Builder().build();
    }
    return roundAndGameOverMsg;
  }

  /**
   * 退出游戏通知  msgType == RM_QUIT_GAME
   */
  public QuitGameMsg getQuitGameMsg() {
    if(quitGameMsg==null){
        return new QuitGameMsg.Builder().build();
    }
    return quitGameMsg;
  }

  /**
   * app进程后台通知  msgType ==  RM_APP_SWAP
   */
  public AppSwapMsg getAppSwapMsg() {
    if(appSwapMsg==null){
        return new AppSwapMsg.Builder().build();
    }
    return appSwapMsg;
  }

  /**
   * 状态同步信令 msgType == RM_SYNC_STATUS
   */
  public SyncStatusMsg getSyncStatusMsg() {
    if(syncStatusMsg==null){
        return new SyncStatusMsg.Builder().build();
    }
    return syncStatusMsg;
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
   * 房间ID
   */
  public boolean hasRoomID() {
    return roomID!=null;
  }

  /**
   * 房间内的消息序号，每个房间有自己的消息序号,不存在则系统生成,一般情况下调用方不必设置
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

  /**
   * 评论消息 msgType == RM_COMMENT
   */
  public boolean hasCommentMsg() {
    return commentMsg!=null;
  }

  /**
   * 特殊表情消息  msgType == RM_SPECIAL_EMOJI
   */
  public boolean hasSpecialEmojiMsg() {
    return specialEmojiMsg!=null;
  }

  /**
   * 动态表情消息  msgType == RM_DYNAMIC_EMOJI
   */
  public boolean hasDynamicemojiMsg() {
    return dynamicemojiMsg!=null;
  }

  /**
   * 加入游戏指令消息 msgType == RM_JOIN_ACTION
   */
  public boolean hasJoinActionMsg() {
    return joinActionMsg!=null;
  }

  /**
   * 加入游戏通知消息 msgType == RM_JOIN_NOTICE
   */
  public boolean hasJoinNoticeMsg() {
    return joinNoticeMsg!=null;
  }

  /**
   * 准备游戏通知消息 msgType == RM_READY_NOTICE
   */
  public boolean hasReadyNoticeMsg() {
    return readyNoticeMsg!=null;
  }

  /**
   * 游戏轮次结束通知消息 msgType == RM_ROUND_OVER
   */
  public boolean hasRoundOverMsg() {
    return roundOverMsg!=null;
  }

  /**
   * 轮次和游戏结束通知消息 msgType == RM_ROUND_AND_GAME_OVER
   */
  public boolean hasRoundAndGameOverMsg() {
    return roundAndGameOverMsg!=null;
  }

  /**
   * 退出游戏通知  msgType == RM_QUIT_GAME
   */
  public boolean hasQuitGameMsg() {
    return quitGameMsg!=null;
  }

  /**
   * app进程后台通知  msgType ==  RM_APP_SWAP
   */
  public boolean hasAppSwapMsg() {
    return appSwapMsg!=null;
  }

  /**
   * 状态同步信令 msgType == RM_SYNC_STATUS
   */
  public boolean hasSyncStatusMsg() {
    return syncStatusMsg!=null;
  }

  public static final class Builder extends Message.Builder<RoomMsg, Builder> {
    public Long timeMs;

    public ERoomMsgType msgType;

    public Integer roomID;

    public Long no;

    public EMsgPosType posType;

    public UserInfo sender;

    public CommentMsg commentMsg;

    public SpecialEmojiMsg specialEmojiMsg;

    public DynamicEmojiMsg dynamicemojiMsg;

    public JoinActionMsg joinActionMsg;

    public JoinNoticeMsg joinNoticeMsg;

    public ReadyNoticeMsg readyNoticeMsg;

    public RoundOverMsg roundOverMsg;

    public RoundAndGameOverMsg roundAndGameOverMsg;

    public QuitGameMsg quitGameMsg;

    public AppSwapMsg appSwapMsg;

    public SyncStatusMsg syncStatusMsg;

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
    public Builder setMsgType(ERoomMsgType msgType) {
      this.msgType = msgType;
      return this;
    }

    /**
     * 房间ID
     */
    public Builder setRoomID(Integer roomID) {
      this.roomID = roomID;
      return this;
    }

    /**
     * 房间内的消息序号，每个房间有自己的消息序号,不存在则系统生成,一般情况下调用方不必设置
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

    /**
     * 评论消息 msgType == RM_COMMENT
     */
    public Builder setCommentMsg(CommentMsg commentMsg) {
      this.commentMsg = commentMsg;
      return this;
    }

    /**
     * 特殊表情消息  msgType == RM_SPECIAL_EMOJI
     */
    public Builder setSpecialEmojiMsg(SpecialEmojiMsg specialEmojiMsg) {
      this.specialEmojiMsg = specialEmojiMsg;
      return this;
    }

    /**
     * 动态表情消息  msgType == RM_DYNAMIC_EMOJI
     */
    public Builder setDynamicemojiMsg(DynamicEmojiMsg dynamicemojiMsg) {
      this.dynamicemojiMsg = dynamicemojiMsg;
      return this;
    }

    /**
     * 加入游戏指令消息 msgType == RM_JOIN_ACTION
     */
    public Builder setJoinActionMsg(JoinActionMsg joinActionMsg) {
      this.joinActionMsg = joinActionMsg;
      return this;
    }

    /**
     * 加入游戏通知消息 msgType == RM_JOIN_NOTICE
     */
    public Builder setJoinNoticeMsg(JoinNoticeMsg joinNoticeMsg) {
      this.joinNoticeMsg = joinNoticeMsg;
      return this;
    }

    /**
     * 准备游戏通知消息 msgType == RM_READY_NOTICE
     */
    public Builder setReadyNoticeMsg(ReadyNoticeMsg readyNoticeMsg) {
      this.readyNoticeMsg = readyNoticeMsg;
      return this;
    }

    /**
     * 游戏轮次结束通知消息 msgType == RM_ROUND_OVER
     */
    public Builder setRoundOverMsg(RoundOverMsg roundOverMsg) {
      this.roundOverMsg = roundOverMsg;
      return this;
    }

    /**
     * 轮次和游戏结束通知消息 msgType == RM_ROUND_AND_GAME_OVER
     */
    public Builder setRoundAndGameOverMsg(RoundAndGameOverMsg roundAndGameOverMsg) {
      this.roundAndGameOverMsg = roundAndGameOverMsg;
      return this;
    }

    /**
     * 退出游戏通知  msgType == RM_QUIT_GAME
     */
    public Builder setQuitGameMsg(QuitGameMsg quitGameMsg) {
      this.quitGameMsg = quitGameMsg;
      return this;
    }

    /**
     * app进程后台通知  msgType ==  RM_APP_SWAP
     */
    public Builder setAppSwapMsg(AppSwapMsg appSwapMsg) {
      this.appSwapMsg = appSwapMsg;
      return this;
    }

    /**
     * 状态同步信令 msgType == RM_SYNC_STATUS
     */
    public Builder setSyncStatusMsg(SyncStatusMsg syncStatusMsg) {
      this.syncStatusMsg = syncStatusMsg;
      return this;
    }

    @Override
    public RoomMsg build() {
      return new RoomMsg(timeMs, msgType, roomID, no, posType, sender, commentMsg, specialEmojiMsg, dynamicemojiMsg, joinActionMsg, joinNoticeMsg, readyNoticeMsg, roundOverMsg, roundAndGameOverMsg, quitGameMsg, appSwapMsg, syncStatusMsg, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_RoomMsg extends ProtoAdapter<RoomMsg> {
    public ProtoAdapter_RoomMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, RoomMsg.class);
    }

    @Override
    public int encodedSize(RoomMsg value) {
      return ProtoAdapter.SINT64.encodedSizeWithTag(1, value.timeMs)
          + ERoomMsgType.ADAPTER.encodedSizeWithTag(2, value.msgType)
          + ProtoAdapter.UINT32.encodedSizeWithTag(3, value.roomID)
          + ProtoAdapter.SINT64.encodedSizeWithTag(4, value.no)
          + EMsgPosType.ADAPTER.encodedSizeWithTag(5, value.posType)
          + UserInfo.ADAPTER.encodedSizeWithTag(6, value.sender)
          + CommentMsg.ADAPTER.encodedSizeWithTag(10, value.commentMsg)
          + SpecialEmojiMsg.ADAPTER.encodedSizeWithTag(11, value.specialEmojiMsg)
          + DynamicEmojiMsg.ADAPTER.encodedSizeWithTag(12, value.dynamicemojiMsg)
          + JoinActionMsg.ADAPTER.encodedSizeWithTag(100, value.joinActionMsg)
          + JoinNoticeMsg.ADAPTER.encodedSizeWithTag(101, value.joinNoticeMsg)
          + ReadyNoticeMsg.ADAPTER.encodedSizeWithTag(102, value.readyNoticeMsg)
          + RoundOverMsg.ADAPTER.encodedSizeWithTag(104, value.roundOverMsg)
          + RoundAndGameOverMsg.ADAPTER.encodedSizeWithTag(105, value.roundAndGameOverMsg)
          + QuitGameMsg.ADAPTER.encodedSizeWithTag(106, value.quitGameMsg)
          + AppSwapMsg.ADAPTER.encodedSizeWithTag(107, value.appSwapMsg)
          + SyncStatusMsg.ADAPTER.encodedSizeWithTag(108, value.syncStatusMsg)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, RoomMsg value) throws IOException {
      ProtoAdapter.SINT64.encodeWithTag(writer, 1, value.timeMs);
      ERoomMsgType.ADAPTER.encodeWithTag(writer, 2, value.msgType);
      ProtoAdapter.UINT32.encodeWithTag(writer, 3, value.roomID);
      ProtoAdapter.SINT64.encodeWithTag(writer, 4, value.no);
      EMsgPosType.ADAPTER.encodeWithTag(writer, 5, value.posType);
      UserInfo.ADAPTER.encodeWithTag(writer, 6, value.sender);
      CommentMsg.ADAPTER.encodeWithTag(writer, 10, value.commentMsg);
      SpecialEmojiMsg.ADAPTER.encodeWithTag(writer, 11, value.specialEmojiMsg);
      DynamicEmojiMsg.ADAPTER.encodeWithTag(writer, 12, value.dynamicemojiMsg);
      JoinActionMsg.ADAPTER.encodeWithTag(writer, 100, value.joinActionMsg);
      JoinNoticeMsg.ADAPTER.encodeWithTag(writer, 101, value.joinNoticeMsg);
      ReadyNoticeMsg.ADAPTER.encodeWithTag(writer, 102, value.readyNoticeMsg);
      RoundOverMsg.ADAPTER.encodeWithTag(writer, 104, value.roundOverMsg);
      RoundAndGameOverMsg.ADAPTER.encodeWithTag(writer, 105, value.roundAndGameOverMsg);
      QuitGameMsg.ADAPTER.encodeWithTag(writer, 106, value.quitGameMsg);
      AppSwapMsg.ADAPTER.encodeWithTag(writer, 107, value.appSwapMsg);
      SyncStatusMsg.ADAPTER.encodeWithTag(writer, 108, value.syncStatusMsg);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public RoomMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
          case 2: {
            try {
              builder.setMsgType(ERoomMsgType.ADAPTER.decode(reader));
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
          case 10: builder.setCommentMsg(CommentMsg.ADAPTER.decode(reader)); break;
          case 11: builder.setSpecialEmojiMsg(SpecialEmojiMsg.ADAPTER.decode(reader)); break;
          case 12: builder.setDynamicemojiMsg(DynamicEmojiMsg.ADAPTER.decode(reader)); break;
          case 100: builder.setJoinActionMsg(JoinActionMsg.ADAPTER.decode(reader)); break;
          case 101: builder.setJoinNoticeMsg(JoinNoticeMsg.ADAPTER.decode(reader)); break;
          case 102: builder.setReadyNoticeMsg(ReadyNoticeMsg.ADAPTER.decode(reader)); break;
          case 104: builder.setRoundOverMsg(RoundOverMsg.ADAPTER.decode(reader)); break;
          case 105: builder.setRoundAndGameOverMsg(RoundAndGameOverMsg.ADAPTER.decode(reader)); break;
          case 106: builder.setQuitGameMsg(QuitGameMsg.ADAPTER.decode(reader)); break;
          case 107: builder.setAppSwapMsg(AppSwapMsg.ADAPTER.decode(reader)); break;
          case 108: builder.setSyncStatusMsg(SyncStatusMsg.ADAPTER.decode(reader)); break;
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
    public RoomMsg redact(RoomMsg value) {
      Builder builder = value.newBuilder();
      if (builder.sender != null) builder.sender = UserInfo.ADAPTER.redact(builder.sender);
      if (builder.commentMsg != null) builder.commentMsg = CommentMsg.ADAPTER.redact(builder.commentMsg);
      if (builder.specialEmojiMsg != null) builder.specialEmojiMsg = SpecialEmojiMsg.ADAPTER.redact(builder.specialEmojiMsg);
      if (builder.dynamicemojiMsg != null) builder.dynamicemojiMsg = DynamicEmojiMsg.ADAPTER.redact(builder.dynamicemojiMsg);
      if (builder.joinActionMsg != null) builder.joinActionMsg = JoinActionMsg.ADAPTER.redact(builder.joinActionMsg);
      if (builder.joinNoticeMsg != null) builder.joinNoticeMsg = JoinNoticeMsg.ADAPTER.redact(builder.joinNoticeMsg);
      if (builder.readyNoticeMsg != null) builder.readyNoticeMsg = ReadyNoticeMsg.ADAPTER.redact(builder.readyNoticeMsg);
      if (builder.roundOverMsg != null) builder.roundOverMsg = RoundOverMsg.ADAPTER.redact(builder.roundOverMsg);
      if (builder.roundAndGameOverMsg != null) builder.roundAndGameOverMsg = RoundAndGameOverMsg.ADAPTER.redact(builder.roundAndGameOverMsg);
      if (builder.quitGameMsg != null) builder.quitGameMsg = QuitGameMsg.ADAPTER.redact(builder.quitGameMsg);
      if (builder.appSwapMsg != null) builder.appSwapMsg = AppSwapMsg.ADAPTER.redact(builder.appSwapMsg);
      if (builder.syncStatusMsg != null) builder.syncStatusMsg = SyncStatusMsg.ADAPTER.redact(builder.syncStatusMsg);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
