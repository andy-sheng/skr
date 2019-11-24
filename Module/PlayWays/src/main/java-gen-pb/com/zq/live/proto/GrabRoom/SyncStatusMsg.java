// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: grab_room.proto
package com.zq.live.proto.GrabRoom;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import okio.ByteString;

public final class SyncStatusMsg extends Message<SyncStatusMsg, SyncStatusMsg.Builder> {
  public static final ProtoAdapter<SyncStatusMsg> ADAPTER = new ProtoAdapter_SyncStatusMsg();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_SYNCSTATUSTIMEMS = 0L;

  public static final Long DEFAULT_GAMEOVERTIMEMS = 0L;

  /**
   * 状态同步时的毫秒时间戳
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long syncStatusTimeMs;

  /**
   * 游戏结束时间
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long gameOverTimeMs;

  /**
   * 在线状态
   */
  @WireField(
      tag = 3,
      adapter = "com.zq.live.proto.GrabRoom.OnlineInfo#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<OnlineInfo> onlineInfo;

  /**
   * 当前轮次信息
   */
  @WireField(
      tag = 4,
      adapter = "com.zq.live.proto.GrabRoom.RoundInfo#ADAPTER"
  )
  private final RoundInfo currentRound;

  /**
   * 下个轮次信息
   */
  @WireField(
      tag = 5,
      adapter = "com.zq.live.proto.GrabRoom.RoundInfo#ADAPTER"
  )
  private final RoundInfo nextRound;

  public SyncStatusMsg(Long syncStatusTimeMs, Long gameOverTimeMs, List<OnlineInfo> onlineInfo,
      RoundInfo currentRound, RoundInfo nextRound) {
    this(syncStatusTimeMs, gameOverTimeMs, onlineInfo, currentRound, nextRound, ByteString.EMPTY);
  }

  public SyncStatusMsg(Long syncStatusTimeMs, Long gameOverTimeMs, List<OnlineInfo> onlineInfo,
      RoundInfo currentRound, RoundInfo nextRound, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.syncStatusTimeMs = syncStatusTimeMs;
    this.gameOverTimeMs = gameOverTimeMs;
    this.onlineInfo = Internal.immutableCopyOf("onlineInfo", onlineInfo);
    this.currentRound = currentRound;
    this.nextRound = nextRound;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.syncStatusTimeMs = syncStatusTimeMs;
    builder.gameOverTimeMs = gameOverTimeMs;
    builder.onlineInfo = Internal.copyOf("onlineInfo", onlineInfo);
    builder.currentRound = currentRound;
    builder.nextRound = nextRound;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof SyncStatusMsg)) return false;
    SyncStatusMsg o = (SyncStatusMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(syncStatusTimeMs, o.syncStatusTimeMs)
        && Internal.equals(gameOverTimeMs, o.gameOverTimeMs)
        && onlineInfo.equals(o.onlineInfo)
        && Internal.equals(currentRound, o.currentRound)
        && Internal.equals(nextRound, o.nextRound);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (syncStatusTimeMs != null ? syncStatusTimeMs.hashCode() : 0);
      result = result * 37 + (gameOverTimeMs != null ? gameOverTimeMs.hashCode() : 0);
      result = result * 37 + onlineInfo.hashCode();
      result = result * 37 + (currentRound != null ? currentRound.hashCode() : 0);
      result = result * 37 + (nextRound != null ? nextRound.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (syncStatusTimeMs != null) builder.append(", syncStatusTimeMs=").append(syncStatusTimeMs);
    if (gameOverTimeMs != null) builder.append(", gameOverTimeMs=").append(gameOverTimeMs);
    if (!onlineInfo.isEmpty()) builder.append(", onlineInfo=").append(onlineInfo);
    if (currentRound != null) builder.append(", currentRound=").append(currentRound);
    if (nextRound != null) builder.append(", nextRound=").append(nextRound);
    return builder.replace(0, 2, "SyncStatusMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return SyncStatusMsg.ADAPTER.encode(this);
  }

  public static final SyncStatusMsg parseFrom(byte[] data) throws IOException {
    SyncStatusMsg c = null;
       c = SyncStatusMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 状态同步时的毫秒时间戳
   */
  public Long getSyncStatusTimeMs() {
    if(syncStatusTimeMs==null){
        return DEFAULT_SYNCSTATUSTIMEMS;
    }
    return syncStatusTimeMs;
  }

  /**
   * 游戏结束时间
   */
  public Long getGameOverTimeMs() {
    if(gameOverTimeMs==null){
        return DEFAULT_GAMEOVERTIMEMS;
    }
    return gameOverTimeMs;
  }

  /**
   * 在线状态
   */
  public List<OnlineInfo> getOnlineInfoList() {
    if(onlineInfo==null){
        return new java.util.ArrayList<OnlineInfo>();
    }
    return onlineInfo;
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
   * 下个轮次信息
   */
  public RoundInfo getNextRound() {
    if(nextRound==null){
        return new RoundInfo.Builder().build();
    }
    return nextRound;
  }

  /**
   * 状态同步时的毫秒时间戳
   */
  public boolean hasSyncStatusTimeMs() {
    return syncStatusTimeMs!=null;
  }

  /**
   * 游戏结束时间
   */
  public boolean hasGameOverTimeMs() {
    return gameOverTimeMs!=null;
  }

  /**
   * 在线状态
   */
  public boolean hasOnlineInfoList() {
    return onlineInfo!=null;
  }

  /**
   * 当前轮次信息
   */
  public boolean hasCurrentRound() {
    return currentRound!=null;
  }

  /**
   * 下个轮次信息
   */
  public boolean hasNextRound() {
    return nextRound!=null;
  }

  public static final class Builder extends Message.Builder<SyncStatusMsg, Builder> {
    private Long syncStatusTimeMs;

    private Long gameOverTimeMs;

    private List<OnlineInfo> onlineInfo;

    private RoundInfo currentRound;

    private RoundInfo nextRound;

    public Builder() {
      onlineInfo = Internal.newMutableList();
    }

    /**
     * 状态同步时的毫秒时间戳
     */
    public Builder setSyncStatusTimeMs(Long syncStatusTimeMs) {
      this.syncStatusTimeMs = syncStatusTimeMs;
      return this;
    }

    /**
     * 游戏结束时间
     */
    public Builder setGameOverTimeMs(Long gameOverTimeMs) {
      this.gameOverTimeMs = gameOverTimeMs;
      return this;
    }

    /**
     * 在线状态
     */
    public Builder addAllOnlineInfo(List<OnlineInfo> onlineInfo) {
      Internal.checkElementsNotNull(onlineInfo);
      this.onlineInfo = onlineInfo;
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
     * 下个轮次信息
     */
    public Builder setNextRound(RoundInfo nextRound) {
      this.nextRound = nextRound;
      return this;
    }

    @Override
    public SyncStatusMsg build() {
      return new SyncStatusMsg(syncStatusTimeMs, gameOverTimeMs, onlineInfo, currentRound, nextRound, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_SyncStatusMsg extends ProtoAdapter<SyncStatusMsg> {
    public ProtoAdapter_SyncStatusMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, SyncStatusMsg.class);
    }

    @Override
    public int encodedSize(SyncStatusMsg value) {
      return ProtoAdapter.SINT64.encodedSizeWithTag(1, value.syncStatusTimeMs)
          + ProtoAdapter.SINT64.encodedSizeWithTag(2, value.gameOverTimeMs)
          + OnlineInfo.ADAPTER.asRepeated().encodedSizeWithTag(3, value.onlineInfo)
          + RoundInfo.ADAPTER.encodedSizeWithTag(4, value.currentRound)
          + RoundInfo.ADAPTER.encodedSizeWithTag(5, value.nextRound)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, SyncStatusMsg value) throws IOException {
      ProtoAdapter.SINT64.encodeWithTag(writer, 1, value.syncStatusTimeMs);
      ProtoAdapter.SINT64.encodeWithTag(writer, 2, value.gameOverTimeMs);
      OnlineInfo.ADAPTER.asRepeated().encodeWithTag(writer, 3, value.onlineInfo);
      RoundInfo.ADAPTER.encodeWithTag(writer, 4, value.currentRound);
      RoundInfo.ADAPTER.encodeWithTag(writer, 5, value.nextRound);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public SyncStatusMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setSyncStatusTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
          case 2: builder.setGameOverTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
          case 3: builder.onlineInfo.add(OnlineInfo.ADAPTER.decode(reader)); break;
          case 4: builder.setCurrentRound(RoundInfo.ADAPTER.decode(reader)); break;
          case 5: builder.setNextRound(RoundInfo.ADAPTER.decode(reader)); break;
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
    public SyncStatusMsg redact(SyncStatusMsg value) {
      Builder builder = value.newBuilder();
      Internal.redactElements(builder.onlineInfo, OnlineInfo.ADAPTER);
      if (builder.currentRound != null) builder.currentRound = RoundInfo.ADAPTER.redact(builder.currentRound);
      if (builder.nextRound != null) builder.nextRound = RoundInfo.ADAPTER.redact(builder.nextRound);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
