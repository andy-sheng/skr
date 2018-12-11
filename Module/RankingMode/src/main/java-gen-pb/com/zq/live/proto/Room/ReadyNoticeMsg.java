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
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import okio.ByteString;

/**
 * 准备游戏通知消息
 */
public final class ReadyNoticeMsg extends Message<ReadyNoticeMsg, ReadyNoticeMsg.Builder> {
  public static final ProtoAdapter<ReadyNoticeMsg> ADAPTER = new ProtoAdapter_ReadyNoticeMsg();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_HASREADYEDUSERCNT = 0;

  public static final Boolean DEFAULT_ISGAMESTART = false;

  /**
   * 准备信息
   */
  @WireField(
      tag = 1,
      adapter = "com.zq.live.proto.Room.ReadyInfo#ADAPTER",
      label = WireField.Label.REPEATED
  )
  public final List<ReadyInfo> readyInfo;

  /**
   * 轮次信息
   */
  @WireField(
      tag = 2,
      adapter = "com.zq.live.proto.Room.RoundInfo#ADAPTER",
      label = WireField.Label.REPEATED
  )
  public final List<RoundInfo> roundInfo;

  /**
   * 游戏信息
   */
  @WireField(
      tag = 3,
      adapter = "com.zq.live.proto.Room.GameInfo#ADAPTER"
  )
  public final GameInfo gameInfo;

