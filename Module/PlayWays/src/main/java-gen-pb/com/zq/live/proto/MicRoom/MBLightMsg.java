// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: mic_room.proto
package com.zq.live.proto.MicRoom;

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
import okio.ByteString;

public final class MBLightMsg extends Message<MBLightMsg, MBLightMsg.Builder> {
  public static final ProtoAdapter<MBLightMsg> ADAPTER = new ProtoAdapter_MBLightMsg();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_USERID = 0;

  public static final Integer DEFAULT_ROUNDSEQ = 0;

  public static final Integer DEFAULT_SUBROUNDSEQ = 0;

  public static final Integer DEFAULT_BLIGHTCNT = 0;

  /**
   * 玩家id
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer userID;

  /**
   * 主轮次顺序
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer RoundSeq;

  /**
   * 子轮次顺序
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer subRoundSeq;

  /**
   * 当前爆灯总数量
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer bLightCnt;

  public MBLightMsg(Integer userID, Integer RoundSeq, Integer subRoundSeq, Integer bLightCnt) {
    this(userID, RoundSeq, subRoundSeq, bLightCnt, ByteString.EMPTY);
  }

  public MBLightMsg(Integer userID, Integer RoundSeq, Integer subRoundSeq, Integer bLightCnt,
      ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.userID = userID;
    this.RoundSeq = RoundSeq;
    this.subRoundSeq = subRoundSeq;
    this.bLightCnt = bLightCnt;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.userID = userID;
    builder.RoundSeq = RoundSeq;
    builder.subRoundSeq = subRoundSeq;
    builder.bLightCnt = bLightCnt;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof MBLightMsg)) return false;
    MBLightMsg o = (MBLightMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(userID, o.userID)
        && Internal.equals(RoundSeq, o.RoundSeq)
        && Internal.equals(subRoundSeq, o.subRoundSeq)
        && Internal.equals(bLightCnt, o.bLightCnt);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (userID != null ? userID.hashCode() : 0);
      result = result * 37 + (RoundSeq != null ? RoundSeq.hashCode() : 0);
      result = result * 37 + (subRoundSeq != null ? subRoundSeq.hashCode() : 0);
      result = result * 37 + (bLightCnt != null ? bLightCnt.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (userID != null) builder.append(", userID=").append(userID);
    if (RoundSeq != null) builder.append(", RoundSeq=").append(RoundSeq);
    if (subRoundSeq != null) builder.append(", subRoundSeq=").append(subRoundSeq);
    if (bLightCnt != null) builder.append(", bLightCnt=").append(bLightCnt);
    return builder.replace(0, 2, "MBLightMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return MBLightMsg.ADAPTER.encode(this);
  }

  public static final MBLightMsg parseFrom(byte[] data) throws IOException {
    MBLightMsg c = null;
       c = MBLightMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 玩家id
   */
  public Integer getUserID() {
    if(userID==null){
        return DEFAULT_USERID;
    }
    return userID;
  }

  /**
   * 主轮次顺序
   */
  public Integer getRoundSeq() {
    if(RoundSeq==null){
        return DEFAULT_ROUNDSEQ;
    }
    return RoundSeq;
  }

  /**
   * 子轮次顺序
   */
  public Integer getSubRoundSeq() {
    if(subRoundSeq==null){
        return DEFAULT_SUBROUNDSEQ;
    }
    return subRoundSeq;
  }

  /**
   * 当前爆灯总数量
   */
  public Integer getBLightCnt() {
    if(bLightCnt==null){
        return DEFAULT_BLIGHTCNT;
    }
    return bLightCnt;
  }

  /**
   * 玩家id
   */
  public boolean hasUserID() {
    return userID!=null;
  }

  /**
   * 主轮次顺序
   */
  public boolean hasRoundSeq() {
    return RoundSeq!=null;
  }

  /**
   * 子轮次顺序
   */
  public boolean hasSubRoundSeq() {
    return subRoundSeq!=null;
  }

  /**
   * 当前爆灯总数量
   */
  public boolean hasBLightCnt() {
    return bLightCnt!=null;
  }

  public static final class Builder extends Message.Builder<MBLightMsg, Builder> {
    private Integer userID;

    private Integer RoundSeq;

    private Integer subRoundSeq;

    private Integer bLightCnt;

    public Builder() {
    }

    /**
     * 玩家id
     */
    public Builder setUserID(Integer userID) {
      this.userID = userID;
      return this;
    }

    /**
     * 主轮次顺序
     */
    public Builder setRoundSeq(Integer RoundSeq) {
      this.RoundSeq = RoundSeq;
      return this;
    }

    /**
     * 子轮次顺序
     */
    public Builder setSubRoundSeq(Integer subRoundSeq) {
      this.subRoundSeq = subRoundSeq;
      return this;
    }

    /**
     * 当前爆灯总数量
     */
    public Builder setBLightCnt(Integer bLightCnt) {
      this.bLightCnt = bLightCnt;
      return this;
    }

    @Override
    public MBLightMsg build() {
      return new MBLightMsg(userID, RoundSeq, subRoundSeq, bLightCnt, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_MBLightMsg extends ProtoAdapter<MBLightMsg> {
    public ProtoAdapter_MBLightMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, MBLightMsg.class);
    }

    @Override
    public int encodedSize(MBLightMsg value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.userID)
          + ProtoAdapter.UINT32.encodedSizeWithTag(2, value.RoundSeq)
          + ProtoAdapter.UINT32.encodedSizeWithTag(3, value.subRoundSeq)
          + ProtoAdapter.UINT32.encodedSizeWithTag(4, value.bLightCnt)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, MBLightMsg value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.userID);
      ProtoAdapter.UINT32.encodeWithTag(writer, 2, value.RoundSeq);
      ProtoAdapter.UINT32.encodeWithTag(writer, 3, value.subRoundSeq);
      ProtoAdapter.UINT32.encodeWithTag(writer, 4, value.bLightCnt);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public MBLightMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setUserID(ProtoAdapter.UINT32.decode(reader)); break;
          case 2: builder.setRoundSeq(ProtoAdapter.UINT32.decode(reader)); break;
          case 3: builder.setSubRoundSeq(ProtoAdapter.UINT32.decode(reader)); break;
          case 4: builder.setBLightCnt(ProtoAdapter.UINT32.decode(reader)); break;
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
    public MBLightMsg redact(MBLightMsg value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
