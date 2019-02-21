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
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

/**
 * 准备并开始游戏通知消息
 */
public final class ReadyAndStartNoticeMsg extends Message<ReadyAndStartNoticeMsg, ReadyAndStartNoticeMsg.Builder> {
  public static final ProtoAdapter<ReadyAndStartNoticeMsg> ADAPTER = new ProtoAdapter_ReadyAndStartNoticeMsg();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_READYUSERID = 0;

  public static final Long DEFAULT_READYTIMEMS = 0L;

  public static final Long DEFAULT_STARTTIMEMS = 0L;

  public static final Integer DEFAULT_FIRSTUSERID = 0;

  public static final Integer DEFAULT_FIRSTMUSICID = 0;

  /**
   * 准备用户ID
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer readyUserID;

  /**
   * 准备的毫秒时间戳
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long readyTimeMs;

  /**
   * 开始的毫秒时间戳
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long startTimeMS;

  /**
   * 第一个用户ID
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer firstUserID;

  /**
   * 第一首歌曲ID
   */
  @WireField(
      tag = 5,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer firstMusicID;

  public ReadyAndStartNoticeMsg(Integer readyUserID, Long readyTimeMs, Long startTimeMS,
      Integer firstUserID, Integer firstMusicID) {
    this(readyUserID, readyTimeMs, startTimeMS, firstUserID, firstMusicID, ByteString.EMPTY);
  }

  public ReadyAndStartNoticeMsg(Integer readyUserID, Long readyTimeMs, Long startTimeMS,
      Integer firstUserID, Integer firstMusicID, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.readyUserID = readyUserID;
    this.readyTimeMs = readyTimeMs;
    this.startTimeMS = startTimeMS;
    this.firstUserID = firstUserID;
    this.firstMusicID = firstMusicID;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.readyUserID = readyUserID;
    builder.readyTimeMs = readyTimeMs;
    builder.startTimeMS = startTimeMS;
    builder.firstUserID = firstUserID;
    builder.firstMusicID = firstMusicID;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof ReadyAndStartNoticeMsg)) return false;
    ReadyAndStartNoticeMsg o = (ReadyAndStartNoticeMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(readyUserID, o.readyUserID)
        && Internal.equals(readyTimeMs, o.readyTimeMs)
        && Internal.equals(startTimeMS, o.startTimeMS)
        && Internal.equals(firstUserID, o.firstUserID)
        && Internal.equals(firstMusicID, o.firstMusicID);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (readyUserID != null ? readyUserID.hashCode() : 0);
      result = result * 37 + (readyTimeMs != null ? readyTimeMs.hashCode() : 0);
      result = result * 37 + (startTimeMS != null ? startTimeMS.hashCode() : 0);
      result = result * 37 + (firstUserID != null ? firstUserID.hashCode() : 0);
      result = result * 37 + (firstMusicID != null ? firstMusicID.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (readyUserID != null) builder.append(", readyUserID=").append(readyUserID);
    if (readyTimeMs != null) builder.append(", readyTimeMs=").append(readyTimeMs);
    if (startTimeMS != null) builder.append(", startTimeMS=").append(startTimeMS);
    if (firstUserID != null) builder.append(", firstUserID=").append(firstUserID);
    if (firstMusicID != null) builder.append(", firstMusicID=").append(firstMusicID);
    return builder.replace(0, 2, "ReadyAndStartNoticeMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return ReadyAndStartNoticeMsg.ADAPTER.encode(this);
  }

  public static final ReadyAndStartNoticeMsg parseFrom(byte[] data) throws IOException {
    ReadyAndStartNoticeMsg c = null;
       c = ReadyAndStartNoticeMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 准备用户ID
   */
  public Integer getReadyUserID() {
    if(readyUserID==null){
        return DEFAULT_READYUSERID;
    }
    return readyUserID;
  }

  /**
   * 准备的毫秒时间戳
   */
  public Long getReadyTimeMs() {
    if(readyTimeMs==null){
        return DEFAULT_READYTIMEMS;
    }
    return readyTimeMs;
  }

  /**
   * 开始的毫秒时间戳
   */
  public Long getStartTimeMS() {
    if(startTimeMS==null){
        return DEFAULT_STARTTIMEMS;
    }
    return startTimeMS;
  }

  /**
   * 第一个用户ID
   */
  public Integer getFirstUserID() {
    if(firstUserID==null){
        return DEFAULT_FIRSTUSERID;
    }
    return firstUserID;
  }

  /**
   * 第一首歌曲ID
   */
  public Integer getFirstMusicID() {
    if(firstMusicID==null){
        return DEFAULT_FIRSTMUSICID;
    }
    return firstMusicID;
  }

  /**
   * 准备用户ID
   */
  public boolean hasReadyUserID() {
    return readyUserID!=null;
  }

  /**
   * 准备的毫秒时间戳
   */
  public boolean hasReadyTimeMs() {
    return readyTimeMs!=null;
  }

  /**
   * 开始的毫秒时间戳
   */
  public boolean hasStartTimeMS() {
    return startTimeMS!=null;
  }

  /**
   * 第一个用户ID
   */
  public boolean hasFirstUserID() {
    return firstUserID!=null;
  }

  /**
   * 第一首歌曲ID
   */
  public boolean hasFirstMusicID() {
    return firstMusicID!=null;
  }

  public static final class Builder extends Message.Builder<ReadyAndStartNoticeMsg, Builder> {
    private Integer readyUserID;

    private Long readyTimeMs;

    private Long startTimeMS;

    private Integer firstUserID;

    private Integer firstMusicID;

    public Builder() {
    }

    /**
     * 准备用户ID
     */
    public Builder setReadyUserID(Integer readyUserID) {
      this.readyUserID = readyUserID;
      return this;
    }

    /**
     * 准备的毫秒时间戳
     */
    public Builder setReadyTimeMs(Long readyTimeMs) {
      this.readyTimeMs = readyTimeMs;
      return this;
    }

    /**
     * 开始的毫秒时间戳
     */
    public Builder setStartTimeMS(Long startTimeMS) {
      this.startTimeMS = startTimeMS;
      return this;
    }

    /**
     * 第一个用户ID
     */
    public Builder setFirstUserID(Integer firstUserID) {
      this.firstUserID = firstUserID;
      return this;
    }

    /**
     * 第一首歌曲ID
     */
    public Builder setFirstMusicID(Integer firstMusicID) {
      this.firstMusicID = firstMusicID;
      return this;
    }

    @Override
    public ReadyAndStartNoticeMsg build() {
      return new ReadyAndStartNoticeMsg(readyUserID, readyTimeMs, startTimeMS, firstUserID, firstMusicID, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_ReadyAndStartNoticeMsg extends ProtoAdapter<ReadyAndStartNoticeMsg> {
    public ProtoAdapter_ReadyAndStartNoticeMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, ReadyAndStartNoticeMsg.class);
    }

    @Override
    public int encodedSize(ReadyAndStartNoticeMsg value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.readyUserID)
          + ProtoAdapter.SINT64.encodedSizeWithTag(2, value.readyTimeMs)
          + ProtoAdapter.SINT64.encodedSizeWithTag(3, value.startTimeMS)
          + ProtoAdapter.UINT32.encodedSizeWithTag(4, value.firstUserID)
          + ProtoAdapter.UINT32.encodedSizeWithTag(5, value.firstMusicID)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, ReadyAndStartNoticeMsg value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.readyUserID);
      ProtoAdapter.SINT64.encodeWithTag(writer, 2, value.readyTimeMs);
      ProtoAdapter.SINT64.encodeWithTag(writer, 3, value.startTimeMS);
      ProtoAdapter.UINT32.encodeWithTag(writer, 4, value.firstUserID);
      ProtoAdapter.UINT32.encodeWithTag(writer, 5, value.firstMusicID);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public ReadyAndStartNoticeMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setReadyUserID(ProtoAdapter.UINT32.decode(reader)); break;
          case 2: builder.setReadyTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
          case 3: builder.setStartTimeMS(ProtoAdapter.SINT64.decode(reader)); break;
          case 4: builder.setFirstUserID(ProtoAdapter.UINT32.decode(reader)); break;
          case 5: builder.setFirstMusicID(ProtoAdapter.UINT32.decode(reader)); break;
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
    public ReadyAndStartNoticeMsg redact(ReadyAndStartNoticeMsg value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
