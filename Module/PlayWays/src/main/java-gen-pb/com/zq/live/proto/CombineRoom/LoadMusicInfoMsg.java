// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: CombineRoom.proto
package com.zq.live.proto.CombineRoom;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class LoadMusicInfoMsg extends Message<LoadMusicInfoMsg, LoadMusicInfoMsg.Builder> {
  public static final ProtoAdapter<LoadMusicInfoMsg> ADAPTER = new ProtoAdapter_LoadMusicInfoMsg();

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_NEXTMUSICDESC = "";

  public static final Boolean DEFAULT_HASNEXTMUSIC = false;

  @WireField(
      tag = 1,
      adapter = "com.zq.live.proto.CombineRoom.CombineRoomMusic#ADAPTER"
  )
  private final CombineRoomMusic currentMusic;

  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String nextMusicDesc;

  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  private final Boolean hasNextMusic;

  public LoadMusicInfoMsg(CombineRoomMusic currentMusic, String nextMusicDesc,
      Boolean hasNextMusic) {
    this(currentMusic, nextMusicDesc, hasNextMusic, ByteString.EMPTY);
  }

  public LoadMusicInfoMsg(CombineRoomMusic currentMusic, String nextMusicDesc, Boolean hasNextMusic,
      ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.currentMusic = currentMusic;
    this.nextMusicDesc = nextMusicDesc;
    this.hasNextMusic = hasNextMusic;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.currentMusic = currentMusic;
    builder.nextMusicDesc = nextMusicDesc;
    builder.hasNextMusic = hasNextMusic;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof LoadMusicInfoMsg)) return false;
    LoadMusicInfoMsg o = (LoadMusicInfoMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(currentMusic, o.currentMusic)
        && Internal.equals(nextMusicDesc, o.nextMusicDesc)
        && Internal.equals(hasNextMusic, o.hasNextMusic);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (currentMusic != null ? currentMusic.hashCode() : 0);
      result = result * 37 + (nextMusicDesc != null ? nextMusicDesc.hashCode() : 0);
      result = result * 37 + (hasNextMusic != null ? hasNextMusic.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (currentMusic != null) builder.append(", currentMusic=").append(currentMusic);
    if (nextMusicDesc != null) builder.append(", nextMusicDesc=").append(nextMusicDesc);
    if (hasNextMusic != null) builder.append(", hasNextMusic=").append(hasNextMusic);
    return builder.replace(0, 2, "LoadMusicInfoMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return LoadMusicInfoMsg.ADAPTER.encode(this);
  }

  public static final LoadMusicInfoMsg parseFrom(byte[] data) throws IOException {
    LoadMusicInfoMsg c = null;
       c = LoadMusicInfoMsg.ADAPTER.decode(data);
    return c;
  }

  public CombineRoomMusic getCurrentMusic() {
    if(currentMusic==null){
        return new CombineRoomMusic.Builder().build();
    }
    return currentMusic;
  }

  public String getNextMusicDesc() {
    if(nextMusicDesc==null){
        return DEFAULT_NEXTMUSICDESC;
    }
    return nextMusicDesc;
  }

  public Boolean getHasNextMusic() {
    if(hasNextMusic==null){
        return DEFAULT_HASNEXTMUSIC;
    }
    return hasNextMusic;
  }

  public boolean hasCurrentMusic() {
    return currentMusic!=null;
  }

  public boolean hasNextMusicDesc() {
    return nextMusicDesc!=null;
  }

  public boolean hasHasNextMusic() {
    return hasNextMusic!=null;
  }

  public static final class Builder extends Message.Builder<LoadMusicInfoMsg, Builder> {
    private CombineRoomMusic currentMusic;

    private String nextMusicDesc;

    private Boolean hasNextMusic;

    public Builder() {
    }

    public Builder setCurrentMusic(CombineRoomMusic currentMusic) {
      this.currentMusic = currentMusic;
      return this;
    }

    public Builder setNextMusicDesc(String nextMusicDesc) {
      this.nextMusicDesc = nextMusicDesc;
      return this;
    }

    public Builder setHasNextMusic(Boolean hasNextMusic) {
      this.hasNextMusic = hasNextMusic;
      return this;
    }

    @Override
    public LoadMusicInfoMsg build() {
      return new LoadMusicInfoMsg(currentMusic, nextMusicDesc, hasNextMusic, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_LoadMusicInfoMsg extends ProtoAdapter<LoadMusicInfoMsg> {
    public ProtoAdapter_LoadMusicInfoMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, LoadMusicInfoMsg.class);
    }

    @Override
    public int encodedSize(LoadMusicInfoMsg value) {
      return CombineRoomMusic.ADAPTER.encodedSizeWithTag(1, value.currentMusic)
          + ProtoAdapter.STRING.encodedSizeWithTag(2, value.nextMusicDesc)
          + ProtoAdapter.BOOL.encodedSizeWithTag(3, value.hasNextMusic)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, LoadMusicInfoMsg value) throws IOException {
      CombineRoomMusic.ADAPTER.encodeWithTag(writer, 1, value.currentMusic);
      ProtoAdapter.STRING.encodeWithTag(writer, 2, value.nextMusicDesc);
      ProtoAdapter.BOOL.encodeWithTag(writer, 3, value.hasNextMusic);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public LoadMusicInfoMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setCurrentMusic(CombineRoomMusic.ADAPTER.decode(reader)); break;
          case 2: builder.setNextMusicDesc(ProtoAdapter.STRING.decode(reader)); break;
          case 3: builder.setHasNextMusic(ProtoAdapter.BOOL.decode(reader)); break;
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
    public LoadMusicInfoMsg redact(LoadMusicInfoMsg value) {
      Builder builder = value.newBuilder();
      if (builder.currentMusic != null) builder.currentMusic = CombineRoomMusic.ADAPTER.redact(builder.currentMusic);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
