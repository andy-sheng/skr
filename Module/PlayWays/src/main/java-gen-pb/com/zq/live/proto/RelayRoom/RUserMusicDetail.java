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
import com.zq.live.proto.Common.MusicInfo;
import java.io.IOException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class RUserMusicDetail extends Message<RUserMusicDetail, RUserMusicDetail.Builder> {
  public static final ProtoAdapter<RUserMusicDetail> ADAPTER = new ProtoAdapter_RUserMusicDetail();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_USERID = 0;

  public static final Integer DEFAULT_PEERID = 0;

  public static final String DEFAULT_UNIQTAG = "";

  public static final ERSongStatus DEFAULT_STATUS = ERSongStatus.ERUSI_UNKNOWN;

  /**
   * 发起用户
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer userID;

  /**
   * 合唱用户
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer peerID;

  /**
   * 歌曲标识
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String uniqTag;

  /**
   * 演唱音乐
   */
  @WireField(
      tag = 4,
      adapter = "com.zq.live.proto.Common.MusicInfo#ADAPTER"
  )
  private final MusicInfo music;

  /**
   * 状态
   */
  @WireField(
      tag = 5,
      adapter = "com.zq.live.proto.RelayRoom.ERSongStatus#ADAPTER"
  )
  private final ERSongStatus status;

  public RUserMusicDetail(Integer userID, Integer peerID, String uniqTag, MusicInfo music,
      ERSongStatus status) {
    this(userID, peerID, uniqTag, music, status, ByteString.EMPTY);
  }

  public RUserMusicDetail(Integer userID, Integer peerID, String uniqTag, MusicInfo music,
      ERSongStatus status, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.userID = userID;
    this.peerID = peerID;
    this.uniqTag = uniqTag;
    this.music = music;
    this.status = status;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.userID = userID;
    builder.peerID = peerID;
    builder.uniqTag = uniqTag;
    builder.music = music;
    builder.status = status;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof RUserMusicDetail)) return false;
    RUserMusicDetail o = (RUserMusicDetail) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(userID, o.userID)
        && Internal.equals(peerID, o.peerID)
        && Internal.equals(uniqTag, o.uniqTag)
        && Internal.equals(music, o.music)
        && Internal.equals(status, o.status);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (userID != null ? userID.hashCode() : 0);
      result = result * 37 + (peerID != null ? peerID.hashCode() : 0);
      result = result * 37 + (uniqTag != null ? uniqTag.hashCode() : 0);
      result = result * 37 + (music != null ? music.hashCode() : 0);
      result = result * 37 + (status != null ? status.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (userID != null) builder.append(", userID=").append(userID);
    if (peerID != null) builder.append(", peerID=").append(peerID);
    if (uniqTag != null) builder.append(", uniqTag=").append(uniqTag);
    if (music != null) builder.append(", music=").append(music);
    if (status != null) builder.append(", status=").append(status);
    return builder.replace(0, 2, "RUserMusicDetail{").append('}').toString();
  }

  public byte[] toByteArray() {
    return RUserMusicDetail.ADAPTER.encode(this);
  }

  public static final RUserMusicDetail parseFrom(byte[] data) throws IOException {
    RUserMusicDetail c = null;
       c = RUserMusicDetail.ADAPTER.decode(data);
    return c;
  }

  /**
   * 发起用户
   */
  public Integer getUserID() {
    if(userID==null){
        return DEFAULT_USERID;
    }
    return userID;
  }

  /**
   * 合唱用户
   */
  public Integer getPeerID() {
    if(peerID==null){
        return DEFAULT_PEERID;
    }
    return peerID;
  }

  /**
   * 歌曲标识
   */
  public String getUniqTag() {
    if(uniqTag==null){
        return DEFAULT_UNIQTAG;
    }
    return uniqTag;
  }

  /**
   * 演唱音乐
   */
  public MusicInfo getMusic() {
    if(music==null){
        return new MusicInfo.Builder().build();
    }
    return music;
  }

  /**
   * 状态
   */
  public ERSongStatus getStatus() {
    if(status==null){
        return new ERSongStatus.Builder().build();
    }
    return status;
  }

  /**
   * 发起用户
   */
  public boolean hasUserID() {
    return userID!=null;
  }

  /**
   * 合唱用户
   */
  public boolean hasPeerID() {
    return peerID!=null;
  }

  /**
   * 歌曲标识
   */
  public boolean hasUniqTag() {
    return uniqTag!=null;
  }

  /**
   * 演唱音乐
   */
  public boolean hasMusic() {
    return music!=null;
  }

  /**
   * 状态
   */
  public boolean hasStatus() {
    return status!=null;
  }

  public static final class Builder extends Message.Builder<RUserMusicDetail, Builder> {
    private Integer userID;

    private Integer peerID;

    private String uniqTag;

    private MusicInfo music;

    private ERSongStatus status;

    public Builder() {
    }

    /**
     * 发起用户
     */
    public Builder setUserID(Integer userID) {
      this.userID = userID;
      return this;
    }

    /**
     * 合唱用户
     */
    public Builder setPeerID(Integer peerID) {
      this.peerID = peerID;
      return this;
    }

    /**
     * 歌曲标识
     */
    public Builder setUniqTag(String uniqTag) {
      this.uniqTag = uniqTag;
      return this;
    }

    /**
     * 演唱音乐
     */
    public Builder setMusic(MusicInfo music) {
      this.music = music;
      return this;
    }

    /**
     * 状态
     */
    public Builder setStatus(ERSongStatus status) {
      this.status = status;
      return this;
    }

    @Override
    public RUserMusicDetail build() {
      return new RUserMusicDetail(userID, peerID, uniqTag, music, status, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_RUserMusicDetail extends ProtoAdapter<RUserMusicDetail> {
    public ProtoAdapter_RUserMusicDetail() {
      super(FieldEncoding.LENGTH_DELIMITED, RUserMusicDetail.class);
    }

    @Override
    public int encodedSize(RUserMusicDetail value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.userID)
          + ProtoAdapter.UINT32.encodedSizeWithTag(2, value.peerID)
          + ProtoAdapter.STRING.encodedSizeWithTag(3, value.uniqTag)
          + MusicInfo.ADAPTER.encodedSizeWithTag(4, value.music)
          + ERSongStatus.ADAPTER.encodedSizeWithTag(5, value.status)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, RUserMusicDetail value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.userID);
      ProtoAdapter.UINT32.encodeWithTag(writer, 2, value.peerID);
      ProtoAdapter.STRING.encodeWithTag(writer, 3, value.uniqTag);
      MusicInfo.ADAPTER.encodeWithTag(writer, 4, value.music);
      ERSongStatus.ADAPTER.encodeWithTag(writer, 5, value.status);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public RUserMusicDetail decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setUserID(ProtoAdapter.UINT32.decode(reader)); break;
          case 2: builder.setPeerID(ProtoAdapter.UINT32.decode(reader)); break;
          case 3: builder.setUniqTag(ProtoAdapter.STRING.decode(reader)); break;
          case 4: builder.setMusic(MusicInfo.ADAPTER.decode(reader)); break;
          case 5: {
            try {
              builder.setStatus(ERSongStatus.ADAPTER.decode(reader));
            } catch (ProtoAdapter.EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
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
    public RUserMusicDetail redact(RUserMusicDetail value) {
      Builder builder = value.newBuilder();
      if (builder.music != null) builder.music = MusicInfo.ADAPTER.redact(builder.music);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
