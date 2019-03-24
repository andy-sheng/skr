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
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class QGameBeginMsg extends Message<QGameBeginMsg, QGameBeginMsg.Builder> {
  public static final ProtoAdapter<QGameBeginMsg> ADAPTER = new ProtoAdapter_QGameBeginMsg();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_ROOMID = 0;

  /**
   * 房间id
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer roomID;

  /**
   * 当前轮次信息
   */
  @WireField(
      tag = 2,
      adapter = "com.zq.live.proto.Room.QRoundInfo#ADAPTER"
  )
  private final QRoundInfo currentRound;

  public QGameBeginMsg(Integer roomID, QRoundInfo currentRound) {
    this(roomID, currentRound, ByteString.EMPTY);
  }

  public QGameBeginMsg(Integer roomID, QRoundInfo currentRound, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.roomID = roomID;
    this.currentRound = currentRound;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.roomID = roomID;
    builder.currentRound = currentRound;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof QGameBeginMsg)) return false;
    QGameBeginMsg o = (QGameBeginMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(roomID, o.roomID)
        && Internal.equals(currentRound, o.currentRound);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (roomID != null ? roomID.hashCode() : 0);
      result = result * 37 + (currentRound != null ? currentRound.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (roomID != null) builder.append(", roomID=").append(roomID);
    if (currentRound != null) builder.append(", currentRound=").append(currentRound);
    return builder.replace(0, 2, "QGameBeginMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return QGameBeginMsg.ADAPTER.encode(this);
  }

  public static final QGameBeginMsg parseFrom(byte[] data) throws IOException {
    QGameBeginMsg c = null;
       c = QGameBeginMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 房间id
   */
  public Integer getRoomID() {
    if(roomID==null){
        return DEFAULT_ROOMID;
    }
    return roomID;
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
   * 房间id
   */
  public boolean hasRoomID() {
    return roomID!=null;
  }

  /**
   * 当前轮次信息
   */
  public boolean hasCurrentRound() {
    return currentRound!=null;
  }

  public static final class Builder extends Message.Builder<QGameBeginMsg, Builder> {
    private Integer roomID;

    private QRoundInfo currentRound;

    public Builder() {
    }

    /**
     * 房间id
     */
    public Builder setRoomID(Integer roomID) {
      this.roomID = roomID;
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
    public QGameBeginMsg build() {
      return new QGameBeginMsg(roomID, currentRound, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_QGameBeginMsg extends ProtoAdapter<QGameBeginMsg> {
    public ProtoAdapter_QGameBeginMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, QGameBeginMsg.class);
    }

    @Override
    public int encodedSize(QGameBeginMsg value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.roomID)
          + QRoundInfo.ADAPTER.encodedSizeWithTag(2, value.currentRound)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, QGameBeginMsg value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.roomID);
      QRoundInfo.ADAPTER.encodeWithTag(writer, 2, value.currentRound);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public QGameBeginMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setRoomID(ProtoAdapter.UINT32.decode(reader)); break;
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
    public QGameBeginMsg redact(QGameBeginMsg value) {
      Builder builder = value.newBuilder();
      if (builder.currentRound != null) builder.currentRound = QRoundInfo.ADAPTER.redact(builder.currentRound);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
