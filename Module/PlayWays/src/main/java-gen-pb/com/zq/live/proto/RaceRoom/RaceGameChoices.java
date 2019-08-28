// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: race_room.proto
package com.zq.live.proto.RaceRoom;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import okio.ByteString;

public final class RaceGameChoices extends Message<RaceGameChoices, RaceGameChoices.Builder> {
  public static final ProtoAdapter<RaceGameChoices> ADAPTER = new ProtoAdapter_RaceGameChoices();

  private static final long serialVersionUID = 0L;

  @WireField(
      tag = 1,
      adapter = "com.zq.live.proto.RaceRoom.RaceGameInfo#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<RaceGameInfo> games;

  public RaceGameChoices(List<RaceGameInfo> games) {
    this(games, ByteString.EMPTY);
  }

  public RaceGameChoices(List<RaceGameInfo> games, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.games = Internal.immutableCopyOf("games", games);
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.games = Internal.copyOf("games", games);
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof RaceGameChoices)) return false;
    RaceGameChoices o = (RaceGameChoices) other;
    return unknownFields().equals(o.unknownFields())
        && games.equals(o.games);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + games.hashCode();
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (!games.isEmpty()) builder.append(", games=").append(games);
    return builder.replace(0, 2, "RaceGameChoices{").append('}').toString();
  }

  public byte[] toByteArray() {
    return RaceGameChoices.ADAPTER.encode(this);
  }

  public static final RaceGameChoices parseFrom(byte[] data) throws IOException {
    RaceGameChoices c = null;
       c = RaceGameChoices.ADAPTER.decode(data);
    return c;
  }

  public List<RaceGameInfo> getGamesList() {
    if(games==null){
        return new java.util.ArrayList<RaceGameInfo>();
    }
    return games;
  }

  public boolean hasGamesList() {
    return games!=null;
  }

  public static final class Builder extends Message.Builder<RaceGameChoices, Builder> {
    private List<RaceGameInfo> games;

    public Builder() {
      games = Internal.newMutableList();
    }

    public Builder addAllGames(List<RaceGameInfo> games) {
      Internal.checkElementsNotNull(games);
      this.games = games;
      return this;
    }

    @Override
    public RaceGameChoices build() {
      return new RaceGameChoices(games, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_RaceGameChoices extends ProtoAdapter<RaceGameChoices> {
    public ProtoAdapter_RaceGameChoices() {
      super(FieldEncoding.LENGTH_DELIMITED, RaceGameChoices.class);
    }

    @Override
    public int encodedSize(RaceGameChoices value) {
      return RaceGameInfo.ADAPTER.asRepeated().encodedSizeWithTag(1, value.games)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, RaceGameChoices value) throws IOException {
      RaceGameInfo.ADAPTER.asRepeated().encodeWithTag(writer, 1, value.games);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public RaceGameChoices decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.games.add(RaceGameInfo.ADAPTER.decode(reader)); break;
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
    public RaceGameChoices redact(RaceGameChoices value) {
      Builder builder = value.newBuilder();
      Internal.redactElements(builder.games, RaceGameInfo.ADAPTER);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
