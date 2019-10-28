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
import java.io.IOException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import okio.ByteString;

/**
 * 轮次和游戏结束通知消息
 */
public final class RoundAndGameOverMsg extends Message<RoundAndGameOverMsg, RoundAndGameOverMsg.Builder> {
  public static final ProtoAdapter<RoundAndGameOverMsg> ADAPTER = new ProtoAdapter_RoundAndGameOverMsg();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_ROUNDOVERTIMEMS = 0L;

  public static final Integer DEFAULT_EXITUSERID = 0;

  public static final Integer DEFAULT_LASTMLIGHTUSERID = 0;

  /**
   * 轮次结束的毫秒时间戳
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long roundOverTimeMs;

  /**
   * 当前轮次信息
   */
  @WireField(
      tag = 2,
      adapter = "com.zq.live.proto.GrabRoom.RoundInfo#ADAPTER"
  )
  private final RoundInfo currentRound;

  /**
   * 退出用户的ID
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer exitUserID;

  /**
   * 投票打分信息
   */
  @WireField(
      tag = 4,
      adapter = "com.zq.live.proto.GrabRoom.VoteInfo#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<VoteInfo> voteInfo;

  /**
   * 所有参与者的评分结果，应该使用这个结构
   */
  @WireField(
      tag = 5,
      adapter = "com.zq.live.proto.GrabRoom.UserScoreResult#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<UserScoreResult> scoreResults;

  /**
   * 游戏结果评分数据
   */
  @WireField(
      tag = 6,
      adapter = "com.zq.live.proto.GrabRoom.UserGameResult#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<UserGameResult> gameResults;

  /**
   * 最后一个灭灯的用户ID
   */
  @WireField(
      tag = 7,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer lastMLightUserID;

  public RoundAndGameOverMsg(Long roundOverTimeMs, RoundInfo currentRound, Integer exitUserID,
      List<VoteInfo> voteInfo, List<UserScoreResult> scoreResults, List<UserGameResult> gameResults,
      Integer lastMLightUserID) {
    this(roundOverTimeMs, currentRound, exitUserID, voteInfo, scoreResults, gameResults, lastMLightUserID, ByteString.EMPTY);
  }

  public RoundAndGameOverMsg(Long roundOverTimeMs, RoundInfo currentRound, Integer exitUserID,
      List<VoteInfo> voteInfo, List<UserScoreResult> scoreResults, List<UserGameResult> gameResults,
      Integer lastMLightUserID, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.roundOverTimeMs = roundOverTimeMs;
    this.currentRound = currentRound;
    this.exitUserID = exitUserID;
    this.voteInfo = Internal.immutableCopyOf("voteInfo", voteInfo);
    this.scoreResults = Internal.immutableCopyOf("scoreResults", scoreResults);
    this.gameResults = Internal.immutableCopyOf("gameResults", gameResults);
    this.lastMLightUserID = lastMLightUserID;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.roundOverTimeMs = roundOverTimeMs;
    builder.currentRound = currentRound;
    builder.exitUserID = exitUserID;
    builder.voteInfo = Internal.copyOf("voteInfo", voteInfo);
    builder.scoreResults = Internal.copyOf("scoreResults", scoreResults);
    builder.gameResults = Internal.copyOf("gameResults", gameResults);
    builder.lastMLightUserID = lastMLightUserID;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof RoundAndGameOverMsg)) return false;
    RoundAndGameOverMsg o = (RoundAndGameOverMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(roundOverTimeMs, o.roundOverTimeMs)
        && Internal.equals(currentRound, o.currentRound)
        && Internal.equals(exitUserID, o.exitUserID)
        && voteInfo.equals(o.voteInfo)
        && scoreResults.equals(o.scoreResults)
        && gameResults.equals(o.gameResults)
        && Internal.equals(lastMLightUserID, o.lastMLightUserID);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (roundOverTimeMs != null ? roundOverTimeMs.hashCode() : 0);
      result = result * 37 + (currentRound != null ? currentRound.hashCode() : 0);
      result = result * 37 + (exitUserID != null ? exitUserID.hashCode() : 0);
      result = result * 37 + voteInfo.hashCode();
      result = result * 37 + scoreResults.hashCode();
      result = result * 37 + gameResults.hashCode();
      result = result * 37 + (lastMLightUserID != null ? lastMLightUserID.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (roundOverTimeMs != null) builder.append(", roundOverTimeMs=").append(roundOverTimeMs);
    if (currentRound != null) builder.append(", currentRound=").append(currentRound);
    if (exitUserID != null) builder.append(", exitUserID=").append(exitUserID);
    if (!voteInfo.isEmpty()) builder.append(", voteInfo=").append(voteInfo);
    if (!scoreResults.isEmpty()) builder.append(", scoreResults=").append(scoreResults);
    if (!gameResults.isEmpty()) builder.append(", gameResults=").append(gameResults);
    if (lastMLightUserID != null) builder.append(", lastMLightUserID=").append(lastMLightUserID);
    return builder.replace(0, 2, "RoundAndGameOverMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return RoundAndGameOverMsg.ADAPTER.encode(this);
  }

  public static final RoundAndGameOverMsg parseFrom(byte[] data) throws IOException {
    RoundAndGameOverMsg c = null;
       c = RoundAndGameOverMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 轮次结束的毫秒时间戳
   */
  public Long getRoundOverTimeMs() {
    if(roundOverTimeMs==null){
        return DEFAULT_ROUNDOVERTIMEMS;
    }
    return roundOverTimeMs;
  }

  /**
   * 当前轮次信息
   */
  public RoundInfo getCurrentRound() {
    if(currentRound==null){
        return new RoundInfo.Builder().build();
    }
    return currentRound;
  }

  /**
   * 退出用户的ID
   */
  public Integer getExitUserID() {
    if(exitUserID==null){
        return DEFAULT_EXITUSERID;
    }
    return exitUserID;
  }

  /**
   * 投票打分信息
   */
  public List<VoteInfo> getVoteInfoList() {
    if(voteInfo==null){
        return new java.util.ArrayList<VoteInfo>();
    }
    return voteInfo;
  }

  /**
   * 所有参与者的评分结果，应该使用这个结构
   */
  public List<UserScoreResult> getScoreResultsList() {
    if(scoreResults==null){
        return new java.util.ArrayList<UserScoreResult>();
    }
    return scoreResults;
  }

  /**
   * 游戏结果评分数据
   */
  public List<UserGameResult> getGameResultsList() {
    if(gameResults==null){
        return new java.util.ArrayList<UserGameResult>();
    }
    return gameResults;
  }

  /**
   * 最后一个灭灯的用户ID
   */
  public Integer getLastMLightUserID() {
    if(lastMLightUserID==null){
        return DEFAULT_LASTMLIGHTUSERID;
    }
    return lastMLightUserID;
  }

  /**
   * 轮次结束的毫秒时间戳
   */
  public boolean hasRoundOverTimeMs() {
    return roundOverTimeMs!=null;
  }

  /**
   * 当前轮次信息
   */
  public boolean hasCurrentRound() {
    return currentRound!=null;
  }

  /**
   * 退出用户的ID
   */
  public boolean hasExitUserID() {
    return exitUserID!=null;
  }

  /**
   * 投票打分信息
   */
  public boolean hasVoteInfoList() {
    return voteInfo!=null;
  }

  /**
   * 所有参与者的评分结果，应该使用这个结构
   */
  public boolean hasScoreResultsList() {
    return scoreResults!=null;
  }

  /**
   * 游戏结果评分数据
   */
  public boolean hasGameResultsList() {
    return gameResults!=null;
  }

  /**
   * 最后一个灭灯的用户ID
   */
  public boolean hasLastMLightUserID() {
    return lastMLightUserID!=null;
  }

  public static final class Builder extends Message.Builder<RoundAndGameOverMsg, Builder> {
    private Long roundOverTimeMs;

    private RoundInfo currentRound;

    private Integer exitUserID;

    private List<VoteInfo> voteInfo;

    private List<UserScoreResult> scoreResults;

    private List<UserGameResult> gameResults;

    private Integer lastMLightUserID;

    public Builder() {
      voteInfo = Internal.newMutableList();
      scoreResults = Internal.newMutableList();
      gameResults = Internal.newMutableList();
    }

    /**
     * 轮次结束的毫秒时间戳
     */
    public Builder setRoundOverTimeMs(Long roundOverTimeMs) {
      this.roundOverTimeMs = roundOverTimeMs;
      return this;
    }

    /**
     * 当前轮次信息
     */
    public Builder setCurrentRound(RoundInfo currentRound) {
      this.currentRound = currentRound;
      return this;
    }

    /**
     * 退出用户的ID
     */
    public Builder setExitUserID(Integer exitUserID) {
      this.exitUserID = exitUserID;
      return this;
    }

    /**
     * 投票打分信息
     */
    public Builder addAllVoteInfo(List<VoteInfo> voteInfo) {
      Internal.checkElementsNotNull(voteInfo);
      this.voteInfo = voteInfo;
      return this;
    }

    /**
     * 所有参与者的评分结果，应该使用这个结构
     */
    public Builder addAllScoreResults(List<UserScoreResult> scoreResults) {
      Internal.checkElementsNotNull(scoreResults);
      this.scoreResults = scoreResults;
      return this;
    }

    /**
     * 游戏结果评分数据
     */
    public Builder addAllGameResults(List<UserGameResult> gameResults) {
      Internal.checkElementsNotNull(gameResults);
      this.gameResults = gameResults;
      return this;
    }

    /**
     * 最后一个灭灯的用户ID
     */
    public Builder setLastMLightUserID(Integer lastMLightUserID) {
      this.lastMLightUserID = lastMLightUserID;
      return this;
    }

    @Override
    public RoundAndGameOverMsg build() {
      return new RoundAndGameOverMsg(roundOverTimeMs, currentRound, exitUserID, voteInfo, scoreResults, gameResults, lastMLightUserID, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_RoundAndGameOverMsg extends ProtoAdapter<RoundAndGameOverMsg> {
    public ProtoAdapter_RoundAndGameOverMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, RoundAndGameOverMsg.class);
    }

    @Override
    public int encodedSize(RoundAndGameOverMsg value) {
      return ProtoAdapter.SINT64.encodedSizeWithTag(1, value.roundOverTimeMs)
          + RoundInfo.ADAPTER.encodedSizeWithTag(2, value.currentRound)
          + ProtoAdapter.UINT32.encodedSizeWithTag(3, value.exitUserID)
          + VoteInfo.ADAPTER.asRepeated().encodedSizeWithTag(4, value.voteInfo)
          + UserScoreResult.ADAPTER.asRepeated().encodedSizeWithTag(5, value.scoreResults)
          + UserGameResult.ADAPTER.asRepeated().encodedSizeWithTag(6, value.gameResults)
          + ProtoAdapter.UINT32.encodedSizeWithTag(7, value.lastMLightUserID)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, RoundAndGameOverMsg value) throws IOException {
      ProtoAdapter.SINT64.encodeWithTag(writer, 1, value.roundOverTimeMs);
      RoundInfo.ADAPTER.encodeWithTag(writer, 2, value.currentRound);
      ProtoAdapter.UINT32.encodeWithTag(writer, 3, value.exitUserID);
      VoteInfo.ADAPTER.asRepeated().encodeWithTag(writer, 4, value.voteInfo);
      UserScoreResult.ADAPTER.asRepeated().encodeWithTag(writer, 5, value.scoreResults);
      UserGameResult.ADAPTER.asRepeated().encodeWithTag(writer, 6, value.gameResults);
      ProtoAdapter.UINT32.encodeWithTag(writer, 7, value.lastMLightUserID);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public RoundAndGameOverMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setRoundOverTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
          case 2: builder.setCurrentRound(RoundInfo.ADAPTER.decode(reader)); break;
          case 3: builder.setExitUserID(ProtoAdapter.UINT32.decode(reader)); break;
          case 4: builder.voteInfo.add(VoteInfo.ADAPTER.decode(reader)); break;
          case 5: builder.scoreResults.add(UserScoreResult.ADAPTER.decode(reader)); break;
          case 6: builder.gameResults.add(UserGameResult.ADAPTER.decode(reader)); break;
          case 7: builder.setLastMLightUserID(ProtoAdapter.UINT32.decode(reader)); break;
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
    public RoundAndGameOverMsg redact(RoundAndGameOverMsg value) {
      Builder builder = value.newBuilder();
      if (builder.currentRound != null) builder.currentRound = RoundInfo.ADAPTER.redact(builder.currentRound);
      Internal.redactElements(builder.voteInfo, VoteInfo.ADAPTER);
      Internal.redactElements(builder.scoreResults, UserScoreResult.ADAPTER);
      Internal.redactElements(builder.gameResults, UserGameResult.ADAPTER);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}