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
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class QSPKInnerRoundOverMsg extends Message<QSPKInnerRoundOverMsg, QSPKInnerRoundOverMsg.Builder> {
  public static final ProtoAdapter<QSPKInnerRoundOverMsg> ADAPTER = new ProtoAdapter_QSPKInnerRoundOverMsg();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_ROUNDOVERTIMEMS = 0L;

  /**
   * 本轮次结束的毫秒时间戳
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
      adapter = "com.zq.live.proto.Room.QRoundInfo#ADAPTER"
  )
  private final QRoundInfo currentRound;

  public QSPKInnerRoundOverMsg(Long roundOverTimeMs, QRoundInfo currentRound) {
    this(roundOverTimeMs, currentRound, ByteString.EMPTY);
  }

  public QSPKInnerRoundOverMsg(Long roundOverTimeMs, QRoundInfo currentRound,
      ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.roundOverTimeMs = roundOverTimeMs;
    this.currentRound = currentRound;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.roundOverTimeMs = roundOverTimeMs;
    builder.currentRound = currentRound;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof QSPKInnerRoundOverMsg)) return false;
    QSPKInnerRoundOverMsg o = (QSPKInnerRoundOverMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(roundOverTimeMs, o.roundOverTimeMs)
        && Internal.equals(currentRound, o.currentRound);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (roundOverTimeMs != null ? roundOverTimeMs.hashCode() : 0);
      result = result * 37 + (currentRound != null ? currentRound.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (roundOverTimeMs != null) builder.append(", roundOverTimeMs=").append(roundOverTimeMs);
    if (currentRound != null) builder.append(", currentRound=").append(currentRound);
    return builder.replace(0, 2, "QSPKInnerRoundOverMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return QSPKInnerRoundOverMsg.ADAPTER.encode(this);
  }

  public static final QSPKInnerRoundOverMsg parseFrom(byte[] data) throws IOException {
    QSPKInnerRoundOverMsg c = null;
       c = QSPKInnerRoundOverMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 本轮次结束的毫秒时间戳
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
  public QRoundInfo getCurrentRound() {
    if(currentRound==null){
        return new QRoundInfo.Builder().build();
    }
    return currentRound;
  }

  /**
   * 本轮次结束的毫秒时间戳
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

  public static final class Builder extends Message.Builder<QSPKInnerRoundOverMsg, Builder> {
    private Long roundOverTimeMs;

    private QRoundInfo currentRound;

    public Builder() {
    }

    /**
     * 本轮次结束的毫秒时间戳
     */
    public Builder setRoundOverTimeMs(Long roundOverTimeMs) {
      this.roundOverTimeMs = roundOverTimeMs;
      return this;
    }

    /**
     * 当前轮次信息
     */
    public Builder setCurrentRound(QRoundInfo currentRound) {
      this.currentRound = currentRound;
      return this;
    }

    @Override
    public QSPKInnerRoundOverMsg build() {
      return new QSPKInnerRoundOverMsg(roundOverTimeMs, currentRound, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_QSPKInnerRoundOverMsg extends ProtoAdapter<QSPKInnerRoundOverMsg> {
    public ProtoAdapter_QSPKInnerRoundOverMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, QSPKInnerRoundOverMsg.class);
    }

    @Override
    public int encodedSize(QSPKInnerRoundOverMsg value) {
      return ProtoAdapter.SINT64.encodedSizeWithTag(1, value.roundOverTimeMs)
          + QRoundInfo.ADAPTER.encodedSizeWithTag(2, value.currentRound)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, QSPKInnerRoundOverMsg value) throws IOException {
      ProtoAdapter.SINT64.encodeWithTag(writer, 1, value.roundOverTimeMs);
      QRoundInfo.ADAPTER.encodeWithTag(writer, 2, value.currentRound);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public QSPKInnerRoundOverMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setRoundOverTimeMs(ProtoAdapter.SINT64.decode(reader)); break;
          case 2: builder.setCurrentRound(QRoundInfo.ADAPTER.decode(reader)); break;
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
    public QSPKInnerRoundOverMsg redact(QSPKInnerRoundOverMsg value) {
      Builder builder = value.newBuilder();
      if (builder.currentRound != null) builder.currentRound = QRoundInfo.ADAPTER.redact(builder.currentRound);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
