// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: relay_room.proto
package com.zq.live.proto.RelayRoom;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Boolean;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import okio.ByteString;

public final class RSyncMsg extends Message<RSyncMsg, RSyncMsg.Builder> {
  public static final ProtoAdapter<RSyncMsg> ADAPTER = new ProtoAdapter_RSyncMsg();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_SYNCSTATUSTIMEMS = 0L;

  public static final Long DEFAULT_PASSEDTIMEMS = 0L;

  public static final Boolean DEFAULT_ENABLENOLIMITDURATION = false;

  /**
   * 状态同步时的毫秒时间戳
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long syncStatusTimeMs;

  /**
   * 房间已经经历的毫秒数
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long passedTimeMs;

  /**
   * 用户锁定信息
   */
  @WireField(
      tag = 3,
      adapter = "com.zq.live.proto.RelayRoom.RUserLockInfo#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<RUserLockInfo> userLockInfo;

  /**
   * 开启没有限制的持续时间
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  private final Boolean enableNoLimitDuration;

  /**
   * 当前轮次信息
   */
  @WireField(
      tag = 5,
      adapter = "com.zq.live.proto.RelayRoom.RRoundInfo#ADAPTER"
  )
  private final RRoundInfo currentRound;

  public RSyncMsg(Long syncStatusTimeMs, Long passedTimeMs, List<RUserLockInfo> userLockInfo,
      Boolean enableNoLimitDuration, RRoundInfo currentRound) {
    this(syncStatusTimeMs, passedTimeMs, userLockInfo, enableNoLimitDuration, currentRound, ByteString.EMPTY);
  }

  public RSyncMsg(Long syncStatusTimeMs, Long passedTimeMs, List<RUserLockInfo> userLockInfo,
      Boolean enableNoLimitDuration, RRoundInfo currentRound, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.syncStatusTimeMs = syncStatusTimeMs;
    this.passedTimeMs = passedTimeMs;
    this.userLockInfo = Internal.immutableCopyOf("userLockInfo", userLockInfo);
    this.enableNoLimitDuration = enableNoLimitDuration;
    this.currentRound = currentRound;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.syncStatusTimeMs = syncStatusTimeMs;
    builder.passedTimeMs = passedTimeMs;
    builder.userLockInfo = Internal.copyOf("userLockInfo", userLockInfo);
    builder.enableNoLimitDuration = enableNoLimitDuration;
    builder.currentRound = currentRound;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof RSyncMsg)) return false;
    RSyncMsg o = (RSyncMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(syncStatusTimeMs, o.syncStatusTimeMs)
        && Internal.equals(passedTimeMs, o.passedTimeMs)
        && userLockInfo.equals(o.userLockInfo)
        && Internal.equals(enableNoLimitDuration, o.enableNoLimitDuration)
        && Internal.equals(currentRound, o.currentRound);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (syncStatusTimeMs != null ? syncStatusTimeMs.hashCode() : 0);
      result = result * 37 + (passedTimeMs != null ? passedTimeMs.hashCode() : 0);
      result = result * 37 + userLockInfo.hashCode();
      result = result * 37 + (enableNoLimitDuration != null ? enableNoLimitDuration.hashCode() : 0);
      result = result * 37 + (currentRound != null ? currentRound.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (syncStatusTimeMs != null) builder.append(", syncStatusTimeMs=").append(syncStatusTimeMs);
    if (passedTimeMs != null) builder.append(", passedTimeMs=").append(passedTimeMs);
    if (!userLockInfo.isEmpty()) builder.append(", userLockInfo=").append(userLockInfo);
    if (enableNoLimitDuration != null) builder.append(", enableNoLimitDuration=").append(enableNoLimitDuration);
    if (currentRound != null) builder.append(", currentRound=").append(currentRound);
    return builder.replace(0, 2, "RSyncMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return RSyncMsg.ADAPTER.encode(this);
  }

  public static final RSyncMsg parseFrom(byte[] data) throws IOException {
    RSyncMsg c = null;
       c = RSyncMsg.ADAPTER.decode(data);
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
   * 房间已经经历的毫秒数
   */
  public Long getPassedTimeMs() {
    if(passedTimeMs==null){
        return DEFAULT_PASSEDTIMEMS;
    }
    return passedTimeMs;
  }

  /**
   * 用户锁定信息
   */
  public List<RUserLockInfo> getUserLockInfoList() {
    if(userLockInfo==null){
        return new java.util.ArrayList<RUserLockInfo>();
    }
    return userLockInfo;
  }

  /**
   * 开启没有限制的持续时间
   */
  public Boolean getEnableNoLimitDuration() {
    if(enableNoLimitDuration==null){
        return DEFAULT_ENABLENOLIMITDURATION;
    }
    return enableNoLimitDuration;
  }

  /**
   * 当前轮次信息
   */
  public RRoundInfo getCurrentRound() {
    if(currentRound==null){
        return new RRoundInfo.Builder().build();
    }
    return currentRound;
  }

  /**
   * 状态同步时的毫秒时间戳
   */
  public boolean hasSyncStatusTimeMs() {
    return syncStatusTimeMs!=null;
  }

  /**
   * 房间已经经历的毫秒数
   */
  public boolean hasPassedTimeMs() {
    return passedTimeMs!=null;
  }

  /**
   * 用户锁定信息
   */
  public boolean hasUserLockInfoList() {
    return userLockInfo!=null;
  }

  /**
   * 开启没有限制的持续时间
   */
  public boolean hasEnableNoLimitDuration() {
    return enableNoLimitDuration!=null;
  }

  /**
   * 当前轮次信息
   */
  public boolean hasCurrentRound() {
    return currentRound!=null;
  }

  public static final class Builder extends Message.Builder<RSyncMsg, Builder> {
    private Long syncStatusTimeMs;

    private Long passedTimeMs;

    private List<RUserLockInfo> userLockInfo;

    private Boolean enableNoLimitDuration;

    private RRoundInfo currentRound;

    public Builder() {
      userLockInfo = Internal.newMutableList();
    }

    /**
     * 状态同步时的毫秒时间戳
     */
    public Builder setSyncStatusTimeMs(Long syncStatusTimeMs) {
      this.syncStatusTimeMs = syncStatusTimeMs;
      return this;
    }

    /**
     * 房间已经经历的毫秒数
     */
    public Builder setPassedTimeMs(Long passedTimeMs) {
      this.passedTimeMs = passedTimeMs;
      return this;
    }

    /**
     * 用户锁定信息
     */
    public Builder addAllUserLockInfo(List<RUserLockInfo> userLockInfo) {
      Internal.checkElementsNotNull(userLockInfo);
      this.userLockInfo = userLockInfo;
      return this;
    }

    /**
     * 开启没有限制的持续时间
     */
    public Builder setEnableNoLimitDuration(Boolean enableNoLimitDuration) {
      this.enableNoLimitDuration = enableNoLimitDuration;
      return this;
    }

    /**
     * 当前轮次信息
     */
    public Builder setCurrentRound(RRoundInfo currentRound) {
      this.currentRound = currentRound;
      return this;
    }

    @Override
    public RSyncMsg build() {
      return new RSyncMsg(syncStatusTimeMs, passedTimeMs, userLockInfo, enableNoLimitDuration, currentRound, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_RSyncMsg extends ProtoAdapter<RSyncMsg> {
    public ProtoAdapter_RSyncMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, RSyncMsg.class);
    }

    @Override
    public int encodedSize(RSyncMsg value) {
      return ProtoAdapter.SINT64.encodedSizeWithTag(1, value.syncStatusTimeMs)
          + ProtoAdapter.SINT64.encodedSizeWithTag(2, value.passedTimeMs)
          + RUserLockInfo.ADAPTER.asRepeated().encodedSizeWithTag(3, value.userLockInfo)
          + ProtoAdapter.BOOL.encodedSizeWithTag(4, value.enableNoLimitDuration)
          + RRoundInfo.ADAPTER.encodedSizeWithTag(5, value.currentRound)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, RSyncMsg value) throws IOException {
      ProtoAdapter.SINT64.encodeWithTag(writer, 1, value.syncStatusTimeMs);
      ProtoAdapter.SINT64.encodeWithTag(writer, 2, value.passedTimeMs);
      RUserLockInfo.ADAPTER.asRepeated().encodeWithTag(writer, 3, value.userLockInfo);
      ProtoAdapter.BOOL.encodeWithTag(writer, 4, value.enableNoLimitDuration);
      RRoundInfo.ADAPTER.encodeWithTag(writer, 5, value.currentRound);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public RSyncMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setSyncStatusTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
          case 2: builder.setPassedTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
          case 3: builder.userLockInfo.add(RUserLockInfo.ADAPTER.decode(reader)); break;
          case 4: builder.setEnableNoLimitDuration(ProtoAdapter.BOOL.decode(reader)); break;
          case 5: builder.setCurrentRound(RRoundInfo.ADAPTER.decode(reader)); break;
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
    public RSyncMsg redact(RSyncMsg value) {
      Builder builder = value.newBuilder();
      Internal.redactElements(builder.userLockInfo, RUserLockInfo.ADAPTER);
      if (builder.currentRound != null) builder.currentRound = RRoundInfo.ADAPTER.redact(builder.currentRound);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
