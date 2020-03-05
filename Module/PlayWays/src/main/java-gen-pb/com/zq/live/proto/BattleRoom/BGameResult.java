// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: battle_room.proto
package com.zq.live.proto.BattleRoom;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import java.io.IOException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class BGameResult extends Message<BGameResult, BGameResult.Builder> {
  public static final ProtoAdapter<BGameResult> ADAPTER = new ProtoAdapter_BGameResult();

  private static final long serialVersionUID = 0L;

  public BGameResult() {
    this(ByteString.EMPTY);
  }

  public BGameResult(ByteString unknownFields) {
    super(ADAPTER, unknownFields);
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof BGameResult;
  }

  @Override
  public int hashCode() {
    return unknownFields().hashCode();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    return builder.replace(0, 2, "BGameResult{").append('}').toString();
  }

  public byte[] toByteArray() {
    return BGameResult.ADAPTER.encode(this);
  }

  public static final BGameResult parseFrom(byte[] data) throws IOException {
    BGameResult c = null;
       c = BGameResult.ADAPTER.decode(data);
    return c;
  }

  public static final class Builder extends Message.Builder<BGameResult, Builder> {
    public Builder() {
    }

    @Override
    public BGameResult build() {
      return new BGameResult(super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_BGameResult extends ProtoAdapter<BGameResult> {
    public ProtoAdapter_BGameResult() {
      super(FieldEncoding.LENGTH_DELIMITED, BGameResult.class);
    }

    @Override
    public int encodedSize(BGameResult value) {
      return value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, BGameResult value) throws IOException {
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public BGameResult decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
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
    public BGameResult redact(BGameResult value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}