  /**
   * 已经准备人数
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#SINT32"
  )
  public final Integer HasReadyedUserCnt;

  /**
   * 游戏是否开始
   */
  @WireField(
      tag = 5,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  public final Boolean isGameStart;

  public ReadyNoticeMsg(List<ReadyInfo> readyInfo, List<RoundInfo> roundInfo, GameInfo gameInfo,
      Integer HasReadyedUserCnt, Boolean isGameStart) {
    this(readyInfo, roundInfo, gameInfo, HasReadyedUserCnt, isGameStart, ByteString.EMPTY);
  }

  public ReadyNoticeMsg(List<ReadyInfo> readyInfo, List<RoundInfo> roundInfo, GameInfo gameInfo,
      Integer HasReadyedUserCnt, Boolean isGameStart, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.readyInfo = Internal.immutableCopyOf("readyInfo", readyInfo);
    this.roundInfo = Internal.immutableCopyOf("roundInfo", roundInfo);
    this.gameInfo = gameInfo;
    this.HasReadyedUserCnt = HasReadyedUserCnt;
    this.isGameStart = isGameStart;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.readyInfo = Internal.copyOf("readyInfo", readyInfo);
    builder.roundInfo = Internal.copyOf("roundInfo", roundInfo);
    builder.gameInfo = gameInfo;
    builder.HasReadyedUserCnt = HasReadyedUserCnt;
    builder.isGameStart = isGameStart;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof ReadyNoticeMsg)) return false;
    ReadyNoticeMsg o = (ReadyNoticeMsg) other;
    return unknownFields().equals(o.unknownFields())
        && readyInfo.equals(o.readyInfo)
        && roundInfo.equals(o.roundInfo)
        && Internal.equals(gameInfo, o.gameInfo)
        && Internal.equals(HasReadyedUserCnt, o.HasReadyedUserCnt)
        && Internal.equals(isGameStart, o.isGameStart);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + readyInfo.hashCode();
      result = result * 37 + roundInfo.hashCode();
      result = result * 37 + (gameInfo != null ? gameInfo.hashCode() : 0);
      result = result * 37 + (HasReadyedUserCnt != null ? HasReadyedUserCnt.hashCode() : 0);
      result = result * 37 + (isGameStart != null ? isGameStart.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (!readyInfo.isEmpty()) builder.append(", readyInfo=").append(readyInfo);
    if (!roundInfo.isEmpty()) builder.append(", roundInfo=").append(roundInfo);
    if (gameInfo != null) builder.append(", gameInfo=").append(gameInfo);
    if (HasReadyedUserCnt != null) builder.append(", HasReadyedUserCnt=").append(HasReadyedUserCnt);
    if (isGameStart != null) builder.append(", isGameStart=").append(isGameStart);
    return builder.replace(0, 2, "ReadyNoticeMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return ReadyNoticeMsg.ADAPTER.encode(this);
  }

  public static final ReadyNoticeMsg parseFrom(byte[] data) throws IOException {
    ReadyNoticeMsg c = null;
       c = ReadyNoticeMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 准备信息
   */
  public List<ReadyInfo> getReadyInfoList() {
    if(readyInfo==null){
        return new java.util.ArrayList<ReadyInfo>();
    }
    return readyInfo;
  }

  /**
   * 轮次信息
   */
  public List<RoundInfo> getRoundInfoList() {
    if(roundInfo==null){
        return new java.util.ArrayList<RoundInfo>();
    }
    return roundInfo;
  }

  /**
   * 游戏信息
   */
  public GameInfo getGameInfo() {
    if(gameInfo==null){
        return new GameInfo.Builder().build();
    }
    return gameInfo;
  }

  /**
   * 已经准备人数
   */
  public Integer getHasReadyedUserCnt() {
    if(HasReadyedUserCnt==null){
        return DEFAULT_HASREADYEDUSERCNT;
    }
    return HasReadyedUserCnt;
  }

  /**
   * 游戏是否开始
   */
  public Boolean getIsGameStart() {
    if(isGameStart==null){
        return DEFAULT_ISGAMESTART;
    }
    return isGameStart;
  }

  /**
   * 准备信息
   */
  public boolean hasReadyInfoList() {
    return readyInfo!=null;
  }

  /**
   * 轮次信息
   */
  public boolean hasRoundInfoList() {
    return roundInfo!=null;
  }

  /**
   * 游戏信息
   */
  public boolean hasGameInfo() {
    return gameInfo!=null;
  }

  /**
   * 已经准备人数
   */
  public boolean hasHasReadyedUserCnt() {
    return HasReadyedUserCnt!=null;
  }

  /**
   * 游戏是否开始
   */
  public boolean hasIsGameStart() {
    return isGameStart!=null;
  }

  public static final class Builder extends Message.Builder<ReadyNoticeMsg, Builder> {
    public List<ReadyInfo> readyInfo;

    public List<RoundInfo> roundInfo;

    public GameInfo gameInfo;

    public Integer HasReadyedUserCnt;

    public Boolean isGameStart;

    public Builder() {
      readyInfo = Internal.newMutableList();
      roundInfo = Internal.newMutableList();
    }

    /**
     * 准备信息
     */
    public Builder addAllReadyInfo(List<ReadyInfo> readyInfo) {
      Internal.checkElementsNotNull(readyInfo);
      this.readyInfo = readyInfo;
      return this;
    }

    /**
     * 轮次信息
     */
    public Builder addAllRoundInfo(List<RoundInfo> roundInfo) {
      Internal.checkElementsNotNull(roundInfo);
      this.roundInfo = roundInfo;
      return this;
    }

    /**
     * 游戏信息
     */
    public Builder setGameInfo(GameInfo gameInfo) {
      this.gameInfo = gameInfo;
      return this;
    }

    /**
     * 已经准备人数
     */
    public Builder setHasReadyedUserCnt(Integer HasReadyedUserCnt) {
      this.HasReadyedUserCnt = HasReadyedUserCnt;
      return this;
    }

    /**
     * 游戏是否开始
     */
    public Builder setIsGameStart(Boolean isGameStart) {
      this.isGameStart = isGameStart;
      return this;
    }

    @Override
    public ReadyNoticeMsg build() {
      return new ReadyNoticeMsg(readyInfo, roundInfo, gameInfo, HasReadyedUserCnt, isGameStart, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_ReadyNoticeMsg extends ProtoAdapter<ReadyNoticeMsg> {
    public ProtoAdapter_ReadyNoticeMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, ReadyNoticeMsg.class);
    }

    @Override
    public int encodedSize(ReadyNoticeMsg value) {
      return ReadyInfo.ADAPTER.asRepeated().encodedSizeWithTag(1, value.readyInfo)
          + RoundInfo.ADAPTER.asRepeated().encodedSizeWithTag(2, value.roundInfo)
          + GameInfo.ADAPTER.encodedSizeWithTag(3, value.gameInfo)
          + ProtoAdapter.SINT32.encodedSizeWithTag(4, value.HasReadyedUserCnt)
          + ProtoAdapter.BOOL.encodedSizeWithTag(5, value.isGameStart)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, ReadyNoticeMsg value) throws IOException {
      ReadyInfo.ADAPTER.asRepeated().encodeWithTag(writer, 1, value.readyInfo);
      RoundInfo.ADAPTER.asRepeated().encodeWithTag(writer, 2, value.roundInfo);
      GameInfo.ADAPTER.encodeWithTag(writer, 3, value.gameInfo);
      ProtoAdapter.SINT32.encodeWithTag(writer, 4, value.HasReadyedUserCnt);
      ProtoAdapter.BOOL.encodeWithTag(writer, 5, value.isGameStart);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public ReadyNoticeMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.readyInfo.add(ReadyInfo.ADAPTER.decode(reader)); break;
          case 2: builder.roundInfo.add(RoundInfo.ADAPTER.decode(reader)); break;
          case 3: builder.setGameInfo(GameInfo.ADAPTER.decode(reader)); break;
          case 4: builder.setHasReadyedUserCnt(ProtoAdapter.SINT32.decode(reader)); break;
          case 5: builder.setIsGameStart(ProtoAdapter.BOOL.decode(reader)); break;
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
    public ReadyNoticeMsg redact(ReadyNoticeMsg value) {
      Builder builder = value.newBuilder();
      Internal.redactElements(builder.readyInfo, ReadyInfo.ADAPTER);
      Internal.redactElements(builder.roundInfo, RoundInfo.ADAPTER);
      if (builder.gameInfo != null) builder.gameInfo = GameInfo.ADAPTER.redact(builder.gameInfo);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
