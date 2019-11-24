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
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class QMINIGameInnerRoundInfo extends Message<QMINIGameInnerRoundInfo, QMINIGameInnerRoundInfo.Builder> {
  public static final ProtoAdapter<QMINIGameInnerRoundInfo> ADAPTER = new ProtoAdapter_QMINIGameInnerRoundInfo();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_USERID = 0;

  /**
   * 抢唱成功的玩家id
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer userID;

  public QMINIGameInnerRoundInfo(Integer userID) {
    this(userID, ByteString.EMPTY);
  }

  public QMINIGameInnerRoundInfo(Integer userID, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.userID = userID;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.userID = userID;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof QMINIGameInnerRoundInfo)) return false;
    QMINIGameInnerRoundInfo o = (QMINIGameInnerRoundInfo) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(userID, o.userID);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (userID != null ? userID.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (userID != null) builder.append(", userID=").append(userID);
    return builder.replace(0, 2, "QMINIGameInnerRoundInfo{").append('}').toString();
  }

  public byte[] toByteArray() {
    return QMINIGameInnerRoundInfo.ADAPTER.encode(this);
  }

  public static final QMINIGameInnerRoundInfo parseFrom(byte[] data) throws IOException {
    QMINIGameInnerRoundInfo c = null;
       c = QMINIGameInnerRoundInfo.ADAPTER.decode(data);
    return c;
  }

  /**
   * 抢唱成功的玩家id
   */
  public Integer getUserID() {
    if(userID==null){
        return DEFAULT_USERID;
    }
    return userID;
  }

  /**
   * 抢唱成功的玩家id
   */
  public boolean hasUserID() {
    return userID!=null;
  }

  public static final class Builder extends Message.Builder<QMINIGameInnerRoundInfo, Builder> {
    private Integer userID;

    public Builder() {
    }

    /**
     * 抢唱成功的玩家id
     */
    public Builder setUserID(Integer userID) {
      this.userID = userID;
      return this;
    }

    @Override
    public QMINIGameInnerRoundInfo build() {
      return new QMINIGameInnerRoundInfo(userID, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_QMINIGameInnerRoundInfo extends ProtoAdapter<QMINIGameInnerRoundInfo> {
    public ProtoAdapter_QMINIGameInnerRoundInfo() {
      super(FieldEncoding.LENGTH_DELIMITED, QMINIGameInnerRoundInfo.class);
    }

    @Override
    public int encodedSize(QMINIGameInnerRoundInfo value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.userID)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, QMINIGameInnerRoundInfo value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.userID);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public QMINIGameInnerRoundInfo decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setUserID(ProtoAdapter.UINT32.decode(reader)); break;
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
    public QMINIGameInnerRoundInfo redact(QMINIGameInnerRoundInfo value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
